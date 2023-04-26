package iudx.data.ingestion.server.authenticator.authorization;


import static iudx.data.ingestion.server.authenticator.authorization.Method.*;

import iudx.data.ingestion.server.authenticator.model.JwtData;
import iudx.data.ingestion.server.common.Api;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DelegateAuthStrategy implements AuthorizationStrategy {
  private static volatile DelegateAuthStrategy instance;
  Map<String, List<AuthorizationRequest>> delegateAuthorizationRules = new HashMap<>();

  private DelegateAuthStrategy(Api apis) {
    buildPermissions(apis);
  }

  public static DelegateAuthStrategy getInstance(Api apis) {
    if (instance == null) {
      synchronized (DelegateAuthStrategy.class) {
        if (instance == null) {
          instance = new DelegateAuthStrategy(apis);
        }
      }
    }
    return instance;
  }

  private void buildPermissions(Api apis) {
    List<AuthorizationRequest> apiAccessList = new ArrayList<>();
    apiAccessList.add(new AuthorizationRequest(POST, apis.getEntitiesEndpoint()));
    delegateAuthorizationRules.put("api", apiAccessList);

    // ingestion access list/rules
    List<AuthorizationRequest> ingestAccessList = new ArrayList<>();
    ingestAccessList.add(new AuthorizationRequest(POST, apis.getIngestionEndpoint()));
    ingestAccessList.add(new AuthorizationRequest(DELETE, apis.getIngestionEndpoint()));
    delegateAuthorizationRules.put("ingestion", ingestAccessList);
  }

  @Override
  public boolean isAuthorized(AuthorizationRequest authRequest, JwtData jwtData) {
    return true;
  }


}

