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
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import java.util.stream.Stream;

import static iudx.data.ingestion.server.databroker.util.Constants.EXCHANGE_NAME;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith({VertxExtension.class, MockitoExtension.class})
class RabbitClientTest {
    RabbitClient rabbitClient;
    String userID;
    String password;
    @Mock
    RabbitMQOptions rabbitConfigs;
    Vertx vertxObj;
    @Mock
    RabbitWebClient webClient;
    /*@Mock
    PostgresClient pgSQLClient;*/
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
    Buffer buffer;
    @Mock
    AsyncResult<RowSet<Row>> rowSetAsyncResult;
    @Mock
    Throwable throwable;
    //PermissionOpType type;
    JsonObject request;
    JsonArray jsonArray;
    String vHost;

    @BeforeEach
    public void setUp(VertxTestContext vertxTestContext) {
        userID = "Dummy UserID";
        password = "Dummy password";
        vertxObj = Vertx.vertx();
        vHost = "Dummy vHost";
        //when(configs.getString(anyString())).thenReturn("Dummy string");
        //when(configs.getInteger(anyString())).thenReturn(400);
        //when(rabbitConfigs.setVirtualHost(anyString())).thenReturn(rabbitConfigs);
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
        //JsonObject jsonObject = new JsonObject().put(EXCHANGE_NAME,"DummyExchange");
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

}