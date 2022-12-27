package iudx.data.ingestion.server.authenticator.authorization;

import iudx.data.ingestion.server.common.Api;

public class AuthorizationContextFactory {

  public static AuthorizationStrategy create(IUDXRole role,Api apis) {
    
    if(role==null) {
      throw new IllegalArgumentException(role + "role is not defined in IUDX");
    }
    
    switch (role) {
      case PROVIDER: {
        return ProviderAuthStrategy.getInstance(apis);
      }
      case DELEGATE: {
        return DelegateAuthStrategy.getInstance(apis);
      }
      case ADMIN: {
        return AdminAuthStrategy.getInstance(apis);
      }
      default:
        throw new IllegalArgumentException(role + "role is not defined in IUDX");
    }
  }

}
