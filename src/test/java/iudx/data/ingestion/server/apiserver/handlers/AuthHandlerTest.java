package iudx.data.ingestion.server.apiserver.handlers;

import io.vertx.core.*;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import iudx.data.ingestion.server.apiserver.util.Configuration;
import iudx.data.ingestion.server.authenticator.AuthenticationService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import static iudx.data.ingestion.server.apiserver.util.Constants.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith({VertxExtension.class, MockitoExtension.class})
public class AuthHandlerTest {
    private static final String AUTH_SERVICE_ADDRESS = "iudx.data.ingestion.authentication.service";

    AuthHandler authHandler;
    JsonObject jsonObject;
    @Mock
    RoutingContext routingContextMock;
    @Mock
    HttpServerResponse httpServerResponse;
    @Mock
    HttpServerRequest httpServerRequest;

    @Mock
    HttpMethod httpMethodMock;

    @Mock
    AsyncResult<JsonObject> asyncResult;
    @Mock
    MultiMap map;
    @Mock
    Throwable throwable;
    @Mock
    Future<Void> voidFuture;

    private JsonObject jsonConfig;
    private String basePath;
    private static final Logger LOGGER = LogManager.getLogger(AuthHandlerTest.class);


    @BeforeEach
    public void setup(VertxTestContext vertxTestContext, Vertx vertx){
        authHandler = new AuthHandler();
        jsonObject = new JsonObject();
        jsonObject.put("Dummy Key", "Dummy Value");
        jsonObject.put("IID", "Dummy IID value");
        jsonObject.put("USER_ID", "Dummy USER_ID");
        jsonObject.put("EXPIRY", "Dummy EXPIRY");
        //lenient().doReturn(httpServerRequest).when(routingContextMock).request();
        //lenient().doReturn(httpServerResponse).when(routingContextMock).response();

        jsonConfig = Configuration.getConfiguration();
        if (jsonConfig != null)
        {
            basePath = jsonConfig.getString("ngsildBasePath");
        }
        if (basePath == null || basePath.isEmpty())
        {
            LOGGER.error("base path is null or empty");
        }

        lenient().when(httpServerRequest.method()).thenReturn(httpMethodMock);
        lenient().when(httpMethodMock.toString()).thenReturn("GET");
        lenient().when(routingContextMock.request()).thenReturn(httpServerRequest);
        vertxTestContext.completeNow();
    }

    @Test
    @DisplayName("Handle Success Test")
    public void testHandleSuccess(VertxTestContext vertxTestContext){
        when(routingContextMock.getBodyAsJson()).thenReturn(jsonObject);
        when(httpServerRequest.path()).thenReturn(ENTITIES_URL_REGEX);
        //doReturn(NGSILD_ENTITIES_URL).when(httpServerRequest).path();

        AuthHandler.authenticator = mock(AuthenticationService.class);

        when(httpServerRequest.headers()).thenReturn(map);
        //when(multiMapMock.get(HEADER_TOKEN)).thenReturn("asd.asd.sad.sad");

        when(map.get(anyString())).thenReturn("Dummy Token");

        //when(routingContextMock.request()).thenReturn(httpServerRequest);
        //when(httpServerRequest.method()).thenReturn(httpMethodMock);
        //when(httpMethodMock.toString()).thenReturn("POST");
        //when(httpServerRequest.getParam(ID)).thenReturn("qeret/dfasfa/zxcvvb");
        //JsonObject authinfo = new JsonObject();

        //AsyncResult<JsonObject> asyncResult = mock(AsyncResult.class);
        when(asyncResult.succeeded()).thenReturn(true);
        when(asyncResult.result()).thenReturn(jsonObject);


        doAnswer(new Answer<AsyncResult<JsonObject>>() {
            @Override
            public AsyncResult<JsonObject> answer(InvocationOnMock arg0) throws Throwable {
                ((Handler<AsyncResult<JsonObject>>) arg0.getArgument(2)).handle(asyncResult);
                return null;
            }
        }).when(AuthHandler.authenticator).tokenIntrospect(any(), any(), any());

        authHandler.handle(routingContextMock);
        // verify(routingContextMock, times(1)).next();
        verify(AuthHandler.authenticator, times(1)).tokenIntrospect(any(), any(), any());
        verify(routingContextMock, times(2)).getBodyAsJson();

        vertxTestContext.completeNow();
    }

