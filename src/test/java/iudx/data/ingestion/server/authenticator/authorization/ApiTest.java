package iudx.data.ingestion.server.authenticator.authorization;

import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static iudx.data.ingestion.server.authenticator.authorization.Api.ENTITIES;
import static org.junit.jupiter.api.Assertions.*;
@ExtendWith(VertxExtension.class)
class ApiTest {
    @ParameterizedTest
    @EnumSource
    public void test(Api api, VertxTestContext testContext){
        assertNotNull(api);
        testContext.completeNow();
    }

    @Test
    public void staticFromEndPointTest(VertxTestContext vertxTestContext){
        Api.fromEndpoint("/ngsi-ld/v1/entities");
        assertEquals("/ngsi-ld/v1/entities","/ngsi-ld/v1/entities");
        vertxTestContext.completeNow();
    }
}