package com.scania.sdos.testOrchestration.testOModel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.scania.sdip.exceptions.IncidentException;
import com.scania.sdip.exceptions.SdipErrorCode;
import com.scania.sdip.exceptions.SdipErrorParameter;
import com.scania.sdos.model.ServiceArguments;
import com.scania.sdos.orchestration.OfgModelRepo;
import com.scania.sdos.orchestration.ParameterMemory;
import com.scania.sdos.orchestration.Rdf4jClient;
import com.scania.sdos.orchestration.factory.ActionModelFactory;
import com.scania.sdos.orchestration.factory.ParameterModelFactory;
import com.scania.sdos.orchestration.interfaces.IActionModel;
import com.scania.sdos.orchestration.interfaces.IParameterMemory;
import com.scania.sdos.orchestration.interfaces.IParameterModel;
import com.scania.sdos.orchestration.model.JsonLdContextModel;
import com.scania.sdos.orchestration.model.VirtualGraphActionModel;
import com.scania.sdos.testUtils.UnitTestHelper;
import com.scania.sdos.utils.SDOSConstants;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.scania.sdos.testUtils.UnitTestConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.parser.ParsedGraphQuery;
import org.eclipse.rdf4j.query.parser.ParsedTupleQuery;
import org.eclipse.rdf4j.query.parser.QueryParserUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VirtualGraphActionModelTest {

  private static final Logger LOGGER = LogManager.getLogger(VirtualGraphActionModelTest.class);

  private static MockedStatic actionModelFactory;
  private static MockedStatic parameterModelFactory;
  private static MockedStatic parameterMemoryFactory;
  private static MockedStatic<QueryParserUtil> queryParserMock;


  @Mock
  private IActionModel iActionModel;
  @Mock
  private ParameterMemory daParams;
  @Mock
  private ServiceArguments serviceArguments;
  @Mock
  private IParameterModel parameterModel;
  @Mock
  private ParsedTupleQuery aParsedTupleQuery;
  @Mock
  private ParsedGraphQuery aParsedGraphQuery;
  @Mock
  private ParameterMemory aParameterMemory;

  @Spy
  private Rdf4jClient rdf4jClient;

  @Spy
  private VirtualGraphActionModel virtualgraphActionModel;
  @Spy
  private ParameterMemory daParamsSpy;
  @Mock
  private JsonLdContextModel jsonLdContextModel;
  @Mock
  private OfgModelRepo ofgModelRepo;


  @BeforeAll
  static void beforeAll() {
    actionModelFactory = mockStatic(ActionModelFactory.class);
    parameterModelFactory = mockStatic(ParameterModelFactory.class);
    parameterMemoryFactory = mockStatic(IParameterMemory.class);
    queryParserMock = mockStatic(QueryParserUtil.class);
  }

  @AfterAll
  static void afterAll() {
    actionModelFactory.close();
    parameterModelFactory.close();
    parameterMemoryFactory.close();
    queryParserMock.close();
  }

  @BeforeEach
  void setUp() {
    reset(rdf4jClient);
    reset(daParamsSpy);
    reset(virtualgraphActionModel);
    actionModelFactory.reset();
    parameterModelFactory.reset();
    parameterMemoryFactory.reset();
    queryParserMock.reset();
  }

  @Test
  void run_ok() {
    StringBuilder stringBuilder = new StringBuilder();
    ArrayList outputKey = new ArrayList<String>();
    outputKey.add("anOutputParameter");

    String aResult = "aJsonString";

    virtualgraphActionModel.setRdf4jClient(rdf4jClient);
    virtualgraphActionModel.setActionSparql("aSparql");
    virtualgraphActionModel.setOutputParameter(parameterModel);
    virtualgraphActionModel.setLabel("aLabel");
    doReturn("aJsonString").when(virtualgraphActionModel)
        .runSelectOrConstructSparql(any(), any(), any());
    doReturn("http://outputIRI").when(parameterModel).getSubjectIri();
    doReturn(outputKey).when(parameterModel).getKeys();
    // run it!
    virtualgraphActionModel.run(daParamsSpy, jsonLdContextModel, serviceArguments);

    try {
      // check it
      assertEquals(aResult,
          daParamsSpy.getValue("http://outputIRI").get("anOutputParameter").get(0));
      assertEquals("aSparql", virtualgraphActionModel.getActionSparql());
      assertEquals("aLabel", virtualgraphActionModel.getLabel());
    } catch (Exception e) {
      Assertions.fail(e.getMessage());
    }

  }

  @Test
  void run_runThrow() {
    //best test:)
    doThrow(MalformedQueryException.class).when(virtualgraphActionModel)
        .runSelectOrConstructSparql(any(), any(), any());
    // run it!
    assertThrows(MalformedQueryException.class, () -> {
      virtualgraphActionModel.run(daParamsSpy, jsonLdContextModel, serviceArguments);
    });
  }

  @Test
  void populate_ok() {
    virtualgraphActionModel.setRdf4jClient(rdf4jClient);
    // test successful case
    String response = UnitTestHelper.readJsonFiles(UnitTestConstants.VIRTUALGRAPHACTIONMODELTESTDATA);
    JsonArray daAnswer = JsonParser.parseString(response).getAsJsonArray();
    doReturn(daAnswer).when(rdf4jClient).selectSparqlOfg(any(), any());
    actionModelFactory.when(() ->
        ActionModelFactory.getAction(any(), any())).thenReturn(iActionModel);
    parameterModelFactory.when(() ->
        ParameterModelFactory.getParameter(any())).thenReturn(parameterModel);
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();

    // run it!
    virtualgraphActionModel.populate("aSubjectIri", daParams, serviceArguments);

    // check it!
    assertEquals("aSubjectIri", virtualgraphActionModel.getSubjectIri());
    assertEquals("daSPARQLQuery", virtualgraphActionModel.getActionSparql());
    assertEquals(Arrays.asList(parameterModel), virtualgraphActionModel.getOutputParameter());
    assertEquals(iActionModel, virtualgraphActionModel.getNextAction());

    verify(iActionModel, times(1))
        .populate("next/action/IRI", daParams, serviceArguments);
    verify(parameterModel, times(1))
        .populate("anOutputParameter", daParams, serviceArguments);
    actionModelFactory.verify(
        () -> ActionModelFactory.getAction("aNextActionType", daParams), times(1)
    );
    parameterModelFactory.verify(
        () -> ParameterModelFactory.getParameter("anOutputParameterType"), times(1)
    );
  }

  @Test
  void populate_emptyResponse() {
    virtualgraphActionModel.setRdf4jClient(rdf4jClient);
    // test empty response

    JsonArray daAnswer = JsonParser.parseString("[{}]").getAsJsonArray();
    doReturn(daAnswer).when(rdf4jClient).selectSparqlOfg(any(), any());
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();

    // run it
    Throwable throwable = assertThrows(IncidentException.class, () -> {
      virtualgraphActionModel.populate("anId", daParams, serviceArguments);
    });
    assertEquals(SdipErrorCode.FAILED_TO_PARSE_JSONARRAY.getSdipErrorCode(),
        Integer
            .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                .getSdipErrorCodes().get(0)
                .replace("SDIP_", "")));
  }

  @Test
  void populate_virtualgraphActionModelFactoryReturnsNull() {
    virtualgraphActionModel.setRdf4jClient(rdf4jClient);
    // test ActionModelFactory returns null
    String response = UnitTestHelper.readJsonFiles(UnitTestConstants.VIRTUALGRAPHACTIONMODELTESTDATA);
    JsonArray daAnswer = JsonParser.parseString(response).getAsJsonArray();
    doReturn(daAnswer).when(rdf4jClient).selectSparqlOfg(any(), any());
    actionModelFactory.when(() ->
        ActionModelFactory.getAction(any(), any())).thenReturn(null);
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();

    // run it!
    Throwable throwable = assertThrows(IncidentException.class, () -> {
      virtualgraphActionModel.populate("", daParams, serviceArguments);
    });

    assertEquals(SdipErrorCode.FAILED_TO_PARSE_JSONARRAY.getSdipErrorCode(),
        Integer
            .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                .getSdipErrorCodes().get(0)
                .replace("SDIP_", "")));

  }

  @Test
  void populate_parameterModelFactoryReturnsNull() {
    virtualgraphActionModel.setRdf4jClient(rdf4jClient);
    // test ActionModelFactory returns null
    String response = UnitTestHelper.readJsonFiles(UnitTestConstants.VIRTUALGRAPHACTIONMODELTESTDATA);
    JsonArray daAnswer = JsonParser.parseString(response).getAsJsonArray();
    doReturn(daAnswer).when(rdf4jClient).selectSparqlOfg(any(), any());
    parameterModelFactory.when(() ->
        ParameterModelFactory.getParameter(any())).thenReturn(null);
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();

    // run it!
    Throwable throwable = assertThrows(IncidentException.class, () -> {
      virtualgraphActionModel.populate("anId", daParams, serviceArguments);
    });
    assertEquals(SdipErrorCode.FAILED_TO_PARSE_JSONARRAY.getSdipErrorCode(),
        Integer
            .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                .getSdipErrorCodes().get(0)
                .replace("SDIP_", "")));
  }

  @Test
  void populate_invalidJsonArrayResponse() {
    virtualgraphActionModel.setRdf4jClient(rdf4jClient);
    doThrow(IllegalStateException.class).when(rdf4jClient)
        .selectSparqlOfg(any(), any());
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      virtualgraphActionModel.populate("1", daParams, serviceArguments);
    });
    assertEquals(SdipErrorCode.INVALID_JSONARRAY_RESPONSE.getSdipErrorCode(),
        Integer
            .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                .getSdipErrorCodes().get(0)
                .replace("SDIP_", "")));

  }


  @Test
  void populate_invalidPropertyJsonArray() {
    virtualgraphActionModel.setRdf4jClient(rdf4jClient);
    JsonArray daAnswer = JsonParser.parseString("[{\"key\":\"value\"}]").getAsJsonArray();
    doReturn(daAnswer).when(rdf4jClient).selectSparqlOfg(any(), any());

    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      virtualgraphActionModel.populate("1", daParams, serviceArguments);
    });

    assertEquals(SdipErrorCode.FAILED_TO_PARSE_JSONARRAY.getSdipErrorCode(),
        Integer
            .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                .getSdipErrorCodes().get(0)
                .replace("SDIP_", "")));

  }

  @Test
  void run_doSelectSparql() {
    virtualgraphActionModel.setRdf4jClient(rdf4jClient);
    String daAnswer = "42";
    JsonArray anArray = mock(JsonArray.class);
    doReturn(daAnswer).when(anArray).toString();
    doReturn(anArray).when(rdf4jClient).doSelectSparql(any(), any(), any());
    String aQuery = "select * where {?s ?o ?p}";
    queryParserMock.when(() ->
        QueryParserUtil.parseQuery(any(), any(), any())).thenReturn(aParsedTupleQuery);
    // run it!
    assertEquals(daAnswer,
            virtualgraphActionModel.runSelectOrConstructSparql(aQuery, aParameterMemory, serviceArguments));
    // check it!
    verify(rdf4jClient, times(1))
        .doSelectSparql(aQuery, serviceArguments.getStardogQueryEndpoint(), aParameterMemory);
  }

  @Test
  void run_doConstructSparql() {
    virtualgraphActionModel.setRdf4jClient(rdf4jClient);
    String daAnswer = "42";
    HashMap<String, List<String>> anEndPoint = new HashMap<>();
    anEndPoint.put(SDOSConstants.ENDPOINT, Collections.singletonList("anEndPoint"));
    anEndPoint.put(SDOSConstants.GRAPH, Collections.singletonList("aGraph"));
    doReturn(daAnswer).when(rdf4jClient).doConstructSparql(any(), any(), any(IParameterMemory.class));
    String aQuery = "";
    queryParserMock.when(() ->
        QueryParserUtil.parseQuery(any(), any(), any())).thenReturn(aParsedGraphQuery);
    // run it!
    assertEquals(daAnswer,
            virtualgraphActionModel.runSelectOrConstructSparql(aQuery, aParameterMemory, serviceArguments));
    // check it!
    String stardogQueryEndpoint = serviceArguments.getStardogQueryEndpoint();
    verify(rdf4jClient, times(1))
        .doConstructSparql(eq(aQuery), eq(stardogQueryEndpoint), any(IParameterMemory.class));
  }

  @Test
  void run_unknownQueryType() {

    virtualgraphActionModel.setRdf4jClient(rdf4jClient);
    String aQuery = "";

    queryParserMock.when(() ->
        QueryParserUtil.parseQuery(any(), any(), any())).thenReturn(null);
    // run it!
    Throwable throwable = assertThrows(IncidentException.class, () -> {
      virtualgraphActionModel.runSelectOrConstructSparql(aQuery, aParameterMemory, serviceArguments);
    });
    assertEquals(SdipErrorCode.UNKNOWN_PARSING_ERROR.getSdipErrorCode(),
        Integer
            .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                .getSdipErrorCodes().get(0)
                .replace("SDIP_", "")));
  }

  @Test
  void run_malformedQuery() {

    virtualgraphActionModel.setRdf4jClient(rdf4jClient);
    String aQuery = "";
    queryParserMock.when(() ->
        QueryParserUtil.parseQuery(any(), any(), any())).thenThrow(MalformedQueryException.class);
    // run it!
    Throwable throwable = assertThrows(IncidentException.class, () -> {
      virtualgraphActionModel.runSelectOrConstructSparql(aQuery, aParameterMemory, serviceArguments);
    });

    assertEquals(SdipErrorCode.MALFORMED_SPARQL_QUERY.getSdipErrorCode(),
        Integer
            .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                .getSdipErrorCodes().get(0)
                .replace("SDIP_", "")));
  }

  @Test
  void populate_unknownError() {
    virtualgraphActionModel.setRdf4jClient(rdf4jClient);
    doThrow(RuntimeException.class).when(rdf4jClient).selectSparqlOfg(any(), any());
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      virtualgraphActionModel.populate("1", daParams, serviceArguments);
    });
    assertEquals(SdipErrorCode.UNKNOWN_PARSING_ERROR.getSdipErrorCode(),
        Integer
            .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                .getSdipErrorCodes().get(0)
                .replace("SDIP_", "")));
  }

  @Test
  void populate_catchException() {
    virtualgraphActionModel.setRdf4jClient(rdf4jClient);
    String message = "Host name may not be null";
    doThrow(new IncidentException(SdipErrorCode.UNKNOWN_REASON_ERROR, LOGGER, message,
        SdipErrorParameter.SUPPORTMAIL))
        .when(rdf4jClient).selectSparqlOfg(any(), any());
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      virtualgraphActionModel.populate("1", daParams, serviceArguments);
    });

    assertEquals(SdipErrorCode.UNKNOWN_REASON_ERROR.getSdipErrorCode(),
        Integer
            .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                .getSdipErrorCodes().get(0)
                .replace("SDIP_", "")));
    assertEquals(2, ((IncidentException) throwable).getErrorBindings().get(0).getArgs().length);
    Object[] args = ((IncidentException) throwable).getErrorBindings().get(0).getArgs();
    assertEquals(message, args[0]);
    assertEquals(SdipErrorParameter.SUPPORTMAIL, args[1]);
  }

  @Test
  void populate_getActionVirtualGraphAction() {
    virtualgraphActionModel.setRdf4jClient(rdf4jClient);
    // test successful case
    String response = UnitTestHelper.readJsonFiles(
        UnitTestConstants.VIRTUALGRAPHACTIONMODELTESTDATA_VIRTUALGRAPHACTION);
    JsonArray daAnswer = JsonParser.parseString(response).getAsJsonArray();
    doReturn(daAnswer).when(rdf4jClient).selectSparqlOfg(any(), any());
    actionModelFactory.when(() ->
        ActionModelFactory.getAction(any(), any())).thenReturn(iActionModel);
    parameterModelFactory.when(() ->
        ParameterModelFactory.getParameter(any())).thenReturn(parameterModel);

    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();

    // run it!
    virtualgraphActionModel.populate("next/action/IRI", daParams, serviceArguments);

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
    virtualgraphActionModel.setRdf4jClient(rdf4jClient);
    String sparql = VirtualGraphActionModel.SPARQL_QUERY.replace(SDOSConstants.VARIABLE, "aSubjectIri");
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
      virtualgraphActionModel.populate("https://atest#VirtualGraphAction", daParams,
          serviceArguments);
    });
    assertEquals(SdipErrorCode.FAILED_TO_PARSE_JSONARRAY.getSdipErrorCode(),
            Integer.parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                    .getSdipErrorCodes().get(0)
                    .replace("SDIP_", "")));
  }
}