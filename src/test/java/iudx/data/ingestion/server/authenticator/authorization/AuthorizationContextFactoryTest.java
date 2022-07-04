package iudx.data.ingestion.server.authenticator.authorization;

import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static iudx.data.ingestion.server.authenticator.Constants.JSON_DELEGATE;
import static iudx.data.ingestion.server.authenticator.Constants.JSON_PROVIDER;
import static org.junit.jupiter.api.Assertions.*;
@ExtendWith(VertxExtension.class)
class AuthorizationContextFactoryTest {
@Test
    public void test(VertxTestContext vertxTestContext){
    AuthorizationContextFactory.create(JSON_PROVIDER);
    AuthorizationContextFactory.create(JSON_DELEGATE);
    vertxTestContext.completeNow();
}
}