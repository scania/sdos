package com.scania.sdip.sdos.services.config;

import com.scania.sdip.sdos.model.ServiceArguments;
import com.scania.sdip.sdos.orchestration.RestTemplateClient;
import com.scania.sdip.sdos.orchestration.interfaces.IParallelActionModel;
import com.scania.sdip.sdos.orchestration.interfaces.IParameterMemory;
import com.scania.sdip.sdos.orchestration.model.HttpConnectorModel;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.Callable;


public class ActionParallelExecutor implements Callable {

  private RestTemplateClient restTemplateClient;
  private IParameterMemory iParameterMemory;
  private String requestEndpoint;
  private String requestHttpBody;
  private IParallelActionModel iParallelActionModel;

  public String getRequestEndpoint() {
    return requestEndpoint;
  }

  public String getRequestHttpBody() {
    return requestHttpBody;
  }

  public HashMap getRequestHeaders() {
    return requestHeaders;
  }

  public HashMap getRequestQueryParams() {
    return requestQueryParams;
  }

  public HttpConnectorModel getConnector() {
    return connector;
  }

  public void setConnector(HttpConnectorModel connector) {
    this.connector = connector;
  }

  private HashMap requestHeaders;
  private HashMap requestQueryParams;
  private HashMap formData;
  private HttpConnectorModel connector;
  private ServiceArguments serviceArguments;


  public ActionParallelExecutor(
      IParameterMemory iParameterMemory, String requestEndpoint, String requestHttpBody,
      HashMap requestHeaders, HashMap requestQueryParams, HashMap formData,
      ServiceArguments serviceArguments,
      IParallelActionModel iActionModel
  ) {
    this.iParameterMemory = iParameterMemory;
    this.requestEndpoint = requestEndpoint;
    this.requestHttpBody = requestHttpBody;
    this.requestHeaders = requestHeaders;
    this.requestQueryParams = requestQueryParams;
    this.formData = formData;
    this.restTemplateClient = new RestTemplateClient();
    this.iParallelActionModel = iActionModel;
    this.serviceArguments = serviceArguments;
  }

  public static ActionParallelExecutor getParallelExecutorInstance(
      IParameterMemory iParameterMemory, String requestEndpoint, String requestHttpBody,
      HashMap requestHeaders, HashMap requestQueryParams, HashMap formData,
      ServiceArguments serviceArguments,
      IParallelActionModel iActionModel) {
    return new ActionParallelExecutor(iParameterMemory, requestEndpoint, requestHttpBody,
        requestHeaders, requestQueryParams, formData, serviceArguments, iActionModel);
  }


  /**
   * Computes a result, or throws an exception if unable to do so.
   *
   * @return computed result
   */
  @Override
  public String call() throws IOException {
    return iParallelActionModel.prepareRequest(iParameterMemory, requestEndpoint,
        requestHttpBody,
        requestHeaders,
        requestQueryParams, formData, serviceArguments,
        iParallelActionModel);
  }
}
