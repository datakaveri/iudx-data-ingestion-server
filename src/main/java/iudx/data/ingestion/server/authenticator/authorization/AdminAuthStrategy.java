package iudx.data.ingestion.server.authenticator.authorization;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import iudx.data.ingestion.server.authenticator.model.JwtData;

public class AdminAuthStrategy implements AuthorizationStrategy {
  private static final Logger LOGGER = LogManager.getLogger(AdminAuthStrategy.class);

  static Map<String, List<AuthorizationRequest>> AdminAuthorizationRules = new HashMap<>();
  static {
    // Admin allowed to access all endpoints
  }

  @Override
  public boolean isAuthorized(AuthorizationRequest authRequest, JwtData jwtData) {
    return true;
  }

}
