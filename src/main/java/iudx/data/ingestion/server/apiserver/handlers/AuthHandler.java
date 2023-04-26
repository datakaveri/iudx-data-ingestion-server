package iudx.data.ingestion.server.apiserver.handlers;

import static iudx.data.ingestion.server.apiserver.response.ResponseUrn.*;
import static iudx.data.ingestion.server.apiserver.util.Constants.*;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import iudx.data.ingestion.server.apiserver.response.ResponseUrn;
import iudx.data.ingestion.server.apiserver.util.HttpStatusCode;
import iudx.data.ingestion.server.authenticator.AuthenticationService;
import iudx.data.ingestion.server.common.Api;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * IUDX Authentication handler to authenticate token passed in HEADER
 */
public class AuthHandler implements Handler<RoutingContext> {

  private static final Logger LOGGER = LogManager.getLogger(AuthHandler.class);
  private static final String AUTH_SERVICE_ADDRESS = "iudx.data.ingestion.authentication.service";
  static AuthenticationService authenticator;
  static Api apis;
  private HttpServerRequest request;

  public static AuthHandler create(Vertx vertx, Api apisEndpoint) {
    authenticator = AuthenticationService.createProxy(vertx, AUTH_SERVICE_ADDRESS);
    apis = apisEndpoint;
    return new AuthHandler();
  }

  @Override
  public void handle(RoutingContext context) {
    request = context.request();
    JsonObject requestJson = context.body().asJsonObject();

    if (requestJson == null) {
      requestJson = new JsonObject();
    }

    LOGGER.debug("Info : path " + request.path());

    final String token = request.headers().get(HEADER_TOKEN);
    final String path = getNormalizedPath(request.path());
    final String method = context.request().method().toString();

    String paramId = getIdFromRequest();
    LOGGER.info("id from param : " + paramId);
    String bodyId = getIdFromBody(context);
    LOGGER.info("id from body : " + bodyId);

    String id;
    if (paramId != null && !paramId.isBlank()) {
      id = paramId;
    } else {
      id = bodyId;
    }
    LOGGER.info("id : " + id);

    JsonObject authInfo =
        new JsonObject().put(API_ENDPOINT, path).put(HEADER_TOKEN, token).put(API_METHOD, method)
            .put(ID, id);

    authenticator.tokenIntrospect(requestJson, authInfo, authHandler -> {

      if (authHandler.succeeded()) {
        authInfo.put(IID, authHandler.result().getValue(IID));
        authInfo.put(USER_ID, authHandler.result().getValue(USER_ID));
        context.data().put(AUTH_INFO, authInfo);
      } else {
        processAuthFailure(context, authHandler.cause().getMessage());
        return;
      }
      context.next();
    });
  }


  private String getIdFromRequest() {
    return request.getParam(ID);
  }

  public void processAuthFailure(RoutingContext ctx, String result) {
    LOGGER.debug("RESULT " + result);
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
        .put(JSON_TITLE, statusCode.getDescription())
        .put(JSON_DETAIL, statusCode.getDescription());
  }

  private String getIdFromBody(RoutingContext context) {
    JsonObject body = context.body().asJsonObject();
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
    if (url.matches(getpathRegex(apis.getEntitiesEndpoint()))) {
      path = apis.getEntitiesEndpoint();
    } else if (url.matches(getpathRegex(apis.getIngestionEndpoint()))) {
      path = apis.getIngestionEndpoint();
    }
    return path;
  }

  private String getpathRegex(String path) {
    return path + "(.*)";
  }
}
