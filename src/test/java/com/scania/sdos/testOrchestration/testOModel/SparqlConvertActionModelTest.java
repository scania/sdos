package com.scania.sdos.testOrchestration.testOModel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.scania.sdip.exceptions.IncidentException;
import com.scania.sdip.exceptions.SdipErrorCode;
import com.scania.sdos.model.ServiceArguments;
import com.scania.sdos.orchestration.HttpClient;
import com.scania.sdos.orchestration.OfgModelRepo;
import com.scania.sdos.orchestration.ParameterMemory;
import com.scania.sdos.orchestration.Rdf4jClient;
import com.scania.sdos.orchestration.factory.ActionModelFactory;
import com.scania.sdos.orchestration.factory.ConnectorModelFactory;
import com.scania.sdos.orchestration.factory.ParameterModelFactory;
import com.scania.sdos.orchestration.interfaces.IActionModel;
import com.scania.sdos.orchestration.interfaces.IParameterMemory;
import com.scania.sdos.orchestration.interfaces.IParameterModel;
import com.scania.sdos.orchestration.model.HttpParameterModel;
import com.scania.sdos.orchestration.model.JsonLdContextModel;
import com.scania.sdos.orchestration.model.HttpConnectorModel;
import com.scania.sdos.orchestration.model.HttpBasicAuthModel;
import com.scania.sdos.orchestration.model.SparqlConvertActionModel;
import com.scania.sdos.orchestration.model.StandardParameterModel;
import com.scania.sdos.testUtils.UnitTestHelper;
import com.scania.sdos.utils.SDOSConstants;
import com.scania.sdos.utils.Utility;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.scania.sdos.testUtils.UnitTestConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SparqlConvertActionModelTest {

  private static final Logger LOGGER = LogManager.getLogger(SparqlConvertActionModel.class);
  private static MockedStatic utilityMock;
  private static MockedStatic actionModelFactory;
  private static MockedStatic connectorModelFactory;
  private static MockedStatic parameterModelFactory;
  private static MockedStatic parameterMemoryFactory;
  //  private static MockedStatic uuid;
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
  private StandardParameterModel parameterModel;
  @Mock
  private HttpParameterModel outputParameterModel;
  @Mock
  private Rdf4jClient rdf4jClient;
  @Mock
  private HttpClient httpClient;
  @Mock
  private HashMap hashMapMock;
  @Mock
  private HttpBasicAuthModel httpBasicAuthModel;
  @Mock
  private JsonLdContextModel jsonLdContextModel;
  @Mock
  private OfgModelRepo ofgModelRepo;
  @Spy
  private SparqlConvertActionModel sparqlConvertActionModel;

  private static final String anUuid = "ac8eb46e-8607-41c8-ae71-07cff2cf7367";
  private static final String aTmpGraphName = "http://convertTmpGraph_" + anUuid;

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
    reset(sparqlConvertActionModel);
    reset(rdf4jClient);
    connectorModelFactory.reset();
    actionModelFactory.reset();
    utilityMock.reset();
    parameterModelFactory.reset();
    parameterMemoryFactory.reset();
    reset(daParams);
    reset(serviceArguments);
    utilityMock.when(() -> Utility.getUUID4String())
        .thenReturn(anUuid);
  }

  @AfterEach
  void tearDown() {
  }

  @Test
  void populate_ok() {
    sparqlConvertActionModel.setRdf4jClient(rdf4jClient);
    // test successful case
    String response = UnitTestHelper.readJsonFiles(UnitTestConstants.SPARQLACTIONMODELTESTDATA);
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
    sparqlConvertActionModel.populate("aSubjectIri", daParams, serviceArguments);

    // verify
    assertEquals("aSubjectIri", sparqlConvertActionModel.getSubjectIri());
    assertEquals("aLabel", sparqlConvertActionModel.getLabel());
    assertEquals("aQuery", sparqlConvertActionModel.getSparqlConvert());
    Assertions.assertEquals(iActionModel, sparqlConvertActionModel.getNextAction());
    assertEquals(connectorModel, sparqlConvertActionModel.getHasConnector());
    assertEquals(aTmpGraphName, sparqlConvertActionModel.getTmpGraphName());
    verify(iActionModel, times(1))
        .populate("next/action/IRI", daParams, serviceArguments);
    verify(connectorModel, times(1))
        .populate("aConnectorURI", daParams, serviceArguments);
    verify(inputParameter, times(1))
        .populate("anInputParameter", daParams, serviceArguments);
    verify(sparqlConvertActionModel, times(1)).generateNewTmpGraphName();
    actionModelFactory.verify(
        () -> ActionModelFactory.getAction("aNextActionType", daParams), times(1)
    );
    connectorModelFactory.verify(
        () -> ConnectorModelFactory.getConnector("HTTPConnector"), times(1)
    );
    parameterModelFactory.verify(
        () -> ParameterModelFactory.getParameter("anInputParameterType"), times(1)
    );
  }

  @Test
  void populate_emptyResponse() {
    sparqlConvertActionModel.setRdf4jClient(rdf4jClient);
    JsonArray daAnswer = JsonParser.parseString("[{}]").getAsJsonArray();
    doReturn(daAnswer).when(rdf4jClient).selectSparqlOfg(any(), any());
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      sparqlConvertActionModel.populate("1", daParams, serviceArguments);
    });

    assertEquals(SdipErrorCode.FAILED_TO_PARSE_JSONARRAY.getSdipErrorCode(),
        Integer
            .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                .getSdipErrorCodes().get(0)
                .replace("SDIP_", "")));
  }

  @Test
  void populate_actionModelFactoryReturnsNull() {
    sparqlConvertActionModel.setRdf4jClient(rdf4jClient);
    String response = UnitTestHelper.readJsonFiles(UnitTestConstants.SPARQLACTIONMODELTESTDATA);
    JsonArray daAnswer = JsonParser.parseString(response).getAsJsonArray();
    doReturn(daAnswer).when(rdf4jClient).selectSparqlOfg(any(), any());
    actionModelFactory.when(() ->
        ActionModelFactory.getAction(any(), any())).thenReturn(null);

    connectorModelFactory.when(() ->
        ConnectorModelFactory.getConnector(any())).thenReturn(connectorModel);
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();
    Throwable throwable = assertThrows(IncidentException.class, () -> {
      sparqlConvertActionModel.populate("1", daParams, serviceArguments);
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
  void populate_actionModelThrowsIncidentException() {
    sparqlConvertActionModel.setRdf4jClient(rdf4jClient);
    String response = UnitTestHelper.readJsonFiles(UnitTestConstants.SPARQLACTIONMODELTESTDATA);
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
    doThrow(new IncidentException(new IllegalStateException("aMessage "),
        SdipErrorCode.HTTP_RESPONSE_NOK, LOGGER,
        "aStatus", "anURi")).
        when(iActionModel).populate(any(), any(), any());

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      sparqlConvertActionModel.populate("1", daParams, serviceArguments);
    });

    assertEquals(SdipErrorCode.HTTP_RESPONSE_NOK.getSdipErrorCode(),
        Integer
            .parseInt(((IncidentException) throwable).
                createHttpErrorResponse().
                getBody().
                getSdipErrorCodes().
                get(0).
                replace("SDIP_", "")));
  }

  @Test
  void populate_invalidJsonArrayResponse() {
    sparqlConvertActionModel.setRdf4jClient(rdf4jClient);
    doThrow(IllegalStateException.class).when(rdf4jClient)
        .selectSparqlOfg(any(), any());

    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();
    Throwable throwable = assertThrows(IncidentException.class, () -> {
      sparqlConvertActionModel.populate("1", daParams, serviceArguments);
    });

    assertEquals(SdipErrorCode.INVALID_JSONARRAY_RESPONSE.getSdipErrorCode(),
        Integer
            .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                .getSdipErrorCodes().get(0)
                .replace("SDIP_", "")));

  }

  @Test
  void populate_unknownException() {
    sparqlConvertActionModel.setRdf4jClient(rdf4jClient);
    doThrow(ArrayIndexOutOfBoundsException.class).when(rdf4jClient)
        .selectSparqlOfg(any(), any());

    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();
    Throwable throwable = assertThrows(IncidentException.class, () -> {
      sparqlConvertActionModel.populate("1", daParams, serviceArguments);
    });

    assertEquals(SdipErrorCode.UNKNOWN_REASON_ERROR.getSdipErrorCode(),
        Integer
            .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                .getSdipErrorCodes().get(0)
                .replace("SDIP_", "")));

  }

  @Test
  void run_ok() {
    sparqlConvertActionModel.setRdf4jClient(rdf4jClient);
    sparqlConvertActionModel.generateNewTmpGraphName(); // need to call, since populate isn't called
    List<String> inputKeys = new ArrayList<>();
    inputKeys.add(SDOSConstants.PARAM_NAME);

    String aJson = "[{\"json\": \"aJson\"}]";
    String aContext = "{\"contextIndex\": \"aContextVal\"}";
    doReturn(aContext).when(jsonLdContextModel).getContext();
    String aConstructQuery = "CONSTRUCT { ?s ?p ?o }\nWHERE { ?s ?p ?o }\n";
    String aConstructQueryWithGraph =
        "CONSTRUCT { ?s ?p ?o }\nWHERE { GRAPH <"
            + aTmpGraphName
            + "> { ?s ?p ?o }\n}";
    JsonObject tmpGraphResult = new JsonObject();
    tmpGraphResult.add("@context", new Gson().fromJson(aContext, JsonObject.class));
    tmpGraphResult.add("@graph", new Gson().fromJson(aJson, JsonArray.class));

    String aConstructResponse = "aConstructResponse";

    HashMap<String, List<String>> inputParam = new HashMap<>();
    inputParam.put(SDOSConstants.PARAM_NAME, Collections.singletonList(aJson));

    HashMap<String, List<String>> outputParam = new HashMap<>();
    outputParam.put(SDOSConstants.HTTPBODY, Collections.singletonList("[]"));

    sparqlConvertActionModel.setInputParameter(parameterModel);
    sparqlConvertActionModel.setOutputParameter(outputParameterModel);
    sparqlConvertActionModel.setHasConnector(connectorModel);
    sparqlConvertActionModel.setSparqlConvert(aConstructQuery);

    utilityMock.when(() -> Utility.getActionIteration(any(), any())).thenReturn(1);
    doReturn("https://INPUT").when(parameterModel).getSubjectIri();
    doReturn("paramName").when(parameterModel).getParamName();
    doReturn(inputParam).when(daParams).getValue("https://INPUT");
    doNothing().when(rdf4jClient).saveJsonLdToGraph(any(), any(), any(), any());
    doReturn(aConstructResponse).when(rdf4jClient).doConstructSparql(any(), any(), any(IParameterMemory.class));
    doReturn("http://outputIRI").when(outputParameterModel).getSubjectIri();
    doReturn(new ArrayList<>(outputParam.keySet())).when(outputParameterModel).getKeys();

    // Run it!
    sparqlConvertActionModel.run(daParams, jsonLdContextModel, serviceArguments);

    String query = "CLEAR GRAPH <" + aTmpGraphName + ">";
    String stardogResultUpdateEndpoint = serviceArguments.getStardogResultUpdateEndpoint();

    // Check it!
    verify(rdf4jClient, times(1)).saveJsonLdToGraph(eq(tmpGraphResult),
        eq(aTmpGraphName),
        eq(stardogResultUpdateEndpoint),
            any(ParameterMemory.class));
    verify(rdf4jClient, times(1)).doConstructSparql(aConstructQueryWithGraph,
        serviceArguments.getStardogResultQueryEndpoint(),
            daParams);
    verify(rdf4jClient, times(1)).executeUpdateSparql(
        eq(query),
        eq(stardogResultUpdateEndpoint),
            any(ParameterMemory.class));
  }

  @Test
  void run_parameterValueIsNull() {
    // ENDPOINT is not used in SparqlConvertActionModel, just using anything to provoke the error...
    List<String> inputKeys = new ArrayList<>();
    inputKeys.add(SDOSConstants.ENDPOINT);
    HashMap<String, List<String>> inputParam = new HashMap<>();
    inputParam.put(SDOSConstants.ENDPOINT, Collections.singletonList("/test"));

    sparqlConvertActionModel.setInputParameter(parameterModel);
    sparqlConvertActionModel.setHasConnector(connectorModel);
    utilityMock.when(() -> Utility.getActionIteration(any(), any())).thenReturn(1);
    doReturn("https://INPUT").when(parameterModel).getSubjectIri();
    doReturn("paramName").when(parameterModel).getParamName();
    doReturn(inputParam).when(daParams).getValue("https://INPUT");

    // Run it!
    Throwable throwable = assertThrows(IncidentException.class, () -> {
      sparqlConvertActionModel.run(daParams, jsonLdContextModel, serviceArguments);
    });
    // Check it!
    assertEquals(SdipErrorCode.UNKNOWN_REASON_ERROR.getSdipErrorCode(),
        Integer
            .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                .getSdipErrorCodes().get(0)
                .replace("SDIP_", "")));
  }

  @Test
  void run_throwsInvalidSparqlQuery() {
    sparqlConvertActionModel.setRdf4jClient(rdf4jClient);
    sparqlConvertActionModel.generateNewTmpGraphName(); // need to call, since populate isn't called
    List<String> inputKeys = new ArrayList<>();
    inputKeys.add(SDOSConstants.PARAM_NAME);

    String aJson = "[{\"json\": \"aJson\"}]";
    String aContext = "{\"contextIndex\": \"aContextVal\"}";
    String anInvalidConstructQuery = "INVALIDCONSTRUCT { ?s ?p ?o }\nWHERE { ?s ?p ?o }\n";
    JsonObject tmpGraphResult = new JsonObject();
    tmpGraphResult.add("@context", new Gson().fromJson(aContext, JsonObject.class));
    tmpGraphResult.add("@graph", new Gson().fromJson(aJson, JsonArray.class));

    HashMap<String, List<String>> inputParam = new HashMap<>();
    inputParam.put(SDOSConstants.PARAM_NAME, Collections.singletonList(aJson));

    HashMap<String, List<String>> outputParam = new HashMap<>();
    outputParam.put(SDOSConstants.HTTPBODY, Collections.singletonList("[]"));

    sparqlConvertActionModel.setInputParameter(parameterModel);
    sparqlConvertActionModel.setOutputParameter(outputParameterModel);
    sparqlConvertActionModel.setHasConnector(connectorModel);
    sparqlConvertActionModel.setSparqlConvert(anInvalidConstructQuery);

    utilityMock.when(() -> Utility.getActionIteration(any(), any())).thenReturn(1);
    doReturn("https://INPUT").when(parameterModel).getSubjectIri();
    doReturn("paramName").when(parameterModel).getParamName();
    doReturn(inputParam).when(daParams).getValue("https://INPUT");
    doNothing().when(rdf4jClient).saveJsonLdToGraph(any(), any(), any(), any());

    // Run it!
    Throwable throwable = assertThrows(IncidentException.class, () -> {
      sparqlConvertActionModel.run(daParams, jsonLdContextModel, serviceArguments);
    });
    // Check it!
    assertEquals(SdipErrorCode.INVALID_SPARQL_QUERY.getSdipErrorCode(),
        Integer
            .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                .getSdipErrorCodes().get(0)
                .replace("SDIP_", "")));

  }

  @Test
  void run_throwsInvalidJsonArrayResponse() {
    sparqlConvertActionModel.setRdf4jClient(rdf4jClient);
    sparqlConvertActionModel.generateNewTmpGraphName(); // need to call, since populate isn't called
    List<String> inputKeys = new ArrayList<>();
    inputKeys.add(SDOSConstants.PARAM_NAME);

    String notAJson = "this is not a valid JSON-array";
    String aConstructQuery = "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o } ";

    HashMap<String, List<String>> inputParam = new HashMap<>();
    inputParam.put(SDOSConstants.PARAM_NAME, Collections.singletonList(notAJson));

    HashMap<String, List<String>> outputParam = new HashMap<>();
    outputParam.put(SDOSConstants.HTTPBODY, Collections.singletonList("[]"));

    sparqlConvertActionModel.setInputParameter(parameterModel);
    sparqlConvertActionModel.setOutputParameter(outputParameterModel);
    sparqlConvertActionModel.setHasConnector(connectorModel);
    sparqlConvertActionModel.setSparqlConvert(aConstructQuery);

    utilityMock.when(() -> Utility.getActionIteration(any(), any())).thenReturn(1);
    doReturn("https://INPUT").when(parameterModel).getSubjectIri();
    doReturn("paramName").when(parameterModel).getParamName();
    doReturn(inputParam).when(daParams).getValue("https://INPUT");

    // Run it!
    Throwable throwable = assertThrows(IncidentException.class, () -> {
      sparqlConvertActionModel.run(daParams, jsonLdContextModel, serviceArguments);
    });
    // Check it!
    assertEquals(SdipErrorCode.INVALID_JSONARRAY_RESPONSE.getSdipErrorCode(),
        Integer
            .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                .getSdipErrorCodes().get(0)
                .replace("SDIP_", "")));
  }
}