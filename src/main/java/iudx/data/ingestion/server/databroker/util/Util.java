package iudx.data.ingestion.server.databroker.util;

import static iudx.data.ingestion.server.databroker.util.Constants.*;

import io.vertx.core.json.JsonObject;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class Util {

  public static JsonObject getMetadata(JsonObject request) {
    JsonObject result = new JsonObject();
    String id = request.getString("id");
    String[] arr = id.split("/");
    String exchangeName = getExchangeName(arr);
    result.put(EXCHANGE_NAME, exchangeName);
    if (arr.length == 5) {
      result.put(ROUTING_KEY, getRoutingKey(exchangeName, getResourceName(arr)));
    } else {
      result.put(ROUTING_KEY, getRoutingKey(exchangeName));
    }
    return result;
  }

  public static String encodeString(String s) {
    return URLEncoder.encode(s, StandardCharsets.UTF_8);
  }

  private static String getResourceName(String[] arr) {
    return arr[4];
  }

  public static String getResourceGroupName(String[] arr) {
    return arr[3];
  }

  public static String getResourceServerName(String[] arr) {
    return arr[2];
  }

  public static String getProviderName(String[] arr) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 2; i++) {
      sb.append(arr[i]);
      sb.append('/');
    }
    sb.setLength(sb.length() - 1);
    return sb.toString();
  }

  private static String getExchangeName(String[] arr) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 4; i++) {
      sb.append(arr[i]);
      sb.append('/');
    }
    sb.setLength(sb.length() - 1);
    return sb.toString();
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