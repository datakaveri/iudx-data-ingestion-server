package iudx.data.ingestion.server.databroker.util;

import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
@ExtendWith({VertxExtension.class, MockitoExtension.class})
class UtilTest {
    @Test
    public void test1(VertxTestContext vertxTestContext){
        String [] arr= {"abcs","iudx","di","gis"};
        assertNotNull(Util.getResourceGroupName(arr));
        assertNotNull(Util.getResourceServerName(arr));
        assertNotNull(Util.getProviderName(arr));
        vertxTestContext.completeNow();
    }

}