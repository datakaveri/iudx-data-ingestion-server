package iudx.data.ingestion.server.databroker;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static iudx.data.ingestion.server.databroker.util.Constants.*;

public class RabbitWebClient {
  private static final Logger LOGGER = LogManager.getLogger(RabbitWebClient.class);

  private final WebClient webClient;
  private final String username;
  private final String password;

  RabbitWebClient(Vertx vertx, WebClientOptions webClientOptions, JsonObject propJson) {
    this.username = propJson.getString(USERNAME);
    this.password = propJson.getString(PASSWORD);
    this.webClient = getRabbitMQWebClient(vertx, webClientOptions);
  }

  private WebClient getRabbitMQWebClient(Vertx vertx, WebClientOptions webClientOptions) {
    return WebClient.create(vertx, webClientOptions);
  }

  public Future<HttpResponse<Buffer>> requestAsync(String requestType, String url,
                                                   JsonObject requestJson) {
    LOGGER.info(url+ requestJson);
    LOGGER.debug("Info : " + requestType + " : " + url + " : " + requestJson);
    Promise<HttpResponse<Buffer>> promise = Promise.promise();
    HttpRequest<Buffer> webRequest = createRequest(requestType, url);
    webRequest.sendJsonObject(requestJson, ar -> {
      LOGGER.info("line at 41"+ requestJson);
      if (ar.succeeded()) {
        LOGGER.info("line at 42"+requestType+ url+ requestJson);
        HttpResponse<Buffer> response = ar.result();
        promise.complete(response);
      } else {
        LOGGER.info("line at 466 "+requestType+ url+ requestJson);
        promise.fail(ar.cause());
      }
    });
    return promise.future();
  }

  public Future<HttpResponse<Buffer>> requestAsync(String requestType, String url) {
    LOGGER.debug("Info : " + requestType + " : " + url);
    Promise<HttpResponse<Buffer>> promise = Promise.promise();
    HttpRequest<Buffer> webRequest = createRequest(requestType, url);
    webRequest.send(ar -> {
      if (ar.succeeded()) {
        HttpResponse<Buffer> response = ar.result();
        promise.complete(response);
      } else {
        promise.fail(ar.cause());
      }
    });
    return promise.future();
  }

  private HttpRequest<Buffer> createRequest(String requestType, String url) {
    HttpRequest<Buffer> webRequest = null;
    switch (requestType) {
      case REQUEST_GET:
        webRequest = webClient.get(url).basicAuthentication(username, password);
        break;
      case REQUEST_POST:
        webRequest = webClient.post(url).basicAuthentication(username, password);
        break;
      case REQUEST_PUT:
        LOGGER.info(REQUEST_PUT + " Line 76 " + username);
        webRequest = webClient.put(url).basicAuthentication(username, password);
        LOGGER.info(" Line 81 " + password);
        break;
      case REQUEST_DELETE:
        webRequest = webClient.delete(url).basicAuthentication(username, password);
        break;
      default:
        break;
    }
    return webRequest;
  }
}