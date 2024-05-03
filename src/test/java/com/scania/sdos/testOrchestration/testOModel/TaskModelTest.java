package com.scania.sdos.testOrchestration.testOModel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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
import com.scania.sdos.model.ServiceArguments;
import com.scania.sdos.orchestration.OfgModelRepo;
import com.scania.sdos.orchestration.ParameterMemory;
import com.scania.sdos.orchestration.Rdf4jClient;
import com.scania.sdos.orchestration.factory.ActionModelFactory;
import com.scania.sdos.orchestration.factory.ConnectorModelFactory;
import com.scania.sdos.orchestration.factory.ParameterModelFactory;
import com.scania.sdos.orchestration.interfaces.IActionModel;
import com.scania.sdos.orchestration.interfaces.IConnectorModel;
import com.scania.sdos.orchestration.interfaces.IParameterModel;
import com.scania.sdos.orchestration.model.JsonLdContextModel;
import com.scania.sdos.orchestration.model.MetaDataModel;
import com.scania.sdos.orchestration.model.TaskModel;
import com.scania.sdos.testUtils.UnitTestHelper;
import java.util.HashMap;

import com.scania.sdos.testUtils.UnitTestConstants;
import org.junit.Assert;
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
public class TaskModelTest {

  private static MockedStatic actionModelFactory;
  private static MockedStatic connectorModelFactory;
  private static MockedStatic parameterModelFactory;

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
  @Spy
  private TaskModel taskModel;
  @Mock
  private Rdf4jClient rdf4jClient;
  @Mock
  private MetaDataModel metaDataModel;
  @Mock
  private JsonLdContextModel jsonLdContextModel;
  @Mock
  private OfgModelRepo ofgModelRepo;
  @BeforeAll
  static void beforeAll() {
    actionModelFactory = mockStatic(ActionModelFactory.class);
    connectorModelFactory = mockStatic(ConnectorModelFactory.class);
    parameterModelFactory = mockStatic(ParameterModelFactory.class);
  }


  @AfterAll
  static void afterAll() {
    actionModelFactory.close();
    connectorModelFactory.close();
    parameterModelFactory.close();

  }

  @BeforeEach
  void setUp() {
    reset(connectorModel);
    reset(taskModel);
    reset(rdf4jClient);
    reset(metaDataModel);
    reset(serviceArguments);
    connectorModelFactory.reset();
    actionModelFactory.reset();
    parameterModelFactory.reset();
  }

  @Test
  void populate_ok() {
    taskModel.setRdf4jClient(rdf4jClient);
    taskModel.setMetaDataModel(metaDataModel);
    taskModel.setContext(jsonLdContextModel);

    // test successful case
    String response = UnitTestHelper.readJsonFiles(UnitTestConstants.TASKMODELTESTDATA);
    JsonArray daAnswer = JsonParser.parseString(response).getAsJsonArray();
    doReturn(daAnswer).when(rdf4jClient).selectSparqlOfg(any(), any());
    doNothing().when(metaDataModel).populate(any(), any(), any());
    doNothing().when(jsonLdContextModel).populate(any(), any(), any());
    doReturn(jsonLdContextModel).when(taskModel).getNewJsonLdContextModel();

    HashMap userInput = new HashMap();
    userInput.put("Test11", "Test11-2");

    actionModelFactory.when(() ->
        ActionModelFactory.getAction(any(), any())).thenReturn(iActionModel);
    connectorModelFactory.when(() ->
        ConnectorModelFactory.getConnector(any())).thenReturn(connectorModel);
    parameterModelFactory.when(() ->
        ParameterModelFactory.getParameter(any())).thenReturn(parameterModel);
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();
    try {
      // run it!
      taskModel.populate(UnitTestConstants.TASKMODEL_TEST_SUBJECT_IRI, userInput, daParams, serviceArguments);

      // TODO: maybe remove hardcoded values?
      Assertions.assertEquals(iActionModel, taskModel.getNextAction());
      // TODO: add and test values for httpHeaders/Query/Body
      verify(jsonLdContextModel, times(1))
          .populate(UnitTestConstants.TEST_CONTEXT_URI, daParams, serviceArguments);

      verify(iActionModel, times(1))
          .populate(UnitTestConstants.TEST_ACTION_A, daParams, serviceArguments);
      verify(parameterModel, times(1))
          .populate("https://ahttptest#SYSTEMA", daParams, serviceArguments);
      actionModelFactory.verify(
          () -> ActionModelFactory.getAction(UnitTestConstants.TEST_ACTIONTYPE_SCRIPTACTION, daParams), times(1)
      );
      parameterModelFactory.verify(
          () -> ParameterModelFactory.getParameter(UnitTestConstants.TEST_INPUTPARAMETERTYPE_A), times(1)
      );
    } catch (Exception e) {
      Assert.fail();
    }
  }

