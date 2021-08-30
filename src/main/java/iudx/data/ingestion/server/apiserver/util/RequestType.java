package iudx.data.ingestion.server.apiserver.util;

public enum RequestType {
  ENTITY("entity");

  private String filename;

  private RequestType(String fileName) {
    this.filename = fileName;
  }

  public String getFilename() {
    return this.filename;
  }
}
