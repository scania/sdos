package com.scania.sdip.sdos.orchestration.model;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonSyntaxException;
import com.scania.sdip.exceptions.IncidentException;
import com.scania.sdip.exceptions.SdipErrorCode;
import com.scania.sdip.exceptions.SdipErrorParameter;
import com.scania.sdip.sdos.model.ServiceArguments;
import com.scania.sdip.sdos.orchestration.Rdf4jClient;
import com.scania.sdip.sdos.orchestration.RestTemplateClient;
import com.scania.sdip.sdos.orchestration.interfaces.IActionModel;
import com.scania.sdip.sdos.orchestration.interfaces.IParallelActionModel;
import com.scania.sdip.sdos.orchestration.interfaces.IParameterMemory;
import com.scania.sdip.sdos.orchestration.interfaces.IParameterModel;
import com.scania.sdip.sdos.services.config.ActionParallelExecutor;
import com.scania.sdip.sdos.utils.SDOSConstants;
import com.scania.sdip.sdos.utils.Utility;
import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.Collections;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import org.apache.http.client.CredentialsProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;

public class HttpActionModel extends ActionModel implements IActionModel, IParallelActionModel {

  private static final Logger LOGGER = LogManager.getLogger(HttpActionModel.class);

  public static final String SPARQL =
      SDOSConstants.PREFIX_RDF
          + SDOSConstants.PREFIX_RDFS
          + "PREFIX : <" + SDOSConstants.ORCHESTRATION_PREFIX + "> \n"
          + "SELECT ?" + SDOSConstants.LABEL
          + " ?" + SDOSConstants.HTTPBODY
          + " ?" + SDOSConstants.HTTPHEADER
          + " ?" + SDOSConstants.HTTPQUERYPARAM
          + " ?" + SDOSConstants.HAS_CONNECTOR
          + " ?" + SDOSConstants.ENDPOINT
          + " ?" + SDOSConstants.INPUT_PARAMETER
          + " ?" + SDOSConstants.OUTPUT_PARAMETER
          + " ?" + SDOSConstants.INPUT_PARAMETER_TYPE
          + " ?" + SDOSConstants.OUTPUT_PARAMETER_TYPE
          + " ?" + SDOSConstants.NEXT_ACTION
          + " ?" + SDOSConstants.NEXT_ACTION_TYPE
          + "\n "
          + "WHERE \n"
          + "{  \n"
          + "BIND(<" + SDOSConstants.VARIABLE + "> AS ?subject)\n"
          + "    ?subject  rdf:type :HTTPAction ;\n"
          + "        rdfs:label ?label ;\n"
          + "        :hasConnector ?" + SDOSConstants.HAS_CONNECTOR + " .\n"
          + "    OPTIONAL{?subject :httpBody ?" + SDOSConstants.HTTPBODY + ".}\n"
          + "    OPTIONAL{?subject :httpHeader ?" + SDOSConstants.HTTPHEADER + ".}\n"
          + "    OPTIONAL{?subject :httpQueryParameter ?" + SDOSConstants.HTTPQUERYPARAM + ".}\n"
          + "    OPTIONAL{?subject :endpoint ?" + SDOSConstants.ENDPOINT + " .}\n"
          + "    OPTIONAL{?subject :inputParameter  ?" + SDOSConstants.INPUT_PARAMETER + " . \n"
          + SDOSConstants.OPENING + SDOSConstants.INPUT_PARAMETER + " rdf:type :Parameter .\n"
          + "            BIND(:Parameter AS ?" + SDOSConstants.INPUT_PARAMETER_TYPE + ")}\n"
          + SDOSConstants.UNION + SDOSConstants.INPUT_PARAMETER + " rdf:type :HTTPParameter .\n"
          + "            BIND(:HTTPParameter AS ?" + SDOSConstants.INPUT_PARAMETER_TYPE + ")}\n"
          + "        UNION { ?inputparameter rdf:type :StandardParameter .\n"
          + "            BIND(:StandardParameter AS ?inputparametertype)}\n"
          + SDOSConstants.HTTPACTIONMODEL_CLOSING2
          + "    OPTIONAL{?subject :outputParameter  ?" + SDOSConstants.OUTPUT_PARAMETER + " .\n"
          + SDOSConstants.OPENING + SDOSConstants.OUTPUT_PARAMETER + " rdf:type :Parameter .\n"
          + "            BIND(:Parameter AS ?" + SDOSConstants.OUTPUT_PARAMETER_TYPE + ")}\n"
          + SDOSConstants.UNION + SDOSConstants.OUTPUT_PARAMETER
          + " rdf:type :StandardParameter .\n"
          + "            BIND(:StandardParameter AS ?" + SDOSConstants.OUTPUT_PARAMETER_TYPE
          + ")}\n"
          + SDOSConstants.UNION + SDOSConstants.OUTPUT_PARAMETER + " rdf:type :HTTPParameter .\n"
          + "            BIND(:HTTPParameter AS ?" + SDOSConstants.OUTPUT_PARAMETER_TYPE + ")}\n"
          + SDOSConstants.HTTPACTIONMODEL_CLOSING2
          + "    OPTIONAL{?subject :hasNextAction ?" + SDOSConstants.NEXT_ACTION + " .\n"
          + SDOSConstants.OPENING + SDOSConstants.NEXT_ACTION + " rdf:type :Action .} \n"
          + SDOSConstants.UNION + SDOSConstants.NEXT_ACTION + " rdf:type :HTTPAction .\n"
          + "            BIND(:HTTPAction AS ?" + SDOSConstants.NEXT_ACTION_TYPE + SDOSConstants.CLOSING
          + SDOSConstants.UNION + SDOSConstants.NEXT_ACTION + " rdf:type :SOAPAction .\n"
          + "            BIND(:SOAPAction AS ?" + SDOSConstants.NEXT_ACTION_TYPE + SDOSConstants.CLOSING
          + SDOSConstants.UNION + SDOSConstants.NEXT_ACTION + " rdf:type :ScriptAction .\n"
          + "            BIND(:ScriptAction AS ?" + SDOSConstants.NEXT_ACTION_TYPE + SDOSConstants.CLOSING
          + SDOSConstants.UNION + SDOSConstants.NEXT_ACTION + " rdf:type :ResultAction . \n"
          + "            BIND(:ResultAction AS ?" + SDOSConstants.NEXT_ACTION_TYPE + SDOSConstants.CLOSING
          + SDOSConstants.UNION + SDOSConstants.NEXT_ACTION + " rdf:type :VirtualGraphAction .\n"
          + "            BIND(:VirtualGraphAction AS ?" + SDOSConstants.NEXT_ACTION_TYPE + ")}\n"
          + SDOSConstants.UNION + SDOSConstants.NEXT_ACTION + " rdf:type :SparqlConvertAction . \n"
          + "            BIND(:SparqlConvertAction AS ?" + SDOSConstants.NEXT_ACTION_TYPE + SDOSConstants.CLOSING
          + SDOSConstants.UNION + SDOSConstants.NEXT_ACTION + " rdf:type :QueryAction . \n"
          + "            BIND(:QueryAction AS ?" + SDOSConstants.NEXT_ACTION_TYPE + SDOSConstants.CLOSING
          + SDOSConstants.HTTPACTIONMODEL_CLOSING2
          + "}\n";

