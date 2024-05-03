package com.scania.sdos.testOrchestration.testOModel;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anySet;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.scania.sdip.exceptions.IncidentException;
import com.scania.sdip.exceptions.SdipErrorCode;
import com.scania.sdip.exceptions.SdipErrorParameter;
import com.scania.sdos.model.ServiceArguments;
import com.scania.sdos.orchestration.OfgModelRepo;
import com.scania.sdos.orchestration.ParameterMemory;
import com.scania.sdos.orchestration.Rdf4jClient;
import com.scania.sdos.orchestration.RestTemplateClient;
import com.scania.sdos.orchestration.factory.ActionModelFactory;
import com.scania.sdos.orchestration.factory.ConnectorModelFactory;
import com.scania.sdos.orchestration.factory.ParameterModelFactory;
import com.scania.sdos.orchestration.interfaces.IActionModel;
import com.scania.sdos.orchestration.interfaces.IParallelActionModel;
import com.scania.sdos.orchestration.interfaces.IParameterMemory;
import com.scania.sdos.orchestration.interfaces.IParameterModel;
import com.scania.sdos.orchestration.model.HttpActionModel;
import com.scania.sdos.orchestration.model.HttpConnectorModel;
import com.scania.sdos.orchestration.model.JsonLdContextModel;
import com.scania.sdos.orchestration.model.SoapActionModel;
import com.scania.sdos.orchestration.model.StandardParameterModel;
import com.scania.sdos.orchestration.model.HttpBasicAuthModel;
import com.scania.sdos.testUtils.UnitTestHelper;
import com.scania.sdos.orchestration.model.HttpParameterModel;
import com.scania.sdos.utils.SDOSConstants;
import com.scania.sdos.utils.Utility;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import com.scania.sdos.testUtils.UnitTestConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;


@ExtendWith(MockitoExtension.class)
class HttpActionModelTest {

  private static final Logger LOGGER = LogManager.getLogger(SoapActionModel.class);
  private static MockedStatic utilityMock;
  private static MockedStatic actionModelFactory;
  private static MockedStatic connectorModelFactory;
  private static MockedStatic parameterModelFactory;
  private static MockedStatic parameterMemoryFactory;


  @Mock
  private HttpConnectorModel connectorModel;
  @Mock
  private IActionModel iActionModel;

  @Mock
  private ParameterMemory daParams;

  @Mock
  private ServiceArguments serviceArguments;

  @Mock
  private IParameterModel inputParameter;

  @Mock
  private IParameterModel outputParameter;

  @Mock
  private Rdf4jClient rdf4jClient;
  @Mock
  private RestTemplateClient restTemplateClient;

  @Spy
  private HttpActionModel httpActionModel;
  @Mock
  private HashMap hashMapMock;
  @Mock
  private HttpBasicAuthModel httpBasicAuthModel;
  private static String SPARQL_QUERY = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
      + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
      + "PREFIX : <https://kg.scania.com/it/iris_orchestration/> \n"
      + "SELECT ?label ?httpBody ?httpHeader ?httpQueryParameter ?hasConnector ?endpoint ?inputparameter ?outputparameter ?inputparametertype ?outputparametertype ?nextaction ?nextactiontype\n"
      + " WHERE \n"
      + "{  \n"
      + "BIND(<aSubjectIri> AS ?subject)\n"
      + "    ?subject  rdf:type :HTTPAction ;\n"
      + "        rdfs:label ?label ;\n"
      + "        :hasConnector ?hasConnector .\n"
      + "    OPTIONAL{?subject :httpBody ?httpBody.}\n"
      + "    OPTIONAL{?subject :httpHeader ?httpHeader.}\n"
      + "    OPTIONAL{?subject :httpQueryParameter ?httpQueryParameter.}\n"
      + "    OPTIONAL{?subject :endpoint ?endpoint .}\n"
      + "    OPTIONAL{?subject :inputParameter  ?inputparameter . \n"
      + "        {?inputparameter rdf:type :Parameter .\n"
      + "            BIND(:Parameter AS ?inputparametertype)}\n"
      + "        UNION { ?inputparameter rdf:type :HTTPParameter .\n"
      + "            BIND(:HTTPParameter AS ?inputparametertype)}\n"
      + "        UNION { ?inputparameter rdf:type :StandardParameter .\n"
      + "            BIND(:StandardParameter AS ?inputparametertype)}\n"
      + "    }\n"
      + "    OPTIONAL{?subject :outputParameter  ?outputparameter .\n"
      + "        {?outputparameter rdf:type :Parameter .\n"
          + "            BIND(:Parameter AS ?outputparametertype)}\n"
          + "        UNION { ?outputparameter rdf:type :StandardParameter .\n"
          + "            BIND(:StandardParameter AS ?outputparametertype)}\n"
          + "        UNION { ?outputparameter rdf:type :HTTPParameter .\n"
          + "            BIND(:HTTPParameter AS ?outputparametertype)}\n"
          + "    }\n"
          + "    OPTIONAL{?subject :hasNextAction ?nextaction .\n"
          + "        {?nextaction rdf:type :Action .} \n"
          + "        UNION { ?nextaction rdf:type :HTTPAction .\n"
          + "            BIND(:HTTPAction AS ?nextactiontype)} \n"
          + "        UNION { ?nextaction rdf:type :SOAPAction .\n"
          + "            BIND(:SOAPAction AS ?nextactiontype)} \n"
          + "        UNION { ?nextaction rdf:type :ScriptAction .\n"
          + "            BIND(:ScriptAction AS ?nextactiontype)} \n"
          + "        UNION { ?nextaction rdf:type :ResultAction . \n"
          + "            BIND(:ResultAction AS ?nextactiontype)} \n"
          + "        UNION { ?nextaction rdf:type :VirtualGraphAction .\n"
          + "            BIND(:VirtualGraphAction AS ?nextactiontype)}\n"
          + "        UNION { ?nextaction rdf:type :SparqlConvertAction . \n"
          + "            BIND(:SparqlConvertAction AS ?nextactiontype)} \n"
          + "        UNION { ?nextaction rdf:type :QueryAction . \n"
          + "            BIND(:QueryAction AS ?nextactiontype)} \n"
          + "    }\n"
          + "}\n";

