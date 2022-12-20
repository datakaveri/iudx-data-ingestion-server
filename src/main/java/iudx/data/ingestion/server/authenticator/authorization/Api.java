package iudx.data.ingestion.server.authenticator.authorization;

import iudx.data.ingestion.server.apiserver.util.Configuration;

import java.util.stream.Stream;

public enum  Api {
  ENTITIES( Configuration.getBasePath() + "/entities"),
  INGESTION(Configuration.getBasePath() + "/ingestion");

  private final String endpoint;

  Api(String endpoint) {
    this.endpoint = endpoint;
  }

  public static Api fromEndpoint(final String endpoint) {
    return Stream.of(values())
        .filter(v -> v.endpoint.equalsIgnoreCase(endpoint))
        .findAny()
        .orElse(null);
  }

  public String getApiEndpoint() {
    return this.endpoint;
  }

}