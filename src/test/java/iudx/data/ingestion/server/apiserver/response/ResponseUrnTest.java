package iudx.data.ingestion.server.apiserver.response;

import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(VertxExtension.class)
public class ResponseUrnTest {
    @ParameterizedTest
    @EnumSource
    public void test(ResponseUrn responseUrn, VertxTestContext vertxTestContext){
        assertNotNull(responseUrn);
        vertxTestContext.completeNow();
    }

    @Test
    @DisplayName("Test for a single enum")
    public void testEnumInternalError(VertxTestContext vertxTestContext){
        assertEquals("Token is invalid",ResponseUrn.INVALID_TOKEN.getMessage());
        vertxTestContext.completeNow();
    }
    @Test
    public void test2(VertxTestContext vertxTestContext){
        assertNotNull(ResponseUrn.fromCode("urn:dx:rs:backend"));
        vertxTestContext.completeNow();
    }

}
