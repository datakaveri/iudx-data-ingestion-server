package iudx.data.ingestion.server.authenticator.authorization;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

@ExtendWith(VertxExtension.class)
class AuthorizationContextFactoryTest {
  @Test
  public void test(VertxTestContext vertxTestContext) {
    AuthorizationStrategy delegateAuthStrategy =
        AuthorizationContextFactory.create(IUDXRole.PROVIDER);
    assertTrue(delegateAuthStrategy instanceof ProviderAuthStrategy);
    AuthorizationStrategy providerAuthStrategy =
        AuthorizationContextFactory.create(IUDXRole.DELEGATE);
    assertTrue(providerAuthStrategy instanceof DelegateAuthStrategy);
    AuthorizationStrategy adminAuthStrategy = AuthorizationContextFactory.create(IUDXRole.ADMIN);
    assertTrue(adminAuthStrategy instanceof AdminAuthStrategy);
    vertxTestContext.completeNow();
  }
  
  
  @Test
  public void testFailure(VertxTestContext testContext) {
    assertThrows(IllegalArgumentException.class,  ()->AuthorizationContextFactory.create(null));
    testContext.completeNow();
  }
}
