package com.scania.sdos.testOrchestration.testOModel;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
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
import com.scania.sdos.orchestration.model.StandardParameterModel;
import com.scania.sdos.orchestration.model.QueryActionModel;
import com.scania.sdos.orchestration.model.SparqlQueryParameterModel;
import com.scania.sdos.testUtils.UnitTestHelper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.scania.sdos.testUtils.UnitTestConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
public class QueryActionModelTest {

  private static final Logger LOGGER = LogManager.getLogger(QueryActionModel.class);
  private String queryRes = "<https://kg.scania.com/it/iris_orchestration/sub_iri> <https://kg.scania.com/it/iris_orchestration/pre> <https://kg.scania.com/it/iris_orchestration/obj> .";
  private String construct =     "PREFIX : <http://kg.scania.com/test#>\n"+
      "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"+
      "CONSTRUCT{\n"+
      "    ?sub :pre ?obj ;\n"+
      "          rdf:type :Test ;\n"+
      "          :pre2 ?obj2 .\n"+
      "}\n"+
      "WHERE {             ?sub  :pre \"test\";\n"+
      "                          :values ?testvalue; \n"+
      "                          :objects ?obj .\n"+
      " }";
  private String res = "{\"p\":{\"type\":\"uri\",\"value\":\"http://kg.scania.com/part#nameLong\"},\"o\":{\"type\":\"literal\",\"value\":\"test_nameLong\"}}";
  private String selectQuery = "SELECT * WHERE {  graph ?g { ?s ?p ?o.}}";

  private static MockedStatic actionModelFactory;
  private static MockedStatic parameterModelFactory;
  private static MockedStatic parameterMemoryFactory;

  @Mock
  private IActionModel iActionModel;
  @Mock
  private ActionModelFactory actionModelFactory1;
  @Mock
  private ParameterMemory daParams;
  @Mock
  private ServiceArguments serviceArguments;
  @Mock
  private IParameterModel inputParameter;
  @Mock
  private OfgModelRepo ofgModelRepo;
  @Mock
  private IParameterModel outputParameter;
  @Mock
  private Rdf4jClient rdf4jClient;
  @Spy
  private QueryActionModel modelUnderTest;
  @Mock
  private JsonLdContextModel jsonLdContextModel;

  @BeforeAll
  static void beforeAll() {
    actionModelFactory = mockStatic(ActionModelFactory.class);
    parameterModelFactory = mockStatic(ParameterModelFactory.class);
    parameterMemoryFactory = mockStatic(IParameterMemory.class);
  }


  @AfterAll
  static void afterAll() {
    actionModelFactory.close();
    parameterModelFactory.close();
    parameterMemoryFactory.close();
  }


  @BeforeEach
  void setUp() {
    reset(rdf4jClient);
    actionModelFactory.reset();
    parameterModelFactory.reset();
    parameterMemoryFactory.reset();
    reset(modelUnderTest);
    reset(daParams);
    reset(serviceArguments);
    reset(jsonLdContextModel);
  }
  @Test
  void populate_ok() {
    modelUnderTest.setRdf4jClient(rdf4jClient);

    String response = UnitTestHelper.readJsonFiles("QueryActionModelTestData.json");
    JsonArray daAnswer = JsonParser.parseString(response).getAsJsonArray();
    doReturn(daAnswer).when(rdf4jClient).selectSparqlOfg(any(), any());
    parameterModelFactory.when(() ->
        ParameterModelFactory.getParameter(any())).thenReturn(inputParameter);
    actionModelFactory.when(() ->
        ActionModelFactory.getAction(any(), any())).thenReturn(iActionModel);
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();

    try {
      modelUnderTest.populate("aSubjectIri", daParams, serviceArguments);
      assertEquals("aSubjectIri", modelUnderTest.getSubjectIri());
      assertEquals("testLabel", modelUnderTest.getLabel());
      assertEquals(true, modelUnderTest.isEnableReasoner());
      verify(iActionModel, times(1))
          .populate("next/action/IRI", daParams, serviceArguments);
      verify(inputParameter, times(1))
          .populate("anInputParameter", daParams, serviceArguments);
      actionModelFactory.verify(
              () -> ActionModelFactory.getAction("aNextActionType", daParams),times(1)
      );
    } catch (Exception e) {
      Assertions.fail(e.getMessage());
    }
  }