  private String httpBody;
  private HashMap<String, String> httpHeaders = new HashMap<>();
  private HashMap<String, String> httpQueryParameter = new HashMap<>();
  private HttpConnectorModel hasConnector;
  private String endpoint;
  private Rdf4jClient rdf4jClient;
  private RestTemplateClient restTemplateClient;

  public HttpActionModel() {
    this.setRdf4jClient(new Rdf4jClient());
    this.setRestTemplateClient(new RestTemplateClient());
  }

  public void setRdf4jClient(Rdf4jClient rdf4jClient) {
    this.rdf4jClient = rdf4jClient;
  }

  public void setRestTemplateClient(RestTemplateClient restTemplateClient) {
    this.restTemplateClient = restTemplateClient;
  }

  @Override
  public void populate(String subjectIri, IParameterMemory iParameterMemory,
      ServiceArguments serviceArguments) {
    try {
      setSubjectIri(subjectIri);
      Gson gson = new Gson();

      JsonArray jsonArray = rdf4jClient
          .selectSparqlOfg(getSparql(subjectIri, SPARQL), iParameterMemory.getOfgModelRepo());
      setLabel(jsonArray.get(0).getAsJsonObject().get(SDOSConstants.LABEL).getAsJsonObject()
          .get(SDOSConstants.VALUE).getAsString());
      processJsonArray(jsonArray, gson);

      setHasConnector(
          (HttpConnectorModel) createPopulateConnector(iParameterMemory, serviceArguments,
              jsonArray,
              SDOSConstants.HTTP_CONNECTOR));

      if (jsonArray.get(0).getAsJsonObject().has(SDOSConstants.ENDPOINT)) {
        setEndpoint(jsonArray.get(0).getAsJsonObject().get(SDOSConstants.ENDPOINT).getAsJsonObject()
            .get(SDOSConstants.VALUE)
            .getAsString());
      }
      populateInputParameter(iParameterMemory, serviceArguments, jsonArray);
      populateOutputParameter(iParameterMemory, serviceArguments, jsonArray);
      populateNextAction(iParameterMemory, serviceArguments, jsonArray);

    } catch (IllegalStateException exception) {
      throw new IncidentException(exception, Utility.getErrorMessage(exception, this.label,
          this.subjectIri), SdipErrorCode.INVALID_JSONARRAY_RESPONSE, LOGGER,
          exception.getMessage());
    } catch (NullPointerException exception) {
      throw new IncidentException(exception, Utility.getErrorMessage(exception, this.label,
          this.subjectIri), SdipErrorCode.FAILED_TO_PARSE_JSONARRAY, LOGGER,
          exception.getMessage());
    } catch (IncidentException exception) {
      throw exception;
    } catch (Exception exception) {
      throw new IncidentException(exception, Utility.getErrorMessage(exception, this.label,
          this.subjectIri), SdipErrorCode.UNKNOWN_PARSING_ERROR, LOGGER,
          exception.getMessage());
    }
  }
  private void processJsonArray(JsonArray jsonArray, Gson gson){
    if (jsonArray.get(0).getAsJsonObject().has(SDOSConstants.HTTPBODY)) {
      setHttpBody(jsonArray.get(0).getAsJsonObject().get(SDOSConstants.HTTPBODY).getAsJsonObject()
              .get(SDOSConstants.VALUE)
              .getAsString());
    }
    if (jsonArray.get(0).getAsJsonObject().has(SDOSConstants.HTTPHEADER)) {
      sethttpHeaders(gson.fromJson(
              jsonArray.get(0).getAsJsonObject().get(SDOSConstants.HTTPHEADER).getAsJsonObject()
                      .get(SDOSConstants.VALUE)
                      .getAsString(), HashMap.class));
    }
    if (jsonArray.get(0).getAsJsonObject().has(SDOSConstants.HTTPQUERYPARAM)) {
      sethttpQueryParameter(gson.fromJson(
              jsonArray.get(0).getAsJsonObject().get(SDOSConstants.HTTPQUERYPARAM).getAsJsonObject()
                      .get(SDOSConstants.VALUE)
                      .getAsString(), HashMap.class));
    }

  }
  public HashMap<String, String> getHttpQueryParameter() {
    return httpQueryParameter;
  }

