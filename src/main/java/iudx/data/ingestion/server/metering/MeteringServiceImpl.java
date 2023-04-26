package iudx.data.ingestion.server.metering;

import static iudx.data.ingestion.server.metering.util.Constants.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.spi.impl.operationservice.impl.responses.Response;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import iudx.data.ingestion.server.databroker.DataBrokerService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MeteringServiceImpl implements MeteringService {

  private static final Logger LOGGER = LogManager.getLogger(MeteringServiceImpl.class);
  private final DataBrokerService dataBrokerService;
  private final ObjectMapper objectMapper = new ObjectMapper();


  public MeteringServiceImpl(DataBrokerService dataBrokerService) {
    this.dataBrokerService = dataBrokerService;
  }

  public MeteringService insertMeteringValuesInRmq(JsonObject writeMessage,
                                                   Handler<AsyncResult<JsonObject>> handler) {
    dataBrokerService.publishMessage(writeMessage, EXCHANGE_NAME, ROUTING_KEY, rmqHandler -> {
      if (rmqHandler.succeeded()) {
        handler.handle(Future.succeededFuture());
      } else {
        LOGGER.error(rmqHandler.cause());
        try {
          Response resp = objectMapper.readValue(rmqHandler.cause().getMessage(), Response.class);
          LOGGER.debug("response from rmq " + resp);
          handler.handle(Future.failedFuture(resp.toString()));
        } catch (JsonProcessingException e) {
          LOGGER.error("Failure message not in format [type,title,detail]");
          handler.handle(Future.failedFuture(e.getMessage()));
        }
      }
    });
    return this;
  }

}