  @Mock
  private JsonLdContextModel jsonLdContextModel;
  @Mock
  private OfgModelRepo ofgModelRepo;
  @BeforeAll
  static void beforeAll() {
    utilityMock = mockStatic(Utility.class);
    actionModelFactory = mockStatic(ActionModelFactory.class);
    connectorModelFactory = mockStatic(ConnectorModelFactory.class);
    parameterModelFactory = mockStatic(ParameterModelFactory.class);
    parameterMemoryFactory = mockStatic(IParameterMemory.class);
  }


  @AfterAll
  static void afterAll() {
    utilityMock.close();
    actionModelFactory.close();
    connectorModelFactory.close();
    parameterModelFactory.close();
    parameterMemoryFactory.close();
  }


  @BeforeEach
  void setUp() {
    reset(connectorModel);
    reset(httpActionModel);
    reset(rdf4jClient);
    connectorModelFactory.reset();
    actionModelFactory.reset();
    utilityMock.reset();
    parameterModelFactory.reset();
    parameterMemoryFactory.reset();
    reset(daParams);
    reset(serviceArguments);
  }

  @Test
  void populate_ok() {
    httpActionModel.setRdf4jClient(rdf4jClient);
    // test successful case
    String response = UnitTestHelper.readJsonFiles(UnitTestConstants.HTTPACTIONMODELTESTDATA);
    JsonArray daAnswer = JsonParser.parseString(response).getAsJsonArray();
    doReturn(daAnswer).when(rdf4jClient).selectSparqlOfg(any(), any());
    actionModelFactory.when(() ->
        ActionModelFactory.getAction(any(), any())).thenReturn(iActionModel);
    connectorModelFactory.when(() ->
        ConnectorModelFactory.getConnector(any())).thenReturn(connectorModel);
    parameterModelFactory.when(() ->
        ParameterModelFactory.getParameter(any())).thenReturn(inputParameter);
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();
    // run it!
    httpActionModel.populate("aSubjectIri", daParams, serviceArguments);
    verify(rdf4jClient, times(1)).
        selectSparqlOfg(eq(SPARQL_QUERY), any());
    assertEquals("aSubjectIri", httpActionModel.getSubjectIri());
    assertEquals("/an/endpoint/", httpActionModel.getEndpoint());
    Assertions.assertEquals(iActionModel, httpActionModel.getNextAction());
    assertEquals(connectorModel, httpActionModel.getHasConnector());
    assertEquals(null, httpActionModel.getHttpHeaders());
    assertEquals("", httpActionModel.getHttpBody());
    verify(iActionModel, times(1))
            .populate("next/action/IRI", daParams, serviceArguments);
    verify(connectorModel, times(1))
            .populate("aConnectorURI", daParams, serviceArguments);
    verify(inputParameter, times(1))
        .populate("anInputParameter", daParams, serviceArguments);
    actionModelFactory.verify(() -> ActionModelFactory.getAction("aNextActionType", daParams),
            times(1)
    );

    connectorModelFactory.verify(() -> ConnectorModelFactory.getConnector("HTTPConnector"),
            times(1)
    );

    parameterModelFactory.verify(() -> ParameterModelFactory.getParameter("anInputParameterType"),
            times(1)
    );
  }

  @Test
  void populate_no_inputParameter() {
    httpActionModel.setRdf4jClient(rdf4jClient);
    // test successful case
    String response = UnitTestHelper.readJsonFiles(UnitTestConstants.HTTPACTIONMODELTESTDATA3);
    JsonArray daAnswer = JsonParser.parseString(response).getAsJsonArray();
    doReturn(daAnswer).when(rdf4jClient).selectSparqlOfg(any(), any());
    actionModelFactory.when(() ->
        ActionModelFactory.getAction(any(), any())).thenReturn(iActionModel);
    connectorModelFactory.when(() ->
        ConnectorModelFactory.getConnector(any())).thenReturn(connectorModel);
    parameterModelFactory.when(() ->
        ParameterModelFactory.getParameter(any())).thenReturn(inputParameter);
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();
    // run it!
    httpActionModel.populate("aSubjectIri", daParams, serviceArguments);

    assertEquals("aSubjectIri", httpActionModel.getSubjectIri());
    assertEquals("/an/endpoint/", httpActionModel.getEndpoint());
    Assertions.assertEquals(iActionModel, httpActionModel.getNextAction());
    assertEquals(connectorModel, httpActionModel.getHasConnector());
    assertEquals(null, httpActionModel.getHttpHeaders());
    assertEquals("", httpActionModel.getHttpBody());
    verify(iActionModel, times(1))
        .populate("next/action/IRI", daParams, serviceArguments);
    verify(connectorModel, times(1))
        .populate("aConnectorURI", daParams, serviceArguments);
    actionModelFactory.verify(() -> ActionModelFactory.getAction("aNextActionType", daParams),
            times(1)
    );

    connectorModelFactory.verify(() -> ConnectorModelFactory.getConnector("HTTPConnector"),
            times(1)
    );
    parameterModelFactory.verify(() -> ParameterModelFactory.getParameter("anInputParameterType"),
            times(0)
    );

  }

