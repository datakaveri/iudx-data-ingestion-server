package iudx.data.ingestion.server.common;

import static iudx.data.ingestion.server.apiserver.util.Constants.*;

public class Api {
  
  private final String dxApiBasePath;
  private final String iudxApiBasePath;
  private static volatile Api apiInstance;
  
  private Api(String dxApiBasePath,String iudxApiBasePath) {
    this.dxApiBasePath=dxApiBasePath;
    this.iudxApiBasePath=iudxApiBasePath;
    buildEndpoints();
  }

  public static Api getInstance(String dxApiBasePath,String iudxApiBasePath)
  {
    if (apiInstance == null)
    {
      synchronized (Api.class)
      {
        if (apiInstance == null)
        {
          apiInstance = new Api(dxApiBasePath,iudxApiBasePath);
        }
      }
    }
    return apiInstance;
  }
  private StringBuilder entitiesEndpoint;
  private StringBuilder ingestionEndpoint;
  
  public void buildEndpoints() {
    entitiesEndpoint=new StringBuilder(dxApiBasePath).append(NGSILD_ENTITIES_URL);
    ingestionEndpoint=new StringBuilder(dxApiBasePath).append(NGSILD_INGESTION_URL);
  }
  
  
  public String getEntitiesEndpoint() {
    return entitiesEndpoint.toString();
  }
  
  public String getIngestionEndpoint() {
    return ingestionEndpoint.toString();
  }
  

}