  @Test
  void populate_without_inputParameter() {
    modelUnderTest.setRdf4jClient(rdf4jClient);
    String response = UnitTestHelper.readJsonFiles("QueryActionModelTestData2.json");
    JsonArray daAnswer = JsonParser.parseString(response).getAsJsonArray();
    doReturn(daAnswer).when(rdf4jClient).selectSparqlOfg(any(), any());
    parameterModelFactory.when(() ->
        ParameterModelFactory.getParameter(any())).thenReturn(inputParameter);
    actionModelFactory.when(() ->
        ActionModelFactory.getAction(any(), any())).thenReturn(iActionModel);
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();

    try {

      Throwable throwable = assertThrows(IncidentException.class, () -> {
        modelUnderTest.populate("1", daParams, serviceArguments);
      });

      Assertions.assertEquals(SdipErrorCode.FAILED_TO_PARSE_JSONARRAY.getSdipErrorCode(),
          Integer
              .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                              .getSdipErrorCodes().get(0)
                              .replace("SDIP_", "")));
    } catch (Exception e) {
      Assertions.fail(e.getMessage());
    }
  }

  @Test
  void populate_emptyResponse() {
    modelUnderTest.setRdf4jClient(rdf4jClient);
    JsonArray daAnswer = JsonParser.parseString("[{}]").getAsJsonArray();
    doReturn(daAnswer).when(rdf4jClient).selectSparqlOfg(any(), any());
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      modelUnderTest.populate("1", daParams, serviceArguments);
    });

    Assertions.assertEquals(SdipErrorCode.FAILED_TO_PARSE_JSONARRAY.getSdipErrorCode(),
        Integer
            .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                .getSdipErrorCodes().get(0)
                .replace("SDIP_", "")));
  }

  @Test
  void populate_actionModelFactoryReturnsNull() {
    modelUnderTest.setRdf4jClient(rdf4jClient);
    String response = UnitTestHelper.readJsonFiles(UnitTestConstants.HTTPACTIONMODELTESTDATA);
    JsonArray daAnswer = JsonParser.parseString(response).getAsJsonArray();
    doReturn(daAnswer).when(rdf4jClient).selectSparqlOfg(any(), any());
    actionModelFactory.when(() ->
        ActionModelFactory.getAction(any(), any())).thenReturn(null);
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      modelUnderTest.populate("1", daParams, serviceArguments);
    });

    Assertions.assertEquals(SdipErrorCode.FAILED_TO_PARSE_JSONARRAY.getSdipErrorCode(),
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
    modelUnderTest.setRdf4jClient(rdf4jClient);
    doThrow(IllegalStateException.class).when(rdf4jClient)
        .selectSparqlOfg(any(), any());
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      modelUnderTest.populate("1", daParams, serviceArguments);
    });

    Assertions.assertEquals(SdipErrorCode.INVALID_JSONARRAY_RESPONSE.getSdipErrorCode(),
        Integer
            .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                .getSdipErrorCodes().get(0)
                .replace("SDIP_", "")));

  }


  @Test
  void populate_invalidPropertyJsonArray() {
    modelUnderTest.setRdf4jClient(rdf4jClient);
    JsonArray daAnswer = JsonParser.parseString("[{\"key\":\"value\"}]").getAsJsonArray();
    doReturn(daAnswer).when(rdf4jClient).selectSparqlOfg(any(), any());
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      modelUnderTest.populate("1", daParams, serviceArguments);
    });

    Assertions.assertEquals(SdipErrorCode.FAILED_TO_PARSE_JSONARRAY.getSdipErrorCode(),
        Integer
            .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                .getSdipErrorCodes().get(0)
                .replace("SDIP_", "")));
  }


  @Test
  void populate_catchIncidentException() {
    modelUnderTest.setRdf4jClient(rdf4jClient);
    doThrow(new IncidentException(SdipErrorCode.SPARQL_ENDPOINT_EMPTY, LOGGER,
        SdipErrorParameter.SUPPORTMAIL)).
        when(rdf4jClient).selectSparqlOfg(any(), any());
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      modelUnderTest.populate("1", daParams, serviceArguments);
    });

    Assertions.assertEquals(SdipErrorCode.SPARQL_ENDPOINT_EMPTY.getSdipErrorCode(),
        Integer
            .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                .getSdipErrorCodes().get(0)
                .replace("SDIP_", "")));
  }

  @Test
  void populate_catch_Exception() {
    modelUnderTest.setRdf4jClient(rdf4jClient);
    doThrow(RuntimeException.class).
        when(rdf4jClient).selectSparqlOfg(any(), any());
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      modelUnderTest.populate("1", daParams, serviceArguments);
    });

    Assertions.assertEquals(SdipErrorCode.UNKNOWN_PARSING_ERROR.getSdipErrorCode(),
        Integer
            .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                .getSdipErrorCodes().get(0)
                .replace("SDIP_", "")));
  }


  private SparqlQueryParameterModel getinputParameterModel() {
    SparqlQueryParameterModel inputParameterModel = new SparqlQueryParameterModel();
    inputParameterModel.setLabel("test_param_query");
    inputParameterModel.
            setSubjectIri("https://kg.scania.com/it/iris_orchestration/subIri_1");
    return inputParameterModel;

  }

  private StandardParameterModel getOutputParameterModel() {
    StandardParameterModel outputParameterModel = new StandardParameterModel();
    outputParameterModel.setSubjectIri("https://kg.scania.com/it/iris_orchestration/output_iri");
    outputParameterModel.setParamName("param_response");
    outputParameterModel.setRdf4jClient(rdf4jClient);
    outputParameterModel.setLabel("svare");
    return outputParameterModel;

  }

  @Test
  void run_with_reasoner_sparql() {
    modelUnderTest.setRdf4jClient(rdf4jClient);
    modelUnderTest.setEnableReasoner(true);
    modelUnderTest.setInputParameter(getinputParameterModel());
    modelUnderTest.setOutputParameter(getOutputParameterModel());

    String response = UnitTestHelper.readJsonFiles("QueryActionModelTestData3.json");
    JsonArray daAnswer = JsonParser.parseString(response).getAsJsonArray();
    doReturn(daAnswer).when(rdf4jClient).doSelectSparql(any(), any(), any(), anyBoolean());

    HashMap map = new HashMap();
    map.put("query", Collections.singletonList(selectQuery));

    ParameterMemory parameterMemory = new ParameterMemory();
    parameterMemory.putParameter("https://kg.scania.com/it/iris_orchestration/subIri_1",map);

    try {
      modelUnderTest.run(parameterMemory, jsonLdContextModel, serviceArguments);
      List iParamList = ((ArrayList) ((HashMap) parameterMemory.
              getValue("https://kg.scania.com/it/iris_orchestration/output_iri")).
              get("param_response"));

      assertEquals(res, iParamList.get(0));
      verify(rdf4jClient, times(1))
              .doSelectSparql(selectQuery, serviceArguments, parameterMemory, true);
    } catch (Exception e) {
    Assertions.fail(e.getMessage());
    }
  }

  @Test
  void run_with_reasoner_construct() {

    modelUnderTest.setRdf4jClient(rdf4jClient);
    modelUnderTest.setEnableReasoner(true);
    modelUnderTest.setInputParameter(getinputParameterModel());
    modelUnderTest.setOutputParameter(getOutputParameterModel());

    doReturn(queryRes).when(rdf4jClient).doConstructSparql(any(), any(), any(), anyBoolean());

    HashMap map = new HashMap();
    map.put("query", Collections.singletonList(construct));

    ParameterMemory parameterMemory = new ParameterMemory();
    parameterMemory.putParameter("https://kg.scania.com/it/iris_orchestration/subIri_1",map);
    try {
      modelUnderTest.run(parameterMemory,jsonLdContextModel,serviceArguments);
      List iParamList = (List) ((HashMap) parameterMemory.
          getValue("https://kg.scania.com/it/iris_orchestration/output_iri")).
          get("param_response");
      assertEquals(queryRes, iParamList.get(0));
      verify(rdf4jClient, times(1))
              .doConstructSparql(construct, serviceArguments, parameterMemory, true);
    } catch (Exception e) {
      Assertions.fail(e.getMessage());
    }
  }

  @Test
  void run_without_reasoner_construct() {

    modelUnderTest.setRdf4jClient(rdf4jClient);
    modelUnderTest.setEnableReasoner(false);
    modelUnderTest.setInputParameter(getinputParameterModel());
    modelUnderTest.setOutputParameter(getOutputParameterModel());

    doReturn(queryRes).when(rdf4jClient).doConstructSparql(any(), any(), any(), anyBoolean());

    HashMap map = new HashMap();
    map.put("query", Collections.singletonList(construct));

    ParameterMemory parameterMemory = new ParameterMemory();
    parameterMemory.putParameter("https://kg.scania.com/it/iris_orchestration/subIri_1",map);
    try {
      modelUnderTest.run(parameterMemory,jsonLdContextModel,serviceArguments);
      List iParamList = (List) ((HashMap) parameterMemory.
          getValue("https://kg.scania.com/it/iris_orchestration/output_iri")).
          get("param_response");
      assertEquals(queryRes, iParamList.get(0));
      verify(rdf4jClient, times(1))
              .doConstructSparql(construct, serviceArguments, parameterMemory, false);
    } catch (Exception e) {
      Assertions.fail(e.getMessage());
    }
  }

  @Test
  void run_without_reasoner_sparql() {
    modelUnderTest.setRdf4jClient(rdf4jClient);
    modelUnderTest.setEnableReasoner(false);
    modelUnderTest.setInputParameter(getinputParameterModel());
    modelUnderTest.setOutputParameter(getOutputParameterModel());

    String response = UnitTestHelper.readJsonFiles("QueryActionModelTestData3.json");
    JsonArray daAnswer = JsonParser.parseString(response).getAsJsonArray();
    doReturn(daAnswer).when(rdf4jClient).doSelectSparql(any(), any(), any(), anyBoolean());

    HashMap map = new HashMap();
    map.put("query", Collections.singletonList(selectQuery));

    ParameterMemory parameterMemory = new ParameterMemory();
    parameterMemory.putParameter("https://kg.scania.com/it/iris_orchestration/subIri_1",map);

    try {
      modelUnderTest.run(parameterMemory,jsonLdContextModel,serviceArguments);
      List iParamList = ((ArrayList) ((HashMap) parameterMemory.
          getValue("https://kg.scania.com/it/iris_orchestration/output_iri")).
          get("param_response"));

      assertEquals(res, iParamList.get(0));
      verify(rdf4jClient, times(1))
              .doSelectSparql(selectQuery, serviceArguments, parameterMemory, false);
    } catch (Exception e) {
      Assertions.fail(e.getMessage());
    }
  }

  @Test
  void run_invalidJson_error(){

    modelUnderTest.setRdf4jClient(rdf4jClient);
    modelUnderTest.setEnableReasoner(false);
    modelUnderTest.setInputParameter(getinputParameterModel());
    modelUnderTest.setOutputParameter(getOutputParameterModel());

    doThrow(JsonSyntaxException.class).when(daParams).getValue(any());

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      modelUnderTest.run(daParams, jsonLdContextModel, serviceArguments);
    });
    Assertions.assertEquals(SdipErrorCode.INVALID_JSONARRAY_RESPONSE.getSdipErrorCode(),
        Integer
            .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                .getSdipErrorCodes().get(0)
                .replace("SDIP_", "")));
  }

  @Test
  void run_catch_incidentException(){

    modelUnderTest.setRdf4jClient(rdf4jClient);
    modelUnderTest.setEnableReasoner(false);
    modelUnderTest.setInputParameter(getinputParameterModel());
    modelUnderTest.setOutputParameter(getOutputParameterModel());

    doThrow(new IncidentException(SdipErrorCode.SPARQL_ENDPOINT_EMPTY, LOGGER,
        SdipErrorParameter.SUPPORTMAIL)).
        when(daParams).getValue(any());
    Throwable throwable = assertThrows(IncidentException.class, () -> {
      modelUnderTest.run(daParams, jsonLdContextModel, serviceArguments);
    });

    Assertions.assertEquals(SdipErrorCode.SPARQL_ENDPOINT_EMPTY.getSdipErrorCode(),
        Integer
            .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                .getSdipErrorCodes().get(0)
                .replace("SDIP_", "")));
  }

  @Test
  void run_catch_Exception(){

    modelUnderTest.setRdf4jClient(rdf4jClient);
    modelUnderTest.setEnableReasoner(false);
    modelUnderTest.setInputParameter(getinputParameterModel());
    modelUnderTest.setOutputParameter(getOutputParameterModel());

    doThrow(RuntimeException.class).
        when(daParams).getValue(any());
    Throwable throwable = assertThrows(IncidentException.class, () -> {
      modelUnderTest.run(daParams, jsonLdContextModel, serviceArguments);
    });

    Assertions.assertEquals(SdipErrorCode.UNKNOWN_REASON_ERROR.getSdipErrorCode(),
        Integer
            .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                .getSdipErrorCodes().get(0)
                .replace("SDIP_", "")));
  }
}
