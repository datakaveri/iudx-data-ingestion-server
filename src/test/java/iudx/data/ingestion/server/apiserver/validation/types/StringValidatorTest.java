package iudx.data.ingestion.server.apiserver.validation.types;

import io.vertx.core.Vertx;
import io.vertx.core.cli.annotations.Description;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import iudx.data.ingestion.server.apiserver.exceptions.DxRuntimeException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;


import static org.junit.jupiter.api.Assertions.*;
@ExtendWith(VertxExtension.class)
class StringValidatorTest {

    JsonObject body = new JsonObject().put("queue",true);
    StringValidator stringValidator;
    JsonObject json = new JsonObject().put("id",
                                        "iisc.ac.in/89a36273d77dac4cf38114fca1bbe64392547f86/rs.iudx.io/pune-env-flood/FWR1059,,")
        .put("server-url", "www.abc.com")
        .put("server-port", 1234)
        .put("isSecure", true);

    @Test
    public void checkFailureCode(VertxTestContext vertxTestContext) {
        stringValidator = new StringValidator("Failed",json);
        assertEquals(400, stringValidator.failureCode());
        vertxTestContext.completeNow();
    }

    @Test
    public void checkFailureMessage(VertxTestContext vertxTestContext) {
        stringValidator = new StringValidator("Failed",json);
        assertEquals("Invalid parameter value passed",
                stringValidator.failureMessage());
        vertxTestContext.completeNow();
    }

    static Stream<Arguments> allowedValues() {
        // Add any valid value for which validation will pass successfully.
        return Stream.of(
                Arguments.of("within"),
                Arguments.of("intersects"),
                Arguments.of("near"));
    }
    static Stream<Arguments> invalidValues() {
        return Stream.of(
                Arguments.of("  "),
                Arguments.of("12323324893438348585478424248284248274274284278427437483458734578345873427942974128472142323783783783123233248934383485854784242482842482742742842784274374834587345783458734279429741284721423237831232332489343834858547842424828424827427428427842743748345873457834587342794297412847214232378378378347537485347537853874573457857873834eyuefhdhfth78378347537485347537853874573457857873834eyuefhdhfth47537485347537853874573457857873834eyuefhdhfth3fe83t7h34873y2y429y423984374297447  ")
                );
    }


    @ParameterizedTest
    @MethodSource("allowedValues")
    /*@Description("String type parameter valid values.")*/
    public void testValidValue(String value, Vertx vertx,
                                     VertxTestContext testContext) {
        StringValidator stringTypeValidator = new StringValidator(value, body);
        assertTrue(stringTypeValidator.isValid());
        testContext.completeNow();
    }

    @ParameterizedTest
    @MethodSource("invalidValues")
    @Description("id type parameter invalid values.")
    public void testInvalidIDTypeValue(String value, Vertx vertx,
                                       VertxTestContext testContext) {
        StringValidator stringTypeValidator = new StringValidator(value, body);
        assertThrows(DxRuntimeException.class, () -> stringTypeValidator.isValid());
        testContext.completeNow();
    }

}