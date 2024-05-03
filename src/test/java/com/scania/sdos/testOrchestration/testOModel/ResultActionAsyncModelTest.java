package com.scania.sdos.testOrchestration.testOModel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
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
import com.scania.sdos.orchestration.RestTemplateClient;
import com.scania.sdos.orchestration.factory.ActionModelFactory;
import com.scania.sdos.orchestration.factory.ConnectorModelFactory;
import com.scania.sdos.orchestration.factory.ParameterModelFactory;
import com.scania.sdos.orchestration.interfaces.IActionModel;
import com.scania.sdos.orchestration.interfaces.IConnectorModel;
import com.scania.sdos.orchestration.interfaces.IParameterMemory;
import com.scania.sdos.orchestration.interfaces.IParameterModel;
import com.scania.sdos.orchestration.model.HttpBasicAuthModel;
import com.scania.sdos.orchestration.model.HttpConnectorModel;
import com.scania.sdos.orchestration.model.HttpParameterModel;
import com.scania.sdos.orchestration.model.JsonLdContextModel;
import com.scania.sdos.orchestration.model.ResultActionAsyncModel;
import com.scania.sdos.testUtils.UnitTestHelper;
import com.scania.sdos.utils.SDOSConstants;
import com.scania.sdos.utils.Utility;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import com.scania.sdos.orchestration.model.*;
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
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ResultActionAsyncModelTest {

  private static final Logger LOGGER = LogManager.getLogger(ResultActionAsyncModel.class);
  private static MockedStatic actionModelFactory;
  private static MockedStatic connectorModelFactory;
  private static MockedStatic parameterModelFactory;
  private static MockedStatic parameterMemoryFactory;
  private static MockedStatic utilityMock;
  private String subjectUri = "http://testsubjecturi";

  @Mock
  private HttpBasicAuthModel httpBasicAuthModel;
  @Mock
  private IConnectorModel connectorModel;
  @Mock
  private IActionModel iActionModel;
  @Mock
  private ParameterMemory daParams;
  @Mock
  private ServiceArguments serviceArguments;
  @Mock
  private IParameterModel parameterModel;
  @Mock
  private HttpConnectorModel httpConnectorModel;
  @Mock
  private JsonLdContextModel jsonLdContextModel;
  @Spy
  private ResultActionAsyncModel modelUnderTest;
  @Mock
  private Rdf4jClient rdf4jClient;
  @Mock
  private RestTemplateClient restTemplateClient;
  @Mock
  private OfgModelRepo ofgModelRepo;
  @Mock
  private HashMap hashMapMock;

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
    reset(rdf4jClient);
    reset(restTemplateClient);
    reset(modelUnderTest);
    connectorModelFactory.reset();
    actionModelFactory.reset();
    parameterModelFactory.reset();
    parameterMemoryFactory.reset();
    utilityMock.reset();
  }

  @Test
  void populate_ok() {
    modelUnderTest.setRdf4jClient(rdf4jClient);
    // test successful case
    String response = UnitTestHelper.readJsonFiles(UnitTestConstants.UPLOADACTIONMODELTESTDATA);
    JsonArray daAnswer = JsonParser.parseString(response).getAsJsonArray();

    doReturn(daAnswer).when(rdf4jClient).selectSparqlOfg(any(), any());
    actionModelFactory.when(() ->
        ActionModelFactory.getAction(any(), any())).thenReturn(iActionModel);
    connectorModelFactory.when(() ->
        ConnectorModelFactory.getConnector(any())).thenReturn(connectorModel);
    parameterModelFactory.when(() ->
        ParameterModelFactory.getParameter(any())).thenReturn(parameterModel);
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();
    // run it!

    try {
      modelUnderTest.populate(subjectUri, daParams, serviceArguments);

      // TODO: maybe remove hardcoded values?
      assertEquals(subjectUri, modelUnderTest.getSubjectIri());
      Assertions.assertEquals(iActionModel, modelUnderTest.getNextAction());
      assertEquals(connectorModel, modelUnderTest.getConnectorModel());
      // TODO: add and test values for httpHeaders/Query/Body
      assertEquals("user1", modelUnderTest.getHttpHeader().get(UnitTestConstants.USERNAME));
      assertEquals("aLabel", modelUnderTest.getLabel());

      verify(iActionModel, times(1))
          .populate(UnitTestConstants.TEST_ACTION_A, daParams, serviceArguments);
      verify(connectorModel, times(1))
          .populate(UnitTestConstants.TEST_CONNECTOR_A, daParams, serviceArguments);
      verify(parameterModel, times(1))
          .populate(UnitTestConstants.TEST_INPUTPARAMETER_A, daParams, serviceArguments);

      actionModelFactory.verify(
          () -> ActionModelFactory.getAction(UnitTestConstants.TEST_ACTIONTYPE_SCRIPTACTION, daParams),times(1)
      );
      connectorModelFactory.verify(
          () -> ConnectorModelFactory.getConnector(UnitTestConstants.TEST_CONNECTORTYPE_HTTP), times(1)
      );
      parameterModelFactory.verify(
          () -> ParameterModelFactory.getParameter("https://ahttptest#SYSTEMA"),
              times(1)
      );
    } catch (Exception e) {
      fail();
    }
  }

  @Test
  void populate_emptyResponse() {
    modelUnderTest.setRdf4jClient(rdf4jClient);

    // test empty response

    JsonArray daAnswer = JsonParser.parseString("[{}]").getAsJsonArray();
    doReturn(daAnswer).when(rdf4jClient).selectSparqlOfg(any(), any());
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();

    // run it
    Throwable throwable = assertThrows(IncidentException.class, () -> {
      modelUnderTest.populate("1", daParams, serviceArguments);
    });
    assertEquals(SdipErrorCode.FAILED_TO_PARSE_JSONARRAY.getSdipErrorCode(),
        Integer
            .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                .getSdipErrorCodes().get(0)
                .replace("SDIP_", "")));
  }

  @Test
  void populate_actionModelFactoryReturnsNull() {
    modelUnderTest.setRdf4jClient(rdf4jClient);

    // test ActionModelFactory returns null

    String response = UnitTestHelper.readJsonFiles(UnitTestConstants.UPLOADACTIONMODELTESTDATA);
    JsonArray daAnswer = JsonParser.parseString(response).getAsJsonArray();

    doReturn(daAnswer).when(rdf4jClient).selectSparqlOfg(any(), any());
    actionModelFactory.when(() ->
        ActionModelFactory.getAction(any(), any())).thenReturn(null);
    connectorModelFactory.when(() ->
        ConnectorModelFactory.getConnector(any())).thenReturn(connectorModel);
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();

    // run it!
    Throwable throwable = assertThrows(IncidentException.class, () -> {
      modelUnderTest.populate("1", daParams, serviceArguments);
    });

    assertEquals(SdipErrorCode.FAILED_TO_PARSE_JSONARRAY.getSdipErrorCode(),
        Integer
            .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                .getSdipErrorCodes().get(0)
                .replace("SDIP_", "")));
  }

  @Test
  void populate_connectorModelFactoryReturnsNull() {
    modelUnderTest.setRdf4jClient(rdf4jClient);

    // test connectorModelFactory returns null
    String response = UnitTestHelper.readJsonFiles(UnitTestConstants.UPLOADACTIONMODELTESTDATA);
    JsonArray daAnswer = JsonParser.parseString(response).getAsJsonArray();
    doReturn(daAnswer).when(rdf4jClient).selectSparqlOfg(any(), any());
    connectorModelFactory.when(() ->
        ConnectorModelFactory.getConnector(any())).thenReturn(null);
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();

    // run it!
    Throwable throwable = assertThrows(IncidentException.class, () -> {
      modelUnderTest.populate("1", daParams, serviceArguments);
    });

    assertEquals(SdipErrorCode.FAILED_TO_PARSE_JSONARRAY.getSdipErrorCode(),
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

    assertEquals(SdipErrorCode.FAILED_TO_PARSE_JSONARRAY.getSdipErrorCode(),
        Integer
            .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                .getSdipErrorCodes().get(0)
                .replace("SDIP_", "")));
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

    assertEquals(SdipErrorCode.INVALID_JSONARRAY_RESPONSE.getSdipErrorCode(),
        Integer
            .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                .getSdipErrorCodes().get(0)
                .replace("SDIP_", "")));

  }

  @Test
  void populate_catchIncidentException() {
    ResultActionAsyncModel modelUnderTest = new ResultActionAsyncModel();
    modelUnderTest.setRdf4jClient(rdf4jClient);
    doThrow(new IncidentException(SdipErrorCode.SPARQL_ENDPOINT_EMPTY, LOGGER,
        SdipErrorParameter.SUPPORTMAIL)).
        when(rdf4jClient).selectSparqlOfg(any(), any());
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      modelUnderTest.populate("1", daParams, serviceArguments);
    });

    assertEquals(SdipErrorCode.SPARQL_ENDPOINT_EMPTY.getSdipErrorCode(),
        Integer
            .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                .getSdipErrorCodes().get(0)
                .replace("SDIP_", "")));
  }

  @Test
  void run_catchIncidentException() throws UnsupportedEncodingException {
    doThrow(new IncidentException(SdipErrorCode.UNKNOWN_REASON_ERROR, LOGGER,
        SdipErrorParameter.SUPPORTMAIL, "Test_Message")).
        when(modelUnderTest).getConnectorModel();
    Throwable throwable = assertThrows(IncidentException.class, () -> {
      modelUnderTest.run(daParams, jsonLdContextModel, serviceArguments);
    });

    assertEquals(SdipErrorCode.UNKNOWN_REASON_ERROR.getSdipErrorCode(),
        Integer
            .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                .getSdipErrorCodes().get(0)
                .replace("SDIP_", "")));
  }

  @Test
  void run_returnsNull() {
    modelUnderTest.setInputParameter(null);
    Throwable throwable = assertThrows(IncidentException.class, () -> {
      modelUnderTest.run(daParams, jsonLdContextModel, serviceArguments);
    });
    assertEquals(SdipErrorCode.UNKNOWN_REASON_ERROR.getSdipErrorCode(),
        Integer
            .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                .getSdipErrorCodes().get(0)
                .replace("SDIP_", "")));
  }

  @Test
  void test_run_skipped() {
    ParameterMemory parameterMemory = new ParameterMemory();
    StringBuilder stringBuilder = new StringBuilder();
    modelUnderTest.setInputParameter(parameterModel);

    doReturn("https://INPUT").when(parameterModel).getSubjectIri();

    try {
      modelUnderTest.run(parameterMemory, jsonLdContextModel, serviceArguments);
    } catch (Exception e) {
      Assertions.fail(e.getMessage());
    }
  }

  @Test
  void test_run() throws UnsupportedEncodingException {
    //to test without inputparameter
    modelUnderTest.setRdf4jClient(rdf4jClient);
    modelUnderTest.setConnectorModel(httpConnectorModel);
    modelUnderTest.setRestTemplateClient(restTemplateClient);
    modelUnderTest.setHttpbody("testHttpbody");
    modelUnderTest.setHttpHeader(hashMapMock);

    doReturn("").when(modelUnderTest)
        .prepareRequest(any(), any(), any(), any(), any(), any(), any(), any());
    try {
      modelUnderTest.run(daParams, jsonLdContextModel, serviceArguments);
      verify(modelUnderTest, times(1)).
          prepareRequest(any(), any(), any(), any(), any(), any(), any(), any());
    } catch (Exception e) {
      Assertions.fail(e.getMessage());
    }
  }

  @Test
  void test_run_ExecutorService() throws InterruptedException {
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
    modelUnderTest.setInputParameter(httpParameterModel);

    ExecutorService executorService = Mockito.mock(ExecutorService.class);
    Set set = Mockito.mock(Set.class);
    utilityMock.when(() -> Utility.getActionIteration(any(), any())).thenReturn(3);
    doReturn(3).when(modelUnderTest).getThreadPoolSize(anyInt(), any());
    doReturn(executorService).when(modelUnderTest).getExecutorService(anyInt());
    doReturn(set).when(modelUnderTest).getCallableSet();
    doNothing().when(modelUnderTest).isThreadActive(any());

    try {
      modelUnderTest.run(parameterMemory, jsonLdContextModel, serviceArguments);
      verify(executorService, times(1)).
          invokeAll(any());
      verify(modelUnderTest, times(1)).
          isThreadActive(executorService);
      verify(modelUnderTest, times(1)).
          getExecutorService(3);
      verify(modelUnderTest, times(1)).
          getCallableSet();
      verify(set, times(3)).
          add(any());
    } catch (Exception e) {
      Assertions.fail(e.getMessage());
    }
  }

  @Test
  void test_run_without_ExecutorService()
      throws InterruptedException, UnsupportedEncodingException {
    ParameterMemory parameterMemory = new ParameterMemory();
    HttpParameterModel httpParameterModel = new HttpParameterModel();
    httpParameterModel.setRdf4jClient(rdf4jClient);
    httpParameterModel.setSubjectIri("https://kg.scania.com/it/iris_orchestration/subIri_1");
    httpParameterModel.setBody("");

    HashMap<String, List<String>> hashMap = new HashMap<>();
    hashMap.put("endpoint", Collections.singletonList("testEndPoint"));
    hashMap.put("httpBody", Collections.singletonList("testHttpBody_1"));
    parameterMemory.putParameter("https://kg.scania.com/it/iris_orchestration/subIri_1", hashMap);

    modelUnderTest.setInputParameter(httpParameterModel);
    ExecutorService executorService = Mockito.mock(ExecutorService.class);
    Set set = Mockito.mock(Set.class);
    utilityMock.when(() -> Utility.getActionIteration(any(), any())).thenReturn(1);
    doReturn("").when(modelUnderTest)
        .prepareRequest(any(), any(), any(), any(), any(), any(), any(), any());

    try {
      modelUnderTest.run(parameterMemory, jsonLdContextModel, serviceArguments);
      verify(executorService, times(0)).
          invokeAll(any());
      verify(modelUnderTest, times(0)).
          isThreadActive(executorService);
      verify(modelUnderTest, times(0)).
          getExecutorService(3);
      verify(modelUnderTest, times(0)).
          getCallableSet();
      verify(set, times(0)).
          add(any());
      verify(modelUnderTest, times(1)).
          prepareRequest(any(), any(), any(), any(), any(), any(), any(), any());
    } catch (Exception e) {
      Assertions.fail(e.getMessage());
    }
  }


  @Test
  void populate_getActionVirtualGraphAction() {
    modelUnderTest.setRdf4jClient(rdf4jClient);
    // test successful case
    String response = UnitTestHelper.readJsonFiles(UnitTestConstants.UPLOADACTIONMODELTESTDATA_VIRTUALGRAPHACTION);
    JsonArray daAnswer = JsonParser.parseString(response).getAsJsonArray();
    doReturn(daAnswer).when(rdf4jClient).selectSparqlOfg(any(), any());
    actionModelFactory.when(() ->
        ActionModelFactory.getAction(any(), any())).thenReturn(iActionModel);
    connectorModelFactory.when(() ->
        ConnectorModelFactory.getConnector(any())).thenReturn(connectorModel);
    parameterModelFactory.when(() ->
        ParameterModelFactory.getParameter(any())).thenReturn(parameterModel);
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();

    // run it!
    modelUnderTest.populate("next/action/IRI", daParams, serviceArguments);

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
    modelUnderTest.setRdf4jClient(rdf4jClient);
    String sparql = ResultActionModel.SPARQL.replace(SDOSConstants.VARIABLE, "aSubjectIri");
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
      modelUnderTest.populate("https://atest#VirtualGraphAction", daParams, serviceArguments);
    });
    assertEquals(SdipErrorCode.FAILED_TO_PARSE_JSONARRAY.getSdipErrorCode(),
        Integer.parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
            .getSdipErrorCodes().get(0)
            .replace("SDIP_", "")));
  }
}
