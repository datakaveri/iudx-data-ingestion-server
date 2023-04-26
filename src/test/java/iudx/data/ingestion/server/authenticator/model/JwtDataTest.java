package iudx.data.ingestion.server.authenticator.model;

import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
@ExtendWith({VertxExtension.class, MockitoExtension.class})
class JwtDataTest {
JwtData jwtData=new JwtData();
@Test
    public void test(VertxTestContext vertxTestContext){
    assertNull(jwtData.getAccessToken());
    assertNull(jwtData.getIss());
    assertEquals(0,jwtData.getExp());
    assertNull(jwtData.getCons());
    assertEquals(0,jwtData.getIat());
    assertNotNull(jwtData.toString());
    assertNotNull(jwtData.toJson());
    vertxTestContext.completeNow();
}
}