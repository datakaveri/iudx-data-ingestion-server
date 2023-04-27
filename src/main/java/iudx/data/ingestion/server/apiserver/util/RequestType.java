package iudx.data.ingestion.server.apiserver.util;

public enum RequestType {
  ENTITY("entity"), INGEST("ingest"), INGEST_DELETE("ingestDelete");

  private final String filename;

  RequestType(String fileName) {
    this.filename = fileName;
  }

  public String getFilename() {
    return this.filename;
  }
}
