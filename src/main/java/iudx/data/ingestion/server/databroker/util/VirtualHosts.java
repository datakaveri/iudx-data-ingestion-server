package iudx.data.ingestion.server.databroker.util;

/**
 * This enum contains a mapping for IUDX vhosts available with config json Keys in dataBroker
 * verticle.
 *
 */
public enum VirtualHosts {


  IUDX_PROD("prodVhost"), IUDX_INTERNAL("internalVhost"), IUDX_EXTERNAL("externalVhost");

  public String value;

  VirtualHosts(String value) {
    this.value = value;
  }

}
