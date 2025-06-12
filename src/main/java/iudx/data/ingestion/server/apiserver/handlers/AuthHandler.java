package iudx.data.ingestion.server.apiserver.handlers;

import static iudx.data.ingestion.server.apiserver.response.ResponseUrn.*;
import static iudx.data.ingestion.server.apiserver.util.Constants.*;
import static iudx.data.ingestion.server.authenticator.Constants.JSON_DID;
import static iudx.data.ingestion.server.authenticator.Constants.JSON_DRL;
import static iudx.data.ingestion.server.authenticator.Constants.JSON_EXPIRY;
import static iudx.data.ingestion.server.authenticator.Constants.JSON_ROLE;

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
    //JsonObject requestJson = context.body().asJsonObject();

    LOGGER.debug("Info : path " + request.path());

    final String token = request.headers().get(HEADER_TOKEN);
    final String path = getNormalizedPath(request.path());
    final String method = context.request().method().toString();

    String idFromPath = request.getParam("id"); // Extract `id` from the path parameter
    String paramId = getIdFromRequest();
    LOGGER.info("id from param : " + paramId);

    String id;
    if (idFromPath != null && !idFromPath.isBlank()) {
      id = idFromPath; // Prioritize `id` from the path
    } else if (paramId != null && !paramId.isBlank()) {
      id = paramId; // Fallback to `paramId`
    } else {
      id = getIdFromBody(context); // Fallback to `bodyId`
    }
    LOGGER.info("id : " + id);

    JsonObject authInfo =
        new JsonObject().put(API_ENDPOINT, path).put(HEADER_TOKEN, token).put(API_METHOD, method)
            .put(ID, id);

    authenticator.tokenIntrospect(new JsonObject(), authInfo, authHandler -> {

      if (authHandler.succeeded()) {
        authInfo.put(IID, authHandler.result().getValue(IID));
        authInfo.put(USER_ID, authHandler.result().getValue(USER_ID));
        authInfo.put(JSON_EXPIRY, authHandler.result().getValue(JSON_EXPIRY));
        authInfo.put(JSON_ROLE, authHandler.result().getValue(JSON_ROLE));
        authInfo.put(JSON_DID, authHandler.result().getValue(JSON_DID));
        authInfo.put(JSON_DRL, authHandler.result().getValue(JSON_DRL));
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
    String id = null;
    try {
      if (context.body().asJsonObject() != null) {
        JsonObject body = context.body().asJsonObject();
        LOGGER.debug("Body is a JsonObject: {}", body);
        id = body.getString(ID);
      }
    } catch (ClassCastException e) {
      LOGGER.debug("Body is not a JsonObject, trying as JsonArray.");
      try {
        if (context.body().asJsonArray() != null) {
          JsonObject body = context.body().asJsonArray().getJsonObject(0);
          LOGGER.debug("Body is a JsonArray, first element: {}", body);
          id = body.getString(ID);
        }
      } catch (Exception ex) {
        LOGGER.error("Error while processing body as JsonArray: {}", ex.getMessage());
      }
    }
    if (id == null) {
      LOGGER.error("ID not found in the request body.");
    }
    return id;
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
