package iudx.data.ingestion.server.databroker;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheStats;
import com.google.common.collect.ImmutableMap;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rabbitmq.RabbitMQClient;
import io.vertx.rabbitmq.RabbitMQOptions;
import iudx.data.ingestion.server.configuration.Configuration;
import iudx.data.ingestion.server.databroker.util.Util;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import static iudx.data.ingestion.server.databroker.util.Constants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
@ExtendWith({VertxExtension.class, MockitoExtension.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DataBrokerServiceTest {

  /*static DataBrokerService databroker;
  static private String dataBrokerIP;
  static private int dataBrokerPort;
  static private int dataBrokerManagementPort;
  static private String dataBrokerVhost;
  static private String dataBrokerUserName;
  static private String dataBrokerPassword;
  static private int connectionTimeout;
  static private int requestedHeartbeat;
  static private int handshakeTimeout;
  static private int requestedChannelMax;
  static private int networkRecoveryInterval;
  private static RabbitMQOptions config;
  private static String exchangeName;
  private static String queueName;
  private static int statusOk;
  private static int statusNotFound;
  private static int statusNoContent;
  private static int statusConflict;
  private static RabbitClient rabbitClient;
  private static String datasetId;
  private static String resourceId;
  private static String badResourceId;

  private static final Logger logger = LogManager.getLogger(DataBrokerServiceTest.class);

  @BeforeAll
  @DisplayName("Deploy a verticle")
  static void startVertx(Vertx vertx, VertxTestContext testContext) {
    datasetId = "datakaveri.org/facec5182e3bf44cc3ac42b0b611263676d668a2/rs.iudx.org.in/agartala-env-aqm";
    resourceId = datasetId + "/shyamali-bazar";
    badResourceId = 'a' + resourceId;
    exchangeName = UUID.randomUUID().toString();
    queueName = UUID.randomUUID().toString();
    statusOk = 200;
    statusNotFound = 404;
    statusNoContent = 204;
    statusConflict = 409;

    Configuration appConfig = new Configuration();
    JsonObject brokerConfig = appConfig.configLoader(0, vertx);

    logger.info("Exchange Name is " + exchangeName);
    logger.info("Queue Name is " + queueName);

    // Read the configuration and set the rabbitMQ server properties.
    dataBrokerIP = brokerConfig.getString("dataBrokerIP");
    dataBrokerPort = brokerConfig.getInteger("dataBrokerPort");
    dataBrokerManagementPort =
        brokerConfig.getInteger("dataBrokerManagementPort");
    dataBrokerVhost = brokerConfig.getString("dataBrokerVhost");
    dataBrokerUserName = brokerConfig.getString("dataBrokerUserName");
    dataBrokerPassword = brokerConfig.getString("dataBrokerPassword");
    connectionTimeout = brokerConfig.getInteger("connectionTimeout");
    requestedHeartbeat = brokerConfig.getInteger("requestedHeartbeat");
    handshakeTimeout = brokerConfig.getInteger("handshakeTimeout");
    requestedChannelMax = brokerConfig.getInteger("requestedChannelMax");
    networkRecoveryInterval = brokerConfig.getInteger("networkRecoveryInterval");

     //Configure the RabbitMQ Data Broker client with input from config files.

    RabbitMQOptions config = new RabbitMQOptions()
        .setUser(dataBrokerUserName)
        .setPassword(dataBrokerPassword)
        .setHost(dataBrokerIP)
        .setPort(dataBrokerPort)
        .setVirtualHost(dataBrokerVhost)
        .setConnectionTimeout(connectionTimeout)
        .setRequestedHeartbeat(requestedHeartbeat)
        .setHandshakeTimeout(handshakeTimeout)
        .setRequestedChannelMax(requestedChannelMax)
        .setNetworkRecoveryInterval(networkRecoveryInterval)
        .setAutomaticRecoveryEnabled(true);

    WebClientOptions webConfig = new WebClientOptions()
        .setKeepAlive(true)
        .setConnectTimeout(86400000)
        .setDefaultHost(dataBrokerIP)
        .setDefaultPort(dataBrokerManagementPort)
        .setKeepAliveTimeout(86400000);

    // Create a RabbitMQ Client with the configuration and vertx cluster instance.

    RabbitMQClient client = RabbitMQClient.create(vertx, config);

   //  Create a Json Object for properties

    JsonObject propObj = new JsonObject()
        .put(USERNAME, dataBrokerUserName)
        .put(PASSWORD, dataBrokerPassword);

    // Call the databroker constructor with the RabbitMQ client.
    RabbitWebClient rabbitWebClient = new RabbitWebClient(vertx, webConfig, propObj);
    rabbitClient = new RabbitClient(client, rabbitWebClient);
    databroker = new DataBrokerServiceImpl(client, rabbitWebClient, dataBrokerVhost);

    testContext.completeNow();
  }


  @Test
  @DisplayName("Registering an adaptor with only datasetId but not queueName")
  @Order(8)
  void successRegisterAdaptorCaseI(VertxTestContext testContext) {
    JsonObject adaptorData = new JsonObject().put("id", datasetId);
    JsonObject metaData = Util.getMetadata(adaptorData);
    JsonObject expected = new JsonObject()
        .put(TYPE, SUCCESS)
        .put(QUEUE_NAME, DEFAULT_QUEUE)
        .put(EXCHANGE_NAME, metaData.getString(EXCHANGE_NAME))
        .put(ROUTING_KEY, metaData.getString(ROUTING_KEY));

    databroker.ingestDataPost(adaptorData, ar -> {
      if (ar.succeeded()) {
        JsonObject response = ar.result();
       // logger.debug("Ingest Data Post response: {}", response);
        assertEquals(expected, response);
        testContext.completeNow();
      } else {
        testContext.failNow(ar.cause());
      }
    });
  }

  @Test
  @DisplayName("Registering an adaptor with datasetId and queueName")
  @Order(9)
  void successRegisterAdaptorCaseII(VertxTestContext testContext) {
    JsonObject adaptorData = new JsonObject()
        .put("id", datasetId)
        .put("queue", queueName);
    JsonObject metaData = Util.getMetadata(adaptorData);
    JsonObject expected = new JsonObject()
        .put(TYPE, SUCCESS)
        .put(QUEUE_NAME, queueName)
        .put(EXCHANGE_NAME, metaData.getString(EXCHANGE_NAME))
        .put(ROUTING_KEY, metaData.getString(ROUTING_KEY));

    databroker.ingestDataPost(adaptorData, ar -> {
      if (ar.succeeded()) {
        JsonObject response = ar.result();
       // logger.debug("Ingest Data Post response: {}", response);
        assertEquals(expected, response);
        testContext.completeNow();
      } else {
        testContext.failNow(ar.cause());
      }
    });
  }

  @Test
  @DisplayName("Registering an adaptor with only resourceId but not queueName")
  @Order(10)
  void successRegisterAdaptorCaseIII(VertxTestContext testContext) {
    JsonObject adaptorData = new JsonObject().put("id", resourceId);
    JsonObject metaData = Util.getMetadata(adaptorData);
    JsonObject expected = new JsonObject()
        .put(TYPE, SUCCESS)
        .put(QUEUE_NAME, DEFAULT_QUEUE)
        .put(EXCHANGE_NAME, metaData.getString(EXCHANGE_NAME))
        .put(ROUTING_KEY, metaData.getString(ROUTING_KEY));

    databroker.ingestDataPost(adaptorData, ar -> {
      if (ar.succeeded()) {
        JsonObject response = ar.result();
     //   logger.debug("Ingest Data Post response: {}", response);
        assertEquals(expected, response);
        testContext.completeNow();
      } else {
        testContext.failNow(ar.cause());
      }
    });
  }

  @Test
  @DisplayName("Registering an adaptor with resourceId and queueName")
  @Order(11)
  void successRegisterAdaptorCaseIV(VertxTestContext testContext) {
    JsonObject adaptorData = new JsonObject()
        .put("id", resourceId)
        .put("queue", queueName);
    JsonObject metaData = Util.getMetadata(adaptorData);
    JsonObject expected = new JsonObject()
        .put(TYPE, SUCCESS)
        .put(QUEUE_NAME, queueName)
        .put(EXCHANGE_NAME, metaData.getString(EXCHANGE_NAME))
        .put(ROUTING_KEY, metaData.getString(ROUTING_KEY));

    databroker.ingestDataPost(adaptorData, ar -> {
      if (ar.succeeded()) {
        JsonObject response = ar.result();
     //   logger.debug("Ingest Data Post response: {}", response);
        assertEquals(expected, response);
        testContext.completeNow();
      } else {
        testContext.failNow(ar.cause());
      }
    });
  }

  @Test
  @DisplayName("Publish message from adaptor")
  @Order(12)
  void successPublishMessage(VertxTestContext testContext) {
    JsonObject adapterData = new JsonObject().put("id", resourceId);
    JsonObject expected = new JsonObject().put(TYPE, SUCCESS);

    databroker.publishData(adapterData, ar -> {
      if (ar.succeeded()) {
        JsonObject response = ar.result();
        //logger.debug("Publish message response: {}", response);
        assertEquals(expected, response);
        testContext.completeNow();
      } else {
        testContext.failNow(ar.cause());
      }
    });
  }

  @Test
  @DisplayName("Publishing a message to exchange which does not exist")
  @Order(13)
  void failPublishMessage(VertxTestContext testContext) {
    JsonObject adapterData = new JsonObject().put("id", badResourceId);
    JsonObject expected = new JsonObject()
        .put(TYPE, FAILURE)
        .put(ERROR_MESSAGE, "Bad Request: Resource ID does not exist");

    databroker.publishData(adapterData, ar -> {
      if (ar.succeeded()) {
        JsonObject response = ar.result();
       // logger.debug("Publish message response: {}", response);
        assertEquals(expected, response);
        testContext.completeNow();
      } else {
        testContext.failNow(ar.cause());
      }
    });
  }

  @Test
  @DisplayName("Deleting an adaptor with resourceId")
  @Order(14)
  void successDeleteAdaptor(VertxTestContext testContext) {
    JsonObject adapterData = new JsonObject().put("id", resourceId);
    JsonObject metaData = Util.getMetadata(adapterData);
    JsonObject expected = new JsonObject().put(EXCHANGE, metaData.getString(EXCHANGE_NAME));

    databroker.ingestDataDelete(adapterData, ar -> {
      if (ar.succeeded()) {
        JsonObject response = ar.result();
      //  logger.debug("Ingest Data delete response: {}", response);
        assertEquals(expected, response);
        testContext.completeNow();
      } else {
        testContext.failNow(ar.cause());
      }
    });
  }

  @Test
  @DisplayName("Deleting an already deleted adaptor")
  @Order(15)
  void failDeleteAdaptor(VertxTestContext testContext) {
    JsonObject adapterData = new JsonObject().put("id", resourceId);

    JsonObject expected = new JsonObject()
        .put(TYPE, statusNotFound)
        .put(TITLE, FAILURE)
        .put(DETAIL, EXCHANGE_NOT_FOUND);

    databroker.ingestDataDelete(adapterData, ar -> {
      if (ar.succeeded()) {
        JsonObject response = ar.result();
       // logger.debug("Ingest Data delete response: {}", response);
        assertEquals(expected, response);
        testContext.completeNow();
      } else {
        testContext.failNow(ar.cause());
      }
    });
  }*/

   /* private final Cache<String, Boolean> exchangeListCache1 = new Cache<String, Boolean>() {
        @Override
        public @Nullable Boolean getIfPresent(Object o) {
            return null;
        }

        @Override
        public Boolean get(String s, Callable<? extends Boolean> callable) throws ExecutionException {
            return null;
        }

        @Override
        public ImmutableMap<String, Boolean> getAllPresent(Iterable<?> iterable) {
            return null;
        }

        @Override
        public void put(String s, Boolean aBoolean) {

        }

        @Override
        public void putAll(Map<? extends String, ? extends Boolean> map) {

        }

        @Override
        public void invalidate(Object o) {

        }

        @Override
        public void invalidateAll(Iterable<?> iterable) {

        }

        @Override
        public void invalidateAll() {

        }

        @Override
        public long size() {
            return 0;
        }

        @Override
        public CacheStats stats() {
            return null;
        }

        @Override
        public ConcurrentMap<String, Boolean> asMap() {
            return null;
        }

        @Override
        public void cleanUp() {

        }
    };
*/

/*
    @Mock
    RabbitMQClient rabbitMQClient;
    @Mock
    RabbitWebClient rabbitWebClient;
@Mock
RabbitClient rabbitClient;
    String vHost = "dummy host";

    String exchangeName;
    String queueName;
    DataBrokerServiceImpl dataBrokerService;
    @Mock
    Future<HttpResponse<Buffer>> httpResponseFuture;
    @Mock
    AsyncResult<HttpResponse<Buffer>> httpResponseAsyncResult;
    @Mock
    HttpResponse<Buffer> bufferHttpResponse;
    @Mock
    HttpRequest<Buffer> bufferHttpRequest;

    DataBrokerServiceImpl databrokerSpy;

  @BeforeEach
  public void setup(VertxTestContext vertxTestContext) {
    dataBrokerService = new DataBrokerServiceImpl(rabbitMQClient, rabbitWebClient, vHost);
    JsonObject jsonObject = new JsonObject().put(NAME, "Exchange name");
    JsonArray jsonArray = new JsonArray().add(jsonObject);

    databrokerSpy = spy(dataBrokerService);
    rabbitClient = new RabbitClient(rabbitMQClient, rabbitWebClient);

    when(rabbitWebClient.requestAsync(anyString(), anyString())).thenReturn(httpResponseFuture);

    // when(httpResponseAsyncResult.succeeded()).thenReturn(true);
    when(bufferHttpResponse.bodyAsJsonArray()).thenReturn(jsonArray);

    doAnswer(
            new Answer<HttpResponse<Buffer>>() {
              @Override
              public HttpResponse<Buffer> answer(InvocationOnMock arg0) throws Throwable {
                ((Handler<HttpResponse<Buffer>>) arg0.getArgument(0)).handle(bufferHttpResponse);
                return null;
              }
            })
        .when(httpResponseFuture)
        .onSuccess(any());
    vertxTestContext.completeNow();
        }

    @Test
    public void dummy(VertxTestContext vertxTestContext){
        vertxTestContext.completeNow();
    }
*/

}