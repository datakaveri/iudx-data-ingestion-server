package iudx.data.ingestion.server.authenticator;

import java.util.List;

public class Constants {
  public static final String ID = "id";
  public static final String API_ENDPOINT = "apiEndpoint";
  public static final String TOKEN = "token";
  public static final String METHOD = "method";
  public static final String KEYSTORE_PATH = "keystore";
  public static final String KEYSTORE_PASSWORD = "keystorePassword";
  public static final long CACHE_TIMEOUT_AMOUNT = 30;
  public static final String CAT_SEARCH_PATH = "/search";
  public static final String AUTH_CERTIFICATE_PATH = "/cert";

  public static final String CAT_ITEM_PATH = "/item";
  public static final String DI_AUDIENCE = "audience";
  public static final String CAT_SERVER_HOST = "catServerHost";
  public static final String CAT_SERVER_PORT = "catServerPort";
  public static final String JSON_PROVIDER = "provider";
  public static final String JSON_DELEGATE = "delegate";
  public static final String JSON_IID = "iid";
  public static final String JSON_USERID = "userid";

  public static final String JSON_ROLE = "role";
  public static final String JSON_DRL = "drl";
  public static final String JSON_DID = "did";
  public static final String JSON_EXPIRY = "expiry";

  public static final List<String> ADMIN_ENDPOINTS = List.of("/ngsi-ld/v1/ingestion");
  public static final int JWT_LEEWAY_TIME = 30;
}
