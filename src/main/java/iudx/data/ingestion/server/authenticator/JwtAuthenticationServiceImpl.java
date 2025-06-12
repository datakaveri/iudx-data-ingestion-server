package iudx.data.ingestion.server.authenticator;

import static iudx.data.ingestion.server.authenticator.Constants.*;

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
import iudx.data.ingestion.server.authenticator.authorization.AuthorizationContextFactory;
import iudx.data.ingestion.server.authenticator.authorization.AuthorizationRequest;
import iudx.data.ingestion.server.authenticator.authorization.AuthorizationStrategy;
import iudx.data.ingestion.server.authenticator.authorization.IudxRole;
import iudx.data.ingestion.server.authenticator.authorization.JwtAuthorization;
import iudx.data.ingestion.server.authenticator.authorization.Method;
import iudx.data.ingestion.server.authenticator.model.JwtData;
import iudx.data.ingestion.server.common.Api;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class JwtAuthenticationServiceImpl implements AuthenticationService {

  private static final Logger LOGGER = LogManager.getLogger(JwtAuthenticationServiceImpl.class);
  // resourceIdCache will contain info about resources available(& their ACL) in ingestion server.
  public final Cache<String, String> resourceIdCache =
      CacheBuilder.newBuilder()
          .maximumSize(1000)
          .expireAfterAccess(CACHE_TIMEOUT_AMOUNT, TimeUnit.MINUTES)
          .build();
  final JWTAuth jwtAuth;
  final WebClient catWebClient;
  final String host;
  final int port;
  final String path;
  final String audience;
  final Api apis;
  final String catBasePath;
  final String authServerHost;

  public JwtAuthenticationServiceImpl(
      Vertx vertx, final JWTAuth jwtAuth, final JsonObject config, Api apis) {
    this.jwtAuth = jwtAuth;
    this.audience = config.getString(DI_AUDIENCE);
    host = config.getString(CAT_SERVER_HOST);
    port = config.getInteger(CAT_SERVER_PORT);
    this.catBasePath = config.getString("dxCatalogueBasePath");
    this.path = catBasePath + CAT_SEARCH_PATH;
    this.apis = apis;
    authServerHost = config.getString("authServerHost");
    WebClientOptions options = new WebClientOptions();
    options.setTrustAll(true).setVerifyHost(false).setSsl(true);
    catWebClient = WebClient.create(vertx, options);
  }

  @Override
  public AuthenticationService tokenIntrospect(
      JsonObject request, JsonObject authenticationInfo, Handler<AsyncResult<JsonObject>> handler) {

    String endPoint = authenticationInfo.getString(API_ENDPOINT);
    String id = authenticationInfo.getString(ID);
    String token = authenticationInfo.getString(TOKEN);

    Future<JwtData> jwtDecodeFuture = decodeJwt(token);
    // stop moving forward if jwtDecode is a failure.
    ResultContainer result = new ResultContainer();
    jwtDecodeFuture
        .compose(
            decodeHandler -> {
              result.jwtData = decodeHandler;
              return isValidAudienceValue(result.jwtData);
            })
        .compose(
            audienceHandler -> {
              LOGGER.debug("audience is valid " + apis.getIngestionEndpoint());
              LOGGER.debug("endpoint :" + endPoint);
              if (endPoint.equals(apis.getIngestionEndpoint())
                  && isValidAdminToken(result.jwtData)) {
                // admin token + /ingestion POST, skip id check
                return Future.succeededFuture(true);
              } else {
                // check id in token
                return isValidId(result.jwtData, id);
              }
            })
        .compose(validIdHandler -> validateAccess(result.jwtData, authenticationInfo))
        .onComplete(
            completeHandler -> {
              if (completeHandler.succeeded()) {
                LOGGER.debug("Completion handler");
                handler.handle(Future.succeededFuture(completeHandler.result()));
              } else {
                LOGGER.debug("Failure handler");
                LOGGER.error("error : " + completeHandler.cause());
                handler.handle(Future.failedFuture(completeHandler.cause().getMessage()));
              }
            });
    return this;
  }

  public Future<JwtData> decodeJwt(String jwtToken) {
    Promise<JwtData> promise = Promise.promise();
    TokenCredentials creds = new TokenCredentials(jwtToken);

    jwtAuth
        .authenticate(creds)
        .onSuccess(
            user -> {
              JwtData jwtData = new JwtData(user.principal());
              jwtData.setExp(user.get("exp"));
              jwtData.setIat(user.get("iat"));
              promise.complete(jwtData);
            })
        .onFailure(
            err -> {
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
    String api = authInfo.getString(API_ENDPOINT);
    AuthorizationRequest authRequest = new AuthorizationRequest(method, api);
    IudxRole role = IudxRole.fromRole(jwtData.getRole());
    AuthorizationStrategy authStrategy = AuthorizationContextFactory.create(role, apis);
    LOGGER.info("strategy : " + authStrategy.getClass().getSimpleName());
    JwtAuthorization jwtAuthStrategy = new JwtAuthorization(authStrategy);
    LOGGER.info("endPoint : " + authInfo.getString(API_ENDPOINT));
    if (jwtAuthStrategy.isAuthorized(authRequest, jwtData)) {
      JsonObject jsonResponse = new JsonObject();
      jsonResponse.put(JSON_IID, jwtId);
      jsonResponse.put(JSON_USERID, jwtData.getSub());
      jsonResponse.put(JSON_IID, jwtId);
      jsonResponse.put(JSON_ROLE, jwtData.getRole());
      jsonResponse.put(JSON_DRL, jwtData.getDrl());
      jsonResponse.put(JSON_DID, jwtData.getDid());
      jsonResponse.put(
          JSON_EXPIRY,
          LocalDateTime.ofInstant(
                  Instant.ofEpochSecond(Long.parseLong(jwtData.getExp().toString())),
                  ZoneId.systemDefault())
              .toString());
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
    LOGGER.debug("jwtdata : " + jwtData);
    if (jwtData.getRole() != null && !jwtData.getRole().equals(IudxRole.ADMIN.getRole())) {
      return false;
    }

    String jwtId = jwtData.getIid().split(":")[1];
    return audience != null && audience.equals(jwtId);
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
