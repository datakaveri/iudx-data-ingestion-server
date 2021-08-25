package iudx.data.ingestion.server.databroker;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rabbitmq.RabbitMQClient;
import iudx.data.ingestion.server.databroker.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;

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

  public Future<JsonObject> getAllExchanges(JsonObject request, String vHost, HashSet<String> exchangeList) {
    Promise<JsonObject> promise = Promise.promise();
    JsonObject response = new JsonObject();
    JsonObject metaData = Util.getMetadata(request);
    String exchangeName = metaData.getString(EXCHANGE_NAME);
    response.put(EXCHANGE_METADATA, metaData);
    LOGGER.debug("Fetching all exchanges");
    if (!exchangeList.isEmpty() && exchangeList.contains(exchangeName)) {
      response
          .put(DOES_EXCHANGE_EXIST, true)
          .put(EXCHANGE_SET, exchangeList);
      promise.complete(response);
    } else {
      String url = "/api/exchanges" + vHost;
      rabbitWebClient.requestAsync(REQUEST_GET, url)
          .onComplete(ar -> {
            if (ar.succeeded()) {
              HashSet<String> updatedList = new HashSet<>();
              JsonArray allExchanges = ar.result().bodyAsJsonArray();
              for (int i = 0; i < allExchanges.size(); i++) {
                JsonObject exchangeObj = allExchanges.getJsonObject(i);
                String exchange = exchangeObj.getString(NAME);
                if (!exchange.isEmpty()) {
                  updatedList.add(exchange);
                }
              }
              response
                  .put(EXCHANGE_SET, updatedList)
                  .put(DOES_EXCHANGE_EXIST, updatedList.contains(exchangeName));
              promise.complete(response);
            } else {
              promise.fail(ar.cause());
              LOGGER.error("ERROR: Could not get all exchanges due to {}", ar.cause().toString());
            }
          });
    }
    return promise.future();
  }
}
