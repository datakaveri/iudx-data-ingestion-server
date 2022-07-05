package iudx.data.ingestion.server.apiserver.util;

import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
@ExtendWith({VertxExtension.class, MockitoExtension.class})
class UtilTest {
    @Test
    public void errorResponseTest(VertxTestContext vertxTestContext){
        assertNotNull(Util.errorResponse(HttpStatusCode.BAD_REQUEST));
        vertxTestContext.completeNow();
    }

}