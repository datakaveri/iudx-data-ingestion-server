package iudx.data.ingestion.server.databroker;

import com.google.common.cache.Cache;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rabbitmq.RabbitMQClient;
import iudx.data.ingestion.server.databroker.util.Util;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static iudx.data.ingestion.server.databroker.util.Constants.*;

public class RabbitClient {

  private static final Logger LOGGER = LogManager.getLogger(RabbitClient.class);

  private final RabbitMQClient client;
  private final RabbitWebClient rabbitWebClient;

  public RabbitClient(RabbitMQClient rabbitmqClient, RabbitWebClient rabbitWebClient) {
    this.client = rabbitmqClient;
    this.rabbitWebClient = rabbitWebClient;

    client.start(clientStartupHandler -> {
      if (clientStartupHandler.succeeded()) {
        LOGGER.debug("Info : rabbit MQ client started");
      } else if (clientStartupHandler.failed()) {
        LOGGER.fatal("Fail : rabbit MQ client startup failed.");
      }
    });
  }

  public Future<JsonObject> publishMessage(JsonObject request, JsonObject metaData) {
    Promise<JsonObject> promise = Promise.promise();
    String exchangeName = metaData.getString(EXCHANGE_NAME);
    String routingKey = metaData.getString(ROUTING_KEY);
    JsonObject response = new JsonObject();
    LOGGER.debug("Sending message to exchange: {}, with routing key: {}", exchangeName, routingKey);
    client.basicPublish(exchangeName, routingKey, request.toBuffer(),
        asyncResult -> {
          if (asyncResult.succeeded()) {
            promise.complete(response);
          } else {
            promise.fail(asyncResult.cause());
          }
        });
    return promise.future();
  }

  public Future<JsonObject> publishMessage(JsonObject request) {
    JsonObject metaData = Util.getMetadata(request);
    String exchangeName = metaData.getString(EXCHANGE_NAME);
    String routingKey = metaData.getString(ROUTING_KEY);
    Promise<JsonObject> promise = Promise.promise();
    JsonObject response = new JsonObject();
    LOGGER.debug("Sending message to exchange: {}, with routing key: {}", exchangeName, routingKey);
    client.basicPublish(exchangeName, routingKey, request.toBuffer(),
        asyncResult -> {
          if (asyncResult.succeeded()) {
            promise.complete(response);
          } else {
            promise.fail(asyncResult.cause());
          }
        });
    return promise.future();
  }

  private Future<JsonObject> fetchExchange(String exchangeName, String vHost) {
    LOGGER.debug("INFO: Fetching Exchange: {} from vHost: {}", exchangeName, vHost);
    Promise<JsonObject> promise = Promise.promise();
    JsonObject result = new JsonObject();
    String exchangeUrl = Util.convertExchangeIntoUrl(exchangeName);
    String url = "/api/exchanges/" + vHost + "/" + exchangeUrl;
    rabbitWebClient.requestAsync(REQUEST_GET, url)
        .onComplete(ar -> {
          if (ar.succeeded()) {
            if (ar.result().statusCode() == HttpStatus.SC_OK) {
              LOGGER.debug("Given exchange exists");
              result.put(DOES_EXCHANGE_EXIST, true);
            } else {
              LOGGER.debug("Given exchange does not exists");
              result.put(DOES_EXCHANGE_EXIST, false);
            }
            promise.complete(result);
          } else {
            promise.fail(ar.cause());
          }
        });
    return promise.future();
  }

  public Future<Boolean> populateExchangeCache(String vHost, Cache<String, Boolean> exchangeListCache) {
    Promise<Boolean> promise = Promise.promise();
    String url = "/api/exchanges/" + vHost;
    rabbitWebClient.requestAsync(REQUEST_GET, url)
        .onSuccess(ar -> {
          JsonArray response = ar.bodyAsJsonArray();
          response.forEach(json -> {
            JsonObject exchange = (JsonObject) json;
            String exchangeName = exchange.getString(NAME);
            if (!exchangeName.isEmpty()) {
              LOGGER.debug("Adding {} exchange into cache", exchangeName);
              exchangeListCache.put(exchangeName, true);
            }
          });
        })
        .onFailure(ar -> {
          LOGGER.fatal(ar.getCause());
        });
    promise.complete(true);
    return promise.future();
  }

  public Future<JsonObject> getExchange(String exchange, String vHost, Boolean doesExchangeExist) {
    LOGGER.debug("INFO: Getting exchange: {} from vHost: {}", exchange, vHost);
    Promise<JsonObject> promise = Promise.promise();
    JsonObject response = new JsonObject();
    if (doesExchangeExist == null) {
      LOGGER.debug("INFO: Cache miss");
      fetchExchange(exchange, vHost)
          .onSuccess(promise::complete)
          .onFailure(ar -> promise.fail(ar.getCause()));
    } else {
      LOGGER.debug("INFO: Cache hit");
      response.put(DOES_EXCHANGE_EXIST, doesExchangeExist);
      promise.complete(response);
    }
    return promise.future();
  }
}
