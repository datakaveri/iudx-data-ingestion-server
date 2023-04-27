package iudx.data.ingestion.server.metering;

import static iudx.data.ingestion.server.databroker.util.Constants.*;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceBinder;
import iudx.data.ingestion.server.databroker.DataBrokerService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MeteringVerticle extends AbstractVerticle {

  private static final String METERING_SERVICE_ADDRESS = "iudx.data.ingestion.metering.service";
  private static final Logger LOGGER = LogManager.getLogger(MeteringVerticle.class);
  private ServiceBinder binder;
  private MessageConsumer<JsonObject> consumer;
  private MeteringService metering;
  private DataBrokerService dataBrokerService;


  @Override
  public void start() throws Exception {
    dataBrokerService = DataBrokerService.createProxy(vertx, BROKER_SERVICE_ADDRESS);

    binder = new ServiceBinder(vertx);
    metering = new MeteringServiceImpl(dataBrokerService);
    consumer =
        binder.setAddress(METERING_SERVICE_ADDRESS).register(MeteringService.class, metering);
    LOGGER.info("Metering Verticle Started");
  }

  @Override
  public void stop() {
    binder.unregister(consumer);
  }
}