  @Test
  void populate_emptyResponse() {
    httpActionModel.setRdf4jClient(rdf4jClient);
    JsonArray daAnswer = JsonParser.parseString("[{}]").getAsJsonArray();
    doReturn(daAnswer).when(rdf4jClient).selectSparqlOfg(any(), any());
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      httpActionModel.populate("1", daParams, serviceArguments);
    });

    assertEquals(SdipErrorCode.FAILED_TO_PARSE_JSONARRAY.getSdipErrorCode(),
        Integer
            .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                .getSdipErrorCodes().get(0)
                .replace("SDIP_", "")));
  }

  @Test
  void populate_actionModelFactoryReturnsNull() {
    httpActionModel.setRdf4jClient(rdf4jClient);
    String response = UnitTestHelper.readJsonFiles(UnitTestConstants.HTTPACTIONMODELTESTDATA);
    JsonArray daAnswer = JsonParser.parseString(response).getAsJsonArray();
    doReturn(daAnswer).when(rdf4jClient).selectSparqlOfg(any(), any());
    actionModelFactory.when(() ->
        ActionModelFactory.getAction(any(), any())).thenReturn(null);

    connectorModelFactory.when(() ->
        ConnectorModelFactory.getConnector(any())).thenReturn(connectorModel);
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      httpActionModel.populate("1", daParams, serviceArguments);
    });

    assertEquals(SdipErrorCode.FAILED_TO_PARSE_JSONARRAY.getSdipErrorCode(),
        Integer
            .parseInt(((IncidentException) throwable).
                createHttpErrorResponse().
                getBody().
                getSdipErrorCodes().
                get(0).
                replace("SDIP_", "")));
  }


  @Test
  void populate_connectorModelFactoryReturnsNull() {
    httpActionModel.setRdf4jClient(rdf4jClient);
    String response = UnitTestHelper.readJsonFiles(UnitTestConstants.HTTPACTIONMODELTESTDATA);
    JsonArray daAnswer = JsonParser.parseString(response).getAsJsonArray();
    doReturn(daAnswer).when(rdf4jClient).selectSparqlOfg(any(), any());
    connectorModelFactory.when(() ->
        ConnectorModelFactory.getConnector(any())).thenReturn(null);
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      httpActionModel.populate("1", daParams, serviceArguments);
    });

    assertEquals(SdipErrorCode.FAILED_TO_PARSE_JSONARRAY.getSdipErrorCode(),
        Integer
            .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                .getSdipErrorCodes().get(0)
                .replace("SDIP_", "")));
  }


  @Test
  void populate_invalidJsonArrayResponse() {
    httpActionModel.setRdf4jClient(rdf4jClient);
    doThrow(IllegalStateException.class).when(rdf4jClient)
        .selectSparqlOfg(any(), any());
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      httpActionModel.populate("1", daParams, serviceArguments);
    });

    assertEquals(SdipErrorCode.INVALID_JSONARRAY_RESPONSE.getSdipErrorCode(),
        Integer
            .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                .getSdipErrorCodes().get(0)
                .replace("SDIP_", "")));

  }


  @Test
  void populate_invalidPropertyJsonArray() {
    httpActionModel.setRdf4jClient(rdf4jClient);
    JsonArray daAnswer = JsonParser.parseString("[{\"key\":\"value\"}]").getAsJsonArray();
    doReturn(daAnswer).when(rdf4jClient).selectSparqlOfg(any(), any());
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      httpActionModel.populate("1", daParams, serviceArguments);
    });

    assertEquals(SdipErrorCode.FAILED_TO_PARSE_JSONARRAY.getSdipErrorCode(),
        Integer
            .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                .getSdipErrorCodes().get(0)
                .replace("SDIP_", "")));
  }


  @Test
  void populate_catchIncidentException() {
    httpActionModel.setRdf4jClient(rdf4jClient);
    doThrow(new IncidentException(SdipErrorCode.SPARQL_ENDPOINT_EMPTY, LOGGER,
        SdipErrorParameter.SUPPORTMAIL)).
        when(rdf4jClient).selectSparqlOfg(any(), any());
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      httpActionModel.populate("1", daParams, serviceArguments);
    });

    assertEquals(SdipErrorCode.SPARQL_ENDPOINT_EMPTY.getSdipErrorCode(),
        Integer
            .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                .getSdipErrorCodes().get(0)
                .replace("SDIP_", "")));
  }


  @Test
  void run_catchIncidentException() {

    List<String> inputKeys = new ArrayList<>();
    inputKeys.add(SDOSConstants.HTTPBODY);

    HashMap<String, List<String>> inputParam = new HashMap<>();
    inputParam.put(SDOSConstants.HTTPBODY, Collections.singletonList("newBODY"));

    httpActionModel.setInputParameter(inputParameter);
    httpActionModel.setHasConnector(connectorModel);
    utilityMock.when(() -> Utility.getActionIteration(any(), any())).thenReturn(1);
    doReturn("https://INPUT").when(inputParameter).getSubjectIri();
    doReturn(inputParam).when(daParams).getValue("https://INPUT");
    doReturn(inputKeys).when(inputParameter).getKeys();
    doReturn(httpBasicAuthModel).when(connectorModel).getHasAuthenticationMethod();
    doThrow(new IncidentException(SdipErrorCode.HTTP_RESPONSE_NOK, LOGGER,
        SdipErrorParameter.SUPPORTMAIL, "Test_Message")).
        when(httpBasicAuthModel).run(daParams);

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      httpActionModel.run(daParams, jsonLdContextModel, serviceArguments);
    });

    assertEquals(SdipErrorCode.HTTP_RESPONSE_NOK.getSdipErrorCode(),
        Integer
            .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                .getSdipErrorCodes().get(0)
                .replace("SDIP_", "")));
  }


  @Test
  void run_returnsNull() {
    List<String> inputKeys = new ArrayList<>();
    inputKeys.add(SDOSConstants.ENDPOINT);
    HashMap<String, List<String>> inputParam = new HashMap<>();
    inputParam.put(SDOSConstants.ENDPOINT, Collections.singletonList("/test"));

    httpActionModel.setInputParameter(inputParameter);
    httpActionModel.setHasConnector(connectorModel);
    utilityMock.when(() -> Utility.getActionIteration(any(), any())).thenReturn(1);
    doReturn(null).when(httpActionModel).getHasConnector();
    doReturn("https://INPUT").when(inputParameter).getSubjectIri();
    doReturn(inputParam).when(daParams).getValue("https://INPUT");
    doReturn(inputKeys).when(inputParameter).getKeys();

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      httpActionModel.run(daParams, jsonLdContextModel, serviceArguments);
    });
    assertEquals(SdipErrorCode.UNKNOWN_REASON_ERROR.getSdipErrorCode(),
        Integer
            .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                .getSdipErrorCodes().get(0)
                .replace("SDIP_", "")));
  }


  @Test
  void test_run() {
    ParameterMemory parameterMemory = new ParameterMemory();
    HashMap<String, List<String>> iterativeInput = new HashMap<>();
    iterativeInput.put(SDOSConstants.HTTPBODY, Collections.singletonList("test"));
    parameterMemory.putParameter("input", iterativeInput);

    HashMap<String, List<String>> orchestrationValue = new HashMap<>();
    orchestrationValue.put(SDOSConstants.ENDPOINT, Collections.singletonList("aEndpoint"));
    parameterMemory.putParameter(SDOSConstants.OFG_ENDPOINT, orchestrationValue);

    StandardParameterModel outputParameterModel = new StandardParameterModel();
    outputParameterModel.setSubjectIri("https://kg.scania.com/it/iris_orchestration/SVAR");
    outputParameterModel.setParamName("svar");
    outputParameterModel.setRdf4jClient(rdf4jClient);
    outputParameterModel.setLabel("svare");

    StringBuilder stringBuilder = new StringBuilder();
    httpActionModel.setInputParameter(inputParameter);
    httpActionModel.setOutputParameter(outputParameterModel);
    httpActionModel.setHasConnector(connectorModel);
    httpActionModel.setRestTemplateClient(restTemplateClient);
    httpActionModel.setRdf4jClient(rdf4jClient);
    httpActionModel.sethttpHeaders(hashMapMock);
    utilityMock.when(() -> Utility.getActionIteration(any(), any())).thenReturn(1);
    doReturn(Collections.singletonList(SDOSConstants.HTTPBODY)).when(inputParameter).getKeys();
    doReturn("input").when(inputParameter).getSubjectIri();

    doReturn("result").when(restTemplateClient).executeHttpPost(ArgumentMatchers.anyString(),
        ArgumentMatchers.any(),
        ArgumentMatchers.any(),
        ArgumentMatchers.anyString(),
        ArgumentMatchers.any(),
        ArgumentMatchers.any());

    try {
      httpActionModel.run(parameterMemory, jsonLdContextModel, serviceArguments);
      assertEquals(1, parameterMemory
          .getValue("https://kg.scania.com/it/iris_orchestration/SVAR")
          .get("svar").size());
      assertEquals("result", parameterMemory
          .getValue("https://kg.scania.com/it/iris_orchestration/SVAR")
          .get("svar").get(0));
    } catch (Exception e) {
      Assertions.fail(e.getMessage());
    }
  }

  @Test
  void test_run_executorService() throws InterruptedException {
    ParameterMemory parameterMemory = new ParameterMemory();
    HttpParameterModel httpParameterModel = new HttpParameterModel();
    httpParameterModel.setRdf4jClient(rdf4jClient);
    httpParameterModel.setSubjectIri("https://kg.scania.com/it/iris_orchestration/subIri_1");
    httpParameterModel.setBody("");

    List resList = new ArrayList();
    for (int i = 0; i < 3; i++) {
      resList.add("testHttpBody_" + i);
    }
    HashMap<String, List<String>> hashMap = new HashMap<>();
    hashMap.put("endpoint", Collections.singletonList("testEndPoint"));
    hashMap.put("httpBody", resList);
    parameterMemory.putParameter("https://kg.scania.com/it/iris_orchestration/subIri_1", hashMap);

    HashMap<String, List<String>> orchestrationValue = new HashMap<>();
    orchestrationValue.put(SDOSConstants.ENDPOINT, Collections.singletonList("aEndpoint"));
    parameterMemory.putParameter(SDOSConstants.OFG_ENDPOINT, orchestrationValue);

    StandardParameterModel outputParameterModel = new StandardParameterModel();
    outputParameterModel.setSubjectIri("https://kg.scania.com/it/iris_orchestration/output_iri");
    outputParameterModel.setParamName("response");
    outputParameterModel.setRdf4jClient(rdf4jClient);
    outputParameterModel.setLabel("svare");

    httpActionModel.setInputParameter(httpParameterModel);
    httpActionModel.setOutputParameter(outputParameterModel);
    httpActionModel.setHasConnector(connectorModel);
    httpActionModel.setRestTemplateClient(restTemplateClient);
    httpActionModel.setRdf4jClient(rdf4jClient);
    httpActionModel.sethttpHeaders(hashMapMock);
    httpActionModel.setHttpBody("testHttpbody");
    utilityMock.when(() -> Utility.getActionIteration(any(), any())).thenReturn(3);

    ExecutorService executorService = Mockito.mock(ExecutorService.class);
    IParallelActionModel iParallelActionModel = Mockito.mock(IParallelActionModel.class);
    Set set = Mockito.mock(Set.class);
    List<Future<String>> futures = new ArrayList<>();
    for (int i = 0; i < 3; i++) {
      futures.add(CompletableFuture.completedFuture("apiResponse_" + i));
    }
    doReturn(futures).when(executorService).invokeAll(anySet());
    doNothing().when(httpActionModel).isThreadActive(any());
    doReturn(set).when(httpActionModel).getCallableSet();
    doReturn(executorService).when(httpActionModel).getExecutorService(anyInt());

    try {
      httpActionModel.run(parameterMemory, jsonLdContextModel, serviceArguments);
      List iParamList = ((ArrayList) ((HashMap) parameterMemory.
          getValue("https://kg.scania.com/it/iris_orchestration/output_iri")).
          get("response"));
      assertEquals(3, iParamList.size());
      assertEquals(true, iParamList.contains("apiResponse_2"));
      verify(executorService, times(1)).
          invokeAll(any());
      verify(httpActionModel, times(1)).
          isThreadActive(executorService);
      verify(httpActionModel, times(1)).
          getExecutorService(3);
      verify(set, times(3)).
          add(any());
    } catch (Exception e) {
      Assertions.fail(e.getMessage());
    }
  }

  @Test
  void run_without_executorService() throws IOException {

    ParameterMemory parameterMemory = new ParameterMemory();
    HttpParameterModel httpParameterModel = new HttpParameterModel();
    httpParameterModel.setRdf4jClient(rdf4jClient);
    httpParameterModel.setSubjectIri("https://kg.scania.com/it/iris_orchestration/subIri_1");
    httpParameterModel.setBody("");

    HashMap<String, List<String>> hashMap = new HashMap<>();
    hashMap.put("endpoint", Collections.singletonList("testEndPoint"));
    hashMap.put("httpBody", Collections.singletonList("testHttpBody_0"));
    parameterMemory.putParameter("https://kg.scania.com/it/iris_orchestration/subIri_1", hashMap);

    HashMap<String, List<String>> orchestrationValue = new HashMap<>();
    orchestrationValue.put(SDOSConstants.ENDPOINT, Collections.singletonList("aEndpoint"));
    parameterMemory.putParameter(SDOSConstants.OFG_ENDPOINT, orchestrationValue);

    StandardParameterModel outputParameterModel = new StandardParameterModel();
    outputParameterModel.setSubjectIri("https://kg.scania.com/it/iris_orchestration/output_iri");
    outputParameterModel.setParamName("response");
    outputParameterModel.setRdf4jClient(rdf4jClient);
    outputParameterModel.setLabel("label");

    httpActionModel.setInputParameter(httpParameterModel);
    httpActionModel.setOutputParameter(outputParameterModel);
    httpActionModel.setHasConnector(connectorModel);
    httpActionModel.setRestTemplateClient(restTemplateClient);
    httpActionModel.setRdf4jClient(rdf4jClient);
    httpActionModel.sethttpHeaders(hashMapMock);
    utilityMock.when(() -> Utility.getActionIteration(any(), any())).thenReturn(1);

    ExecutorService executorService = Mockito.mock(ExecutorService.class);
    doReturn("apiResponse_2").when(httpActionModel).prepareRequest(any(), any(), any(),
        any(), any(), any(), any(), any());

    try {
      httpActionModel.run(parameterMemory, jsonLdContextModel, serviceArguments);
      List iParamList = ((List) ((HashMap) parameterMemory.
          getValue("https://kg.scania.com/it/iris_orchestration/output_iri")).
          get("response"));
      assertEquals(1, iParamList.size());
      verify(httpActionModel, times(1)).
          prepareRequest(any(), any(), any(), any(), any(), any(), any(), any());
      assertEquals(true, iParamList.contains("apiResponse_2"));
      verify(executorService, times(0)).
          invokeAll(any());
      verify(httpActionModel, times(0)).
          isThreadActive(executorService);
      verify(httpActionModel, times(0)).
          getExecutorService(3);
    } catch (Exception e) {
      Assertions.fail(e.getMessage());
    }
  }

  @Test
  void test_run__Existing_values() {
    List<String> inputKeys = new ArrayList<>();
    inputKeys.add(SDOSConstants.HTTPHEADER);
    inputKeys.add(SDOSConstants.HTTPQUERYPARAM);
    inputKeys.add(SDOSConstants.ENDPOINT);
    inputKeys.add(SDOSConstants.HTTPBODY);

    HashMap<String, String> httpQueryParameter_existing = new HashMap<>();
    httpQueryParameter_existing.put("param", "value");

    HashMap<String, String> headers_existing = new HashMap<>();
    headers_existing.put("header", "headervalue");

    HashMap<String, String> httpQueryParameter_new = new HashMap<>();
    httpQueryParameter_new.put("param1", "value1");

    HashMap<String, String> headers_new = new HashMap<>();
    headers_new.put(SDOSConstants.CONTENT_TYPE, "headervalue1");

    HashMap<String, List<String>> inputParam = new HashMap<>();
    inputParam.put(SDOSConstants.ENDPOINT, Collections.singletonList("/test"));
    inputParam.put(SDOSConstants.HTTPBODY, Collections.singletonList("newBODY"));
    inputParam.put(SDOSConstants.HTTPHEADER,
        Collections.singletonList(new Gson().toJson(headers_new)));
    inputParam.put(SDOSConstants.HTTPQUERYPARAM,
        Collections.singletonList(new Gson().toJson(httpQueryParameter_new)));

    StringBuilder stringBuilder = new StringBuilder();
    httpActionModel.setInputParameter(inputParameter);
    httpActionModel.setOutputParameter(outputParameter);
    httpActionModel.setHasConnector(connectorModel);
    httpActionModel.setRestTemplateClient(restTemplateClient);
    httpActionModel.setRdf4jClient(rdf4jClient);
    httpActionModel.setEndpoint("/result");
    httpActionModel.setHttpBody("body");
    httpActionModel.sethttpHeaders(headers_existing);
    httpActionModel.sethttpQueryParameter(httpQueryParameter_existing);
    utilityMock.when(() -> Utility.getActionIteration(any(), any())).thenReturn(1);
    doReturn("https://INPUT").when(inputParameter).getSubjectIri();
    doReturn(inputParam).when(daParams).getValue("https://INPUT");
    doReturn(inputKeys).when(inputParameter).getKeys();
    doReturn("http://endpoint").when(connectorModel).getBaseUrl();
    doReturn("result").when(restTemplateClient).executeHttpPost(ArgumentMatchers.anyString(),
        ArgumentMatchers.any(),
        ArgumentMatchers.any(),
        ArgumentMatchers.anyString(),
        ArgumentMatchers.any(),
        ArgumentMatchers.any());
    doReturn(Collections.singletonList("outPutKey")).when(outputParameter).getKeys();
    doReturn("https://OUTPUT").when(outputParameter).getSubjectIri();

    try {
      httpActionModel.run(daParams, jsonLdContextModel, serviceArguments);
      assertEquals("body", httpActionModel.getHttpBody());
      assertEquals("/result", httpActionModel.getEndpoint());
      assertTrue(httpActionModel.getHttpHeaders().containsKey("header"));
      assertTrue(httpActionModel.getHttpHeaders().containsKey(SDOSConstants.CONTENT_TYPE));
      assertTrue(httpActionModel.getHttpQueryParameter().containsKey("param"));
      assertTrue(httpActionModel.getHttpQueryParameter().containsKey("param1"));
    } catch (Exception e) {
      Assertions.fail(e.getMessage());
    }
  }

  @Test
  void test_run_No_Existing_values() {
    List<String> inputKeys = new ArrayList<>();
    inputKeys.add(SDOSConstants.HTTPHEADER);
    inputKeys.add(SDOSConstants.HTTPQUERYPARAM);
    inputKeys.add(SDOSConstants.ENDPOINT);

    HashMap<String, String> httpQueryParameter_new = new HashMap<>();
    httpQueryParameter_new.put("param1", "value1");

    HashMap<String, String> headers_new = new HashMap<>();
    headers_new.put(SDOSConstants.CONTENT_TYPE, "aContentType");

    HashMap<String, List<String>> inputParam = new HashMap<>();
    inputParam.put(SDOSConstants.ENDPOINT, Collections.singletonList("/test"));
    inputParam.put(SDOSConstants.HTTPHEADER,
        Collections.singletonList(new Gson().toJson(headers_new)));
    inputParam.put(SDOSConstants.HTTPQUERYPARAM,
        Collections.singletonList(new Gson().toJson(httpQueryParameter_new)));

    StringBuilder stringBuilder = new StringBuilder();
    httpActionModel.setInputParameter(inputParameter);
    httpActionModel.setOutputParameter(outputParameter);
    httpActionModel.setHasConnector(connectorModel);
    httpActionModel.setRestTemplateClient(restTemplateClient);
    httpActionModel.setRdf4jClient(rdf4jClient);
    utilityMock.when(() -> Utility.getActionIteration(any(), any())).thenReturn(1);
    doReturn("https://INPUT").when(inputParameter).getSubjectIri();
    doReturn(inputParam).when(daParams).getValue("https://INPUT");
    doReturn(inputKeys).when(inputParameter).getKeys();
    doReturn("http://endpoint").when(connectorModel).getBaseUrl();
    doReturn("result").when(restTemplateClient).executeHttpGET(any(), any(), any(), any());
    doReturn(Collections.singletonList("outPutKey")).when(outputParameter).getKeys();
    doReturn("https://OUTPUT").when(outputParameter).getSubjectIri();

    try {
      httpActionModel.run(daParams, jsonLdContextModel, serviceArguments);
      assertEquals("", httpActionModel.getHttpBody());
      assertEquals("", httpActionModel.getEndpoint());
      assertTrue(httpActionModel.getHttpHeaders().containsKey(SDOSConstants.CONTENT_TYPE));
      assertTrue(httpActionModel.getHttpQueryParameter().containsKey("param1"));
    } catch (Exception e) {
      Assertions.fail(e.getMessage());
    }
  }

  @Test
  void test_run_with_urlEncoded() {
    List<String> inputKeys = new ArrayList<>();
    inputKeys.add(SDOSConstants.HTTPHEADER);
    inputKeys.add(SDOSConstants.HTTPBODY);

    HashMap<String, String> urlEncoded = new HashMap<>();
    urlEncoded.put("key", "value");

    HashMap<String, String> headers_first = new HashMap<>();
    headers_first.put("header", "value");

    HashMap<String, String> headers = new HashMap<>();
    headers.put(SDOSConstants.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);

    HashMap<String, List<String>> inputParam = new HashMap<>();

    inputParam.put(SDOSConstants.HTTPBODY,
        Collections.singletonList(new Gson().toJson(urlEncoded)));
    inputParam.put(SDOSConstants.HTTPHEADER, Collections.singletonList(new Gson().toJson(headers)));

    StringBuilder stringBuilder = new StringBuilder();
    httpActionModel.setInputParameter(inputParameter);
    httpActionModel.setOutputParameter(outputParameter);
    httpActionModel.setHasConnector(connectorModel);
    httpActionModel.setRestTemplateClient(restTemplateClient);
    httpActionModel.setRdf4jClient(rdf4jClient);
    httpActionModel.setHttpBody("body");
    httpActionModel.sethttpHeaders(headers_first);
    utilityMock.when(() -> Utility.getActionIteration(any(), any())).thenReturn(1);
    doReturn("https://INPUT").when(inputParameter).getSubjectIri();
    doReturn(inputParam).when(daParams).getValue("https://INPUT");
    doReturn(inputKeys).when(inputParameter).getKeys();
    doReturn("http://endpoint").when(connectorModel).getBaseUrl();
    doReturn("result").when(restTemplateClient).executeHttpPost(ArgumentMatchers.anyString(),
        ArgumentMatchers.any(),
        ArgumentMatchers.any(),
        ArgumentMatchers.anyString(),
        ArgumentMatchers.any(),
        ArgumentMatchers.any());
    doReturn(Collections.singletonList("outPutKey")).when(outputParameter).getKeys();
    doReturn("https://OUTPUT").when(outputParameter).getSubjectIri();

    try {
      httpActionModel.run(daParams, jsonLdContextModel, serviceArguments);
      assertEquals("body", httpActionModel.getHttpBody());
      assertTrue(httpActionModel.getHttpHeaders().containsKey(SDOSConstants.CONTENT_TYPE));
      assertTrue(
          httpActionModel.getHttpHeaders()
              .containsValue(MediaType.APPLICATION_FORM_URLENCODED_VALUE));
    } catch (Exception e) {
      Assertions.fail(e.getMessage());
    }
  }

  @Test
  void test_run_with_urlEncoded_String() {
    List<String> inputKeys = new ArrayList<>();
    inputKeys.add(SDOSConstants.HTTPHEADER);
    inputKeys.add(SDOSConstants.HTTPBODY);

    HashMap<String, String> urlEncoded = new HashMap<>();
    urlEncoded.put("key", "value");

    HashMap<String, String> headers_first = new HashMap<>();
    headers_first.put("header", "value");

    HashMap<String, String> headers = new HashMap<>();
    headers.put(SDOSConstants.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);

    HashMap<String, List<String>> inputParam = new HashMap<>();

    inputParam.put(SDOSConstants.HTTPBODY,
        Collections.singletonList(urlEncoded.toString()));
    inputParam.put(SDOSConstants.HTTPHEADER, Collections.singletonList(new Gson().toJson(headers)));

    StringBuilder stringBuilder = new StringBuilder();
    httpActionModel.setInputParameter(inputParameter);
    httpActionModel.setOutputParameter(outputParameter);
    httpActionModel.setHasConnector(connectorModel);
    httpActionModel.setRestTemplateClient(restTemplateClient);
    httpActionModel.setRdf4jClient(rdf4jClient);
    httpActionModel.setHttpBody("body");
    httpActionModel.sethttpHeaders(headers_first);
    utilityMock.when(() -> Utility.getActionIteration(any(), any())).thenReturn(1);
    doReturn("https://INPUT").when(inputParameter).getSubjectIri();
    doReturn(inputParam).when(daParams).getValue("https://INPUT");
    doReturn(inputKeys).when(inputParameter).getKeys();
    doReturn("http://endpoint").when(connectorModel).getBaseUrl();
    doReturn("result").when(restTemplateClient).executeHttpPost(ArgumentMatchers.anyString(),
        ArgumentMatchers.any(),
        ArgumentMatchers.any(),
        ArgumentMatchers.anyString(),
        ArgumentMatchers.any(),
        ArgumentMatchers.any());
    doReturn(Collections.singletonList("outPutKey")).when(outputParameter).getKeys();
    doReturn("https://OUTPUT").when(outputParameter).getSubjectIri();

    try {
      httpActionModel.run(daParams, jsonLdContextModel, serviceArguments);
      assertEquals("body", httpActionModel.getHttpBody());
      assertTrue(httpActionModel.getHttpHeaders().containsKey(SDOSConstants.CONTENT_TYPE));
      assertTrue(
          httpActionModel.getHttpHeaders()
              .containsValue(MediaType.APPLICATION_FORM_URLENCODED_VALUE));
    } catch (Exception e) {
      Assertions.fail(e.getMessage());
    }
  }

  @Test
  void run_invalid_header() {
    List<String> inputKeys = new ArrayList<>();
    inputKeys.add(SDOSConstants.HTTPHEADER);

    HashMap<String, List<String>> inputParam = new HashMap<>();

    inputParam.put(SDOSConstants.HTTPHEADER,
        Collections.singletonList("notaHashmap"));

    httpActionModel.setInputParameter(inputParameter);
    httpActionModel.setOutputParameter(outputParameter);
    httpActionModel.setHasConnector(connectorModel);
    httpActionModel.setRestTemplateClient(restTemplateClient);
    httpActionModel.setRdf4jClient(rdf4jClient);
    utilityMock.when(() -> Utility.getActionIteration(any(), any())).thenReturn(1);
    doReturn(inputParam).when(daParams).getValue("https://INPUT");
    doReturn("https://INPUT").when(inputParameter).getSubjectIri();
    doReturn(inputKeys).when(inputParameter).getKeys();

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      httpActionModel.run(daParams, jsonLdContextModel, serviceArguments);
    });
    assertEquals(SdipErrorCode.INVALID_JSONARRAY_RESPONSE.getSdipErrorCode(),
        Integer
            .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                .getSdipErrorCodes().get(0)
                .replace("SDIP_", "")));
  }

  @Test
  void run_invalid_HttpQueryParam() {
    List<String> inputKeys = new ArrayList<>();
    inputKeys.add(SDOSConstants.HTTPQUERYPARAM);

    HashMap<String, List<String>> inputParam = new HashMap<>();

    inputParam.put(SDOSConstants.HTTPQUERYPARAM,
        Collections.singletonList("notaHashmap"));

    httpActionModel.setInputParameter(inputParameter);
    httpActionModel.setOutputParameter(outputParameter);
    httpActionModel.setHasConnector(connectorModel);
    httpActionModel.setRestTemplateClient(restTemplateClient);
    httpActionModel.setRdf4jClient(rdf4jClient);
    utilityMock.when(() -> Utility.getActionIteration(any(), any())).thenReturn(1);
    doReturn(inputParam).when(daParams).getValue("https://INPUT");
    doReturn("https://INPUT").when(inputParameter).getSubjectIri();
    doReturn(inputKeys).when(inputParameter).getKeys();

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      httpActionModel.run(daParams, jsonLdContextModel, serviceArguments);
    });
    assertEquals(SdipErrorCode.INVALID_JSONARRAY_RESPONSE.getSdipErrorCode(),
        Integer
            .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                .getSdipErrorCodes().get(0)
                .replace("SDIP_", "")));
  }

  @Test
  void populate_getActionVirtualGraphAction() {
    httpActionModel.setRdf4jClient(rdf4jClient);
    // test successful case
    String response = UnitTestHelper.readJsonFiles(UnitTestConstants.UPLOADACTIONMODELTESTDATA_VIRTUALGRAPHACTION);
    JsonArray daAnswer = JsonParser.parseString(response).getAsJsonArray();
    doReturn(daAnswer).when(rdf4jClient).selectSparqlOfg(any(), any());
    actionModelFactory.when(() ->
        ActionModelFactory.getAction(any(), any())).thenReturn(iActionModel);
    connectorModelFactory.when(() ->
        ConnectorModelFactory.getConnector(any())).thenReturn(connectorModel);
    parameterModelFactory.when(() ->
        ParameterModelFactory.getParameter(any())).thenReturn(inputParameter);
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();
    // run it!
    httpActionModel.populate("next/action/IRI", daParams, serviceArguments);

    verify(iActionModel, times(1))
        .populate("https://atest#daactiona", daParams, serviceArguments);
    actionModelFactory.verify(
        () -> ActionModelFactory.getAction("https://atest#VirtualGraphAction", daParams),
            times(1)
    );
  }

  @Test
  void populate_missingNextActionType() {
    //reset(doThrow());
    // Testing that the SPARQL-query consists of a UNION for VirtualGraphAction as nextactiontype
    httpActionModel.setRdf4jClient(rdf4jClient);
    String sparql = HttpActionModel.SPARQL.replace(SDOSConstants.VARIABLE, "aSubjectIri");
    String response = UnitTestHelper.readJsonFiles(UnitTestConstants.UPLOADACTIONMODELTESTDATA_MISSINGNEXTACTIONTYPE);
    JsonArray daAnswer = JsonParser.parseString(response).getAsJsonArray();
    doReturn(daAnswer).when(rdf4jClient).selectSparqlOfg(any(), any());
      daParams.setOfgModelRepo(ofgModelRepo);
      doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();

    // Testing that the populate function throws an error when 'nextactiontype' is missing.
    assertEquals(false, daAnswer.get(0).getAsJsonObject().has(SDOSConstants.NEXT_ACTION_TYPE));
    assertThrows(NullPointerException.class, () -> {
      daAnswer.get(0).getAsJsonObject().get(SDOSConstants.NEXT_ACTION_TYPE).getAsJsonObject()
          .get(SDOSConstants.VALUE)
          .getAsString();
    });

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      httpActionModel.populate("https://atest#VirtualGraphAction", daParams, serviceArguments);
    });
    assertEquals(SdipErrorCode.FAILED_TO_PARSE_JSONARRAY.getSdipErrorCode(),
        Integer.parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
            .getSdipErrorCodes().get(0)
            .replace("SDIP_", "")));
  }
}