  public void sethttpQueryParameter(HashMap<String, String> httpQueryParameter) {
    this.httpQueryParameter = httpQueryParameter;
  }

  public HashMap<String, String> getHttpHeaders() {
    return httpHeaders;
  }

  public void sethttpHeaders(HashMap<String, String> httpHeaders) {
    this.httpHeaders = httpHeaders;
  }

  public String getHttpBody() {
    return Optional.ofNullable(httpBody).orElse("");
  }

  public void setHttpBody(String httpBody) {
    this.httpBody = httpBody;
  }

  public HttpConnectorModel getHasConnector() {
    return hasConnector;
  }

  public void setHasConnector(HttpConnectorModel hasConnector) {
    this.hasConnector = hasConnector;
  }

  @Override
  public void run(IParameterMemory iParameterMemory, JsonLdContextModel context,
      ServiceArguments serviceArguments) {
    try {
      String requestEndpoint = getEndpoint();
      String requestHttpBody = getHttpBody();
      HashMap requestHeaders = getHttpHeaders();
      HashMap requestQueryParams = getHttpQueryParameter();
      List list = new ArrayList();
      List<Future<String>> futures=null;
      for (IParameterModel inputParameterModel : inputParameter) {
        HashMap<String, List<String>> memoryValue = iParameterMemory
                .getValue(inputParameterModel.getSubjectIri());
       String result = extractKeys(inputParameterModel, memoryValue, requestHeaders, list);
       if(result != null){
         requestEndpoint = result;
       }

      }
      if (!list.contains(SDOSConstants.HTTPBODY) && !list.contains(SDOSConstants.HTTPQUERYPARAM)) {
        executeRequest(iParameterMemory, requestEndpoint, requestHttpBody,
                requestHeaders, requestQueryParams, Utility.getStaticHashmap(), serviceArguments);
      } else {
        for (IParameterModel inputParameterModel : inputParameter) {
          HashMap<String, List<String>> memoryValue;
          if (inputParameterModel.getKeys().contains(SDOSConstants.HTTPBODY) ||
                  inputParameterModel.getKeys().contains(SDOSConstants.HTTPQUERYPARAM)) {
            memoryValue = iParameterMemory
                    .getValue(inputParameterModel.getSubjectIri());
            Integer iterations = Utility.getActionIteration(memoryValue, inputParameterModel);
            processInputParameter(iterations, inputParameterModel, memoryValue, requestEndpoint, requestHeaders, requestHttpBody,
                    requestQueryParams, iParameterMemory, serviceArguments, futures);
          }
        }
      }
    } catch (JsonSyntaxException exception) {
      throw new IncidentException(SdipErrorCode.INVALID_JSONARRAY_RESPONSE, LOGGER,
          exception.getMessage());
    } catch (IncidentException exception) {
      throw exception;
    } catch (Exception exception) {
      throw new IncidentException(exception, SdipErrorCode.UNKNOWN_REASON_ERROR, LOGGER,
          exception.getMessage(), SdipErrorParameter.SUPPORTMAIL);
    }
  }

