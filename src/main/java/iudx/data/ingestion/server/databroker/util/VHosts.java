package iudx.data.ingestion.server.databroker.util;

/**
 * This enum contains a mapping for IUDX vhosts available with config json Keys in dataBroker
 * verticle.
 *
 */
public enum VHosts {


  IUDX_PROD("prodVhost"), IUDX_INTERNAL("internalVhost"), IUDX_EXTERNAL("externalVhost");

  public String value;

  VHosts(String value) {
    this.value = value;
  }

}
