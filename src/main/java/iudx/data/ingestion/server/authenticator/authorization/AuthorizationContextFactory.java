package iudx.data.ingestion.server.authenticator.authorization;

import static iudx.data.ingestion.server.authenticator.Constants.JSON_DELEGATE;
import static iudx.data.ingestion.server.authenticator.Constants.JSON_PROVIDER;


public class AuthorizationContextFactory {

  public static AuthorizationStrategy create(IUDXRole role) {
    switch (role) {
      case PROVIDER: {
        return new ProviderAuthStrategy();
      }
      case DELEGATE: {
        return new DelegateAuthStrategy();
      }
      case ADMIN: {
        return new AdminAuthStrategy();
      }
      default:
        throw new IllegalArgumentException(role + "role is not defined in IUDX");
    }
  }

}
