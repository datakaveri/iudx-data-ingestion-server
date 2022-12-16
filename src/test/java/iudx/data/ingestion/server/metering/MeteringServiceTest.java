package iudx.data.ingestion.server.metering;

import static iudx.data.ingestion.server.apiserver.util.Constants.*;
import static iudx.data.ingestion.server.authenticator.Constants.ID;
import static iudx.data.ingestion.server.databroker.util.Constants.SUCCESS;
import static iudx.data.ingestion.server.metering.util.Constants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import iudx.data.ingestion.server.apiserver.response.ResponseUrn;
import iudx.data.ingestion.server.configuration.Configuration;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import iudx.data.ingestion.server.databroker.DataBrokerService;
import iudx.data.ingestion.server.metering.util.ResponseBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

@ExtendWith({VertxExtension.class})
public class MeteringServiceTest {
  private static final Logger LOGGER = LogManager.getLogger(MeteringServiceTest.class);
  private static MeteringService meteringService;
  private static Vertx vertxObj;
  private static DataBrokerService dataBrokerService;

    @BeforeAll
    @DisplayName("Deploying Verticle")
    static void startVertex(Vertx vertx, VertxTestContext vertxTestContext) {
        vertxObj = vertx;
        meteringService = new MeteringServiceImpl(dataBrokerService);
        vertxTestContext.completeNow();
    }

  // @AfterAll
  // public void finish(VertxTestContext testContext) {
  // logger.info("finishing");
  // vertxObj.close(testContext.succeeding(response -> testContext.completeNow()));
  // }

    @Test
    @DisplayName("Testing Write Query Successful")
    void writeDataSuccessful(VertxTestContext vertxTestContext) {
        JsonObject request = new JsonObject();
        ZonedDateTime zst = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
        long time = zst.toInstant().toEpochMilli();
        String isoTime = zst.truncatedTo(ChronoUnit.SECONDS).toString();
        request.put(PRIMARY_KEY, "primaryKey");
        request.put(EPOCH_TIME, time);
        request.put(ISO_TIME, isoTime);
        request.put(USER_ID, "15c7506f-c800-48d6-adeb-0542b03947c6");
        request.put(PROVIDER_ID, "iisc.ac.in/89a36273d77dac4cf38114fca1bbe64392547f86");
        request.put(ID, "iisc.ac.in/89a36273d77dac4cf38114fca1bbe64392547f86/rs.iudx.io/surat-itms-realtime-information/surat-itms-live-eta");
        request.put(API, "/iudx/v1/list");
        request.put(RESPONSE_SIZE, 12);
        request.put(ORIGIN,ORIGIN_SERVER);
        MeteringServiceImpl auditingService = new MeteringServiceImpl(dataBrokerService);

        AsyncResult<JsonObject> asyncResult = mock(AsyncResult.class);
        MeteringServiceImpl.dataBrokerService = mock(DataBrokerService.class);

        when(asyncResult.succeeded()).thenReturn(true);
        doAnswer(
                new Answer<AsyncResult<JsonObject>>() {
                    @Override
                    public AsyncResult<JsonObject> answer(InvocationOnMock arg0) throws Throwable {
                        ((Handler<AsyncResult<JsonObject>>) arg0.getArgument(3)).handle(asyncResult);
                        return null;
                    }
                })
                .when(auditingService.dataBrokerService)
                .publishMessage(any(), anyString(), anyString(), any());

        meteringService.insertMeteringValuesInRMQ(
                request,
                handler -> {
                    if (handler.succeeded()) {
                        vertxTestContext.completeNow();
                    } else {
                        vertxTestContext.failNow("Failed");
                    }
                });
        verify(auditingService.dataBrokerService, times(1))
                .publishMessage(any(), anyString(), anyString(), any());
    }

  @Test
  @DisplayName("Set Type And Title Test")
  public void setTypeAndTitleTest(VertxTestContext vertxTestContext){
    ResponseBuilder responseBuilder= new ResponseBuilder("200");
    responseBuilder.setTypeAndTitle(200);
    assertEquals("success", ResponseUrn.SUCCESS.getMessage());
    responseBuilder.setTypeAndTitle(204);
    assertEquals("success",SUCCESS);
    responseBuilder.setTypeAndTitle(400);
    assertEquals("bad request parameter",ResponseUrn.BAD_REQUEST_URN.getMessage());
    vertxTestContext.completeNow();
  }
}
