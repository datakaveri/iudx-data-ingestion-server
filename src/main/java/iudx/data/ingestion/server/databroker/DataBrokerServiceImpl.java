package iudx.data.ingestion.server.databroker;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.rabbitmq.RabbitMQClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;

import static iudx.data.ingestion.server.databroker.util.Constants.*;

public class DataBrokerServiceImpl implements DataBrokerService {

  private static final Logger LOGGER = LogManager.getLogger(DataBrokerServiceImpl.class);
  private final RabbitClient rabbitClient;
  private final String databrokerVhost;
  private HashSet<String> exchangeList = new HashSet();

  public DataBrokerServiceImpl(RabbitMQClient client, RabbitWebClient rabbitWebClient, String vHost) {
    this.rabbitClient = new RabbitClient(client, rabbitWebClient);
    this.databrokerVhost = vHost;
  }

  @Override
  public DataBrokerService publishData(JsonObject request, Handler<AsyncResult<JsonObject>> handler) {
    // TODO Auto-generated method stub
    LOGGER.debug("Info : DataBrokerServiceImpl#publishData() started");
    if (request != null && !request.isEmpty()) {
      rabbitClient.getAllExchanges(request, databrokerVhost, exchangeList)
          .compose(ar -> {
            exchangeList = (HashSet<String>) ar.getValue(EXCHANGE_SET);
            Boolean exchangeFound = ar.getBoolean(DOES_EXCHANGE_EXIST);
            if (!exchangeFound) {
              return Future.failedFuture("Bad Request: Resource ID does not exist");
            }
            JsonObject metaData = ar.getJsonObject(EXCHANGE_METADATA);
            return rabbitClient.publishMessage(request, metaData);
          })
          .onSuccess(ar -> {
            LOGGER.debug("Message published Successfully");
            handler.handle(Future.succeededFuture(new JsonObject().put(TYPE, SUCCESS)));
          })
          .onFailure(ar -> {
            LOGGER.fatal(ar);
            handler.handle(Future.succeededFuture(new JsonObject()
                .put(TYPE, FAILURE)
                .put(ERROR_MESSAGE, ar.getLocalizedMessage())
            ));
          });
    } else {
      handler.handle(Future.succeededFuture(new JsonObject()
          .put(TYPE, FAILURE)
          .put(ERROR_MESSAGE, "Bad Request: Request Json empty")
      ));
    }
    return this;
  }
}