  private  void processInputParameter(Integer iterations, IParameterModel inputParameterModel, HashMap<String, List<String>> memoryValue,
                                      String requestEndpoint, HashMap requestHeaders, String requestHttpBody, HashMap requestQueryParams,
                                      IParameterMemory iParameterMemory, ServiceArguments serviceArguments, List<Future<String>> futures) throws InterruptedException, ExecutionException {
    if (iterations == 1) {
      HashMap<String, String> formData = new HashMap<>();
      processIteration1(inputParameterModel, memoryValue, requestEndpoint, requestHeaders, formData, requestHttpBody,
              requestQueryParams, iParameterMemory, serviceArguments);

    } else {
      int threadPoolSize = getThreadPoolSize(iterations, serviceArguments);
      ExecutorService executorService = getExecutorService(threadPoolSize);
      Set<Callable<String>> callables = getCallableSet();
      processIterations(iterations, inputParameterModel, memoryValue, requestEndpoint, requestHeaders, requestHttpBody,
              requestQueryParams, iParameterMemory, serviceArguments, callables);

      futures = executorService.invokeAll(callables);
      isThreadActive(executorService);
      List<String> apiResult = new ArrayList<>();
      for (Future<String> obj : futures) {
        apiResult.add(((Future) obj).get().toString());
      }
      HashMap<String, List<String>> outputMemoryValue = new HashMap<>();
      outputMemoryValue
              .put(outputParameter.get(0).getKeys().get(0), apiResult);
      iParameterMemory.putParameter(outputParameter.get(0).getSubjectIri(),
              outputMemoryValue);
    }
  }

  private String extractKeys(IParameterModel inputParameterModel, HashMap<String, List<String>> memoryValue,
                           HashMap requestHeaders, List list){
    String requestEndpoint = null;
    for (String key : inputParameterModel.getKeys()) {
      list.add(key);
      if (key.equals(SDOSConstants.ENDPOINT)) {
        requestEndpoint = setRequestEndpoint(memoryValue, 0);
      } else if (key.equals(SDOSConstants.HTTPHEADER)) {
        setRequestHeaders(requestHeaders, memoryValue, 0);
      } else if (getHttpQueryParameter().containsKey(key)) {
        getHttpQueryParameter().put(key, memoryValue.get(key).get(0));
      }
    }
    return requestEndpoint;
  }

