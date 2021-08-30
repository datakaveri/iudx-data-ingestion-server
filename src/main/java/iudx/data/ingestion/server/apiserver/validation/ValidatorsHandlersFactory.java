package iudx.data.ingestion.server.apiserver.validation;

import static iudx.data.ingestion.server.apiserver.util.Constants.NGSILD_QUERY_ID;

import io.vertx.core.json.JsonObject;
import iudx.data.ingestion.server.apiserver.util.RequestType;
import iudx.data.ingestion.server.apiserver.validation.types.IDTypeValidator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class ValidatorsHandlersFactory {

  private static final Logger LOGGER =
      LogManager.getLogger(ValidatorsHandlersFactory.class);
  private static Map<String, String> jsonSchemaMap = new HashMap<>();

  public List<Validator> build(final RequestType requestType,
                               final JsonObject body) {
    LOGGER.debug("getValidationForContext() started for : " + requestType);
    LOGGER.debug("type :" + requestType);
    List<Validator> validator = null;

    switch (requestType) {
      case ENTITY:
        validator = getEntityRequestValidations(body);
        break;
      default:
        break;
    }

    return validator;
  }

  private List<Validator> getEntityRequestValidations(JsonObject body) {
    List<Validator> validators = new ArrayList<>();
    validators.add(new IDTypeValidator(body.getString(NGSILD_QUERY_ID), true));
    return validators;
  }

}
