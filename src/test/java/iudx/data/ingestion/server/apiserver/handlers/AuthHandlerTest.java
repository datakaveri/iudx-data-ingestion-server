package iudx.data.ingestion.server.apiserver.handlers;

import io.vertx.core.*;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import iudx.data.ingestion.server.authenticator.AuthenticationService;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith({VertxExtension.class, MockitoExtension.class})
public class AuthHandlerTest {
    private static final String AUTH_SERVICE_ADDRESS = "iudx.data.ingestion.authentication.service";

    AuthHandler authHandler;
    @Mock
    RoutingContext routingContextMock;
    @Mock
    HttpServerResponse httpServerResponse;
    @Mock
    HttpServerRequest httpServerRequest;
    @Mock
    HttpMethod httpMethod;
    @Mock
    AsyncResult<JsonObject> asyncResult;

    @BeforeEach
    public void setup(VertxTestContext vertxTestContext, Vertx vertx){
        authHandler= AuthHandler.create(vertx);
        //lenient().when(httpServerRequest.method()).thenReturn(httpMethod);
        //lenient().when(httpMethod.toString()).thenReturn("POST");
        //lenient().when(routingContextMock.request()).thenReturn(httpServerRequest);
        vertxTestContext.completeNow();
    }

    @Test
    @DisplayName("Handle Success Test")
    public void testHandleSuccess(VertxTestContext vertxTestContext){
        AuthenticationService authenticationServiceMock= mock(AuthenticationService.class);
        JsonObject jsonObject =  new JsonObject().put("Dummy Key", "Dummy Value")
                .put("IID", "Dummy IID value")
                .put("USER_ID", "Dummy USER_ID");

        //HttpMethod httpMethodMock= mock(HttpMethod.class);
        //MultiMap mapss = MultiMap.caseInsensitiveMultiMap();
        MultiMap mapss= mock(MultiMap.class);

        when(routingContextMock.request()).thenReturn(httpServerRequest);
        when(routingContextMock.getBodyAsJson()).thenReturn(jsonObject);

        //when(httpServerRequest.headers().get(HEADER_TOKEN)).thenReturn(/*anyString()*/"asd.asd.sad.sad");

        when(httpServerRequest.headers()).thenReturn(mapss);
        when(mapss.get(anyString())).thenReturn("asd.asd.sad.sad");

        when(httpServerRequest.path()).thenReturn(NGSILD_ENTITIES_URL);

        when(routingContextMock.request()).thenReturn(httpServerRequest);
        when(httpServerRequest.method()).thenReturn(httpMethod);
        when(httpMethod.toString()).thenReturn(anyString());

        when(httpServerRequest.getParam(ID)).thenReturn(NGSILD_QUERY_ID);

        when(routingContextMock.getBodyAsJson()).thenReturn(jsonObject);

        //when(jsonObject.getString()).thenReturn(anyString());

        JsonObject authInfo= new JsonObject().put(API_ENDPOINT,NGSILD_ENTITIES_URL)
                .put(HEADER_TOKEN,"asd.asd.sad.sad")
                .put(API_METHOD,"POST")
                .put(ID,NGSILD_QUERY_ID);
        authInfo.put(IID, "Some IID value").put(USER_ID,"USER_ID");
        // AsyncResult<JsonObject> asyncResult = mock(AsyncResult.class);

        lenient().when(asyncResult.succeeded()).thenReturn(true);
        //when(asyncResult.succeeded()).thenReturn(true);
        /*when(asyncResult.result()).thenReturn(authInfo.put(IID,"iid")
                .put(USER_ID,"USER_ID"))
                .thenReturn((JsonObject) routingContextMock.data()
                        .put("authInfo",jsonObject));*/

        lenient().when(asyncResult.result()).thenReturn(authInfo);

        lenient().doAnswer(new Answer<AsyncResult<JsonObject>>() {
            @SuppressWarnings("unchecked")
            @Override
            public AsyncResult<JsonObject> answer(InvocationOnMock arg0) throws Throwable {
                ((Handler<AsyncResult<JsonObject>>) arg0.getArgument(2 )).handle(asyncResult);
                return null;
            }
        }).when(authenticationServiceMock).tokenIntrospect(any(),any(),any());

        authHandler.handle(routingContextMock);
        //verify(routingContextMock, times(1)).next();
        vertxTestContext.completeNow();
    }