  private void processIteration1(IParameterModel inputParameterModel, HashMap<String, List<String>> memoryValue,
                                 String requestEndpoint, HashMap requestHeaders, HashMap<String, String> formData,
                                 String requestHttpBody, HashMap requestQueryParams, IParameterMemory iParameterMemory,
                                 ServiceArguments serviceArguments){
    if(inputParameterModel.getKeys().contains(SDOSConstants.HTTPQUERYPARAM)){
      setRequestHttpQueryParameters(requestQueryParams, memoryValue, 0);
    }else if (this.httpHeaders.containsKey(SDOSConstants.CONTENT_TYPE) && this.httpHeaders.get(
                    SDOSConstants.CONTENT_TYPE)
            .contains(MediaType.APPLICATION_FORM_URLENCODED_VALUE)) {
      formData = setFormData(memoryValue, 0);
      requestHttpBody = "";
    } else {
      requestHttpBody = setRequestBody(memoryValue, 0);
    }
    if (requestHttpBody.isEmpty() && getHttpBody() != null) {
      requestHttpBody = getHttpBody();
    }
    executeRequest(iParameterMemory, requestEndpoint, requestHttpBody, requestHeaders,
            requestQueryParams, formData, serviceArguments);
  }

  private void processIterations(Integer iterations, IParameterModel inputParameterModel, HashMap<String, List<String>> memoryValue,
                                 String requestEndpoint, HashMap requestHeaders, String requestHttpBody, HashMap requestQueryParams,
                                 IParameterMemory iParameterMemory, ServiceArguments serviceArguments, Set<Callable<String>> callables){
    for (int i = 0; i < iterations; i++) {
      HashMap requestQueryParam = new HashMap();
      HashMap<String, String> formData = new HashMap<>();
      if(inputParameterModel.getKeys().contains(SDOSConstants.HTTPQUERYPARAM)){
        HashMap<String, List<String>> Value =iParameterMemory
                .getValue(inputParameterModel.getSubjectIri());
        setRequestHttpQueryParameters(requestQueryParam, Value, i);
      }else if (this.httpHeaders.containsKey(SDOSConstants.CONTENT_TYPE) && this.httpHeaders
              .get(
                      SDOSConstants.CONTENT_TYPE)
              .contains(MediaType.APPLICATION_FORM_URLENCODED_VALUE)) {
        formData = setFormData(memoryValue, i);
        requestHttpBody = "";

      } else {
        requestHttpBody = setRequestBody(memoryValue, i);
      }
      if (requestHttpBody.isEmpty() && getHttpBody() != null) {
        requestHttpBody = getHttpBody();
      }

      callables.add(ActionParallelExecutor.getParallelExecutorInstance(
              iParameterMemory, requestEndpoint, requestHttpBody,
              requestHeaders, requestQueryParam, formData, serviceArguments, this)
      );
    }
  }
  private void executeRequest(IParameterMemory iParameterMemory, String requestEndpoint, String requestHttpBody,
                              HashMap requestHeaders, HashMap requestQueryParams, HashMap formData, ServiceArguments serviceArguments) {
    try {
      String result = prepareRequest(iParameterMemory, requestEndpoint, requestHttpBody,
          requestHeaders, requestQueryParams, formData, serviceArguments, this);
      HashMap<String, List<String>> outputMemoryValue = new HashMap<>();
      if(result!=null && !result.isEmpty()) {
        outputMemoryValue
                .put(outputParameter.get(0).getKeys().get(0), Collections.singletonList(result));
        iParameterMemory.putParameter(outputParameter.get(0).getSubjectIri(), outputMemoryValue);
      }
    } catch (IOException exception) {
      throw new IncidentException(exception, SdipErrorCode.IO_ERROR, LOGGER, exception.getMessage(),
          SdipErrorParameter.SUPPORTMAIL);
    }
  }

  private void addHttpQueryParameter(String key, String value) {
    this.httpQueryParameter.put(key, value);
  }

  private void addHttpHeaders(String key, String value) {
    this.httpHeaders.put(key, value);
  }

