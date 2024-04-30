package com.scania.sdip.sdos.services;

/**
 * This enum class represents command line arguments options.
 */
public enum Argument {
  CONFIGURATION_HOST("configurationUrl"),
  SERVICE_ID("serviceId"),
  STARDOG_HOST("stardogBaseUrl"),
  OFG_DB("ofgDb"),
  RESULT_DB("resultDb"),
  THREADPOOLSIZE("threadPoolSize"),
  STARDOG_CLIENTSCOPE("stardogClientScope"),
  CLIENT_SECRET("sdosClientSecret"),
  TENANT_ID("tenantId");

  private final String argumentLookupKey;

  Argument(String argumentLookupKey) {
    this.argumentLookupKey = argumentLookupKey;
  }

  @Override
  public String toString() {
    return argumentLookupKey;
  }
}
