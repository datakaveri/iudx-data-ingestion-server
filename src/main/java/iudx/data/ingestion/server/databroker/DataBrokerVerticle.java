package iudx.data.ingestion.server.databroker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.vertx.core.AbstractVerticle;

public class DataBrokerVerticle extends AbstractVerticle {

	private static final String BROKER_SERVICE_ADDRESS = "iudx.rs.broker.service";
	private static final Logger LOGGER = LogManager.getLogger(DataBrokerVerticle.class);

}
