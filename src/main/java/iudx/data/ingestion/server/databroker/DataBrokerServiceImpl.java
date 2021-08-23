package iudx.data.ingestion.server.databroker;

import static iudx.data.ingestion.server.databroker.util.Constants.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.rabbitmq.RabbitMQClient;

public class DataBrokerServiceImpl implements DataBrokerService {

  private static final Logger LOGGER = LogManager.getLogger(DataBrokerServiceImpl.class);
  private final RabbitClient rabbitClient;

  public DataBrokerServiceImpl(RabbitMQClient client) {
    this.rabbitClient = new RabbitClient(client);
  }

  @Override
  public DataBrokerService publishData(JsonObject request, Handler<AsyncResult<JsonObject>> handler) {
    // TODO Auto-generated method stub
    LOGGER.debug("Info : DataBrokerServiceImpl#publishData() started");
    if (request != null && !request.isEmpty()) {
      rabbitClient.publishMessage(request).onComplete(resultHandler -> {
        if (resultHandler.succeeded()) {
          LOGGER.debug("Info: Data published successfully");
          handler.handle(Future.succeededFuture(new JsonObject().put(TYPE, SUCCESS)));
        } else {
          LOGGER.error("Error: Could not publish data due to {}",
              resultHandler.cause().toString());
          handler.handle(Future.succeededFuture(new JsonObject().put(TYPE, FAILURE)));
        }
        LOGGER.debug("Info : DataBrokerServiceImpl#publishData() ended");
      });
    } else {
      handler.handle(Future.succeededFuture(new JsonObject().put(TYPE, FAILURE)));
    }
    return this;
  }
}
