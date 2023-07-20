package iudx.data.ingestion.server.databroker.util;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith({VertxExtension.class, MockitoExtension.class})
class UtilTest {
  @Test
  public void test1(VertxTestContext vertxTestContext) {
    JsonObject arr = new JsonObject().put("id", "dummy_id")
        .put("resourceGroup", "dummy_group")
        .put("resourceServer", "dummy_Server")
        .put("provider", "dummy_provider");
    assertNotNull(Util.getResourceGroupName(arr));
    assertNotNull(Util.getResourceServerName(arr));
    assertNotNull(Util.getProviderName(arr));
    vertxTestContext.completeNow();
  }

}