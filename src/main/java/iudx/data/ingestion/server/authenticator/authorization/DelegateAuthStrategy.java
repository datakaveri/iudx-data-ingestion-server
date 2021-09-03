package iudx.data.ingestion.server.authenticator.authorization;


import static iudx.data.ingestion.server.authenticator.authorization.Api.ENTITIES;
import static iudx.data.ingestion.server.authenticator.authorization.Api.INGESTION;
import static iudx.data.ingestion.server.authenticator.authorization.Method.DELETE;
import static iudx.data.ingestion.server.authenticator.authorization.Method.GET;
import static iudx.data.ingestion.server.authenticator.authorization.Method.POST;

import io.vertx.core.json.JsonArray;
import iudx.data.ingestion.server.authenticator.model.JwtData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DelegateAuthStrategy implements AuthorizationStrategy {

  private static final Logger LOGGER = LogManager.getLogger(DelegateAuthStrategy.class);

  static Map<String, List<AuthorizationRequest>> delegateAuthorizationRules = new HashMap<>();

  static {
    List<AuthorizationRequest> apiAccessList = new ArrayList<>();
    apiAccessList.add(new AuthorizationRequest(POST, ENTITIES));
    delegateAuthorizationRules.put("api", apiAccessList);

    // ingestion access list/rules
    List<AuthorizationRequest> ingestAccessList = new ArrayList<>();
    ingestAccessList.add(new AuthorizationRequest(POST, INGESTION));
    ingestAccessList.add(new AuthorizationRequest(DELETE, INGESTION));
    delegateAuthorizationRules.put("ingestion", ingestAccessList);
  }

  @Override
  public boolean isAuthorized(AuthorizationRequest authRequest, JwtData jwtData) {
    JsonArray access = jwtData.getCons() != null ? jwtData.getCons().getJsonArray("access") : null;
    boolean result = false;
    if (access == null) {
      return false;
    }
    String endpoint = authRequest.getApi().getApiEndpoint();
    Method method = authRequest.getMethod();
    LOGGER.info("authorization request for : " + endpoint + " with method : " + method.name());
    LOGGER.info("allowed access : " + access);

    if (access.contains("api")) {
      result = delegateAuthorizationRules.get("api").contains(authRequest);
    }

    if (access.contains("ingestion")) {
      result = delegateAuthorizationRules.get("ingestion").contains(authRequest);
    }

    return result;
  }


}