    @Test
    @DisplayName("Handle Success Test")
    public void testHandleSuccess2(VertxTestContext vertxTestContext){
        AuthenticationService authenticationServiceMock= mock(AuthenticationService.class);
        JsonObject jsonObject =  new JsonObject().put("Dummy Key", "Dummy Value")
                .put("IID", "Dummy IID value")
                .put("USER_ID", "Dummy USER_ID");
        //AuthenticationService authenticationServiceMock= mock(AuthenticationService.class);
        //HttpMethod httpMethodMock= mock(HttpMethod.class);
        //MultiMap mapss = MultiMap.caseInsensitiveMultiMap();
        MultiMap mapss= mock(MultiMap.class);

        when(routingContextMock.request()).thenReturn(httpServerRequest);
        when(routingContextMock.getBodyAsJson()).thenReturn(jsonObject);

        //when(httpServerRequest.headers().get(HEADER_TOKEN)).thenReturn(/*anyString()*/"asd.asd.sad.sad");

        when(httpServerRequest.headers()).thenReturn(mapss);
        when(mapss.get(anyString())).thenReturn("asd.asd.sad.sad");

        when(httpServerRequest.path()).thenReturn(NGSILD_INGESTION_URL);

        when(routingContextMock.request()).thenReturn(httpServerRequest);
        when(httpServerRequest.method()).thenReturn(httpMethod);
        when(httpMethod.toString()).thenReturn(anyString());

        when(httpServerRequest.getParam(ID)).thenReturn(NGSILD_QUERY_ID);

        when(routingContextMock.getBodyAsJson()).thenReturn(jsonObject);

        //when(jsonObject.getString()).thenReturn(anyString());

        JsonObject authInfo= new JsonObject().put(API_ENDPOINT,NGSILD_ENTITIES_URL)
                .put(HEADER_TOKEN,"asd.asd.sad.sad")
                .put(API_METHOD,"POST")
                .put(ID,NGSILD_QUERY_ID);
        authInfo.put(IID, "Some IID value").put(USER_ID,"USER_ID");
        AsyncResult<JsonObject> asyncResult = mock(AsyncResult.class);

        lenient().when(asyncResult.succeeded()).thenReturn(true);
        //when(asyncResult.succeeded()).thenReturn(true);
        /*when(asyncResult.result()).thenReturn(authInfo.put(IID,"iid")
                .put(USER_ID,"USER_ID"))
                .thenReturn((JsonObject) routingContextMock.data()
                        .put("authInfo",jsonObject));*/

        lenient().when(asyncResult.result()).thenReturn(authInfo);

        lenient().doAnswer(new Answer<AsyncResult<JsonObject>>() {
            @SuppressWarnings("unchecked")
            @Override
            public AsyncResult<JsonObject> answer(InvocationOnMock arg0) throws Throwable {
                ((Handler<AsyncResult<JsonObject>>) arg0.getArgument(2 )).handle(asyncResult);
                return null;
            }
        }).when(authenticationServiceMock).tokenIntrospect(any(),any(),any());

        authHandler.handle(routingContextMock);
        //verify(routingContextMock, times(1)).next();
        vertxTestContext.completeNow();
    }

    @Test
    @DisplayName("Failed Success Test")
    public void failedHandleSuccess(VertxTestContext vertxTestContext){
        AuthenticationService authenticationServiceMock= mock(AuthenticationService.class);
        JsonObject jsonObject =  new JsonObject().put("Dummy Key", "Dummy Value")
                .put("IID", "Dummy IID value")
                .put("USER_ID", "Dummy USER_ID");

        //HttpMethod httpMethodMock= mock(HttpMethod.class);
        //MultiMap mapss = MultiMap.caseInsensitiveMultiMap();
        MultiMap mapss= mock(MultiMap.class);

        when(routingContextMock.request()).thenReturn(httpServerRequest);
        when(routingContextMock.getBodyAsJson()).thenReturn(jsonObject);

        //when(httpServerRequest.headers().get(HEADER_TOKEN)).thenReturn(/*anyString()*/"asd.asd.sad.sad");

        when(httpServerRequest.headers()).thenReturn(mapss);
        when(mapss.get(anyString())).thenReturn("asd.asd.sad.sad");

        when(httpServerRequest.path()).thenReturn(ENTITIES_URL_REGEX);

        when(routingContextMock.request()).thenReturn(httpServerRequest);
        when(httpServerRequest.method()).thenReturn(httpMethod);
        when(httpMethod.toString()).thenReturn(anyString());

        when(httpServerRequest.getParam(ID)).thenReturn(NGSILD_QUERY_ID);

        when(routingContextMock.getBodyAsJson()).thenReturn(jsonObject);

        //when(jsonObject.getString()).thenReturn(anyString());

        JsonObject authInfo= new JsonObject().put(API_ENDPOINT,NGSILD_ENTITIES_URL)
                .put(HEADER_TOKEN,"asd.asd.sad.sad")
                .put(API_METHOD,"POST")
                .put(ID,NGSILD_QUERY_ID);

        AsyncResult<JsonObject> asyncResult = mock(AsyncResult.class);

        lenient().when(asyncResult.succeeded()).thenReturn(false);
        //when(asyncResult.result()).thenReturn(authInfo.put(IID,"iid").put(USER_ID,"USER_ID")).thenReturn((JsonObject) routingContextMock.data().put("authInfo",jsonObject));

        lenient().when(asyncResult.cause()).thenReturn(new Throwable("fail"));

        lenient().doAnswer(new Answer<AsyncResult<JsonObject>>() {
            @SuppressWarnings("unchecked")
            @Override
            public AsyncResult<JsonObject> answer(InvocationOnMock arg0) throws Throwable {
                ((Handler<AsyncResult<JsonObject>>) arg0.getArgument(2 )).handle(asyncResult);
                return null;
            }
        }).when(authenticationServiceMock).tokenIntrospect(any(),any(),any());

        authHandler.handle(routingContextMock);
        //verify(routingContextMock, times(1)).next();
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


}