    @Test
    @DisplayName("Failed Success Test")
    public void failedHandleSuccess(VertxTestContext vertxTestContext){
        authHandler = new AuthHandler();
        String str = ENTITIES_URL_REGEX;
        JsonObject jsonObject = new JsonObject();
        jsonObject.put("Dummy Key", "Dummy Value");

        //MultiMap multiMapMock = mock(MultiMap.class);
        //HttpMethod httpMethodMock = mock(HttpMethod.class);
        //Map map = new HashMap<String, Object>();
        //AuthenticationService authenticationServiceMock = mock(AuthenticationService.class);
        //AsyncResult<JsonObject> asyncResult = mock(AsyncResult.class);

        when(routingContextMock.getBodyAsJson()).thenReturn(jsonObject);
        when(httpServerRequest.path()).thenReturn(str);
        AuthHandler.authenticator = mock(AuthenticationService.class);

        //when(routingContextMock.request()).thenReturn(httpServerRequest);
        //when(routingContextMock.getBodyAsJson()).thenReturn(jsonObjectMock);
        //doReturn(NGSILD_ENTITIES_URL).when(httpServerRequest).path();
        when(httpServerRequest.headers()).thenReturn(map);
        //when(map.get(HEADER_TOKEN)).thenReturn("asd.asd.sad.sad");
        when(map.get(anyString())).thenReturn("Dummy token");

        //when(routingContextMock.request()).thenReturn(httpServerRequest);
        //when(httpServerRequest.method()).thenReturn(httpMethodMock);
        //when(httpMethodMock.toString()).thenReturn("POST");

        //when(httpServerRequest.getParam(ID)).thenReturn("qeret/dfasfa/zxcvvb");
        //JsonObject authinfo = new JsonObject();
        when(asyncResult.cause()).thenReturn(throwable);
        when(throwable.getMessage()).thenReturn("Dummy throwable message: Not Found");
        when(routingContextMock.response()).thenReturn(httpServerResponse);
        when(httpServerResponse.putHeader(anyString(), anyString())).thenReturn(httpServerResponse);
        when(httpServerResponse.setStatusCode(anyInt())).thenReturn(httpServerResponse);
        when(httpServerResponse.end(anyString())).thenReturn(voidFuture);
        when(asyncResult.succeeded()).thenReturn(false);
        //lenient().when(asyncResult.succeeded()).thenReturn(false);
        //lenient().when(asyncResult.cause()).thenReturn(new Throwable("fail"));

        doAnswer((Answer<AsyncResult<JsonObject>>) arg0 -> {
            ((Handler<AsyncResult<JsonObject>>) arg0.getArgument(2)).handle(asyncResult);
            return null;
        }).when(AuthHandler.authenticator).tokenIntrospect(any(), any(), any());

        authHandler.handle(routingContextMock);
        // Mockito.verify(httpServerResponse, times(0)).putHeader(anyString(), anyString());
        // Mockito.verify(httpServerResponse, times(0)).setStatusCode(anyInt());
        // Mockito.verify(httpServerResponse, times(0)).end(anyString());
        verify(AuthHandler.authenticator, times(1)).tokenIntrospect(any(), any(), any());
        verify(httpServerResponse, times(1)).setStatusCode(anyInt());
        verify(httpServerResponse, times(1)).putHeader(anyString(), anyString());
        verify(httpServerResponse, times(1)).end(anyString());
        verify(routingContextMock, times(2)).getBodyAsJson();

        vertxTestContext.completeNow();
    }

    @Test
    @DisplayName("Process AuthFailure NotFound")
    public void processAuthFailureNotFound(VertxTestContext vertxTestContext){
        RoutingContext routingContextMock= mock(RoutingContext.class);

        HttpServerResponse httpServerResponseMock = mock(HttpServerResponse.class);
        Future<Void> voidFutureMock = mock(Future.class);

        when(routingContextMock.response()).thenReturn(httpServerResponseMock);
        when(httpServerResponseMock.putHeader(anyString(),anyString())).thenReturn(httpServerResponseMock);
        when(httpServerResponseMock.setStatusCode(anyInt())).thenReturn(httpServerResponseMock);
        when(httpServerResponseMock.end(anyString())).thenReturn(voidFutureMock);
        authHandler.processAuthFailure(routingContextMock,"Not Found");

        verify(httpServerResponseMock, times(1)).setStatusCode(anyInt());
        verify(httpServerResponseMock, times(1)).putHeader(anyString(),anyString());
        verify(httpServerResponseMock, times(1)).end(anyString());

        vertxTestContext.completeNow();

    }
    @Test
    @DisplayName("Process AuthFailure Except Found")
    public void processAuthFailureExceptFound(VertxTestContext vertxTestContext){
        RoutingContext routingContextMock= mock(RoutingContext.class);
        HttpServerResponse httpServerResponseMock = mock(HttpServerResponse.class);
        Future<Void> voidFutureMock = mock(Future.class);

        when(routingContextMock.response()).thenReturn(httpServerResponseMock);
        when(httpServerResponseMock.putHeader(anyString(),anyString())).thenReturn(httpServerResponseMock);
        when(httpServerResponseMock.setStatusCode(anyInt())).thenReturn(httpServerResponseMock);
        when(httpServerResponseMock.end(anyString())).thenReturn(voidFutureMock);
        authHandler.processAuthFailure(routingContextMock,"");
        verify(httpServerResponseMock, times(1)).setStatusCode(anyInt());
        verify(httpServerResponseMock, times(1)).putHeader(anyString(),anyString());
        verify(httpServerResponseMock, times(1)).end(anyString());
        vertxTestContext.completeNow();
    }
    @Test
    @DisplayName("Test static method: create")
    public void testCreate(VertxTestContext vertxTestContext) {
        AuthHandler res = AuthHandler.create(Vertx.vertx());
        assertNotNull(res);
        vertxTestContext.completeNow();
    }

}
