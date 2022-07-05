package iudx.data.ingestion.server.metering.util;

import io.vertx.core.json.JsonArray;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;
@ExtendWith(VertxExtension.class)
class ResponseBuilderTest {
    ResponseBuilder responseBuilder;
    @Test
    public void test(VertxTestContext vertxTestContext){
        responseBuilder=new ResponseBuilder("200");
        JsonArray jsonArray= new JsonArray().add(123);

        assertNotNull(responseBuilder.setCount(200));
        assertNotNull(responseBuilder.setData(jsonArray));
        vertxTestContext.completeNow();
    }
}