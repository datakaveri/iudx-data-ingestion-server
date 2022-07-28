package iudx.data.ingestion.server.authenticator;

import static iudx.data.ingestion.server.authenticator.Constants.*;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.authentication.TokenCredentials;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import iudx.data.ingestion.server.authenticator.authorization.Api;
import iudx.data.ingestion.server.authenticator.authorization.AuthorizationContextFactory;
import iudx.data.ingestion.server.authenticator.authorization.AuthorizationRequest;
import iudx.data.ingestion.server.authenticator.authorization.AuthorizationStrategy;
import iudx.data.ingestion.server.authenticator.authorization.IUDXRole;
import iudx.data.ingestion.server.authenticator.authorization.JwtAuthorization;
import iudx.data.ingestion.server.authenticator.authorization.Method;
import iudx.data.ingestion.server.authenticator.model.JwtData;

public class JwtAuthenticationServiceImpl implements AuthenticationService {

  private static final Logger LOGGER = LogManager.getLogger(JwtAuthenticationServiceImpl.class);
  // resourceIdCache will contain info about resources available(& their ACL) in ingestion server.
  public final Cache<String, String> resourceIdCache = CacheBuilder.newBuilder().maximumSize(1000)
      .expireAfterAccess(Constants.CACHE_TIMEOUT_AMOUNT, TimeUnit.MINUTES).build();
  final JWTAuth jwtAuth;
  final WebClient catWebClient;
  final String host;
  final int port;
  final String path;
  final String audience;
  final String authServerHost;
  // resourceGroupCache will contain ACL info about all resource group in ingestion server
  private final Cache<String, String> resourceGroupCache =
      CacheBuilder.newBuilder().maximumSize(1000)
          .expireAfterAccess(Constants.CACHE_TIMEOUT_AMOUNT, TimeUnit.MINUTES).build();

  public JwtAuthenticationServiceImpl(Vertx vertx, final JWTAuth jwtAuth, final WebClient webClient,
      final JsonObject config) {
    this.jwtAuth = jwtAuth;
    this.audience = config.getString(DI_AUDIENCE);
    host = config.getString(CAT_SERVER_HOST);
    port = config.getInteger(CAT_SERVER_PORT);
    path = Constants.CAT_RSG_PATH;
    authServerHost = config.getString("authServerHost");
    WebClientOptions options = new WebClientOptions();
    options.setTrustAll(true).setVerifyHost(false).setSsl(true);
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


    boolean isAdminIngestionEndpoint =
        endPoint != null && endPoint.equals(Api.INGESTION.getApiEndpoint());

    ResultContainer result = new ResultContainer();
    jwtDecodeFuture.compose(decodeHandler -> {
      result.jwtData = decodeHandler;
      return isValidAudienceValue(result.jwtData);
    }).compose(audienceHandler -> {
      if (endPoint.equals(Api.INGESTION.getApiEndpoint()) && isValidAdminToken(result.jwtData)) {
        //admin token + /ingestion POST, skip id check
        return Future.succeededFuture(true);
      } else {
        //check id in token  
        return isValidId(result.jwtData, id);
      }
    }).compose(validIdHandler ->

    validateAccess(result.jwtData, authenticationInfo))
        .onComplete(completeHandler -> {
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

    jwtAuth.authenticate(creds).onSuccess(user -> {
      JwtData jwtData = new JwtData(user.principal());
      promise.complete(jwtData);
    }).onFailure(err -> {
      LOGGER.error("failed to decode/validate jwt token : " + err.getMessage());
      promise.fail("failed");
    });

    return promise.future();
  }

  public Future<JsonObject> validateAccess(JwtData jwtData, JsonObject authInfo) {
    LOGGER.info("validateAccess() started");
    Promise<JsonObject> promise = Promise.promise();
    String jwtId = jwtData.getIid().split(":")[1];

    Method method = Method.valueOf(authInfo.getString(METHOD));
    Api api = Api.fromEndpoint(authInfo.getString(API_ENDPOINT));
    AuthorizationRequest authRequest = new AuthorizationRequest(method, api);
    IUDXRole role = IUDXRole.fromRole(jwtData.getRole());
    AuthorizationStrategy authStrategy = AuthorizationContextFactory.create(role);
    LOGGER.info("strategy : " + authStrategy.getClass().getSimpleName());
    JwtAuthorization jwtAuthStrategy = new JwtAuthorization(authStrategy);
    LOGGER.info("endPoint : " + authInfo.getString(API_ENDPOINT));
    if (jwtAuthStrategy.isAuthorized(authRequest, jwtData)) {
      JsonObject jsonResponse = new JsonObject();
      jsonResponse.put(JSON_IID, jwtId);
      jsonResponse.put(JSON_USERID, jwtData.getSub());
      if (jwtData.getRole().equalsIgnoreCase(JSON_PROVIDER)) {
        jsonResponse.put(JSON_PROVIDER, jwtData.getSub());
      } else if (jwtData.getRole().equalsIgnoreCase(JSON_DELEGATE)) {
        jsonResponse.put(JSON_DELEGATE, jwtData.getSub());
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

    if (audience != null && audience.equalsIgnoreCase(jwtData.getAud())) {
      promise.complete(true);
    } else {
      LOGGER.error("Incorrect audience value in jwt");
      promise.fail("Incorrect audience value in jwt");
    }
    return promise.future();
  }

  public boolean isValidAdminToken(JwtData jwtData) {
    LOGGER.debug("jwtdata : "+jwtData);
    if (jwtData.getRole() != null && !jwtData.getRole().equals(IUDXRole.ADMIN.getRole())) {
      return false;
    }
   
    String jwtId = jwtData.getIid().split(":")[1];
    String jwtIss = jwtData.getIss();
    if (audience != null && audience.equals(jwtId) && jwtIss != null
        && authServerHost.equalsIgnoreCase(jwtIss)) {
      return true;
    } else {
      return false;
    }

  }

  public Future<Boolean> isValidId(JwtData jwtData, String id) {
    Promise<Boolean> promise = Promise.promise();
    String jwtId = jwtData.getIid().split(":")[1];
    LOGGER.info("jwtId" + jwtId);
    LOGGER.info("id " + id);
    if (id.equalsIgnoreCase(jwtId)) {
      promise.complete(true);
    } else if (id.equalsIgnoreCase(jwtId)) {
      promise.complete(true);
    } else {
      LOGGER.error("Incorrect token : id mismatch");
      promise.fail("Incorrect token : id mismatch");
    }
    return promise.future();
  }

  Future<JsonObject> isValidRole(JwtData jwtData) {
    Promise<JsonObject> promise = Promise.promise();
    String jwtId = jwtData.getIid().split(":")[1];
    String role = jwtData.getRole();
    JsonObject jsonResponse = new JsonObject();
    if (role.equalsIgnoreCase("admin")) {
      jsonResponse.put(JSON_USERID, jwtData.getSub());
      jsonResponse.put(JSON_IID, jwtId);
      promise.complete(jsonResponse);
    } else {
      LOGGER.debug("failed");
      JsonObject result = new JsonObject().put("401", "Only admin access allowed.");
      promise.fail(result.toString());
    }
    return promise.future();
  }

  // class to contain intermediate data for token introspection
  final class ResultContainer {
    JwtData jwtData;
    boolean isResourceExist;

  }


}
