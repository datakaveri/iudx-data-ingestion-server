package iudx.data.ingestion.server.authenticator.authorization;

import static iudx.data.ingestion.server.authenticator.authorization.Method.*;

import iudx.data.ingestion.server.authenticator.model.JwtData;
import iudx.data.ingestion.server.common.Api;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProviderAuthStrategy implements AuthorizationStrategy {

  private static volatile ProviderAuthStrategy instance;

  Map<String, List<AuthorizationRequest>> providerAuthorizationRules = new HashMap<>();

  private ProviderAuthStrategy(Api apis) {
    buildPermissions(apis);
  }

  public static ProviderAuthStrategy getInstance(Api apis) {
    if (instance == null) {
      synchronized (ProviderAuthStrategy.class) {
        if (instance == null) {
          instance = new ProviderAuthStrategy(apis);
        }
      }
    }
    return instance;
  }

  private void buildPermissions(Api apis) {
    // api access list/rules
    List<AuthorizationRequest> apiAccessList = new ArrayList<>();
    apiAccessList.add(new AuthorizationRequest(POST, apis.getEntitiesEndpoint()));
    providerAuthorizationRules.put("api", apiAccessList);

    // ingestion access list/rules
    List<AuthorizationRequest> ingestAccessList = new ArrayList<>();
    ingestAccessList.add(new AuthorizationRequest(POST, apis.getIngestionEndpoint()));
    ingestAccessList.add(new AuthorizationRequest(DELETE, apis.getIngestionEndpoint()));
    providerAuthorizationRules.put("ingestion", ingestAccessList);
  }

  @Override
  public boolean isAuthorized(AuthorizationRequest authRequest, JwtData jwtData) {
    return true;
  }

}
