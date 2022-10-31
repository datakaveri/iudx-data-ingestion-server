package iudx.data.ingestion.server.databroker;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rabbitmq.RabbitMQClient;
import io.vertx.rabbitmq.RabbitMQOptions;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import java.sql.SQLOutput;
import java.util.UUID;
import java.util.stream.Stream;

import static iudx.data.ingestion.server.databroker.util.Constants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith({VertxExtension.class, MockitoExtension.class})
class RabbitClientTest {
    @Mock
    RabbitClient rabbitClient;
    String userID;
    String password;
    @Mock
    RabbitMQOptions rabbitConfigs;
    @Mock
    Throwable throwable;
    Vertx vertxObj;
    @Mock
    RabbitWebClient webClient;

    @Mock
    JsonObject configs;
    @Mock
    RabbitMQClient rabbitMQClient;
    @Mock
    Future<HttpResponse<Buffer>> httpResponseFuture;
    @Mock
    Future<RowSet<Row>> rowSetFuture;
    @Mock
    AsyncResult<HttpResponse<Buffer>> httpResponseAsyncResult;
    @Mock
    HttpResponse<Buffer> bufferHttpResponse;
    @Mock
    AsyncResult<JsonObject> asyncResult;
    @Mock
    Buffer buffer;
    @Mock
    AsyncResult<RowSet<Row>> rowSetAsyncResult;
    @Mock
    Future<JsonObject> jsonObjectFuture;

    DataBrokerServiceImpl databroker;
    JsonObject request;
    JsonArray jsonArray;
    String vHost;
    String exchangeName;
    private static String queueName;
    JsonObject expected;

    @BeforeEach
    public void setUp(VertxTestContext vertxTestContext) {
        userID = "Dummy UserID";
        password = "Dummy password";
        vertxObj = Vertx.vertx();
        vHost = "IUDX_INTERNAL";
        exchangeName = UUID.randomUUID().toString();
        queueName = UUID.randomUUID().toString();
        request = new JsonObject();
        jsonArray = new JsonArray();
        jsonArray.add(0,"{\"Dummy key\" : \"Dummy value\"}");
        request.put("exchangeName", "Dummy exchangeName");
        request.put("queueName","Dummy Queue name");
        request.put("id","Dummy ID");
        request.put("vHost","Dummy vHost");
        request.put("entities",jsonArray);
        rabbitClient = new RabbitClient(rabbitMQClient, webClient);
        vertxTestContext.completeNow();
    }
    static Stream<Arguments> statusCodeValues() {
        return Stream.of(
                Arguments.of(204, "{\"Dummy UserID\":\"Dummy UserID\",\"password\":\"Dummy password\"}"),
                Arguments.of(400, "{\"failure\":\"Network Issue\"}")
        );
    }
    static Stream<Arguments> inputStatusCode()
    {
        return Stream.of(
                Arguments.of(201,"{\"type\":200,\"title\":\"topic_permissions\",\"detail\":\"topic permission set\"}"),
                Arguments.of(204,"{\"type\":200,\"title\":\"topic_permissions\",\"detail\":\"topic permission already set\"}"),
                Arguments.of(400,"{\"type\":500,\"title\":\"topic_permissions\",\"detail\":\"Error in setting Topic permissions\"}")
        );
    }

    @ParameterizedTest
    @MethodSource("inputStatusCode")
    @DisplayName("Test setTopicPermissions method : with different status code")
    public void testSetTopicPermissions(int statusCode, String expected,VertxTestContext vertxTestContext)
    {
        when(webClient.requestAsync(anyString(),anyString(),any())).thenReturn(httpResponseFuture);
        when(httpResponseAsyncResult.succeeded()).thenReturn(true);
        when(httpResponseAsyncResult.result()).thenReturn(bufferHttpResponse);
        when(bufferHttpResponse.statusCode()).thenReturn(statusCode);

        doAnswer(new Answer<AsyncResult<HttpResponse<Buffer>>>() {
            @Override
            public AsyncResult<HttpResponse<Buffer>> answer(InvocationOnMock arg0) throws Throwable {
                ((Handler<AsyncResult<HttpResponse<Buffer>>>) arg0.getArgument(0)).handle(httpResponseAsyncResult);
                return null;
            }
        }).when(httpResponseFuture).onComplete(any());
        rabbitClient.setTopicPermissions(request, "Dummy adaptorID", userID).onComplete(handler -> {
            if(handler.succeeded())
            {
                assertEquals(expected, handler.result().toString());
            }
            else
            {
                assertEquals(expected,handler.cause().getMessage());
            }
        });
        vertxTestContext.completeNow();
    }

    @Test
    @DisplayName("Test deleteExchange method : when status code is 204")
    public void testDeleteExchangeWhenSC_NO_CONTENT (VertxTestContext vertxTestContext)
    {
        when(webClient.requestAsync(anyString(),anyString())).thenReturn(httpResponseFuture);
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
        rabbitClient.deleteExchange(exchangeName,vHost).onComplete(handler -> {
            if(handler.succeeded())
            {
                assertEquals(exchangeName,handler.result().getString(EXCHANGE));
                vertxTestContext.completeNow();
            }
            else
            {
                vertxTestContext.failNow(handler.cause());
            }
        });
    }