  public String getEndpoint() {
    return Optional.ofNullable(endpoint).orElse("");
  }

  public void setEndpoint(String endpoint) {
    this.endpoint = endpoint;
  }

  private void setRequestHttpQueryParameters(HashMap requestQueryParams,
      HashMap<String, List<String>> memoryValue, Integer id) {
    HashMap<String, String> inputqueries = new Gson()
        .fromJson(memoryValue.get(SDOSConstants.HTTPQUERYPARAM).get(id), LinkedHashMap.class);
    for (Map.Entry<String, String> entry : inputqueries.entrySet()) {
      requestQueryParams.put(entry.getKey(), entry.getValue().replace(" ", ""));
    }
  }

  private String setRequestBody(
      HashMap<String, List<String>> memoryValue,
      Integer id) {
    return memoryValue.get(SDOSConstants.HTTPBODY).get(id);
  }

  private HashMap setFormData(HashMap<String, List<String>> memoryValue, Integer id) {
    Gson gson = new Gson();
    try {
      return gson
          .fromJson(gson.toJson(memoryValue.get(SDOSConstants.HTTPBODY).get(id)),
              HashMap.class);
    } catch (Exception e) {
      return gson
          .fromJson(memoryValue.get(SDOSConstants.HTTPBODY).get(id),
              HashMap.class);
    }

  }

  private void setRequestHeaders(HashMap requestHeaders, HashMap<String, List<String>> memoryValue,
      Integer id) {
    HashMap<String, String> inputHeaders = new Gson()
        .fromJson(memoryValue.get(SDOSConstants.HTTPHEADER).get(id), HashMap.class);
    for (Map.Entry<String, String> entry : inputHeaders.entrySet()) {
      requestHeaders.put(entry.getKey(), entry.getValue());
    }
  }

  private String setRequestEndpoint(HashMap<String, List<String>> memoryValue, Integer id) {
    if (getEndpoint() != null) {
      return getEndpoint() + memoryValue.get(SDOSConstants.ENDPOINT).get(id);
    } else {
      return memoryValue.get(SDOSConstants.ENDPOINT).get(id);
    }
  }

  @Override
  public String prepareRequest(IParameterMemory iParameterMemory, String requestEndpoint, String requestHttpBody,
                               HashMap requestHeaders, HashMap requestQueryParams, HashMap formData,
                               ServiceArguments serviceArguments, IParallelActionModel iParallelActionModel) throws IOException {
    HttpConnectorModel httpConnectorModel = ((HttpActionModel) iParallelActionModel)
        .getHasConnector();
    String result = "";
    CredentialsProvider credentialsProvider = null;

    if (httpConnectorModel.getHasAuthenticationMethod() != null &&
        httpConnectorModel.getHasAuthenticationMethod() instanceof HttpBearerTokenAuthModel) {
      HttpBearerTokenAuthModel authModel = (HttpBearerTokenAuthModel) httpConnectorModel
          .getHasAuthenticationMethod();
      authModel.run(iParameterMemory);
      requestHeaders = authModel.addCredentials(requestHeaders, iParameterMemory);

//      credentialsProvider = authModel.getCredentials();
    } else if (httpConnectorModel.getHasAuthenticationMethod() != null &&
        httpConnectorModel.getHasAuthenticationMethod() instanceof HttpBasicAuthModel) {
      HttpBasicAuthModel authModel = (HttpBasicAuthModel) httpConnectorModel
          .getHasAuthenticationMethod();
      authModel.run(iParameterMemory);
      credentialsProvider = authModel.getCredentials();
      requestHeaders = authModel.addCredentials(requestHeaders, iParameterMemory);
    }

    if (!requestHttpBody.isEmpty() || !formData.isEmpty()) {
      result = restTemplateClient
          .executeHttpPost(httpConnectorModel.getBaseUrl() + requestEndpoint,
              requestQueryParams,
              requestHeaders,
              requestHttpBody,
              credentialsProvider,
              formData);
    } else {
      result = restTemplateClient
          .executeHttpGET(httpConnectorModel.getBaseUrl() + requestEndpoint,
              requestQueryParams,
              requestHeaders, credentialsProvider);
    }
    return result;
  }
}