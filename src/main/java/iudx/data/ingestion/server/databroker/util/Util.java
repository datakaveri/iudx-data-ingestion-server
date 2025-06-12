package iudx.data.ingestion.server.databroker.util;

import static iudx.data.ingestion.server.databroker.util.Constants.DETAIL;
import static iudx.data.ingestion.server.databroker.util.Constants.EXCHANGE_NAME;
import static iudx.data.ingestion.server.databroker.util.Constants.ROUTING_KEY;
import static iudx.data.ingestion.server.databroker.util.Constants.ROUTING_KEY_ALL;
import static iudx.data.ingestion.server.databroker.util.Constants.TITLE;
import static iudx.data.ingestion.server.databroker.util.Constants.TYPE;

import io.vertx.core.json.JsonObject;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class Util {

  public static JsonObject getMetadata(JsonObject request) {
    JsonObject result = new JsonObject();
    String exchangeName = getExchangeName(request);
    result.put("ownerId", request.getString("ownerId"));
    result.put(EXCHANGE_NAME, exchangeName);
    if (request.containsKey("resourceGroup")) {
      result.put(ROUTING_KEY, getRoutingKey(exchangeName, getResourceName(request)));
    } else {
      result.put(ROUTING_KEY, getRoutingKey(exchangeName));
    }
    return result;
  }

  public static String encodeString(String s) {
    return URLEncoder.encode(s, StandardCharsets.UTF_8);
  }

  private static String getResourceName(JsonObject request) {
    return request.getString("id");
  }

  public static String getResourceGroupName(JsonObject request) {
    return request.getString("resourceGroup");
  }

  public static String getResourceServerName(JsonObject request) {
    return request.getString("resourceServer");
  }

  public static String getProviderName(JsonObject request) {
    return request.getString("provider");
  }

  private static String getExchangeName(JsonObject request) {
    return request.containsKey("resourceGroup") ? request.getString("resourceGroup") :
        request.getString("id");
  }

  private static String getRoutingKey(String exchangeName) {
    return exchangeName + '/' + ROUTING_KEY_ALL;
  }

  private static String getRoutingKey(String exchangeName, String resourceName) {
    return exchangeName + '/' + '.' + resourceName;
  }

  public static JsonObject getResponseJson(int type, String title, String detail) {
    JsonObject entries = new JsonObject();
    return entries.put(TYPE, type)
        .put(TITLE, title)
        .put(DETAIL, detail);
  }
}