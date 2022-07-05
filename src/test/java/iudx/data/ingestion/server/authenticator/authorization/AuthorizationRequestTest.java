package iudx.data.ingestion.server.authenticator.authorization;

import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;
@ExtendWith({VertxExtension.class, MockitoExtension.class})
class AuthorizationRequestTest {
    @Test
    @DisplayName("AuthRequest should not Null")
    public void authRequestShouldNotNull(VertxTestContext vertxTestContext) {
        AuthorizationRequest authR1= new AuthorizationRequest(Method.GET, Api.ENTITIES);
        assertNotNull(authR1);
        vertxTestContext.completeNow();
    }

    @Test
    @DisplayName("authRequest should have same hashcode")

    public void authRequestShouldhaveSameHash(VertxTestContext vertxTestContext) {
        AuthorizationRequest authR1= new AuthorizationRequest(Method.GET, Api.ENTITIES);
        AuthorizationRequest authR2= new AuthorizationRequest(Method.GET, Api.ENTITIES);
        assertEquals(authR1.hashCode(), authR2.hashCode());
        vertxTestContext.completeNow();
    }

    @Test
    @DisplayName("authRequest Equals")

    public void authRequestEquals(VertxTestContext vertxTestContext) {
        AuthorizationRequest authR1= new AuthorizationRequest(Method.GET, Api.ENTITIES);
        Object obj= new Object();
        assertNotNull(authR1.equals(obj));
        vertxTestContext.completeNow();
    }

    @Test
    @DisplayName("authRequest Equals null")

    public void authRequestEquals2(VertxTestContext vertxTestContext) {
        AuthorizationRequest authR1= new AuthorizationRequest(Method.GET, Api.ENTITIES);
        Object obj= new Object();
        assertNotNull(authR1.equals(null));
        vertxTestContext.completeNow();
    }

}