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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static iudx.data.ingestion.server.databroker.util.Constants.*;
import static iudx.data.ingestion.server.databroker.util.Constants.DURABLE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith({VertxExtension.class, MockitoExtension.class})
public class RabitClientNewTest {
    @Mock
    RabbitMQClient rabbitMQClient;
    @Mock
    RabbitWebClient rabbitWebClient;

    String exchangeName;
    String queueName;
    String vHost;
    @Mock
    Future<HttpResponse<Buffer>> httpResponseFuture;
    @Mock
    AsyncResult<HttpResponse<Buffer>> httpResponseAsyncResult;
    @Mock
    HttpResponse<Buffer> bufferHttpResponse;
    @Mock
    HttpRequest<Buffer> bufferHttpRequest;

    RabbitClient rabbitClient;
    @BeforeEach
    public void setup(VertxTestContext vertxTestContext){
        rabbitClient = new RabbitClient(rabbitMQClient,rabbitWebClient);
        exchangeName = UUID.randomUUID().toString();
        queueName = UUID.randomUUID().toString();
        vHost = "Dummy host";
        vertxTestContext.completeNow();
    }
    @Test
    @DisplayName("create exchange test 204")
    public void createExchangeTest204(VertxTestContext vertxTestContext){

        String url = "abc/abc/abc";
        JsonObject exchangeProperties = new JsonObject();
        exchangeProperties
                .put(TYPE, EXCHANGE_TYPE)
                .put(AUTO_DELETE, false)
                .put(DURABLE, true);

        when(rabbitWebClient.requestAsync(anyString(),anyString(),any())).thenReturn(httpResponseFuture);
        when(httpResponseAsyncResult.succeeded()).thenReturn(true);
        when(httpResponseAsyncResult.result()).thenReturn(bufferHttpResponse);

        when(bufferHttpResponse.statusCode()).thenReturn(204);

        doAnswer(new Answer<AsyncResult<HttpResponse<Buffer>>>() {
            @Override
            public AsyncResult<HttpResponse<Buffer>> answer(InvocationOnMock arg0) throws Throwable {
                ((Handler<AsyncResult<HttpResponse<Buffer>>>) arg0.getArgument(0)).handle(httpResponseAsyncResult);
                return null;
            }
        }).when(httpResponseFuture).onComplete(any());

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
    @DisplayName("Get Queue test 404")
    public void getQueueTest404(VertxTestContext vertxTestContext){

        String url = "abc/abc/abc";

        when(rabbitWebClient.requestAsync(anyString(),anyString())).thenReturn(httpResponseFuture);
        when(httpResponseAsyncResult.succeeded()).thenReturn(true);
        when(httpResponseAsyncResult.result()).thenReturn(bufferHttpResponse);

        when(bufferHttpResponse.statusCode()).thenReturn(404);

        doAnswer(new Answer<AsyncResult<HttpResponse<Buffer>>>() {
            @Override
            public AsyncResult<HttpResponse<Buffer>> answer(InvocationOnMock arg0) throws Throwable {
                ((Handler<AsyncResult<HttpResponse<Buffer>>>) arg0.getArgument(0)).handle(httpResponseAsyncResult);
                return null;
            }
        }).when(httpResponseFuture).onComplete(any());

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
    public void getQueueJsonTest200(VertxTestContext vertxTestContext){
        JsonObject request = new JsonObject()
                .put("queue",queueName);

        when(rabbitWebClient.requestAsync(anyString(),anyString())).thenReturn(httpResponseFuture);
        when(httpResponseAsyncResult.succeeded()).thenReturn(true);
        when(httpResponseAsyncResult.result()).thenReturn(bufferHttpResponse);

        when(bufferHttpResponse.statusCode()).thenReturn(200);

        doAnswer(new Answer<AsyncResult<HttpResponse<Buffer>>>() {
            @Override
            public AsyncResult<HttpResponse<Buffer>> answer(InvocationOnMock arg0) throws Throwable {
                ((Handler<AsyncResult<HttpResponse<Buffer>>>) arg0.getArgument(0)).handle(httpResponseAsyncResult);
                return null;
            }
        }).when(httpResponseFuture).onComplete(any());

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
    public void getQueueJsonTest404(VertxTestContext vertxTestContext){
        JsonObject request = new JsonObject()
                .put("queue",queueName);

        when(rabbitWebClient.requestAsync(anyString(),anyString())).thenReturn(httpResponseFuture);
        when(httpResponseAsyncResult.succeeded()).thenReturn(true);
        when(httpResponseAsyncResult.result()).thenReturn(bufferHttpResponse);

        when(bufferHttpResponse.statusCode()).thenReturn(404);

        doAnswer(new Answer<AsyncResult<HttpResponse<Buffer>>>() {
            @Override
            public AsyncResult<HttpResponse<Buffer>> answer(InvocationOnMock arg0) throws Throwable {
                ((Handler<AsyncResult<HttpResponse<Buffer>>>) arg0.getArgument(0)).handle(httpResponseAsyncResult);
                return null;
            }
        }).when(httpResponseFuture).onComplete(any());

        // stubbing for createQueue
       lenient().when(rabbitWebClient.requestAsync(anyString(),anyString(),any())).thenReturn(httpResponseFuture);
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
    public void getQueueJsonTest40(VertxTestContext vertxTestContext){
        JsonObject request = new JsonObject();

        rabbitClient
                .getQueue(request, vHost)
                .onComplete(
                        handler -> {
                            if (handler.succeeded()) {
                                assertEquals(SUCCESS,handler.result().getString(TITLE));
                                vertxTestContext.completeNow();
                            } else {
                                handler.failed();
                            }});
    }
    @Test
    @DisplayName("Test populateExchangeCache for succcess")
   public void populateExchangeCacheTest(VertxTestContext vertxTestContext){
          Cache<String, Boolean> exchangeListCache = CacheBuilder.newBuilder().maximumSize(1000)
                .expireAfterAccess(CACHE_TIMEOUT_AMOUNT, TimeUnit.MINUTES).build();

        when(rabbitWebClient.requestAsync(anyString(),anyString())).thenReturn(httpResponseFuture);
        when(httpResponseAsyncResult.succeeded()).thenReturn(true);


       when(httpResponseAsyncResult.result()).thenReturn(bufferHttpResponse);

        JsonObject jsonObject = new JsonObject()
                .put("name","dummy name");
        JsonArray jsonArray = new JsonArray()
                .add(jsonObject);

      when(bufferHttpResponse.bodyAsJsonArray()).thenReturn(jsonArray);





        doAnswer(new Answer<AsyncResult<HttpResponse<Buffer>>>() {
            @Override
            public AsyncResult<HttpResponse<Buffer>> answer(InvocationOnMock arg0) throws Throwable {
                ((Handler<AsyncResult<HttpResponse<Buffer>>>) arg0.getArgument(0)).handle(httpResponseAsyncResult);
                return null;
            }
        }).when(httpResponseFuture).onComplete(any());


        rabbitClient.populateExchangeCache(vHost,exchangeListCache)
                .onComplete(
                        handler -> {
                            if (handler.succeeded()) {
                               vertxTestContext.completeNow();

                            } else {
                               vertxTestContext.failNow("error");
                            }});
    }
    @Test
    @DisplayName("Test populateExchangeCache for Failure")
    public void populateExchangeCacheTest4Failure(VertxTestContext vertxTestContext){
        Cache<String, Boolean> exchangeListCache = CacheBuilder.newBuilder().maximumSize(1000)
                .expireAfterAccess(CACHE_TIMEOUT_AMOUNT, TimeUnit.MINUTES).build();

        when(rabbitWebClient.requestAsync(anyString(),anyString())).thenReturn(httpResponseFuture);
        when(httpResponseAsyncResult.succeeded()).thenReturn(false);


        doAnswer(new Answer<AsyncResult<HttpResponse<Buffer>>>() {
            @Override
            public AsyncResult<HttpResponse<Buffer>> answer(InvocationOnMock arg0) throws Throwable {
                ((Handler<AsyncResult<HttpResponse<Buffer>>>) arg0.getArgument(0)).handle(httpResponseAsyncResult);
                return null;
            }
        }).when(httpResponseFuture).onComplete(any());


        rabbitClient.populateExchangeCache(vHost,exchangeListCache)
                .onComplete(
                        handler -> {
                            if (handler.failed()) {
                                 vertxTestContext.completeNow();

                            } else {
                                 vertxTestContext.failNow("error");
                            }});
    }
}
