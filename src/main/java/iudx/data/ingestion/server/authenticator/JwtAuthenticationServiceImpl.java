package iudx.data.ingestion.server.authenticator;

import static iudx.data.ingestion.server.authenticator.Constants.API_ENDPOINT;
import static iudx.data.ingestion.server.authenticator.Constants.CAT_HOST;
import static iudx.data.ingestion.server.authenticator.Constants.CAT_SERVER_HOST;
import static iudx.data.ingestion.server.authenticator.Constants.CAT_SERVER_PORT;
import static iudx.data.ingestion.server.authenticator.Constants.ID;
import static iudx.data.ingestion.server.authenticator.Constants.JSON_CONSUMER;
import static iudx.data.ingestion.server.authenticator.Constants.JSON_DELEGATE;
import static iudx.data.ingestion.server.authenticator.Constants.JSON_PROVIDER;
import static iudx.data.ingestion.server.authenticator.Constants.METHOD;
import static iudx.data.ingestion.server.authenticator.Constants.TOKEN;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.authentication.TokenCredentials;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.client.predicate.ResponsePredicate;
import iudx.data.ingestion.server.authenticator.authorization.Api;
import iudx.data.ingestion.server.authenticator.authorization.AuthorizationContextFactory;
import iudx.data.ingestion.server.authenticator.authorization.AuthorizationRequest;
import iudx.data.ingestion.server.authenticator.authorization.AuthorizationStrategy;
import iudx.data.ingestion.server.authenticator.authorization.JwtAuthorization;
import iudx.data.ingestion.server.authenticator.authorization.Method;
import iudx.data.ingestion.server.authenticator.model.JwtData;
import java.util.concurrent.TimeUnit;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class JwtAuthenticationServiceImpl implements AuthenticationService {

  private static final Logger LOGGER = LogManager.getLogger(JwtAuthenticationServiceImpl.class);
  // resourceIdCache will contain info about resources available(& their ACL) in ingestion server.
  public final Cache<String, String> resourceIdCache = CacheBuilder
      .newBuilder()
      .maximumSize(1000)
      .expireAfterAccess(Constants.CACHE_TIMEOUT_AMOUNT, TimeUnit.MINUTES)
      .build();
  final JWTAuth jwtAuth;
  final WebClient catWebClient;
  final String host;
  final int port;
  final String path;
  final String audience;
  // resourceGroupCache will contain ACL info about all resource group in ingestion server
  private final Cache<String, String> resourceGroupCache = CacheBuilder
      .newBuilder()
      .maximumSize(1000)
      .expireAfterAccess(Constants.CACHE_TIMEOUT_AMOUNT, TimeUnit.MINUTES)
      .build();

  public JwtAuthenticationServiceImpl(Vertx vertx, final JWTAuth jwtAuth, final WebClient webClient,
                                      final JsonObject config) {
    this.jwtAuth = jwtAuth;
    this.audience = config.getString(CAT_HOST);
    host = config.getString(CAT_SERVER_HOST);
    port = config.getInteger(CAT_SERVER_PORT);
    path = Constants.CAT_RSG_PATH;
    LOGGER.info("CONFIG " + config);
    WebClientOptions options = new WebClientOptions();
    options.setTrustAll(true)
        .setVerifyHost(false)
        .setSsl(true);
    catWebClient = WebClient.create(vertx, options);
  }

  @Override
  public AuthenticationService tokenIntrospect(JsonObject request, JsonObject authenticationInfo,
                                               Handler<AsyncResult<JsonObject>> handler) {

    String endPoint = authenticationInfo.getString(API_ENDPOINT);
    String id = authenticationInfo.getString(ID);
    String token = authenticationInfo.getString(TOKEN);

    Future<JwtData> jwtDecodeFuture = decodeJwt(token);
    // stop moving forward if jwtDecode is a failure.

    ResultContainer result = new ResultContainer();
    jwtDecodeFuture.compose(decodeHandler -> {
      result.jwtData = decodeHandler;
      return isValidAudienceValue(result.jwtData);
    }).compose(audienceHandler -> {

//      return isValidId(result.jwtData, id);
//uncomment above line once you get a valid JWT token. and delete below line
      return Future.succeededFuture(true);
    }).compose(validIdHandler -> {
      return validateAccess(result.jwtData, result.isResourceExist, authenticationInfo);
    }).onComplete(completeHandler -> {
      if (completeHandler.succeeded()) {
        LOGGER.debug("Completion handler");
        handler.handle(Future.succeededFuture(completeHandler.result()));
      } else {
        LOGGER.debug("Failure handler");
        LOGGER.error("error : " + completeHandler.cause().getMessage());
        handler.handle(Future.failedFuture(completeHandler.cause().getMessage()));
      }
    });
    return this;
  }

  public Future<JwtData> decodeJwt(String jwtToken) {
    Promise<JwtData> promise = Promise.promise();

    // jwtToken =
    // "eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NiJ9.eyJzdWIiOiIzNDliNGI1NS0wMjUxLTQ5MGUtYmVlOS0wMGYzYTVkM2U2NDMiLCJpc3MiOiJhdXRoLnRlc3QuY29tIiwiYXVkIjoiZm9vYmFyLml1ZHguaW8iLCJleHAiOjE2MjU5NDUxMTQsImlhdCI6MTYyNTkwMTkxNCwiaWlkIjoicmc6ZXhhbXBsZS5jb20vOGQ0YjIwZWM0YmYyMWVmYjM2M2U3MjY3MWUxYjViZDc3ZmQ2Y2Y5MS9yZXNvdXJjZS1ncm91cCIsInJvbGUiOiJjb25zdW1lciIsImNvbnMiOnt9fQ.44MehPzbPBgAFWz7k3CSF2b-wHBQktGVJVk-unDLnO3_SrbClyQ3k42PgD7TFKB9H13rqBegr7vI0C4BShZbAw";

    TokenCredentials creds = new TokenCredentials(jwtToken);

    jwtAuth.authenticate(creds)
        .onSuccess(user -> {
          JwtData jwtData = new JwtData(user.principal());
          promise.complete(jwtData);
        }).onFailure(err -> {
          LOGGER.error("failed to decode/validate jwt token : " + err.getMessage());
          promise.fail("failed");
        });

    return promise.future();
  }

  public Future<JsonObject> validateAccess(JwtData jwtData, boolean resourceExist,
                                           JsonObject authInfo) {
    LOGGER.trace("validateAccess() started");
    Promise<JsonObject> promise = Promise.promise();

    Method method = Method.valueOf(authInfo.getString(METHOD));
    Api api = Api.fromEndpoint(authInfo.getString(API_ENDPOINT));
    AuthorizationRequest authRequest = new AuthorizationRequest(method, api);

    AuthorizationStrategy authStrategy = AuthorizationContextFactory.create(jwtData.getRole());
    LOGGER.info("strategy : " + authStrategy.getClass().getSimpleName());
    JwtAuthorization jwtAuthStrategy = new JwtAuthorization(authStrategy);
    LOGGER.info("endPoint : " + authInfo.getString(API_ENDPOINT));
    if (jwtAuthStrategy.isAuthorized(authRequest, jwtData)) {
      JsonObject jsonResponse = new JsonObject();

      if (jwtData.getRole().equalsIgnoreCase(JSON_PROVIDER)) {
        jsonResponse.put(JSON_PROVIDER, jwtData.getSub());
      } else if (jwtData.getRole().equalsIgnoreCase(JSON_DELEGATE)) {
        jsonResponse.put(JSON_DELEGATE, jwtData.getSub());
      } else {
        jsonResponse.put(JSON_CONSUMER, jwtData.getSub());
      }
      promise.complete(jsonResponse);
    } else {
      LOGGER.info("failed");
      JsonObject result = new JsonObject().put("401", "no access provided to endpoint");
      promise.fail(result.toString());
    }
    return promise.future();
  }

  public Future<Boolean> isValidAudienceValue(JwtData jwtData) {
    Promise<Boolean> promise = Promise.promise();

    LOGGER.info("AUD " + audience);
    LOGGER.info("GET AUD " + jwtData.getAud());
    if (audience != null && audience.equalsIgnoreCase(jwtData.getAud())) {
      promise.complete(true);
    } else {
      LOGGER.error("Incorrect audience value in jwt");
      promise.fail("Incorrect audience value in jwt");
    }
    return promise.future();
  }

  public Future<Boolean> isValidId(JwtData jwtData, String id) {
    Promise<Boolean> promise = Promise.promise();
    String jwtId = jwtData.getIid().split(":")[1];
    LOGGER.info("JWT " + jwtId);
    LOGGER.info("ID " + id);
    if (id.equalsIgnoreCase(jwtId)) {
      promise.complete(true);
    } else {
      LOGGER.error("Incorrect id value in jwt");
      promise.fail("Incorrect id value in jwt");
    }

    return promise.future();
  }

  private Future<Boolean> isItemExist(String itemId) {
    LOGGER.debug("isItemExist() started");
    Promise<Boolean> promise = Promise.promise();
    String id = itemId.replace("/*", "");
    LOGGER.info("id : " + id);
    catWebClient.get(port, host, "/iudx/cat/v1/item").addQueryParam("id", id)
        .expect(ResponsePredicate.JSON).send(responseHandler -> {
          if (responseHandler.succeeded()) {
            HttpResponse<Buffer> response = responseHandler.result();
            JsonObject responseBody = response.bodyAsJsonObject();
            if (responseBody.getString("status").equalsIgnoreCase("success")
                && responseBody.getInteger("totalHits") > 0) {
              promise.complete(true);
            } else {
              promise.fail(responseHandler.cause());
            }
          } else {
            promise.fail(responseHandler.cause());
          }
        });
    return promise.future();
  }

  private Future<Boolean> isResourceExist(String id, String groupACL) {
    LOGGER.debug("isResourceExist() started");
    Promise<Boolean> promise = Promise.promise();
    String resourceExist = resourceIdCache.getIfPresent(id);
    if (resourceExist != null) {
      LOGGER.debug("Info : cache Hit");
      promise.complete(true);
    } else {
      LOGGER.debug("Info : Cache miss : call cat server");
      catWebClient.get(port, host, path).addQueryParam("property", "[id]")
          .addQueryParam("value", "[[" + id + "]]").addQueryParam("filter", "[id]")
          .expect(ResponsePredicate.JSON).send(responseHandler -> {
            if (responseHandler.failed()) {
              promise.fail("false");
            }
            HttpResponse<Buffer> response = responseHandler.result();
            JsonObject responseBody = response.bodyAsJsonObject();
            if (response.statusCode() != HttpStatus.SC_OK) {
              promise.fail("false");
            } else if (!responseBody.getString("status").equals("success")) {
              promise.fail("Not Found");
              return;
            } else if (responseBody.getInteger("totalHits") == 0) {
              LOGGER.debug("Info: Resource ID invalid : Catalogue item Not Found");
              promise.fail("Not Found");
            } else {
              LOGGER.debug("is Exist response : " + responseBody);
              resourceIdCache.put(id, groupACL);
              promise.complete(true);
            }
          });
    }
    return promise.future();
  }

  private Future<String> getGroupAccessPolicy(String groupId) {
    LOGGER.debug("getGroupAccessPolicy() started");
    Promise<String> promise = Promise.promise();
    String groupACL = resourceGroupCache.getIfPresent(groupId);
    if (groupACL != null) {
      LOGGER.debug("Info : cache Hit");
      promise.complete(groupACL);
    } else {
      LOGGER.debug("Info : cache miss");
      catWebClient.get(port, host, path).addQueryParam("property", "[id]")
          .addQueryParam("value", "[[" + groupId + "]]").addQueryParam("filter", "[accessPolicy]")
          .expect(ResponsePredicate.JSON).send(httpResponseAsyncResult -> {
            if (httpResponseAsyncResult.failed()) {
              LOGGER.error(httpResponseAsyncResult.cause());
              promise.fail("Resource not found");
              return;
            }
            HttpResponse<Buffer> response = httpResponseAsyncResult.result();
            if (response.statusCode() != HttpStatus.SC_OK) {
              promise.fail("Resource not found");
              return;
            }
            JsonObject responseBody = response.bodyAsJsonObject();
            if (!responseBody.getString("status").equals("success")) {
              promise.fail("Resource not found");
              return;
            }
            String resourceACL = "SECURE";
            try {
              resourceACL =
                  responseBody.getJsonArray("results").getJsonObject(0).getString("accessPolicy");
              resourceGroupCache.put(groupId, resourceACL);
              LOGGER.debug("Info: Group ID valid : Catalogue item Found");
              promise.complete(resourceACL);
            } catch (Exception ignored) {
              LOGGER.error(ignored.getMessage());
              LOGGER.debug("Info: Group ID invalid : Empty response in results from Catalogue");
              promise.fail("Resource not found");
            }
          });
    }
    return promise.future();
  }

  // class to contain intermediate data for token introspection
  final class ResultContainer {
    JwtData jwtData;
    boolean isResourceExist;

  }


}