  @Test
  void test_populateOnlyTask() {
    taskModel.setRdf4jClient(rdf4jClient);

    // test successful case
    String response = UnitTestHelper.readJsonFiles(UnitTestConstants.TASKMODELTESTDATA);
    JsonArray daAnswer = JsonParser.parseString(response).getAsJsonArray();
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();
    doReturn(daAnswer).when(rdf4jClient).selectSparqlOfg(any(), any());
    parameterModelFactory.when(() ->
        ParameterModelFactory.getParameter(any())).thenReturn(parameterModel);

    try {
      taskModel.populateOnlyTask(UnitTestConstants.TASKMODEL_TEST_SUBJECT_IRI, daParams, serviceArguments);

      verify(parameterModel, times(1))
          .populateHelp("https://ahttptest#SYSTEMA", daParams, serviceArguments);
      parameterModelFactory.verify(
          () -> ParameterModelFactory.getParameter(UnitTestConstants.TEST_INPUTPARAMETERTYPE_A), times(1)
      );
    } catch (Exception e) {
      Assertions.fail();
    }
  }

  @Test
  void populateOnlyTask_emptyResponse() {
    taskModel.setRdf4jClient(rdf4jClient);
    JsonArray daAnswer = JsonParser.parseString("[{}]").getAsJsonArray();
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(daAnswer).when(rdf4jClient).selectSparqlOfg(any(), any());

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      taskModel.populateOnlyTask("",daParams, serviceArguments);
    });
    assertEquals(SdipErrorCode.FAILED_TO_PARSE_JSONARRAY.getSdipErrorCode(),
        Integer
            .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                .getSdipErrorCodes().get(0)
                .replace("SDIP_", "")));
  }

  @Test
  void populate_invalidJsonArrayResponse() {
    taskModel.setRdf4jClient(rdf4jClient);
    doThrow(IllegalStateException.class).when(rdf4jClient).selectSparqlOfg(any(), any());
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();
    Throwable throwable = assertThrows(IncidentException.class, () -> {
      taskModel.populate(UnitTestConstants.TASKMODEL_TEST_SUBJECT_IRI, new HashMap(), daParams, serviceArguments);
    });
    assertEquals(SdipErrorCode.INVALID_JSONARRAY_RESPONSE.getSdipErrorCode(),
        Integer
            .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                .getSdipErrorCodes().get(0)
                .replace("SDIP_", "")));
  }

  @Test
  void populate_invalidPropertyJsonArray() {
    taskModel.setRdf4jClient(rdf4jClient);
    JsonArray daAnswer = JsonParser.parseString("[{\"key\":\"value\"}]").getAsJsonArray();
    doReturn(daAnswer).when(rdf4jClient).selectSparqlOfg(any(), any());
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();
    Throwable throwable = assertThrows(IncidentException.class, () -> {
      taskModel.populate(UnitTestConstants.TASKMODEL_TEST_SUBJECT_IRI, new HashMap(), daParams, serviceArguments);
    });
    assertEquals(SdipErrorCode.FAILED_TO_PARSE_JSONARRAY.getSdipErrorCode(),
        Integer
            .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                .getSdipErrorCodes().get(0)
                .replace("SDIP_", "")));
  }

  @Test
  void populateOnlyTask_invalidJsonArrayResponse() {
    taskModel.setRdf4jClient(rdf4jClient);
    doThrow(IllegalStateException.class).when(rdf4jClient).selectSparqlOfg(any(), any());
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();
    Throwable throwable = assertThrows(IncidentException.class, () -> {
      taskModel.populateOnlyTask(UnitTestConstants.TASKMODEL_TEST_SUBJECT_IRI, daParams, serviceArguments);
    });
    assertEquals(SdipErrorCode.INVALID_JSONARRAY_RESPONSE.getSdipErrorCode(),
        Integer
            .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                .getSdipErrorCodes().get(0)
                .replace("SDIP_", "")));
  }

  @Test
  void populateOnlyTask_invalidPropertyJsonArray() {
    taskModel.setRdf4jClient(rdf4jClient);
    JsonArray daAnswer = JsonParser.parseString("[{\"key\":\"value\"}]").getAsJsonArray();
    doReturn(daAnswer).when(rdf4jClient).selectSparqlOfg(any(), any());
    daParams.setOfgModelRepo(ofgModelRepo);
    Throwable throwable = assertThrows(IncidentException.class, () -> {
      taskModel.populateOnlyTask("",daParams, serviceArguments);
    });

    assertEquals(SdipErrorCode.FAILED_TO_PARSE_JSONARRAY.getSdipErrorCode(),
        Integer
            .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                .getSdipErrorCodes().get(0)
                .replace("SDIP_", "")));
  }


  @Test
  void populate_ModelFactoryReturnsNull() {
    taskModel.setRdf4jClient(rdf4jClient);
    String response = UnitTestHelper.readJsonFiles(UnitTestConstants.TASKMODELTESTDATA);
    JsonArray daAnswer = JsonParser.parseString(response).getAsJsonArray();
    doReturn(daAnswer).when(rdf4jClient).selectSparqlOfg(any(), any());
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();
    connectorModelFactory.when(() ->
        ConnectorModelFactory.getConnector(any())).thenReturn(null);

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      taskModel.populate(UnitTestConstants.TASKMODEL_TEST_SUBJECT_IRI, new HashMap(), daParams, serviceArguments);
    });
    assertEquals(SdipErrorCode.FAILED_TO_PARSE_JSONARRAY.getSdipErrorCode(),
        Integer
            .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                .getSdipErrorCodes().get(0)
                .replace("SDIP_", "")));

  }

  @Test
  void populate_shouldFailIfNoContext() {
    taskModel.setRdf4jClient(rdf4jClient);
    taskModel.setMetaDataModel(metaDataModel);

    String response = UnitTestHelper.readJsonFiles(UnitTestConstants.TASKMODELTESTDATA_NO_CONTEXT);
    JsonArray daAnswer = JsonParser.parseString(response).getAsJsonArray();
    doReturn(daAnswer).when(rdf4jClient).selectSparqlOfg(any(), any());
    Mockito.lenient().doNothing().when(metaDataModel).populate(any(), any(), any());

    HashMap userInput = new HashMap();
    userInput.put("Test11", "Test11-2");
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();
    actionModelFactory.when(() ->
        ActionModelFactory.getAction(any(), any())).thenReturn(iActionModel);
    connectorModelFactory.when(() ->
        ConnectorModelFactory.getConnector(any())).thenReturn(connectorModel);
    parameterModelFactory.when(() ->
        ParameterModelFactory.getParameter(any())).thenReturn(parameterModel);

    assertThrows(IncidentException.class,
            () -> taskModel.populate(UnitTestConstants.TASKMODEL_TEST_SUBJECT_IRI, userInput, daParams, serviceArguments));
  }
}