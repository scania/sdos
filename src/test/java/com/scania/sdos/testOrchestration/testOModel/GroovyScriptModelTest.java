package com.scania.sdos.testOrchestration.testOModel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.reset;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.scania.sdip.exceptions.IncidentException;
import com.scania.sdip.exceptions.SdipErrorCode;
import com.scania.sdip.exceptions.SdipErrorParameter;
import com.scania.sdos.model.ServiceArguments;
import com.scania.sdos.orchestration.JavaGroovy;
import com.scania.sdos.orchestration.OfgModelRepo;
import com.scania.sdos.orchestration.ParameterMemory;
import com.scania.sdos.orchestration.Rdf4jClient;
import com.scania.sdos.orchestration.interfaces.IParameterModel;
import com.scania.sdos.orchestration.model.GroovyScriptModel;
import com.scania.sdos.orchestration.model.JsonLdContextModel;
import com.scania.sdos.services.config.SpringContext;
import com.scania.sdos.testUtils.UnitTestHelper;
import com.scania.sdos.utils.Utility;
import com.scania.sdos.testUtils.UnitTestConstants;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class GroovyScriptModelTest {

  private static final Logger LOGGER = LogManager.getLogger(GroovyScriptModelTest.class);

  static final String VARIABLE = "VARIABLE";
  private static MockedStatic utilityMock;
  private static MockedStatic springContextGroovy;

  @Mock
  private ParameterMemory daParams;
  @Mock
  private ServiceArguments serviceArguments;
  @Mock
  private Rdf4jClient rdf4jClient;
  @Spy
  private GroovyScriptModel groovyScriptModel;
  @Mock
  private JavaGroovy javaGroovy;
  @Mock
  private JsonLdContextModel jsonLdContextModel;
  @Mock
  private IParameterModel outputParameterModel;
  @Mock
  private IParameterModel inputParameterModel;
  @Mock
  private OfgModelRepo ofgModelRepo;
  @Before
  public void init() {
    MockitoAnnotations.initMocks(this);
  }

  @BeforeAll
  static void beforeAll() {
    utilityMock = mockStatic(Utility.class);
    springContextGroovy = mockStatic(SpringContext.class);
  }


  @AfterAll
  static void afterAll() {
    utilityMock.close();
    springContextGroovy.close();
  }

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(groovyScriptModel, "javaGroovy", javaGroovy);
    reset(groovyScriptModel);
    reset(rdf4jClient);
    reset(serviceArguments);
  }


  private String getJsonRsp(String scriptString, String sparql) {
    return sparql.replace(VARIABLE, scriptString);
  }

  @Test
  void populate_ok() {
    groovyScriptModel.setRdf4jClient(rdf4jClient);
    String response = UnitTestHelper.readJsonFiles(UnitTestConstants.GROOVYSCRIPTMODELTESTDATA);
    JsonArray daAnswer = JsonParser.parseString(response).getAsJsonArray();
    doReturn(daAnswer).when(rdf4jClient).selectSparqlOfg(any(), any());
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();
    groovyScriptModel.populate("anIri", daParams, serviceArguments);
    assertEquals(VARIABLE, groovyScriptModel.getScript());
    assertEquals("anIri", groovyScriptModel.getSubjectIri());
    assertEquals("aLabel", groovyScriptModel.getLabel());
  }

  @Test
  void populate_emptyResponse() {
    groovyScriptModel.setRdf4jClient(rdf4jClient);
    JsonArray daAnswer = JsonParser.parseString("[]").getAsJsonArray();
    doReturn(daAnswer).when(rdf4jClient).selectSparqlOfg(any(), any());
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();
    Throwable throwable = assertThrows(IncidentException.class, () -> {
      groovyScriptModel.populate("1", daParams, serviceArguments);
    });
    assertEquals(SdipErrorCode.INVALID_JSONARRAY_RESPONSE.getSdipErrorCode(),
            Integer
                    .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                            .getSdipErrorCodes().get(0)
                            .replace("SDIP_", "")));
  }

  @Test
  void populate_emptyScript() {
    groovyScriptModel.setRdf4jClient(rdf4jClient);
    String response = UnitTestHelper.readJsonFiles(UnitTestConstants.GROOVYSCRIPTMODELTESTDATA2);
    JsonArray daAnswer = JsonParser.parseString(response).getAsJsonArray();
    doReturn(daAnswer).when(rdf4jClient).selectSparqlOfg(any(), any());
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();
    groovyScriptModel.populate("anIri", daParams, serviceArguments);
    assertEquals("", groovyScriptModel.getScript());
  }

  @Test
  void populate_emptyLabel() {
    groovyScriptModel.setRdf4jClient(rdf4jClient);
    String response = UnitTestHelper.readJsonFiles(UnitTestConstants.GROOVYSCRIPTMODELTESTDATA2);
    JsonArray daAnswer = JsonParser.parseString(response).getAsJsonArray();
    doReturn(daAnswer).when(rdf4jClient).selectSparqlOfg(any(), any());
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();
    groovyScriptModel.populate("anIri", daParams, serviceArguments);
    assertEquals("", groovyScriptModel.getLabel());
  }

  @Test
  void populate_invalidJsonArrayResponse() {
    groovyScriptModel.setRdf4jClient(rdf4jClient);
    doThrow(IllegalStateException.class).when(rdf4jClient).selectSparqlOfg(any(), any());
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();
    Throwable throwable = assertThrows(IncidentException.class, () -> {
      groovyScriptModel.populate("1", daParams, serviceArguments);
    });
    assertEquals(SdipErrorCode.INVALID_JSONARRAY_RESPONSE.getSdipErrorCode(),
            Integer
                    .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                            .getSdipErrorCodes().get(0)
                            .replace("SDIP_", "")));
  }

  @Test
  void populate_invalidPropertyJsonArray() {
    groovyScriptModel.setRdf4jClient(rdf4jClient);
    JsonArray daAnswer = JsonParser.parseString("[{\"key\":\"value\"}]").getAsJsonArray();
    doReturn(daAnswer).when(rdf4jClient).selectSparqlOfg(any(), any());
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      groovyScriptModel.populate("1", daParams, serviceArguments);
    });
    assertEquals(SdipErrorCode.FAILED_TO_PARSE_JSONARRAY.getSdipErrorCode(),
            Integer
                    .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                            .getSdipErrorCodes().get(0)
                            .replace("SDIP_", "")));
  }

  @Test
  void populate_noScript() {
    groovyScriptModel.setRdf4jClient(rdf4jClient);
    JsonArray daAnswer = JsonParser.parseString("[{\"key\":\"value\"}]").getAsJsonArray();
    doReturn(daAnswer).when(rdf4jClient).selectSparqlOfg(any(), any());
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      groovyScriptModel.populate("1", daParams, serviceArguments);
    });
    assertEquals(SdipErrorCode.FAILED_TO_PARSE_JSONARRAY.getSdipErrorCode(),
            Integer
                    .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                            .getSdipErrorCodes().get(0)
                            .replace("SDIP_", "")));
  }

  @Test
  void populate_unknownError() {
    groovyScriptModel.setRdf4jClient(rdf4jClient);
    doThrow(RuntimeException.class).when(rdf4jClient).selectSparqlOfg(any(), any());
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      groovyScriptModel.populate("1", daParams, serviceArguments);
    });
    assertEquals(SdipErrorCode.UNKNOWN_PARSING_ERROR.getSdipErrorCode(),
            Integer
                    .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                            .getSdipErrorCodes().get(0)
                            .replace("SDIP_", "")));
  }

  @Test
  void populate_catchException() {
    groovyScriptModel.setRdf4jClient(rdf4jClient);
    String message = "Host name may not be null";
    doThrow(new IncidentException(SdipErrorCode.UNKNOWN_REASON_ERROR, LOGGER, message,
            SdipErrorParameter.SUPPORTMAIL))
            .when(rdf4jClient).selectSparqlOfg(any(), any());
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      groovyScriptModel.populate("1", daParams, serviceArguments);
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
  void test_executeScript() {
    String POLARION_TO_OAS = "https://kg.scania.com/it/iris_orchestration/PARAM_POLARION_TO_OAS";
    JsonLdContextModel anotherContext = Mockito.mock(JsonLdContextModel.class);

    HashMap<String, List<String>> inputContent = new HashMap<>();
    inputContent.put("KEY", Collections.singletonList("VALUE"));

    HashMap<String, List<String>> inputContent2 = new HashMap<>();
    inputContent2.put("KEY2", Collections.singletonList("VALUE2"));

    List<IParameterModel> outputParameter=new ArrayList<>();
    outputParameter.add(outputParameterModel);

    List<IParameterModel> inputParameter=new ArrayList<>();
    inputParameter.add(inputParameterModel);


    HashMap<String, List<String>> map = new HashMap<>();
    map.put("utf", Collections.singletonList("18"));
    HashMap map2 = new HashMap();
    map2.put(POLARION_TO_OAS, map);

    List<String> inputKeys = new ArrayList<>();
    inputKeys.add("KEY");

    List<String> inputKeys2 = new ArrayList<>();
    inputKeys2.add("KEY2");
    doReturn("outputLabel1", "outputLabel2").when(outputParameterModel).getLabel();
    doReturn("http://outputIRI1", "http://outputIRI2").when(outputParameterModel).getSubjectIri();

    doReturn(inputKeys, inputKeys2).when(inputParameterModel).getKeys();
    doReturn("http://inputIRI1", "http://inputIRI2").when(inputParameterModel).getSubjectIri();
    doReturn(inputContent).when(daParams).getValue("http://inputIRI1");

    doReturn("anotherContext").when(anotherContext).getContext();
    springContextGroovy.when(() ->
            SpringContext.getBean(any())).thenReturn(javaGroovy);
    Mockito.doReturn(UnitTestConstants.GROOVYSCRIPT).when(groovyScriptModel).getScript();
    doReturn(map2).when(javaGroovy).runGroovyShell(any(), any());

    Map<String, HashMap<String, List<String>>> resList =groovyScriptModel.executeScript(daParams,jsonLdContextModel,
            serviceArguments,anotherContext,outputParameter,inputParameter);

    assertNotNull(map,"Not Null");
    assertEquals("18", resList.get(POLARION_TO_OAS).get("utf").get(0));

    //Handle NUll Pointer Exception
    Throwable throwable = assertThrows(IncidentException.class, () -> {
      groovyScriptModel.executeScript(daParams,jsonLdContextModel,
              serviceArguments,anotherContext,outputParameter,null);
    });
    assertEquals(SdipErrorCode.FAILED_TO_PARSE_JSONARRAY.getSdipErrorCode(),
            Integer
                    .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                            .getSdipErrorCodes().get(0)
                            .replace("SDIP_", "")));

    //IncidentException
    Throwable throwableIncidentException = assertThrows(IncidentException.class, () -> {
      groovyScriptModel.executeScript(daParams,null,
              serviceArguments,null,outputParameter,inputParameter);
    });
    assertEquals(SdipErrorCode.INVALID_CONTEXT_DATA.getSdipErrorCode(),
            Integer
                    .parseInt(((IncidentException) throwableIncidentException).createHttpErrorResponse().getBody()
                            .getSdipErrorCodes().get(0)
                            .replace("SDIP_", "")));
  }

}
