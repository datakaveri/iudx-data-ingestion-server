package iudx.data.ingestion.server.apiserver.util;

import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(VertxExtension.class)
public class RequestTypeTest {
    @ParameterizedTest
    @EnumSource
    public void test(RequestType requestType, VertxTestContext testContext){
        assertNotNull(requestType);
        assertNotNull(requestType.getFilename());
        testContext.completeNow();
    }
}
