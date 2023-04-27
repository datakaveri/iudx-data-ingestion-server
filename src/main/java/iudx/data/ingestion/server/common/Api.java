package iudx.data.ingestion.server.common;

import static iudx.data.ingestion.server.apiserver.util.Constants.*;

public class Api {
  private static volatile Api apiInstance;
  private final String dxApiBasePath;
  private StringBuilder entitiesEndpoint;
  private StringBuilder ingestionEndpoint;

  private Api(String dxApiBasePath) {
    this.dxApiBasePath = dxApiBasePath;
    buildEndpoints();
  }

  public static Api getInstance(String dxApiBasePath) {
    if (apiInstance == null) {
      synchronized (Api.class) {
        if (apiInstance == null) {
          apiInstance = new Api(dxApiBasePath);
        }
      }
    }
    return apiInstance;
  }

  public void buildEndpoints() {
    entitiesEndpoint = new StringBuilder(dxApiBasePath).append(NGSILD_ENTITIES_URL);
    ingestionEndpoint = new StringBuilder(dxApiBasePath).append(NGSILD_INGESTION_URL);
  }

  public String getEntitiesEndpoint() {
    return entitiesEndpoint.toString();
  }

  public String getIngestionEndpoint() {
    return ingestionEndpoint.toString();
  }

}
