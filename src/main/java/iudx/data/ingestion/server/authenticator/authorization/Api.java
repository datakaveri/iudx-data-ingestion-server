package iudx.data.ingestion.server.authenticator.authorization;

import java.util.stream.Stream;

public enum  Api {

  ENTITIES( "/entities"),
  INGESTION("/ingestion");


  private final String endpoint;

  Api(String endpoint) {
    this.endpoint = endpoint;
  }

  public static Api fromEndpoint(final String basePath,final String endpoint) {
    return Stream.of(values())
        .filter(v -> (basePath+v.endpoint).equalsIgnoreCase(endpoint))
        .findAny()
        .orElse(null);
  }

  public String getApiEndpoint() {
    return this.endpoint;
  }

}