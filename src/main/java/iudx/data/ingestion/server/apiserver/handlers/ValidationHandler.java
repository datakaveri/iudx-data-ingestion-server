package iudx.data.ingestion.server.apiserver.handlers;

import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import iudx.data.ingestion.server.apiserver.util.RequestType;
import iudx.data.ingestion.server.apiserver.validation.Validator;
import iudx.data.ingestion.server.apiserver.validation.ValidatorsHandlersFactory;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ValidationHandler implements Handler<RoutingContext> {

  private static final Logger LOGGER = LogManager.getLogger(ValidationHandler.class);

  private final RequestType requestType;

  public ValidationHandler(RequestType apiRequestType) {
    this.requestType = apiRequestType;
  }

  @Override
  public void handle(RoutingContext context) {
    ValidatorsHandlersFactory validationFactory = new ValidatorsHandlersFactory();
    MultiMap parameters = context.request().params();
    JsonObject requestBody = context.getBodyAsJson();
    List<Validator>
        validations = validationFactory.build(requestType, requestBody, parameters);
    for (Validator validator : Optional.ofNullable(validations).orElse(Collections.emptyList())) {
      LOGGER.debug("validator :" + validator.getClass().getName());
      validator.isValid();
    }
    context.next();
  }
}
