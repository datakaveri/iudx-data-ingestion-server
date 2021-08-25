package iudx.data.ingestion.server.authenticator.authorization;


import static iudx.data.ingestion.server.authenticator.authorization.Api.ENTITIES;
import static iudx.data.ingestion.server.authenticator.authorization.Method.GET;
import static iudx.data.ingestion.server.authenticator.authorization.Method.POST;

import iudx.data.ingestion.server.authenticator.model.JwtData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.vertx.core.json.JsonArray;

public class DelegateAuthStrategy implements AuthorizationStrategy {

  private static final Logger LOGGER = LogManager.getLogger(DelegateAuthStrategy.class);

  static Map<String, List<AuthorizationRequest>> delegateAuthorizationRules = new HashMap<>();
  static {

    List<AuthorizationRequest> apiAccessList = new ArrayList<>();
    apiAccessList.add(new AuthorizationRequest(GET, ENTITIES));
    apiAccessList.add(new AuthorizationRequest(POST, ENTITIES));
    delegateAuthorizationRules.put("api", apiAccessList);
  }

  @Override
  public boolean isAuthorized(AuthorizationRequest authRequest, JwtData jwtData) {
    JsonArray access = jwtData.getCons() != null ? jwtData.getCons().getJsonArray("access") : null;
    boolean result = false;
    if (access == null) {
      return result;
    }
    String endpoint = authRequest.getApi().getApiEndpoint();
    Method method = authRequest.getMethod();
    LOGGER.info("authorization request for : " + endpoint + " with method : " + method.name());
    LOGGER.info("allowed access : " + access);

    if (!result && access.contains("api")) {
      result = delegateAuthorizationRules.get("api").contains(authRequest);
    }

    return result;
  }


}

