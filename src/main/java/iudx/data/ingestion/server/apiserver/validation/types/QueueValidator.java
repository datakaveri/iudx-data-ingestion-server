package iudx.data.ingestion.server.apiserver.validation.types;

import static iudx.data.ingestion.server.apiserver.response.ResponseUrn.INVALID_QUEUE_VALUE;
import static iudx.data.ingestion.server.apiserver.util.Constants.VALIDATION_QUEUE_PATTERN;

import io.vertx.core.json.JsonObject;
import iudx.data.ingestion.server.apiserver.exceptions.DxRuntimeException;
import iudx.data.ingestion.server.apiserver.util.HttpStatusCode;
import iudx.data.ingestion.server.apiserver.validation.Validator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class QueueValidator implements Validator {
  private static final Logger LOGGER = LogManager.getLogger(QueueValidator.class);

  private final String value;

  public QueueValidator(final String value) {
    this.value = value;
  }


  boolean queueValidation(final String value) {
    JsonObject json = new JsonObject();
    int length = value.length();
    Character allowedSpecialCharacter = '/';
    Character prevChar = null;
    for (int i = 0; i < length; i++) {
      Character charAtCurrIndex = value.charAt(i);
      boolean letterOrDigitChecker =
          Character.isLetter(charAtCurrIndex) || Character.isDigit(charAtCurrIndex);

      if (letterOrDigitChecker) {
        prevChar = charAtCurrIndex;
        letterOrDigitChecker = false;
      } else if (charAtCurrIndex.equals(allowedSpecialCharacter) &&
          !prevChar.equals(allowedSpecialCharacter)) {
        prevChar = charAtCurrIndex;
      } else {
        LOGGER.error("Invalid Queue " + value);
        throw new DxRuntimeException(failureCode(), INVALID_QUEUE_VALUE, failureMessage(value));
      }
    }
    return true;

//    return VALIDATION_QUEUE_PATTERN.matcher(value).matches();
  }

  @Override
  public boolean isValid() {
    LOGGER.debug("value : " + value );

    if (value == null) {
      return true;
    }
    if (value.isBlank()) {
      LOGGER.error("Validation error :  blank value for passed");
      throw new DxRuntimeException(failureCode(), INVALID_QUEUE_VALUE, failureMessage(value));
    }
    if (value.length() > 512) {
      LOGGER.error("Validation error : Exceeding max length(512 characters) criteria");
      throw new DxRuntimeException(failureCode(), INVALID_QUEUE_VALUE, failureMessage(value));
    }
    if(!queueValidation(value)){
      LOGGER.error("Validation error : Invalid format of Queue found.");
      throw new DxRuntimeException(failureCode(), INVALID_QUEUE_VALUE, failureMessage(value));
    }
    LOGGER.info("Valid Queue found in Body.");
    return true;
  }

  @Override
  public int failureCode() {
    return HttpStatusCode.INVALID_QUEUE_VALUE.getValue();
  }

  @Override
  public String failureMessage() {
    return INVALID_QUEUE_VALUE.getMessage();
  }

}
