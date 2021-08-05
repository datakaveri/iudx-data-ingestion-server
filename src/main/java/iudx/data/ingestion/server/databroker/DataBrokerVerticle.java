package iudx.data.ingestion.server.databroker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.rabbitmq.RabbitMQClient;
import io.vertx.rabbitmq.RabbitMQOptions;
import io.vertx.serviceproxy.ServiceBinder;

public class DataBrokerVerticle extends AbstractVerticle {

	private static final String BROKER_SERVICE_ADDRESS = "iudx.data.ingestion.broker.service";
	private static final Logger LOGGER = LogManager.getLogger(DataBrokerVerticle.class);
	private DataBrokerService databroker;
	private RabbitMQOptions config;
	private RabbitMQClient client;
	private String dataBrokerIP;
	private int dataBrokerPort;
	private int dataBrokerManagementPort;
	private String dataBrokerVhost;
	private String dataBrokerUserName;
	private String dataBrokerPassword;
	private int connectionTimeout;
	private int requestedHeartbeat;
	private int handshakeTimeout;
	private int requestedChannelMax;
	private int networkRecoveryInterval;
	private ServiceBinder binder;
	private MessageConsumer<JsonObject> consumer;

	@Override
	public void start() throws Exception {

		/* Read the configuration and set the rabbitMQ server properties. */
		dataBrokerIP = config().getString("dataBrokerIP");
		dataBrokerPort = config().getInteger("dataBrokerPort");
		dataBrokerManagementPort = config().getInteger("dataBrokerManagementPort");
		dataBrokerVhost = config().getString("dataBrokerVhost");
		dataBrokerUserName = config().getString("dataBrokerUserName");
		dataBrokerPassword = config().getString("dataBrokerPassword");
		connectionTimeout = config().getInteger("connectionTimeout");
		requestedHeartbeat = config().getInteger("requestedHeartbeat");
		handshakeTimeout = config().getInteger("handshakeTimeout");
		requestedChannelMax = config().getInteger("requestedChannelMax");
		networkRecoveryInterval = config().getInteger("networkRecoveryInterval");

		/* Configure the RabbitMQ Data Broker client with input from config files. */

		config = new RabbitMQOptions();
		config.setUser(dataBrokerUserName);
		config.setPassword(dataBrokerPassword);
		config.setHost(dataBrokerIP);
		config.setPort(dataBrokerPort);
		config.setVirtualHost(dataBrokerVhost);
		config.setConnectionTimeout(connectionTimeout);
		config.setRequestedHeartbeat(requestedHeartbeat);
		config.setHandshakeTimeout(handshakeTimeout);
		config.setRequestedChannelMax(requestedChannelMax);
		config.setNetworkRecoveryInterval(networkRecoveryInterval);
		config.setAutomaticRecoveryEnabled(true);

		/*
		 * Create a RabbitMQ Clinet with the configuration and vertx cluster instance.
		 */

		client = RabbitMQClient.create(vertx, config);

		binder = new ServiceBinder(vertx);
		databroker = new DataBrokerServiceImpl(client);

		/* Publish the Data Broker service with the Event Bus against an address. */

		consumer = binder.setAddress(BROKER_SERVICE_ADDRESS).register(DataBrokerService.class, databroker);

	}

	@Override
	public void stop() {
		binder.unregister(consumer);
	}

}
