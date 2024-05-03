package com.scania.sdos.testOrchestration.testOModel;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import com.scania.sdos.orchestration.JavaGroovy;
import com.scania.sdos.orchestration.OfgModelRepo;
import com.scania.sdos.orchestration.ParameterMemory;
import com.scania.sdos.orchestration.Rdf4jClient;
import com.scania.sdos.orchestration.factory.ActionModelFactory;
import com.scania.sdos.orchestration.factory.ParameterModelFactory;
import com.scania.sdos.orchestration.factory.ScriptModelFactory;
import com.scania.sdos.orchestration.interfaces.IActionModel;
import com.scania.sdos.orchestration.interfaces.IParameterModel;
import com.scania.sdos.orchestration.interfaces.IScriptModel;
import com.scania.sdos.orchestration.model.GroovyScriptModel;
import com.scania.sdos.orchestration.model.JsonLdContextModel;
import com.scania.sdos.orchestration.model.ScriptActionModel;
import com.scania.sdos.services.config.SpringContext;
import com.scania.sdos.testUtils.UnitTestHelper;
import com.scania.sdos.utils.SDOSConstants;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import com.scania.sdos.testUtils.UnitTestConstants;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ScriptActionModelTest {
  private static final Logger LOGGER = LogManager.getLogger(ScriptActionModelTest.class);
  private static MockedStatic scriptModelFactory;
  private static MockedStatic actionModelFactory;
  private static MockedStatic parameterModelFactory;
  private static MockedStatic springContext;
  private String SCRIPT = "https://kg.scania.com/it/iris_orchestration/SCRIPT1";
  private String POLARION_TO_OAS = "https://kg.scania.com/it/iris_orchestration/PARAM_POLARION_TO_OAS";
  @Mock
  private IScriptModel iScriptModel;
  @Mock
  private IActionModel iActionModel;
  @Mock
  private ParameterMemory daParams;
  @Mock
  private ServiceArguments serviceArguments;
  @Mock
  private IParameterModel parameterModel;
  @Mock
  private IParameterModel outputParameterModel;
  @Spy
  private ScriptActionModel scriptActionModel;
  @Mock
  private Rdf4jClient rdf4jClient;
  @Mock
  private JavaGroovy javaGroovy;
  @Spy
  private GroovyScriptModel groovyScriptModel;
  @Spy
  private ParameterMemory daParamsSpy;
  @Mock
  private JsonLdContextModel jsonLdContextModel;
  @Mock
  private OfgModelRepo ofgModelRepo;
  @BeforeAll
  static void beforeAll() {
    scriptModelFactory = mockStatic(ScriptModelFactory.class);
    actionModelFactory = mockStatic(ActionModelFactory.class);
    parameterModelFactory = mockStatic(ParameterModelFactory.class);
    springContext = mockStatic(SpringContext.class);
  }

  @AfterAll
  static void afterAll() {
    scriptModelFactory.close();
    actionModelFactory.close();
    parameterModelFactory.close();
    springContext.close();
  }

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(groovyScriptModel, "javaGroovy", javaGroovy);
    reset(daParamsSpy);
    reset(iActionModel);
    reset(iScriptModel);
    reset(rdf4jClient);
    actionModelFactory.reset();
    scriptModelFactory.reset();
    parameterModelFactory.reset();
  }

  @Test
  void populate_ok() {
    ScriptActionModel actionModel = new ScriptActionModel();
    actionModel.setRdf4jClient(rdf4jClient);

    // test successful case
    JsonArray daAnswer = UnitTestHelper.getResponse(UnitTestConstants.SCRIPTACTIONMODELTESTDATA);
    doReturn(daAnswer).when(rdf4jClient).selectSparqlOfg(any(), any());
    scriptModelFactory.when(() ->
            ScriptModelFactory.getScript(any())).thenReturn(iScriptModel);
    actionModelFactory.when(() ->
            ActionModelFactory.getAction(any(), any())).thenReturn(iActionModel);
    parameterModelFactory.when(() ->
            ParameterModelFactory.getParameter(any())).thenReturn(parameterModel);
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();

    try {
      // run it!
      actionModel.populate(UnitTestConstants.SUBJECTURI, daParams, serviceArguments);
      // check it!
      Assertions.assertEquals(UnitTestConstants.SUBJECTURI, actionModel.getSubjectIri());
      Assertions.assertEquals(iScriptModel, actionModel.getScript());
      Assertions.assertEquals(iActionModel, actionModel.getNextAction());
      assertEquals("aLabel", actionModel.getLabel());

      // TODO: maybe remove hardcoded values?
      verify(iScriptModel, times(1))
              .populate(SCRIPT, daParams, serviceArguments);
      verify(iActionModel, times(1))
              .populate("3", daParams, serviceArguments);
      verify(parameterModel, times(1))
              .populate(UnitTestConstants.PARAM_POLARION_RAW, daParams, serviceArguments);
      verify(parameterModel, times(1))
              .populate(UnitTestConstants.PARAM_SCRIPT_OUTPUT, daParams, serviceArguments);
      scriptModelFactory.verify(
              () -> ScriptModelFactory
                      .getScript("https://kg.scania.com/it/iris_orchestration/GroovyScript"),
              times(1)
      );

      actionModelFactory.verify(
              () -> ActionModelFactory
                      .getAction("https://kg.scania.com/it/iris_orchestration/ResultAction", daParams),
              times(1)
      );

      parameterModelFactory.verify(
              () -> ParameterModelFactory
                      .getParameter("https://kg.scania.com/it/iris_orchestration/Parameter"),
              times(1)
      );

      parameterModelFactory.verify(
              () -> ParameterModelFactory
                      .getParameter("https://kg.scania.com/it/iris_orchestration/HTTPParameter"),
              times(1)
      );

    } catch (Exception e) {
      Assert.fail();
    }
  }


  @Test
  void populate_emptyResponse() {
    scriptActionModel.setRdf4jClient(rdf4jClient);
    JsonArray daAnswer = JsonParser.parseString("[{}]").getAsJsonArray();
    doReturn(daAnswer).when(rdf4jClient).selectSparqlOfg(any(), any());
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      scriptActionModel.populate("1", daParams, serviceArguments);
    });

    assertEquals(SdipErrorCode.FAILED_TO_PARSE_JSONARRAY.getSdipErrorCode(),
            Integer
                    .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                            .getSdipErrorCodes().get(0)
                            .replace("SDIP_", "")));

  }

  @Test
  void populate_unknownError() {
    scriptActionModel.setRdf4jClient(rdf4jClient);
    doThrow(NotImplementedException.class).when(rdf4jClient)
            .selectSparqlOfg(any(), any());
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      scriptActionModel.populate("1", daParams, serviceArguments);
    });

    assertEquals(SdipErrorCode.UNKNOWN_REASON_ERROR.getSdipErrorCode(),
            Integer
                    .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                            .getSdipErrorCodes().get(0)
                            .replace("SDIP_", "")));

  }


  @Test
  void populate_scriptModelFactoryReturnsNull() {
    scriptActionModel.setRdf4jClient(rdf4jClient);
    JsonArray daAnswer = UnitTestHelper.getResponse(UnitTestConstants.SCRIPTACTIONMODELTESTDATA);
    doReturn(daAnswer).when(rdf4jClient).selectSparqlOfg(any(), any());
    scriptModelFactory.when(() ->
            ScriptModelFactory.getScript(any())).thenReturn(null);
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      scriptActionModel.populate("1", daParams, serviceArguments);
    });

    assertEquals(SdipErrorCode.FAILED_TO_PARSE_JSONARRAY.getSdipErrorCode(),
            Integer
                    .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                            .getSdipErrorCodes().get(0)
                            .replace("SDIP_", "")));
  }


  @Test
  void populate_actionModelFactoryReturnsNull() {
    scriptActionModel.setRdf4jClient(rdf4jClient);
    JsonArray daAnswer = UnitTestHelper.getResponse(UnitTestConstants.SCRIPTACTIONMODELTESTDATA);
    doReturn(daAnswer).when(rdf4jClient).selectSparqlOfg(any(), any());
    scriptModelFactory.when(() ->
            ScriptModelFactory.getScript(any())).thenReturn(iScriptModel);
    actionModelFactory.when(() ->
            ActionModelFactory.getAction(any(), any())).thenReturn(null);
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      scriptActionModel.populate("1", daParams, serviceArguments);
    });

    assertEquals(SdipErrorCode.FAILED_TO_PARSE_JSONARRAY.getSdipErrorCode(),
            Integer
                    .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                            .getSdipErrorCodes().get(0)
                            .replace("SDIP_", "")));

  }

  @Test
  void populate_invalidJsonArrayResponse() {
    scriptActionModel.setRdf4jClient(rdf4jClient);
    doThrow(IllegalStateException.class).when(rdf4jClient)
            .selectSparqlOfg(any(), any());
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      scriptActionModel.populate("1", daParams, serviceArguments);
    });
    assertEquals(SdipErrorCode.INVALID_JSONARRAY_RESPONSE.getSdipErrorCode(),
            Integer
                    .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                            .getSdipErrorCodes().get(0)
                            .replace("SDIP_", "")));

  }

  @Test
  void populate_invalidPropertyJsonArray() {
    scriptActionModel.setRdf4jClient(rdf4jClient);
    JsonArray daAnswer = JsonParser.parseString("[{\"key\":\"value\"}]").getAsJsonArray();
    doReturn(daAnswer).when(rdf4jClient).selectSparqlOfg(any(), any());
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      scriptActionModel.populate("1", daParams, serviceArguments);
    });

    assertEquals(SdipErrorCode.FAILED_TO_PARSE_JSONARRAY.getSdipErrorCode(),
            Integer
                    .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                            .getSdipErrorCodes().get(0)
                            .replace("SDIP_", "")));

  }

  @Test
  void populate_catchIncidentException() {
    scriptActionModel.setRdf4jClient(rdf4jClient);
    JsonArray daAnswer = JsonParser.parseString("[{\"key\":\"value\"}]").getAsJsonArray();
    doThrow(new IncidentException(SdipErrorCode.SCRIPT_INVALID, LOGGER, "InvalidScript"))
            .when(rdf4jClient).selectSparqlOfg(any(), any());
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      scriptActionModel.populate("", daParams, serviceArguments);
    });
    assertEquals(SdipErrorCode.SCRIPT_INVALID.getSdipErrorCode(),
            Integer
                    .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                            .getSdipErrorCodes().get(0)
                            .replace("SDIP_", "")));

  }

  @Test
  void populate_getActionVirtualGraphAction() {
    scriptActionModel.setRdf4jClient(rdf4jClient);
    // test successful case
    String response = UnitTestHelper.readJsonFiles(UnitTestConstants.SCRIPTACTIONMODELTESTDATA_VIRTUALGRAPHACTION);
    JsonArray daAnswer = JsonParser.parseString(response).getAsJsonArray();
    doReturn(daAnswer).when(rdf4jClient).selectSparqlOfg(any(), any());
    scriptModelFactory.when(() ->
            ScriptModelFactory.getScript(any())).thenReturn(iScriptModel);
    actionModelFactory.when(() ->
            ActionModelFactory.getAction(any(), any())).thenReturn(iActionModel);
    parameterModelFactory.when(() ->
            ParameterModelFactory.getParameter(any())).thenReturn(parameterModel);
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();

    // run it!
    scriptActionModel.populate(UnitTestConstants.SUBJECTURI, daParams, serviceArguments);

    verify(iActionModel, times(1))
            .populate("3", daParams, serviceArguments);
    actionModelFactory.verify(
            () -> ActionModelFactory.getAction(
                    "https://kg.scania.com/it/iris_orchestration/VirtualGraphAction", daParams),
            times(1)
    );
  }

  @Test
  void populate_missingNextActionType() {
    //reset(doThrow());
    // Testing that the SPARQL-query consists of a UNION for VirtualGraphAction as nextactiontype
    scriptActionModel.setRdf4jClient(rdf4jClient);
    String sparql = ScriptActionModel.SPARQL.replace(SDOSConstants.VARIABLE, "aSubjectIri");
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
      scriptActionModel.populate("https://atest#VirtualGraphAction", daParams, serviceArguments);
    });
    assertEquals(SdipErrorCode.FAILED_TO_PARSE_JSONARRAY.getSdipErrorCode(),
            Integer.parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                    .getSdipErrorCodes().get(0)
                    .replace("SDIP_", "")));
  }

  @Test
  void populate_w_context() {
    scriptActionModel.setRdf4jClient(rdf4jClient);
    // TODO: Test run with context, check correct context in scriptParams!
    // test successful case
    JsonArray daAnswer = UnitTestHelper.getResponse(UnitTestConstants.SCRIPTACTIONMODELTESTDATA_W_CONTEXT);
    doReturn(daAnswer).when(rdf4jClient).selectSparqlOfg(any(), any());
    JsonLdContextModel anotherContext = Mockito.mock(JsonLdContextModel.class);
    doReturn(anotherContext).when(scriptActionModel).getNewJsonLdContextModel();
    doNothing().when(anotherContext).populate(any(), any(), any());
    scriptModelFactory.when(() ->
            ScriptModelFactory.getScript(any())).thenReturn(iScriptModel);
    actionModelFactory.when(() ->
            ActionModelFactory.getAction(any(), any())).thenReturn(iActionModel);
    parameterModelFactory.when(() ->
            ParameterModelFactory.getParameter(any())).thenReturn(parameterModel);
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();

    try {
      // run it!
      scriptActionModel.populate(UnitTestConstants.SUBJECTURI, daParams, serviceArguments);
      // check it!
      Assertions.assertEquals(UnitTestConstants.SUBJECTURI, scriptActionModel.getSubjectIri());
      Assertions.assertEquals(iScriptModel, scriptActionModel.getScript());
      Assertions.assertEquals(iActionModel, scriptActionModel.getNextAction());
      assertEquals("aLabel", scriptActionModel.getLabel());
      assertEquals(anotherContext, scriptActionModel.getContextModel());
      verify(iScriptModel, times(1))
              .populate(SCRIPT, daParams, serviceArguments);
      verify(iActionModel, times(1))
              .populate("3", daParams, serviceArguments);
      verify(parameterModel, times(1))
              .populate(UnitTestConstants.PARAM_POLARION_RAW, daParams, serviceArguments);
      verify(parameterModel, times(1))
              .populate(UnitTestConstants.PARAM_SCRIPT_OUTPUT, daParams, serviceArguments);
      verify(anotherContext, times(1))
              .populate(UnitTestConstants.ACONTEXT_URI, daParams, serviceArguments);

      scriptModelFactory.verify(
              () -> ScriptModelFactory
                      .getScript("https://kg.scania.com/it/iris_orchestration/GroovyScript"),
              times(1)
      );

      actionModelFactory.verify(
              () -> ActionModelFactory
                      .getAction("https://kg.scania.com/it/iris_orchestration/ResultAction", daParams),
              times(1)
      );

      parameterModelFactory.verify(
              () -> ParameterModelFactory
                      .getParameter("https://kg.scania.com/it/iris_orchestration/Parameter"),
              times(1)
      );

      parameterModelFactory.verify(
              () -> ParameterModelFactory
                      .getParameter("https://kg.scania.com/it/iris_orchestration/HTTPParameter"),
              times(1)
      );

    } catch (Exception e) {
      Assert.fail();
    }
  }

  @Test
  void test_run() {
    JsonLdContextModel anotherContext = Mockito.mock(JsonLdContextModel.class);

    HashMap<String, HashMap<String, List<String>>> resList = new HashMap();
    HashMap<String, List<String>> map = new HashMap<>();
    map.put("utf", Collections.singletonList("18"));
    HashMap<String, List<String>> map1 = new HashMap<>();
    map1.put("utf", Collections.singletonList("19"));
    resList.put(POLARION_TO_OAS, map);
    resList.put("http://outputIRI2", map1);

    groovyScriptModel.setSubjectIri(SCRIPT);
    groovyScriptModel.setScript(UnitTestConstants.GROOVYSCRIPT);

    iScriptModel = groovyScriptModel;

    List<String> inputKeys = new ArrayList<>();
    inputKeys.add("KEY");
    inputKeys.add("KEY2");
    HashMap<String, List<String>> inputContent = new HashMap<>();
    inputContent.put("KEY", Collections.singletonList("VALUE"));
    inputContent.put("KEY2", Collections.singletonList("VALUE2"));

    List<IParameterModel> inputParameter = new ArrayList();
    inputParameter.add(parameterModel);
    List<IParameterModel> outputParameter = new ArrayList();
    outputParameter.add(outputParameterModel);

    scriptActionModel.setInputParameter(parameterModel);
    scriptActionModel.setOutputParameter(outputParameterModel);
    scriptActionModel.setScript(iScriptModel);
    scriptActionModel.setContextModel(anotherContext);

    doReturn("outputLabel").when(outputParameterModel).getLabel();
    doReturn("http://outputIRI").when(outputParameterModel).getSubjectIri();
    doReturn(inputKeys).when(parameterModel).getKeys();
    doReturn("http://inputIRI").when(parameterModel).getSubjectIri();
    doReturn(inputContent).when(daParamsSpy).getValue("http://inputIRI");
    doReturn("anotherContext").when(anotherContext).getContext();
    HashMap<String, String> expectedScriptInput = new LinkedHashMap<>();
    expectedScriptInput.put(SDOSConstants.CONTEXT, "anotherContext");
    expectedScriptInput.put(SDOSConstants.OUTPUTPARAMETERS,
            "{\"outputLabel\":\"http://outputIRI\"}");
    expectedScriptInput.put(SDOSConstants.INPUTPARAMETERS,
            "{\"KEY\":[{\"http://inputIRI\":\"[\\\"VALUE\\\"]\"}]}");
    springContext.when(() ->
            SpringContext.getBean(any())).thenReturn(javaGroovy);
    doReturn(resList).when(javaGroovy).runGroovyShell(any(), any());
    try {
      scriptActionModel.run(daParamsSpy, jsonLdContextModel, serviceArguments);

      verify(iScriptModel, times(1))
              .executeScript(daParamsSpy,jsonLdContextModel, serviceArguments, anotherContext,
                      outputParameter, inputParameter);

      assertEquals("18", daParamsSpy.getValue(POLARION_TO_OAS).get("utf").get(0));
      assertEquals("19", daParamsSpy.getValue("http://outputIRI2").get("utf").get(0));
    } catch (Exception e) {
      Assert.fail();
    }
  }

  @Test
  void test_run_multiple_input_output() {
    JsonLdContextModel anotherContext1 = Mockito.mock(JsonLdContextModel.class);
    GroovyScriptModel groovy = new GroovyScriptModel();
    HashMap<String, List<String>> map = new HashMap<>();
    map.put("utf", Collections.singletonList("18"));
    HashMap<String, List<String>> map1 = new HashMap<>();
    map1.put("utf", Collections.singletonList("19"));
    HashMap scriptResult = new HashMap();
    scriptResult.put("http://outputIRI1", map);
    scriptResult.put("http://outputIRI2", map1);
    groovy.setSubjectIri(SCRIPT);
    groovy.setScript(UnitTestConstants.GROOVYSCRIPT);

    List<IParameterModel> inputParameter = new ArrayList();
    inputParameter.add(parameterModel);
    inputParameter.add(parameterModel);
    List<IParameterModel> outputParameter = new ArrayList();
    outputParameter.add(outputParameterModel);
    outputParameter.add(outputParameterModel);

    StringBuilder stringBuilder = new StringBuilder();
    scriptActionModel.setInputParameter(parameterModel);
    scriptActionModel.setOutputParameter(outputParameterModel);
    scriptActionModel.setInputParameter(parameterModel);
    scriptActionModel.setOutputParameter(outputParameterModel);
    scriptActionModel.setContextModel(anotherContext1);
    scriptActionModel.setScript(iScriptModel);
    springContext.when(() ->
            SpringContext.getBean(any())).thenReturn(javaGroovy);
    doReturn(scriptResult).when(iScriptModel).executeScript(any(ParameterMemory.class), any(JsonLdContextModel.class),
            any(ServiceArguments.class), any(JsonLdContextModel.class),
            any(List.class), any(List.class));

    try {
      scriptActionModel.run(daParamsSpy, jsonLdContextModel, serviceArguments);
      assertEquals("18", daParamsSpy.getValue("http://outputIRI1").get("utf").get(0));
      assertEquals("19", daParamsSpy.getValue("http://outputIRI2").get("utf").get(0));
      assertEquals(2, scriptActionModel.getOutputParameter().size());
      assertEquals(2, scriptActionModel.getInputParameter().size());

      verify(iScriptModel, times(1))
              .executeScript(daParamsSpy,jsonLdContextModel, serviceArguments, anotherContext1,
                      outputParameter, inputParameter);

      assertTrue(daParamsSpy.getValue("http://outputIRI1") != null);
      assertTrue(daParamsSpy.getValue("http://outputIRI2") != null);

      //Test with contextModel
      JsonLdContextModel anotherContext2 = Mockito.mock(JsonLdContextModel.class);
      scriptActionModel.setContextModel(anotherContext2);
      scriptActionModel.run(daParamsSpy, jsonLdContextModel, serviceArguments);
      assertEquals("18", daParamsSpy.getValue("http://outputIRI1").get("utf").get(0));
    } catch (Exception e) {
      Assert.fail();
    }
  }

  @Test
  void run_catchUnknownReasonError() {
    ParameterMemory parameterMemory = new ParameterMemory();
    scriptActionModel.setScript(groovyScriptModel);
    doReturn(null).when(groovyScriptModel).executeScript(any(), any(),any(), any(),any(), any());
    Throwable throwable = assertThrows(IncidentException.class, () -> {
      scriptActionModel.run(parameterMemory, jsonLdContextModel, serviceArguments);
    });
    assertEquals(SdipErrorCode.UNKNOWN_REASON_ERROR.getSdipErrorCode(),
            Integer
                    .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                            .getSdipErrorCodes().get(0)
                            .replace("SDIP_", "")));

  }

  @Test
  void test_run_no_context_at_all() {
    GroovyScriptModel groovy = new GroovyScriptModel();
    scriptActionModel.setScript(groovy);
    scriptActionModel.setOutputParameter(outputParameterModel);
    scriptActionModel.setInputParameter(parameterModel);
    scriptActionModel.setContextModel(null);
    assertThrows(IncidentException.class, () -> {scriptActionModel.run(daParamsSpy, null, serviceArguments);});
  }

  @Test
  void test_run_NPE() {
    JsonLdContextModel anotherContext = Mockito.mock(JsonLdContextModel.class);
    GroovyScriptModel groovy = new GroovyScriptModel();
    scriptActionModel.setScript(groovy);
    scriptActionModel.setOutputParameter(outputParameterModel);
    scriptActionModel.setInputParameter(parameterModel);
    scriptActionModel.setContextModel(anotherContext);
    assertThrows(IncidentException.class, () -> {scriptActionModel.run(daParamsSpy, jsonLdContextModel, serviceArguments);});
  }
}
