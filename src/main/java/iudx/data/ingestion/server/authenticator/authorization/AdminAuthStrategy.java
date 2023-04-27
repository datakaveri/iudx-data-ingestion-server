package iudx.data.ingestion.server.authenticator.authorization;

import iudx.data.ingestion.server.authenticator.model.JwtData;
import iudx.data.ingestion.server.common.Api;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminAuthStrategy implements AuthorizationStrategy {
  static Map<String, List<AuthorizationRequest>> AdminAuthorizationRules = new HashMap<>();
  private static volatile AdminAuthStrategy instance;
  public static AdminAuthStrategy getInstance(Api apis) {
    if (instance == null) {
      synchronized (AdminAuthStrategy.class) {
        if (instance == null) {
          instance = new AdminAuthStrategy();
        }
      }
    }
    return instance;
  }

  @Override
  public boolean isAuthorized(AuthorizationRequest authRequest, JwtData jwtData) {
    return true;
  }

}
