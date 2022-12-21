package iudx.data.ingestion.server.authenticator.authorization;

import iudx.data.ingestion.server.common.Api;

public class AuthorizationContextFactory {

  public static AuthorizationStrategy create(IUDXRole role,Api apis) {
    
    if(role==null) {
      throw new IllegalArgumentException(role + "role is not defined in IUDX");
    }
    
    switch (role) {
      case PROVIDER: {
        return new ProviderAuthStrategy(apis);
      }
      case DELEGATE: {
        return new DelegateAuthStrategy(apis);
      }
      case ADMIN: {
        return new AdminAuthStrategy(apis);
      }
      default:
        throw new IllegalArgumentException(role + "role is not defined in IUDX");
    }
  }

}
