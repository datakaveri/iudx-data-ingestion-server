package iudx.data.ingestion.server.authenticator.authorization;


import static iudx.data.ingestion.server.authenticator.authorization.Method.DELETE;
import static iudx.data.ingestion.server.authenticator.authorization.Method.POST;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import iudx.data.ingestion.server.authenticator.model.JwtData;
import iudx.data.ingestion.server.common.Api;

public class DelegateAuthStrategy implements AuthorizationStrategy {

  private static final Logger LOGGER = LogManager.getLogger(DelegateAuthStrategy.class);

  Map<String, List<AuthorizationRequest>> delegateAuthorizationRules = new HashMap<>();
  private final Api apis;
  public DelegateAuthStrategy(Api apis) {
    this.apis=apis;
    buildPermissions(apis);
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

