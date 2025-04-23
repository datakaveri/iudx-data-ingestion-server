package iudx.data.ingestion.server.apiserver.validation;

import static iudx.data.ingestion.server.apiserver.util.Constants.*;

import io.vertx.core.MultiMap;
import io.vertx.core.json.JsonObject;
import iudx.data.ingestion.server.apiserver.util.RequestType;
import iudx.data.ingestion.server.apiserver.validation.types.IdTypeValidator;
import iudx.data.ingestion.server.apiserver.validation.types.StringValidator;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class ValidatorsHandlersFactory {

  private static final Logger LOGGER =
      LogManager.getLogger(ValidatorsHandlersFactory.class);

  public List<Validator> build(final RequestType requestType,
                               final JsonObject body, final MultiMap parameters) {
    LOGGER.debug("getValidationForContext() started for : " + requestType);
    LOGGER.debug("type :" + requestType);
    List<Validator> validator = null;

    switch (requestType) {
      case ENTITY:
        validator = getEntityRequestValidations(body);
        break;
      case INGEST:
        validator = getIngestRequestValidations(body);
        break;
      case INGEST_DELETE:
        validator = getIngestDeleteRequestValidations(body);
        break;
      default:
        break;
    }

    return validator;
  }

  private List<Validator> getIngestDeleteRequestValidations(JsonObject body) {
    List<Validator> validators = new ArrayList<>();
    validators.add(new IdTypeValidator(body.getString(NGSILD_QUERY_ID), true));
    return validators;
  }

  private List<Validator> getIngestRequestValidations(JsonObject body) {
    List<Validator> validators = new ArrayList<>();
    validators.add(new IdTypeValidator(body.getString(NGSILD_QUERY_ID), true));
    if (body.containsKey(QUEUE)) {
      validators.add(new StringValidator(body.getString(QUEUE), body));
    }
    return validators;
  }

  private List<Validator> getEntityRequestValidations(JsonObject body) {
    List<Validator> validators = new ArrayList<>();
    validators.add(new IdTypeValidator(body.getString(NGSILD_QUERY_ID), true));
    return validators;
  }

}
