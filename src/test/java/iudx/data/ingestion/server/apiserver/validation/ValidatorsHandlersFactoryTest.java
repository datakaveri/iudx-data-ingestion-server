package iudx.data.ingestion.server.apiserver.validation;

import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import iudx.data.ingestion.server.apiserver.util.RequestType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@ExtendWith({VertxExtension.class, MockitoExtension.class})
class ValidatorsHandlersFactoryTest {
    ValidatorsHandlersFactory validatorsHandlersFactory;
    Map<String, String> jsonSchemaMap = new HashMap<>();
    @Mock
    Vertx vertx;

    @BeforeEach
    public void setUp(VertxTestContext vertxTestContext){
        validatorsHandlersFactory = new ValidatorsHandlersFactory();
        vertxTestContext.completeNow();
    }

    @Test
    @DisplayName("getEntityRequestValidations Test")
    public void getEntityRequestValidationsTest(VertxTestContext vertxTestContext){
        MultiMap params = MultiMap.caseInsensitiveMultiMap();
        MultiMap headers = MultiMap.caseInsensitiveMultiMap();
        JsonObject jsonObject = mock(JsonObject.class);

        var validator =validatorsHandlersFactory.build(RequestType.ENTITY,jsonObject,params);

        assertEquals(1,validator.size());
        vertxTestContext.completeNow();
    }

    @Test
    @DisplayName("getIngestRequestValidations Test")
    public void getIngestRequestValidationsTest(VertxTestContext vertxTestContext){
        MultiMap params = MultiMap.caseInsensitiveMultiMap();
        MultiMap headers = MultiMap.caseInsensitiveMultiMap();
        JsonObject jsonObject = mock(JsonObject.class);

        var validator =validatorsHandlersFactory.build(RequestType.INGEST,jsonObject,params);

        assertEquals(1,validator.size());
        vertxTestContext.completeNow();
    }

    @Test
    @DisplayName("getIngestDeleteRequestValidations Test")
    public void getIngestDeleteRequestValidationsTest(VertxTestContext vertxTestContext){
        MultiMap params = MultiMap.caseInsensitiveMultiMap();
        MultiMap headers = MultiMap.caseInsensitiveMultiMap();
        JsonObject jsonObject = mock(JsonObject.class);

        var validator =validatorsHandlersFactory.build(RequestType.INGEST_DELETE,jsonObject,params);

        assertEquals(1,validator.size());
        vertxTestContext.completeNow();
    }

}