package com.scania.sdip.sdos.model;

import com.scania.sdip.exceptions.IncidentException;
import com.scania.sdip.exceptions.SdipErrorCode;
import com.scania.sdip.sdos.services.Argument;
import com.scania.sdip.sdos.services.IArguments;
import com.scania.sdip.sdos.utils.SDOSConstants;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

/**
 * The Service class is a Singleton that holds information submitted at startup about the service.
 */
@Component
public final class ServiceArguments {

  private static final Logger LOGGER = LogManager.getLogger(ServiceArguments.class);
  private URL configurationUrl;
  private String serviceId;
  private String envname;
  private String resultDB;
  private String ofgDB;
  private URL stardogBaseUrl;
  private String threadPoolSize;
  private String sdosClientSecret;
  private String stardogClientScope;
  private String tenantId;
  private IArguments arguments;

  public void init(IArguments arguments) {
    this.arguments = arguments;
    verifyOrchestrationArgumentsToModel();
    String environment = System.getenv(SDOSConstants.ENVIRONMENT_TAG);
    if (environment == null || environment.isEmpty()) {
      environment = SDOSConstants.LOCAL;
    }
    setEnvname(environment);
  }

  private void verifyOrchestrationArgumentsToModel() {
    setStardogBaseUrl(validateUrl(Argument.STARDOG_HOST.toString()));
    setResultDB(arguments.getString(Argument.RESULT_DB.toString()));
    setOfgDB(arguments.getString(Argument.OFG_DB.toString()));
    setServiceId(arguments.getString(Argument.SERVICE_ID.toString()));
    setThreadPoolSize(arguments.getString(Argument.THREADPOOLSIZE.toString()));
    setSdosClientSecret(arguments.getString(Argument.CLIENT_SECRET.toString()));
    setStardogClientScope(arguments.getString(Argument.STARDOG_CLIENTSCOPE.toString()));
    setTenantId(arguments.getString(Argument.TENANT_ID.toString()));
  }

  private URL validateUrl(String arg){
    try {
      URL url = new URL(arguments.getString(arg));
      return url;
    } catch (MalformedURLException e) {
      throw new IncidentException(SdipErrorCode.ARGUMENT_NOT_A_VALID_URL, LOGGER,
              arguments.getString(Argument.CONFIGURATION_HOST.toString()));
    }
  }


  /**
   * Returns the URL of the SDCS instance that must be used to configure SDOS.
   *
   * @return a URL of an SDCS instance to be used by SDOS.
   */
  public URL getConfigurationUrl() {
    return configurationUrl;
  }

  /**
   * Provides the service with a URL to the SDCS instance that must be used to configure SDOS.
   *
   * @param configurationUrl a URL of an SDCS instance to be used by SDOS.
   */
  public void setConfigurationUrl(URL configurationUrl) {
    this.configurationUrl = configurationUrl;
  }

  /**
   * Returns the service ID.
   *
   * @return a service ID.
   */
  public String getServiceId() { return serviceId; }

  /**
   * Provides the service with a service ID.
   *
   * @param serviceId a service ID.
   */
  public void setServiceId(String serviceId) { this.serviceId = serviceId; }

  public String getEnvname() { return envname; }

  public void setEnvname(String envname) { this.envname = envname; }

  public String getResultDB() { return resultDB; }

  public void setResultDB(String resultDB) { this.resultDB = resultDB; }

  public String getOfgDB() { return ofgDB; }

  public void setOfgDB(String ofgDB) { this.ofgDB = ofgDB; }

  public URL getStardogBaseUrl() { return stardogBaseUrl; }

  public String getThreadPoolSize() { return threadPoolSize; }

  public String getSdosClientSecret() { return sdosClientSecret; }

  public String getStardogClientScope() { return stardogClientScope; }

  public String getTenantId() { return tenantId; }

  public void setThreadPoolSize(String threadPoolSize) { this.threadPoolSize = threadPoolSize; }

  public void setStardogBaseUrl(URL stardogBaseUrl) { this.stardogBaseUrl = stardogBaseUrl; }

  public void setSdosClientSecret(String sdosClientSecret) { this.sdosClientSecret = sdosClientSecret; }

  public void setStardogClientScope(String stardogClientScope) { this.stardogClientScope = stardogClientScope; }

  public void setTenantId(String tenantId) { this.tenantId = tenantId; }

  public String getStardogResultEndpoint() {
    return stardogBaseUrl + "/" + resultDB;
  }

  public String getStardogResultQueryEndpoint() {
    return stardogBaseUrl + "/" + resultDB + "/" + SDOSConstants.QUERY;
  }

  public String getStardogResultUpdateEndpoint() {
    return stardogBaseUrl + "/" + resultDB + SDOSConstants.UPDATE;
  }

  public String getStardogOFGEndpoint() {
    return stardogBaseUrl + "/" + ofgDB;
  }

  public String getStardogQueryEndpoint() {
    return stardogBaseUrl + "/" + ofgDB + "/" + SDOSConstants.QUERY;
  }

  public String getAzureTenantUrl() {
    return SDOSConstants.AZURE_TENANT_URL.replace(SDOSConstants.TENANT_ID,getTenantId());
  }
}
