package com.scania.sdos.orchestration.model;

import com.google.gson.Gson;
import com.scania.sdip.exceptions.IncidentException;
import com.scania.sdip.exceptions.SdipErrorCode;
import com.scania.sdip.exceptions.SdipErrorParameter;
import com.scania.sdos.model.ServiceArguments;
import com.scania.sdos.orchestration.RestTemplateClient;
import com.scania.sdos.orchestration.interfaces.IParallelActionModel;
import com.scania.sdos.orchestration.interfaces.IParameterMemory;
import com.scania.sdos.orchestration.interfaces.IParameterModel;
import com.scania.sdos.services.config.ActionParallelExecutor;
import com.scania.sdos.utils.SDOSConstants;
import com.scania.sdos.utils.Utility;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ResultActionAsyncModel extends ResultActionModel implements IParallelActionModel {

  private static final Logger LOGGER = LogManager.getLogger(ResultActionAsyncModel.class);

  private RestTemplateClient restTemplateClient;


  public ResultActionAsyncModel() {
    super();
    this.setRestTemplateClient(new RestTemplateClient());
  }

  public void setRestTemplateClient(RestTemplateClient restTemplateClient) {
    this.restTemplateClient = restTemplateClient;
  }

  @Override
  public void run(IParameterMemory iParameterMemory, JsonLdContextModel context,
                  ServiceArguments serviceArguments) {
    try {
      if (getInputParameter().size() == 0) {
        String requestBody = getHttpbody();
        HashMap<String, String> requestheaders = getHttpHeader();
        prepareRequest(iParameterMemory, "", requestBody,
            requestheaders, Utility.getStaticHashmap(), Utility.getStaticHashmap(),
            serviceArguments, this);
      } else {
        for (IParameterModel inputParameterModel : inputParameter) {
          HashMap<String, List<String>> memoryValue = iParameterMemory
              .getValue(inputParameterModel.getSubjectIri());
          Integer iterations = Utility.getActionIteration(memoryValue, inputParameterModel);

          if (memoryValue != null && !memoryValue.isEmpty()) {
            if (iterations == 1) {
              iterations1(inputParameterModel,memoryValue,iParameterMemory,serviceArguments);
            } else {
              int threadPoolSize = getThreadPoolSize(iterations, serviceArguments);
              ExecutorService executorService = getExecutorService(threadPoolSize);
              Set<Callable<String>> callables = getCallableSet();
              iterations(callables, iterations, inputParameterModel, memoryValue,iParameterMemory, serviceArguments);

              executorService.invokeAll(callables);
              isThreadActive(executorService);
            }
          }
        }
      }
    } catch (IncidentException exception) {
      throw exception;
    } catch (Exception exception) {
      throw new IncidentException(exception, SdipErrorCode.UNKNOWN_REASON_ERROR, LOGGER,
          exception.getMessage(), SdipErrorParameter.SUPPORTMAIL);
    }
  }

  private void iterations1(IParameterModel inputParameterModel,HashMap<String, List<String>> memoryValue,IParameterMemory iParameterMemory, ServiceArguments serviceArguments){
    String requestBody = getHttpbody();
    HashMap<String, String> requestHeaders = getHttpHeader();
    if (inputParameterModel != null) {
      for (String key : inputParameterModel.getKeys()) {
        if (key.equals(SDOSConstants.HTTPBODY)) {
          requestBody = memoryValue.get(SDOSConstants.HTTPBODY).get(0);
        } else if (key.equals(SDOSConstants.HTTPHEADER)) {
          HashMap<String, String> inputHeaders = new Gson()
                  .fromJson(memoryValue.get(SDOSConstants.HTTPHEADER).get(0), HashMap.class);
          for (Map.Entry<String, String> entry : inputHeaders.entrySet()) {
            requestHeaders.put(entry.getKey(), entry.getValue());
          }
        }
      }
    }

    if (requestBody == null && getHttpbody() != null) {
      requestBody = getHttpbody();
    }
    prepareRequest(iParameterMemory, "", requestBody,
            requestHeaders, Utility.getStaticHashmap(), Utility.getStaticHashmap(),
            serviceArguments, this);
  }
  private void iterations(Set<Callable<String>> callables,Integer iterations, IParameterModel inputParameterModel,HashMap<String, List<String>> memoryValue ,IParameterMemory iParameterMemory, ServiceArguments serviceArguments){
    String requestBody = "";
    HashMap<String, String> requestheaders = null;
    for (int i = 0; i < iterations; i++) {
      requestBody = getHttpbody();
      requestheaders = getHttpHeader();
      if (inputParameterModel != null) {
        for (String key : inputParameterModel.getKeys()) {
          if (key.equals(SDOSConstants.HTTPBODY)) {
            requestBody = memoryValue.get(SDOSConstants.HTTPBODY).get(i);
          } else if (key.equals(SDOSConstants.HTTPHEADER)) {
            readHeaderFromMemory(i,requestheaders,memoryValue);
          }
        }
      }

      if (requestBody == null && getHttpbody() != null) {
        requestBody = getHttpbody();
      }

      callables.add(ActionParallelExecutor.getParallelExecutorInstance(
              iParameterMemory, "", requestBody, requestheaders,
              Utility.getStaticHashmap(), Utility.getStaticHashmap(),
              serviceArguments, this
      ));
    }
  }

  private void readHeaderFromMemory(int i ,HashMap<String, String> requestHeaders,HashMap<String, List<String>> memoryValue){
    HashMap<String, String> inputHeaders = new Gson()
            .fromJson(memoryValue.get(SDOSConstants.HTTPHEADER).get(i),
                    HashMap.class);
    for (Map.Entry<String, String> entry : inputHeaders.entrySet()) {
      requestHeaders.put(entry.getKey(), entry.getValue());
    }
  }
  @Override
  public String prepareRequest(IParameterMemory iParameterMemory, String requestEndpoint,
      String requestBody, HashMap requestheaders, HashMap requestQueryParams, HashMap formData,
      ServiceArguments serviceArguments, IParallelActionModel iParallelActionModel) {
    String result = "";
    HttpConnectorModel httpConnectorModel = (HttpConnectorModel) ((ResultActionAsyncModel) iParallelActionModel)
        .getConnectorModel();
    HashMap<String, String> queryParamForGraph = new HashMap<>();
    queryParamForGraph.put(SDOSConstants.GRAPH,
        iParameterMemory.getValue(SDOSConstants.O_RESULT).get(SDOSConstants.GRAPH).get(0));
    if(this instanceof ResultActionAsyncModel && iParameterMemory.getValue(SDOSConstants.OBO_TOKEN) != null){
      requestheaders.put(SDOSConstants.AUTHORIZATION , SDOSConstants.BEARER+" "+iParameterMemory.getValue(SDOSConstants.OBO_TOKEN).get(SDOSConstants.VALUE).get(0));
      result = restTemplateClient
              .executeHttpPost(serviceArguments.getStardogResultEndpoint(),
                      queryParamForGraph,
                      requestheaders, requestBody, new BasicCredentialsProvider(),
                      null);
    } else if (httpConnectorModel.getHasAuthenticationMethod() != null) {
      HttpBasicAuthModel authModel = (HttpBasicAuthModel) httpConnectorModel
          .getHasAuthenticationMethod();
      authModel.run(iParameterMemory);
      requestheaders = authModel.addCredentials(requestheaders, iParameterMemory);
      result = restTemplateClient
          .executeHttpPost(serviceArguments.getStardogResultEndpoint(),
              queryParamForGraph,
              requestheaders, requestBody, authModel.getCredentials(),
              null);
    } else {
      result = restTemplateClient
          .executeHttpPost(serviceArguments.getStardogResultEndpoint(),
              queryParamForGraph,
              requestheaders, requestBody, null, null);
    }
    return result;
  }
}
