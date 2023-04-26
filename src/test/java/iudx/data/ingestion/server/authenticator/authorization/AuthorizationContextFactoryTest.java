package iudx.data.ingestion.server.authenticator.authorization;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import iudx.data.ingestion.server.common.Api;

@ExtendWith(VertxExtension.class)
class AuthorizationContextFactoryTest {
  @Test
  public void test(VertxTestContext vertxTestContext) {
    Api apis=Api.getInstance("abc");
    AuthorizationStrategy delegateAuthStrategy =
        AuthorizationContextFactory.create(IudxRole.PROVIDER,apis);
    assertTrue(delegateAuthStrategy instanceof ProviderAuthStrategy);
    AuthorizationStrategy providerAuthStrategy =
        AuthorizationContextFactory.create(IudxRole.DELEGATE,apis);
    assertTrue(providerAuthStrategy instanceof DelegateAuthStrategy);
    AuthorizationStrategy adminAuthStrategy = AuthorizationContextFactory.create(IudxRole.ADMIN,apis);
    assertTrue(adminAuthStrategy instanceof AdminAuthStrategy);
    vertxTestContext.completeNow();
  }
  
  
  @Test
  public void testFailure(VertxTestContext testContext) {
    Api apis=Api.getInstance("abc");
    assertThrows(IllegalArgumentException.class,  ()->AuthorizationContextFactory.create(null,apis));
    testContext.completeNow();
  }
}