    @Test
    @DisplayName("Test deleteExchange : Failure")
    public void testDeleteExchange(VertxTestContext vertxTestContext) {
        when(webClient.requestAsync(anyString(), anyString())).thenReturn(httpResponseFuture);
        when(httpResponseAsyncResult.succeeded()).thenReturn(false);
        when(httpResponseAsyncResult.cause()).thenReturn(throwable);
        doAnswer(new Answer<AsyncResult<HttpResponse<Buffer>>>() {
            @Override
            public AsyncResult<HttpResponse<Buffer>> answer(InvocationOnMock arg0) throws Throwable {
                ((Handler<AsyncResult<HttpResponse<Buffer>>>) arg0.getArgument(0)).handle(httpResponseAsyncResult);
                return null;
            }
        }).when(httpResponseFuture).onComplete(any());
    rabbitClient
        .deleteExchange(exchangeName, "Dummy vhost")
        .onFailure(
            handler -> {
              JsonObject jsonObject = new JsonObject(handler.getMessage());
              assertEquals(500, jsonObject.getInteger("type"));
              assertEquals("error", jsonObject.getString("title"));
              vertxTestContext.completeNow();
            });
    }

    @Test
    @DisplayName("Test deleteQueue method : Failure")
    public void testDeleteQueueFailure(VertxTestContext vertxTestContext) {
        when(webClient.requestAsync(anyString(), anyString())).thenReturn(httpResponseFuture);
        when(httpResponseAsyncResult.succeeded()).thenReturn(false);
        when(httpResponseAsyncResult.cause()).thenReturn(throwable);

        doAnswer(new Answer<AsyncResult<HttpResponse<Buffer>>>() {
            @Override
            public AsyncResult<HttpResponse<Buffer>> answer(InvocationOnMock arg0) throws Throwable {
                ((Handler<AsyncResult<HttpResponse<Buffer>>>) arg0.getArgument(0)).handle(httpResponseAsyncResult);
                return null;
            }
        }).when(httpResponseFuture).onComplete(any());

        rabbitClient
                .deleteExchange(queueName, "Dummy vhost")
                .onFailure(
                        handler -> {
                            JsonObject jsonObject = new JsonObject(handler.getMessage());
                            assertEquals(500, jsonObject.getInteger("type"));
                            assertEquals("error", jsonObject.getString("title"));
                            vertxTestContext.completeNow();
                        });
    }

    static Stream<Arguments> statusCodeInput()
    {
        return Stream.of(
                Arguments.of(204, "{\"queue\":\"Dummy Queue name\"}"),
                Arguments.of(404, "{\"type\":404,\"title\":\"failure\",\"detail\":\"Queue does not exist\"}")
        );
    }

    @ParameterizedTest
    @MethodSource("statusCodeInput")
    @DisplayName("Test deleteQueue method : with different status code")
    public void testDeleteQueue (int code, String expected, VertxTestContext vertxTestContext)
    {
        when(webClient.requestAsync(anyString(),anyString())).thenReturn(httpResponseFuture);
        when(httpResponseAsyncResult.succeeded()).thenReturn(true);
        when(httpResponseAsyncResult.result()).thenReturn(bufferHttpResponse);
        when(bufferHttpResponse.statusCode()).thenReturn(code);

        doAnswer(new Answer<AsyncResult<HttpResponse<Buffer>>>() {
            @Override
            public AsyncResult<HttpResponse<Buffer>> answer(InvocationOnMock arg0) throws Throwable {
                ((Handler<AsyncResult<HttpResponse<Buffer>>>) arg0.getArgument(0)).handle(httpResponseAsyncResult);
                return null;
            }
        }).when(httpResponseFuture).onComplete(any());
        rabbitClient.deleteQueue("Dummy Queue name",vHost).onComplete(handler -> {
            if(handler.succeeded())
            {
                assertEquals(expected,handler.result().toString());
                vertxTestContext.completeNow();
            }
            else
            {
                vertxTestContext.failNow(handler.cause());
            }
        });
    }
    @Test
    @DisplayName("Test bindQueue method : with different status code")
    public void testBindQueue ( VertxTestContext vertxTestContext)
    {
        when(webClient.requestAsync(anyString(),anyString(), any())).thenReturn(httpResponseFuture);
        when(httpResponseAsyncResult.succeeded()).thenReturn(true);
        doAnswer(new Answer<AsyncResult<HttpResponse<Buffer>>>() {
            @Override
            public AsyncResult<HttpResponse<Buffer>> answer(InvocationOnMock arg0) throws Throwable {
                ((Handler<AsyncResult<HttpResponse<Buffer>>>) arg0.getArgument(0)).handle(httpResponseAsyncResult);
                return null;
            }
        }).when(httpResponseFuture).onComplete(any());
        rabbitClient.bindQueue(request,vHost).onComplete(handler -> {
            if(handler.succeeded())
            {
                JsonObject jsonObject= new JsonObject(handler.result().toString());
                assertEquals(SUCCESS,jsonObject.getString(TYPE));
                vertxTestContext.completeNow();
            }
            else
            {
                vertxTestContext.failNow(handler.cause());
            }
        });
    }

   /* @Test
    @DisplayName("Publish Message test")
    public void publicMessageTest(VertxTestContext vertxTestContext){
        JsonObject metaData = new JsonObject().put(EXCHANGE_NAME,exchangeName)
                .put(ROUTING_KEY,"dummy key");
        JsonObject request= new JsonObject().put("some","something");
        Buffer buffer = Buffer.buffer(request.toString());
        doAnswer(Answer -> Future.succeededFuture()).when(rabbitMQClient).basicPublish(any(), any(), buffer);

        rabbitClient.publishMessage(request,metaData,buffer).onComplete(
                handler->{
                    if (handler.succeeded())
                    {
                        //assertion
                        vertxTestContext.completeNow();
                    }
                    else {
                        vertxTestContext.failNow("failed");
                    }
                }
        );
    }*/







    // doAnswer(Answer -> Future.succeededFuture()).when(rabbitMQClient).basicPublish(eq(exchangeName), eq("dummy key"), eq(buffer));
    //when(rabbitMQClient.basicPublish(anyString(),anyString(),any())).thenReturn(Future.succeededFuture());




}


