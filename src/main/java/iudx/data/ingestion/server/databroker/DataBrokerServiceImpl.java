package iudx.data.ingestion.server.databroker;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.rabbitmq.RabbitMQClient;
import iudx.data.ingestion.server.databroker.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.TimeUnit;

import static iudx.data.ingestion.server.databroker.util.Constants.*;

public class DataBrokerServiceImpl implements DataBrokerService {

  private static final Logger LOGGER = LogManager.getLogger(DataBrokerServiceImpl.class);
  private final RabbitClient rabbitClient;
  private final String databrokerVhost;
  private final Cache<String, Boolean> exchangeListCache = CacheBuilder.newBuilder().maximumSize(1000)
      .expireAfterAccess(CACHE_TIMEOUT_AMOUNT, TimeUnit.MINUTES).build();

  public DataBrokerServiceImpl(RabbitMQClient client, RabbitWebClient rabbitWebClient, String vHost) {
    this.rabbitClient = new RabbitClient(client, rabbitWebClient);
    this.databrokerVhost = vHost;

    rabbitClient.populateExchangeCache(databrokerVhost, exchangeListCache);
  }

  @Override
  public DataBrokerService publishData(JsonObject request, Handler<AsyncResult<JsonObject>> handler) {
    // TODO Auto-generated method stub
    LOGGER.debug("Info : DataBrokerServiceImpl#publishData() started");
    if (request != null && !request.isEmpty()) {
      JsonObject metaData = Util.getMetadata(request);
      String exchange = metaData.getString(EXCHANGE_NAME);
      Boolean doesExchangeExist = exchangeListCache.getIfPresent(exchange);
      rabbitClient.getExchange(exchange, databrokerVhost, doesExchangeExist)
          .compose(ar -> {
            Boolean exchangeFound = ar.getBoolean(DOES_EXCHANGE_EXIST);
            exchangeListCache.put(exchange, exchangeFound);
            if(!exchangeFound) {
              return Future.failedFuture("Bad Request: Resource ID does not exist");
            }
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
