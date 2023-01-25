package iudx.data.ingestion.server.authenticator.authorization;

import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import iudx.data.ingestion.server.common.Api;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeAll;
@ExtendWith({VertxExtension.class, MockitoExtension.class})
class AuthorizationRequestTest {
  
  static Api apis;
  
  @BeforeAll
  static void init(Vertx vertx, VertxTestContext testContext) {
    apis=Api.getInstance("/ngsi-ld/v1");
    testContext.completeNow();
  }
  
    @Test
    @DisplayName("AuthRequest should not Null")
    public void authRequestShouldNotNull(VertxTestContext vertxTestContext) {
        AuthorizationRequest authR1= new AuthorizationRequest(Method.GET, apis.getEntitiesEndpoint());
        assertNotNull(authR1);
        vertxTestContext.completeNow();
    }

    @Test
    @DisplayName("authRequest should have same hashcode")
    public void authRequestShouldhaveSameHash(VertxTestContext vertxTestContext) {
        AuthorizationRequest authR1= new AuthorizationRequest(Method.GET, apis.getEntitiesEndpoint());
        AuthorizationRequest authR2= new AuthorizationRequest(Method.GET, apis.getEntitiesEndpoint());
        assertEquals(authR1.hashCode(), authR2.hashCode());
        vertxTestContext.completeNow();
    }

    @Test
    @DisplayName("authRequest Equals")
    public void authRequestEquals(VertxTestContext vertxTestContext) {
        AuthorizationRequest authR1= new AuthorizationRequest(Method.GET, apis.getEntitiesEndpoint());
        Object obj= new Object();
        assertFalse(authR1.equals(obj));
        vertxTestContext.completeNow();
    }

    @Test
    @DisplayName("authRequest Equals null")
    public void authRequestEquals2(VertxTestContext vertxTestContext) {
        AuthorizationRequest authR1= new AuthorizationRequest(Method.GET, apis.getEntitiesEndpoint());
        Object obj= new Object();
        assertFalse(authR1.equals(null));
        vertxTestContext.completeNow();
    }

    @Test
    @DisplayName("Get Method")
    public void getMethodTest(VertxTestContext vertxTestContext) {
        AuthorizationRequest authR1= new AuthorizationRequest(Method.GET, apis.getEntitiesEndpoint());
        Object obj= new Object();
        assertNotNull(authR1.getMethod());
        vertxTestContext.completeNow();
    }
    @Test
    @DisplayName("Get Api")
    public void getApiTest(VertxTestContext vertxTestContext) {
        AuthorizationRequest authR1= new AuthorizationRequest(Method.GET, apis.getEntitiesEndpoint());
        Object obj= new Object();
        assertNotNull(authR1.getApi());
        vertxTestContext.completeNow();
    }

}