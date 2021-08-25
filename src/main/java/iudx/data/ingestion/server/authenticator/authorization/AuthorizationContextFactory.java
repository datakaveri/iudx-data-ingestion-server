package iudx.data.ingestion.server.authenticator.authorization;

import iudx.data.ingestion.server.authenticator.Constants;

public class AuthorizationContextFactory {

  public static AuthorizationStrategy create(String role) {
    switch (role) {
      case Constants.JSON_CONSUMER: {
        return new ConsumerAuthStrategy();
      }
      case Constants.JSON_PROVIDER: {
        return new ProviderAuthStrategy();
      }
      case Constants.JSON_DELEGATE: {
        return new DelegateAuthStrategy();
      }
      default:
        throw new IllegalArgumentException(role + "role is not defined in IUDX");
    }
  }

}