package iudx.data.ingestion.server.authenticator.authorization;

import io.vertx.core.json.JsonArray;
import iudx.data.ingestion.server.authenticator.model.JwtData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ConsumerAuthStrategy implements AuthorizationStrategy {

  private static final Logger LOGGER = LogManager.getLogger(ConsumerAuthStrategy.class);

  static Map<String, List<AuthorizationRequest>> consumerAuthorizationRules = new HashMap<>();

  static {

    List<AuthorizationRequest> apiAccessList = new ArrayList<>();
    apiAccessList.add(new AuthorizationRequest(Method.GET, Api.ENTITIES));
    consumerAuthorizationRules.put("api", apiAccessList);
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
      LOGGER.info(consumerAuthorizationRules.get("api").contains(authRequest));
      result = consumerAuthorizationRules.get("api").contains(authRequest);
    }

    return result;
  }

}
