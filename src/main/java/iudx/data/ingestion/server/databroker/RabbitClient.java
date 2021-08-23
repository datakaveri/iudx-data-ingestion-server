package iudx.data.ingestion.server.databroker;

import static iudx.data.ingestion.server.databroker.util.Constants.*;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.rabbitmq.RabbitMQClient;
import iudx.data.ingestion.server.databroker.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RabbitClient {

  private static final Logger LOGGER = LogManager.getLogger(RabbitClient.class);

  private RabbitMQClient client;

  public RabbitClient(RabbitMQClient rabbitMQClient) {
    this.client = rabbitMQClient;

    client.start(clientStartupHandler -> {
      if (clientStartupHandler.succeeded()) {
        LOGGER.debug("Info : rabbit MQ client started");
      } else if (clientStartupHandler.failed()) {
        LOGGER.fatal("Fail : rabbit MQ client startup failed.");
      }
    });
  }

  public Future<JsonObject> publishMessage(JsonObject request) {
    Promise<JsonObject> promise = Promise.promise();
    JsonObject response = new JsonObject();
    JsonObject metaData = Util.getMetadata(request);
    String exchangeName = metaData.getString(EXCHANGE_NAME);
    String routingKey = metaData.getString(ROUTING_KEY);
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
}
