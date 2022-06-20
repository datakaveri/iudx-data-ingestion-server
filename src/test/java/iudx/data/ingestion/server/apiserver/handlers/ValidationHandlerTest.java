package iudx.data.ingestion.server.apiserver.handlers;

import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import iudx.data.ingestion.server.apiserver.util.Constants;
import iudx.data.ingestion.server.apiserver.util.RequestType;
import iudx.data.ingestion.server.apiserver.validation.Validator;
import iudx.data.ingestion.server.apiserver.validation.ValidatorsHandlersFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith({VertxExtension.class, MockitoExtension.class})
public class ValidationHandlerTest {
    ValidationHandler validationHandler,validationHandler2;
    Vertx vertx;
    RequestType requestTypeMock;
    MultiMap parameters;
    @Mock
    Validator validator;
    @Mock
    ValidatorsHandlersFactory validatorsHandlersFactory;

    @BeforeEach
    public void setUp(Vertx vertx){
        validationHandler =new ValidationHandler(vertx,RequestType.ENTITY);
    }

    /*@Test
    @DisplayName("Validation Successful")
    public void testHandle(VertxTestContext vertxTestContext){
        MultiMap map = MultiMap.caseInsensitiveMultiMap();
        RoutingContext routingContextMock= mock(RoutingContext.class);
        HttpServerRequest httpServerRequestMock = mock(HttpServerRequest.class);
        JsonObject jsonObjectMock= new JsonObject();
        jsonObjectMock.put("Dummy key", "Dummy value");

        when(routingContextMock.request()).thenReturn(httpServerRequestMock);
        when(httpServerRequestMock.params()).thenReturn(map);

        parameters = MultiMap.caseInsensitiveMultiMap();
        parameters.set(Constants.ID, "asdasd/asdasd/adasd/adasd/adasd");

        when(routingContextMock.getBodyAsJson()).thenReturn(jsonObjectMock);

        assertFalse(validator.isValid());

        validationHandler.handle(routingContextMock);
        verify(routingContextMock,times(1)).next();
        vertxTestContext.completeNow();
    }*/

    @Test
    @DisplayName("Validation Failure")
    public void testHandle2(VertxTestContext vertxTestContext){
        MultiMap multiMapMock= mock(MultiMap.class);
        RoutingContext routingContextMock= mock(RoutingContext.class);
        HttpServerRequest httpServerRequestMock = mock(HttpServerRequest.class);
        JsonObject jsonObjectMock= new JsonObject();
        jsonObjectMock.put("Dummy key", "Dummy value");

        when(routingContextMock.request()).thenReturn(httpServerRequestMock);

       /* parameters = MultiMap.caseInsensitiveMultiMap();
        parameters.set(Constants.ID, "asdasd/asdasd");*/

        validationHandler2 = new ValidationHandler(vertx,RequestType.ENTITY);

        assertThrows(Exception.class, ()-> validationHandler.handle(routingContextMock));
        verify(routingContextMock,times(0)).next();
        vertxTestContext.completeNow();

    }
}
