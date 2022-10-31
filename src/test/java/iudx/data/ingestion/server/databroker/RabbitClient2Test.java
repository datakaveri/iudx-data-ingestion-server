package iudx.data.ingestion.server.databroker;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rabbitmq.RabbitMQClient;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static iudx.data.ingestion.server.databroker.util.Constants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith({VertxExtension.class, MockitoExtension.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RabbitClient2Test {

  @Mock RabbitMQClient rabbitMQClient;
  @Mock RabbitWebClient rabbitWebClient;
  @Mock JsonArray jsonArray;

  String exchangeName;
  String queueName;
  String vHost;
  @Mock Future<HttpResponse<Buffer>> httpResponseFuture;
  @Mock AsyncResult<HttpResponse<Buffer>> httpResponseAsyncResult;
  @Mock HttpResponse<Buffer> bufferHttpResponse;
  @Mock HttpRequest<Buffer> bufferHttpRequest;
  @Mock RabbitClient mockRabbitClient;
  RabbitClient rabbitClient;

  @BeforeEach
  public void setup(VertxTestContext vertxTestContext) {
    rabbitClient = new RabbitClient(rabbitMQClient, rabbitWebClient);
    exchangeName = UUID.randomUUID().toString();
    queueName = UUID.randomUUID().toString();
    vHost = "Dummy host";

    vertxTestContext.completeNow();
  }

  @Test
  @DisplayName("create exchange test")
  public void createExchangeTest(VertxTestContext vertxTestContext) {

    String url = "abc/abc/abc";
    JsonObject exchangeProperties = new JsonObject();
    exchangeProperties.put(TYPE, EXCHANGE_TYPE).put(AUTO_DELETE, false).put(DURABLE, true);

    when(rabbitWebClient.requestAsync(anyString(), anyString(), any()))
        .thenReturn(httpResponseFuture);
    when(httpResponseAsyncResult.succeeded()).thenReturn(true);
    when(httpResponseAsyncResult.result()).thenReturn(bufferHttpResponse);

    when(bufferHttpResponse.statusCode()).thenReturn(201);

    doAnswer(
            new Answer<AsyncResult<HttpResponse<Buffer>>>() {
              @Override
              public AsyncResult<HttpResponse<Buffer>> answer(InvocationOnMock arg0)
                  throws Throwable {
                ((Handler<AsyncResult<HttpResponse<Buffer>>>) arg0.getArgument(0))
                    .handle(httpResponseAsyncResult);
                return null;
              }
            })
        .when(httpResponseFuture)
        .onComplete(any());

    rabbitClient
        .createExchange(exchangeName, vHost)
        .onComplete(
            handler -> {
              if (handler.succeeded()) {
                assertEquals(exchangeName, handler.result().getString(EXCHANGE));
                vertxTestContext.completeNow();
              } else {
                handler.failed();
              }
            });
  }

  @Test
  @DisplayName("create exchange test 204")
  public void createExchangeTest204(VertxTestContext vertxTestContext) {

    String url = "abc/abc/abc";
    JsonObject exchangeProperties = new JsonObject();
    exchangeProperties.put(TYPE, EXCHANGE_TYPE).put(AUTO_DELETE, false).put(DURABLE, true);

    when(rabbitWebClient.requestAsync(anyString(), anyString(), any()))
        .thenReturn(httpResponseFuture);
    when(httpResponseAsyncResult.succeeded()).thenReturn(true);
    when(httpResponseAsyncResult.result()).thenReturn(bufferHttpResponse);

    when(bufferHttpResponse.statusCode()).thenReturn(204);

    doAnswer(
            new Answer<AsyncResult<HttpResponse<Buffer>>>() {
              @Override
              public AsyncResult<HttpResponse<Buffer>> answer(InvocationOnMock arg0)
                  throws Throwable {
                ((Handler<AsyncResult<HttpResponse<Buffer>>>) arg0.getArgument(0))
                    .handle(httpResponseAsyncResult);
                return null;
              }
            })
        .when(httpResponseFuture)
        .onComplete(any());

    rabbitClient
        .createExchange(exchangeName, vHost)
        .onComplete(
            handler -> {
              if (handler.succeeded()) {
                assertEquals("Exchange already exists", handler.result().getString("detail"));
                assertEquals("failure", handler.result().getString("title"));
                vertxTestContext.completeNow();
              } else {
                handler.failed();
              }
            });
  }

  @Test
  @DisplayName("create exchange test 400")
  public void createExchangeTest400(VertxTestContext vertxTestContext) {

    String url = "abc/abc/abc";
    JsonObject exchangeProperties = new JsonObject();
    exchangeProperties.put(TYPE, EXCHANGE_TYPE).put(AUTO_DELETE, false).put(DURABLE, true);

    when(rabbitWebClient.requestAsync(anyString(), anyString(), any()))
        .thenReturn(httpResponseFuture);
    when(httpResponseAsyncResult.succeeded()).thenReturn(true);
    when(httpResponseAsyncResult.result()).thenReturn(bufferHttpResponse);

    when(bufferHttpResponse.statusCode()).thenReturn(400);

    doAnswer(
            new Answer<AsyncResult<HttpResponse<Buffer>>>() {
              @Override
              public AsyncResult<HttpResponse<Buffer>> answer(InvocationOnMock arg0)
                  throws Throwable {
                ((Handler<AsyncResult<HttpResponse<Buffer>>>) arg0.getArgument(0))
                    .handle(httpResponseAsyncResult);
                return null;
              }
            })
        .when(httpResponseFuture)
        .onComplete(any());

    rabbitClient
        .createExchange(exchangeName, vHost)
        .onComplete(
            handler -> {
              if (handler.succeeded()) {
                assertEquals(
                    EXCHANGE_EXISTS_WITH_DIFFERENT_PROPERTIES,
                    handler.result().getString("detail"));
                assertEquals("failure", handler.result().getString("title"));
                vertxTestContext.completeNow();
              } else {
                handler.failed();
              }
            });
  }

  @Test
  @DisplayName("Get Queue test 200")
  public void getQueueTest200(VertxTestContext vertxTestContext) {

    String url = "abc/abc/abc";

    when(rabbitWebClient.requestAsync(anyString(), anyString())).thenReturn(httpResponseFuture);
    when(httpResponseAsyncResult.succeeded()).thenReturn(true);
    when(httpResponseAsyncResult.result()).thenReturn(bufferHttpResponse);

    when(bufferHttpResponse.statusCode()).thenReturn(200);

    doAnswer(
            new Answer<AsyncResult<HttpResponse<Buffer>>>() {
              @Override
              public AsyncResult<HttpResponse<Buffer>> answer(InvocationOnMock arg0)
                  throws Throwable {
                ((Handler<AsyncResult<HttpResponse<Buffer>>>) arg0.getArgument(0))
                    .handle(httpResponseAsyncResult);
                return null;
              }
            })
        .when(httpResponseFuture)
        .onComplete(any());

    rabbitClient
        .getQueue(queueName, vHost)
        .onComplete(
            handler -> {
              if (handler.succeeded()) {
                assertEquals(200, handler.result().getInteger("type"));
                assertEquals("success", handler.result().getString("title"));
                assertEquals("Queue Found", handler.result().getString("detail"));
                vertxTestContext.completeNow();
              } else {
                handler.failed();
              }
            });
  }

  @Test
  @DisplayName("Get Queue test 404")
  public void getQueuesTest404(VertxTestContext vertxTestContext) {

    String url = "abc/abc/abc";

    when(rabbitWebClient.requestAsync(anyString(), anyString())).thenReturn(httpResponseFuture);
    when(httpResponseAsyncResult.succeeded()).thenReturn(true);
    when(httpResponseAsyncResult.result()).thenReturn(bufferHttpResponse);

    when(bufferHttpResponse.statusCode()).thenReturn(404);

    doAnswer(
            new Answer<AsyncResult<HttpResponse<Buffer>>>() {
              @Override
              public AsyncResult<HttpResponse<Buffer>> answer(InvocationOnMock arg0)
                  throws Throwable {
                ((Handler<AsyncResult<HttpResponse<Buffer>>>) arg0.getArgument(0))
                    .handle(httpResponseAsyncResult);
                return null;
              }
            })
        .when(httpResponseFuture)
        .onComplete(any());

    rabbitClient
        .getQueue(queueName, vHost)
        .onComplete(
            handler -> {
              if (handler.succeeded()) {
                assertEquals(404, handler.result().getInteger("type"));
                assertEquals(FAILURE, handler.result().getString("title"));
                assertEquals(QUEUE_NOT_FOUND, handler.result().getString("detail"));
                vertxTestContext.completeNow();
              } else {
                handler.failed();
              }
            });
  }

  @Test
  @DisplayName("Create Queue test 201")
  public void createQueueTest201(VertxTestContext vertxTestContext) {

    String url = "abc/abc/abc";

    when(rabbitWebClient.requestAsync(anyString(), anyString(), any()))
        .thenReturn(httpResponseFuture);
    when(httpResponseAsyncResult.succeeded()).thenReturn(true);
    when(httpResponseAsyncResult.result()).thenReturn(bufferHttpResponse);

    when(bufferHttpResponse.statusCode()).thenReturn(201);

    doAnswer(
            new Answer<AsyncResult<HttpResponse<Buffer>>>() {
              @Override
              public AsyncResult<HttpResponse<Buffer>> answer(InvocationOnMock arg0)
                  throws Throwable {
                ((Handler<AsyncResult<HttpResponse<Buffer>>>) arg0.getArgument(0))
                    .handle(httpResponseAsyncResult);
                return null;
              }
            })
        .when(httpResponseFuture)
        .onComplete(any());

    rabbitClient
        .createQueue(queueName, vHost)
        .onComplete(
            handler -> {
              if (handler.succeeded()) {
                assertEquals(queueName, handler.result().getString(QUEUE_NAME));
                vertxTestContext.completeNow();
              } else {
                handler.failed();
              }
            });
  }

  @Test
  @DisplayName("Create Queue test 204")
  public void createQueueTest204(VertxTestContext vertxTestContext) {

    String url = "abc/abc/abc";

    when(rabbitWebClient.requestAsync(anyString(), anyString(), any()))
        .thenReturn(httpResponseFuture);
    when(httpResponseAsyncResult.succeeded()).thenReturn(true);
    when(httpResponseAsyncResult.result()).thenReturn(bufferHttpResponse);

    when(bufferHttpResponse.statusCode()).thenReturn(204);

    doAnswer(
            new Answer<AsyncResult<HttpResponse<Buffer>>>() {
              @Override
              public AsyncResult<HttpResponse<Buffer>> answer(InvocationOnMock arg0)
                  throws Throwable {
                ((Handler<AsyncResult<HttpResponse<Buffer>>>) arg0.getArgument(0))
                    .handle(httpResponseAsyncResult);
                return null;
              }
            })
        .when(httpResponseFuture)
        .onComplete(any());

    rabbitClient
        .createQueue(queueName, vHost)
        .onComplete(
            handler -> {
              if (handler.succeeded()) {
                assertEquals(FAILURE, handler.result().getString("title"));
                assertEquals(QUEUE_ALREADY_EXISTS, handler.result().getString("detail"));
                vertxTestContext.completeNow();
              } else {
                handler.failed();
              }
            });
  }

  @Test
  @DisplayName("Create Queue test 400")
  public void createQueueTest400(VertxTestContext vertxTestContext) {

    String url = "abc/abc/abc";

    when(rabbitWebClient.requestAsync(anyString(), anyString(), any()))
        .thenReturn(httpResponseFuture);
    when(httpResponseAsyncResult.succeeded()).thenReturn(true);
    when(httpResponseAsyncResult.result()).thenReturn(bufferHttpResponse);

    when(bufferHttpResponse.statusCode()).thenReturn(400);

    doAnswer(
            new Answer<AsyncResult<HttpResponse<Buffer>>>() {
              @Override
              public AsyncResult<HttpResponse<Buffer>> answer(InvocationOnMock arg0)
                  throws Throwable {
                ((Handler<AsyncResult<HttpResponse<Buffer>>>) arg0.getArgument(0))
                    .handle(httpResponseAsyncResult);
                return null;
              }
            })
        .when(httpResponseFuture)
        .onComplete(any());

    rabbitClient
        .createQueue(queueName, vHost)
        .onComplete(
            handler -> {
              if (handler.succeeded()) {
                assertEquals(FAILURE, handler.result().getString("title"));
                assertEquals(
                    QUEUE_ALREADY_EXISTS_WITH_DIFFERENT_PROPERTIES,
                    handler.result().getString("detail"));
                vertxTestContext.completeNow();
              } else {
                handler.failed();
              }
            });
  }

  @Test
  @DisplayName("Get Exchange")
  public void getExchangeTestTrue(VertxTestContext vertxTestContext) {

    String url = "abc/abc/abc";

    when(rabbitWebClient.requestAsync(anyString(), anyString())).thenReturn(httpResponseFuture);
    when(httpResponseAsyncResult.succeeded()).thenReturn(true);
    when(httpResponseAsyncResult.result()).thenReturn(bufferHttpResponse);

    lenient().when(bufferHttpResponse.statusCode()).thenReturn(200);

    doAnswer(
            new Answer<AsyncResult<HttpResponse<Buffer>>>() {
              @Override
              public AsyncResult<HttpResponse<Buffer>> answer(InvocationOnMock arg0)
                  throws Throwable {
                ((Handler<AsyncResult<HttpResponse<Buffer>>>) arg0.getArgument(0))
                    .handle(httpResponseAsyncResult);
                return null;
              }
            })
        .when(httpResponseFuture)
        .onComplete(any());
    rabbitClient
        .getExchange(exchangeName, vHost, null)
        .onComplete(
            handler -> {
              if (handler.succeeded()) {
                assertEquals(true, handler.result().getBoolean(DOES_EXCHANGE_EXIST));
                vertxTestContext.completeNow();
              } else {
                handler.failed();
              }
            });
  }

  @Test
  @DisplayName("Get Exchange False")
  public void getExchangeTestFalse(VertxTestContext vertxTestContext) {

    String url = "abc/abc/abc";

    when(rabbitWebClient.requestAsync(anyString(), anyString())).thenReturn(httpResponseFuture);
    when(httpResponseAsyncResult.succeeded()).thenReturn(true);
    when(httpResponseAsyncResult.result()).thenReturn(bufferHttpResponse);

    when(bufferHttpResponse.statusCode()).thenReturn(400);

    doAnswer(
            new Answer<AsyncResult<HttpResponse<Buffer>>>() {
              @Override
              public AsyncResult<HttpResponse<Buffer>> answer(InvocationOnMock arg0)
                  throws Throwable {
                ((Handler<AsyncResult<HttpResponse<Buffer>>>) arg0.getArgument(0))
                    .handle(httpResponseAsyncResult);
                return null;
              }
            })
        .when(httpResponseFuture)
        .onComplete(any());
    rabbitClient
        .getExchange(exchangeName, vHost, null)
        .onComplete(
            handler -> {
              if (handler.succeeded()) {
                assertEquals(false, handler.result().getBoolean(DOES_EXCHANGE_EXIST));
                vertxTestContext.completeNow();
              } else {
                handler.failed();
              }
            });
  }

  @Test
  @DisplayName("Get Exchange boolean is somethinng")
  public void getExchangeTest(VertxTestContext vertxTestContext) {

    rabbitClient
        .getExchange(exchangeName, vHost, true)
        .onComplete(
            handler -> {
              if (handler.succeeded()) {
                assertEquals(true, handler.result().getBoolean(DOES_EXCHANGE_EXIST));
                vertxTestContext.completeNow();
              } else {
                handler.failed();
              }
            });
  }

  @Test
  @DisplayName("Get Queue test 404")
  public void getQueueTest404(VertxTestContext vertxTestContext) {

    String url = "abc/abc/abc";

    when(rabbitWebClient.requestAsync(anyString(), anyString())).thenReturn(httpResponseFuture);
    when(httpResponseAsyncResult.succeeded()).thenReturn(true);
    when(httpResponseAsyncResult.result()).thenReturn(bufferHttpResponse);

    when(bufferHttpResponse.statusCode()).thenReturn(404);

    doAnswer(
            new Answer<AsyncResult<HttpResponse<Buffer>>>() {
              @Override
              public AsyncResult<HttpResponse<Buffer>> answer(InvocationOnMock arg0)
                  throws Throwable {
                ((Handler<AsyncResult<HttpResponse<Buffer>>>) arg0.getArgument(0))
                    .handle(httpResponseAsyncResult);
                return null;
              }
            })
        .when(httpResponseFuture)
        .onComplete(any());

    rabbitClient
        .getQueue(queueName, vHost)
        .onComplete(
            handler -> {
              if (handler.succeeded()) {
                assertEquals(404, handler.result().getInteger("type"));
                assertEquals(FAILURE, handler.result().getString("title"));
                assertEquals(QUEUE_NOT_FOUND, handler.result().getString("detail"));
                vertxTestContext.completeNow();
              } else {
                handler.failed();
              }
            });
  }

  @Test
  @DisplayName("Get Queue test 200")
  public void getQueueJsonTest200(VertxTestContext vertxTestContext) {
    JsonObject request = new JsonObject().put("queue", queueName);

    when(rabbitWebClient.requestAsync(anyString(), anyString())).thenReturn(httpResponseFuture);
    when(httpResponseAsyncResult.succeeded()).thenReturn(true);
    when(httpResponseAsyncResult.result()).thenReturn(bufferHttpResponse);

    when(bufferHttpResponse.statusCode()).thenReturn(200);

    doAnswer(
            new Answer<AsyncResult<HttpResponse<Buffer>>>() {
              @Override
              public AsyncResult<HttpResponse<Buffer>> answer(InvocationOnMock arg0)
                  throws Throwable {
                ((Handler<AsyncResult<HttpResponse<Buffer>>>) arg0.getArgument(0))
                    .handle(httpResponseAsyncResult);
                return null;
              }
            })
        .when(httpResponseFuture)
        .onComplete(any());

    rabbitClient
        .getQueue(request, vHost)
        .onComplete(
            handler -> {
              if (handler.succeeded()) {

                assertEquals(SUCCESS, handler.result().getString("title"));
                vertxTestContext.completeNow();
              } else {
                handler.failed();
              }
            });
  }

  @Test
  @DisplayName("Get Queue test 404")
  public void getQueueJsonTest404(VertxTestContext vertxTestContext) {
    JsonObject request = new JsonObject().put("queue", queueName);

    when(rabbitWebClient.requestAsync(anyString(), anyString())).thenReturn(httpResponseFuture);
    when(httpResponseAsyncResult.succeeded()).thenReturn(true);
    when(httpResponseAsyncResult.result()).thenReturn(bufferHttpResponse);

    when(bufferHttpResponse.statusCode()).thenReturn(404);

    doAnswer(
            new Answer<AsyncResult<HttpResponse<Buffer>>>() {
              @Override
              public AsyncResult<HttpResponse<Buffer>> answer(InvocationOnMock arg0)
                  throws Throwable {
                ((Handler<AsyncResult<HttpResponse<Buffer>>>) arg0.getArgument(0))
                    .handle(httpResponseAsyncResult);
                return null;
              }
            })
        .when(httpResponseFuture)
        .onComplete(any());

    // stubbing for createQueue
    lenient()
        .when(rabbitWebClient.requestAsync(anyString(), anyString(), any()))
        .thenReturn(httpResponseFuture);
    lenient().when(httpResponseAsyncResult.failed()).thenReturn(true);

    rabbitClient
        .getQueue(request, vHost)
        .onComplete(
            handler -> {
              if (handler.succeeded()) {
                vertxTestContext.completeNow();
              } else {
                handler.failed();
              }
            });
  }

  @Test
  @DisplayName("Get Queue test 404")
  public void getQueueJsonTest40(VertxTestContext vertxTestContext) {
    JsonObject request = new JsonObject();

    rabbitClient
        .getQueue(request, vHost)
        .onComplete(
            handler -> {
              if (handler.succeeded()) {
                assertEquals(SUCCESS, handler.result().getString(TITLE));
                vertxTestContext.completeNow();
              } else {
                handler.failed();
              }
            });
  }

  @Test
  @DisplayName("create exchange test 204")
  public void createExchangesTest204(VertxTestContext vertxTestContext) {

    String url = "abc/abc/abc";
    JsonObject exchangeProperties = new JsonObject();
    exchangeProperties.put(TYPE, EXCHANGE_TYPE).put(AUTO_DELETE, false).put(DURABLE, true);

    when(rabbitWebClient.requestAsync(anyString(), anyString(), any()))
        .thenReturn(httpResponseFuture);
    when(httpResponseAsyncResult.succeeded()).thenReturn(true);
    when(httpResponseAsyncResult.result()).thenReturn(bufferHttpResponse);

    when(bufferHttpResponse.statusCode()).thenReturn(204);

    doAnswer(
            new Answer<AsyncResult<HttpResponse<Buffer>>>() {
              @Override
              public AsyncResult<HttpResponse<Buffer>> answer(InvocationOnMock arg0)
                  throws Throwable {
                ((Handler<AsyncResult<HttpResponse<Buffer>>>) arg0.getArgument(0))
                    .handle(httpResponseAsyncResult);
                return null;
              }
            })
        .when(httpResponseFuture)
        .onComplete(any());

    rabbitClient
        .createExchange(exchangeName, vHost)
        .onComplete(
            handler -> {
              if (handler.succeeded()) {
                assertEquals("Exchange already exists", handler.result().getString("detail"));
                assertEquals("failure", handler.result().getString("title"));
                vertxTestContext.completeNow();
              } else {
                handler.failed();
              }
            });
  }

  @Test
  @DisplayName("Test populateExchangeCache for succcess")
  public void populateExchangeCacheTest(VertxTestContext vertxTestContext) {
    Cache<String, Boolean> exchangeListCache =
        CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterAccess(CACHE_TIMEOUT_AMOUNT, TimeUnit.MINUTES)
            .build();

    when(rabbitWebClient.requestAsync(anyString(), anyString())).thenReturn(httpResponseFuture);
    when(httpResponseAsyncResult.succeeded()).thenReturn(true);

    when(httpResponseAsyncResult.result()).thenReturn(bufferHttpResponse);

    JsonObject jsonObject = new JsonObject().put("name", "dummy name");
    JsonArray jsonArray = new JsonArray().add(jsonObject);

    when(bufferHttpResponse.bodyAsJsonArray()).thenReturn(jsonArray);

    doAnswer(
            new Answer<AsyncResult<HttpResponse<Buffer>>>() {
              @Override
              public AsyncResult<HttpResponse<Buffer>> answer(InvocationOnMock arg0)
                  throws Throwable {
                ((Handler<AsyncResult<HttpResponse<Buffer>>>) arg0.getArgument(0))
                    .handle(httpResponseAsyncResult);
                return null;
              }
            })
        .when(httpResponseFuture)
        .onComplete(any());

    rabbitClient
        .populateExchangeCache(vHost, exchangeListCache)
        .onComplete(
            handler -> {
              if (handler.succeeded()) {
                vertxTestContext.completeNow();

              } else {
                vertxTestContext.failNow("error");
              }
            });
  }

  @Test
  @DisplayName("Test populateExchangeCache for Failure")
  public void populateExchangeCacheTest4Failure(VertxTestContext vertxTestContext) {
    Cache<String, Boolean> exchangeListCache =
        CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterAccess(CACHE_TIMEOUT_AMOUNT, TimeUnit.MINUTES)
            .build();

    when(rabbitWebClient.requestAsync(anyString(), anyString())).thenReturn(httpResponseFuture);
    when(httpResponseAsyncResult.succeeded()).thenReturn(false);

    doAnswer(
            new Answer<AsyncResult<HttpResponse<Buffer>>>() {
              @Override
              public AsyncResult<HttpResponse<Buffer>> answer(InvocationOnMock arg0)
                  throws Throwable {
                ((Handler<AsyncResult<HttpResponse<Buffer>>>) arg0.getArgument(0))
                    .handle(httpResponseAsyncResult);
                return null;
              }
            })
        .when(httpResponseFuture)
        .onComplete(any());

    rabbitClient
        .populateExchangeCache(vHost, exchangeListCache)
        .onComplete(
            handler -> {
              if (handler.failed()) {
                vertxTestContext.completeNow();

              } else {
                vertxTestContext.failNow("error");
              }
            });
  }
}
