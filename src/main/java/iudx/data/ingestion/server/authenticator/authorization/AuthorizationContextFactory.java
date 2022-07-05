package iudx.data.ingestion.server.authenticator.authorization;

public class AuthorizationContextFactory {

  public static AuthorizationStrategy create(IUDXRole role) {
    
    if(role==null) {
      throw new IllegalArgumentException(role + "role is not defined in IUDX");
    }
    
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
