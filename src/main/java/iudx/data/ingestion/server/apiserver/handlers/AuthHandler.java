package iudx.data.ingestion.server.apiserver.handlers;

import static iudx.data.ingestion.server.apiserver.response.ResponseUrn.INVALID_TOKEN;
import static iudx.data.ingestion.server.apiserver.response.ResponseUrn.RESOURCE_NOT_FOUND;
import static iudx.data.ingestion.server.apiserver.util.Constants.API_ENDPOINT;
import static iudx.data.ingestion.server.apiserver.util.Constants.API_METHOD;
import static iudx.data.ingestion.server.apiserver.util.Constants.APPLICATION_JSON;
import static iudx.data.ingestion.server.apiserver.util.Constants.CONTENT_TYPE;
import static iudx.data.ingestion.server.apiserver.util.Constants.ENTITES_URL_REGEX;
import static iudx.data.ingestion.server.apiserver.util.Constants.HEADER_TOKEN;
import static iudx.data.ingestion.server.apiserver.util.Constants.ID;
import static iudx.data.ingestion.server.apiserver.util.Constants.JSON_DETAIL;
import static iudx.data.ingestion.server.apiserver.util.Constants.JSON_TYPE;
import static iudx.data.ingestion.server.apiserver.util.Constants.NGSILD_ENTITIES_URL;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import iudx.data.ingestion.server.apiserver.response.ResponseUrn;
import iudx.data.ingestion.server.apiserver.util.HttpStatusCode;
import iudx.data.ingestion.server.authenticator.AuthenticationService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * IUDX Authentication handler to authenticate token passed in HEADER
 */
public class AuthHandler implements Handler<RoutingContext> {

  private static final Logger LOGGER = LogManager.getLogger(AuthHandler.class);
  private static final String AUTH_SERVICE_ADDRESS = "iudx.data.ingestion.authentication.service";
  private static AuthenticationService authenticator;
  private final String AUTH_INFO = "authInfo";
  private HttpServerRequest request;

  public static AuthHandler create(Vertx vertx) {
    authenticator = AuthenticationService.createProxy(vertx, AUTH_SERVICE_ADDRESS);
    return new AuthHandler();
  }

  @Override
  public void handle(RoutingContext context) {
    request = context.request();
    JsonObject requestJson = context.getBodyAsJson();

    if (requestJson == null) {
      requestJson = new JsonObject();
    }

    LOGGER.debug("Info : path " + request.path());

    String token = request.headers().get(HEADER_TOKEN);
    final String path = getNormalizedPath(request.path());
    final String method = context.request().method().toString();

    JsonObject authInfo =
        new JsonObject().put(API_ENDPOINT, path).put(HEADER_TOKEN, token).put(API_METHOD, method)
            .put(ID, getIdFromBody(context, path));

    LOGGER.debug("Info :" + context.request().path());
    LOGGER.debug("Info :" + context.request().path().split("/").length);
    LOGGER.debug("request" + requestJson);
    LOGGER.debug("authInfo: " + authInfo);

    authenticator.tokenIntrospect(requestJson, authInfo, authHandler -> {

      if (authHandler.succeeded()) {
        LOGGER.debug("Auth info : " + authHandler.result());
        context.data().put(AUTH_INFO, authHandler.result());
      } else {
        processAuthFailure(context, authHandler.cause().getMessage());
        return;
      }
      context.next();
      return;
    });
  }

  private void processAuthFailure(RoutingContext ctx, String result) {
    LOGGER.info("RESULT " + result);
    if (result.contains("Not Found")) {
      LOGGER.error("Error : Item Not Found");
      HttpStatusCode statusCode = HttpStatusCode.getByValue(404);
      ctx.response()
          .putHeader(CONTENT_TYPE, APPLICATION_JSON)
          .setStatusCode(statusCode.getValue())
          .end(generateResponse(RESOURCE_NOT_FOUND, statusCode).toString());
    } else {
      LOGGER.error("Error : Authentication Failure");
      HttpStatusCode statusCode = HttpStatusCode.getByValue(401);
      ctx.response()
          .putHeader(CONTENT_TYPE, APPLICATION_JSON)
          .setStatusCode(statusCode.getValue())
          .end(generateResponse(INVALID_TOKEN, statusCode).toString());
    }
  }

  private JsonObject generateResponse(ResponseUrn urn, HttpStatusCode statusCode) {
    return new JsonObject()
        .put(JSON_TYPE, urn.getUrn())
        .put(JSON_DETAIL, statusCode.getDescription());
  }

  private String getIdFromBody(RoutingContext context, String api) {
    JsonObject body = context.getBodyAsJson();
    return body.getString(ID);
  }

  /**
   * get normalized path without id as path param.
   *
   * @param url complete path from request
   * @return path without id.
   */
  private String getNormalizedPath(String url) {
    LOGGER.debug("URL : " + url);
    String path = null;
    if (url.matches(ENTITES_URL_REGEX)) {
      path = NGSILD_ENTITIES_URL;
    }
    return path;
  }
}
