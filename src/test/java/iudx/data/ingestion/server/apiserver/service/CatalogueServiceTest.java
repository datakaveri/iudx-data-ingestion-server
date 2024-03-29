package iudx.data.ingestion.server.apiserver.service;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ExtendWith(VertxExtension.class)
public class CatalogueServiceTest {

    @Mock
    Vertx vertxObj;
    JsonObject config;
    CatalogueService catalogueService;
    @Mock
    HttpRequest<Buffer> httpRequest;
    @Mock
    AsyncResult<HttpResponse<Buffer>> asyncResult;
    @Mock
    HttpResponse<Buffer> httpResponse;


    @BeforeEach
    public void setUp(VertxTestContext vertxTestContext)
    {
        config = new JsonObject();
        config.put("catServerHost","guest");
        config.put("catServerPort",8443);
        JsonObject jsonObject = new JsonObject();
        JsonArray jsonArray = new JsonArray();
        JsonArray jsonArray1 = new JsonArray();
        JsonObject jsonObject1 = new JsonObject();

        jsonObject1.put("id", "abcd/abcd/abcd/abcd");
        jsonObject1.put("iudxResourceAPIs", jsonArray1);
        jsonArray.add(jsonObject1);
        jsonObject.put("results", jsonArray);
        CatalogueService.catWebClient = mock(WebClient.class);

        when(CatalogueService.catWebClient.get(anyInt(),anyString(),anyString())).thenReturn(httpRequest);
        when(httpRequest.addQueryParam(anyString(),anyString())).thenReturn(httpRequest);
        when(httpRequest.expect(any())).thenReturn(httpRequest);
        when(asyncResult.succeeded()).thenReturn(true);
        when(asyncResult.result()).thenReturn(httpResponse);
        when(httpResponse.bodyAsJsonObject()).thenReturn(jsonObject);
        doAnswer(new Answer<AsyncResult<HttpResponse<Buffer>>>() {
            @Override
            public AsyncResult<HttpResponse<Buffer>> answer(InvocationOnMock arg0) throws Throwable {

                ((Handler<AsyncResult<HttpResponse<Buffer>>>)arg0.getArgument(0)).handle(asyncResult);
                return null;
            }
        }).when(httpRequest).send(any());
        catalogueService = new CatalogueService(vertxObj,config);
        vertxTestContext.completeNow();
    }


    @Test
    @DisplayName("Testing Success for isItemExist method with String IDs")
    public void testIsItemExistSuccess(VertxTestContext vertxTestContext)
    {

        JsonObject responseJSonObject = new JsonObject();
        responseJSonObject.put("type","urn:dx:cat:Success");
        responseJSonObject.put("totalHits", 10);
        when(httpResponse.bodyAsJsonObject()).thenReturn(responseJSonObject);

        catalogueService.isItemExist("asd/asd").onComplete(handler -> {
            if (handler.succeeded())
            {
                assertTrue(handler.result());
                vertxTestContext.completeNow();
            }
            else
            {
                vertxTestContext.failNow(handler.cause());
            }
        });
        verify(CatalogueService.catWebClient,times(2)).get(anyInt(),anyString(),anyString());
        verify(httpRequest,times(4)).addQueryParam(anyString(),anyString());
        verify(httpRequest,times(2)).send(any());

    }


    @Test
    @DisplayName("Testing Failed for isItemExist method with String IDs")
    public void testIsItemExistFail(VertxTestContext vertxTestContext)
    {

        JsonObject responseJSonObject = new JsonObject();
        responseJSonObject.put("type","urn:dx:cat:Success");
        responseJSonObject.put("totalHits", 0);
        when(httpResponse.bodyAsJsonObject()).thenReturn(responseJSonObject);

        catalogueService.isItemExist("asd/asd").onComplete(handler -> {
            if (handler.succeeded())
            {
                vertxTestContext.failNow(handler.cause());
                vertxTestContext.completeNow();
            }
            else
            {
                vertxTestContext.completeNow();
            }
        });
        verify(CatalogueService.catWebClient,times(2)).get(anyInt(),anyString(),anyString());
        verify(httpRequest, times(2)).send(any());
    }

    @Test
    @DisplayName("Testing pass for get item from cat server")
    public void testGetCatItemSucess(VertxTestContext vertxTestContext) {
        JsonObject responseJSonObject = new JsonObject();
        responseJSonObject.put("type", "urn:dx:cat:Success");
        responseJSonObject.put("totalHits", 10)
            .put("results", new JsonArray().add(new JsonObject()));
        when(httpResponse.bodyAsJsonObject()).thenReturn(responseJSonObject);
        catalogueService.getCatItem("dummy_id");
        vertxTestContext.completeNow();
        verify(CatalogueService.catWebClient, times(2)).get(anyInt(), anyString(), anyString());
        verify(httpRequest, times(2)).send(any());
    }

}
