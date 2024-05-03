package com.scania.sdos.testOrchestration.testOModel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.scania.sdip.exceptions.IncidentException;
import com.scania.sdip.exceptions.SdipErrorCode;
import com.scania.sdos.model.ServiceArguments;
import com.scania.sdos.orchestration.OfgModelRepo;
import com.scania.sdos.orchestration.ParameterMemory;
import com.scania.sdos.orchestration.Rdf4jClient;
import com.scania.sdos.orchestration.SoapClient;
import com.scania.sdos.orchestration.factory.ParameterModelFactory;
import com.scania.sdos.orchestration.factory.ScriptModelFactory;
import com.scania.sdos.orchestration.interfaces.IParameterModel;
import com.scania.sdos.orchestration.interfaces.IScriptModel;
import com.scania.sdos.orchestration.model.SoapBasicAuthModel;
import com.scania.sdos.testUtils.UnitTestHelper;
import com.scania.sdos.utils.SDOSConstants;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.scania.sdos.testUtils.UnitTestConstants;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SoapBasicAuthModelTest {

  private static MockedStatic parameterModelFactory;
  private static MockedStatic scriptModelFactory;

  @Mock
  private ParameterMemory daParams;

  @Mock
  private ServiceArguments serviceArguments;

  @Mock
  private IParameterModel parameterModel;
  @Mock
  private IScriptModel scriptModel;

  @Mock
  private Rdf4jClient rdf4jClient;

  @Mock
  private SoapClient soapClient;

  @Spy
  private SoapBasicAuthModel soapBasicAuthModel;
  @Mock
  private OfgModelRepo ofgModelRepo;
  @BeforeAll
  static void beforeAll() {
    parameterModelFactory = mockStatic(ParameterModelFactory.class);
    scriptModelFactory = mockStatic(ScriptModelFactory.class);
  }


  @AfterAll
  static void afterAll() {
    parameterModelFactory.close();
    scriptModelFactory.close();
  }


  @BeforeEach
  void setUp() {
    reset(soapBasicAuthModel);
    reset(rdf4jClient);
    parameterModelFactory.reset();
    scriptModelFactory.reset();

  }


  @Test
  void populate_ok1() {
    soapBasicAuthModel.setRdf4jClient(rdf4jClient);
    // test successful case
    String response = UnitTestHelper.readJsonFiles(UnitTestConstants.AUTHMODELTESTDATA);
    JsonArray daAnswer = JsonParser.parseString(response).getAsJsonArray();
    doReturn(daAnswer).when(rdf4jClient).selectSparqlOfg(any(), any());
    scriptModelFactory.when(() ->
        ScriptModelFactory.getScript(any())).thenReturn(scriptModel);
    parameterModelFactory.when(() ->
        ParameterModelFactory.getParameter(any())).thenReturn(parameterModel);
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();

    // run it!
    soapBasicAuthModel.populate("aSubjectIri", daParams, serviceArguments);
    // check it!
    assertEquals("aSubjectIri", soapBasicAuthModel.getSubjectIri());
    assertEquals("aWsdlFile", soapBasicAuthModel.getWsdlFile());
    assertEquals("SessionWebServiceSoapBinding", soapBasicAuthModel.getBindingName());
    assertEquals("aSoapOpera", soapBasicAuthModel.getSoapOperation());
    Assertions.assertEquals(scriptModel, soapBasicAuthModel.getScript());
    Assertions.assertEquals(parameterModel, soapBasicAuthModel.getInputParameter());

    verify(scriptModel, times(1))
        .populate("aScriptIri", daParams, serviceArguments);
    scriptModelFactory.verify(
        () -> ScriptModelFactory.getScript("aScriptType"), times(1)
    );
    parameterModelFactory.verify(
        () -> ParameterModelFactory
            .getParameter("https://kg.scania.com/it/iris_orchestration/Parameter"),
            times(1));
  }

  @Test
  void populate_scriptModelFactoryReturnsNull() {
    soapBasicAuthModel.setRdf4jClient(rdf4jClient);
    // test ScriptModelFactory returns null
    String response = UnitTestHelper.readJsonFiles(UnitTestConstants.AUTHMODELTESTDATA);
    JsonArray daAnswer = JsonParser.parseString(response).getAsJsonArray();
    doReturn(daAnswer).when(rdf4jClient).selectSparqlOfg(any(), any());
    scriptModelFactory.when(() ->
        ScriptModelFactory.getScript(any())).thenReturn(null);
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      soapBasicAuthModel.populate("aSubjectIRI", daParams, serviceArguments);
    });
    assertEquals(SdipErrorCode.FAILED_TO_PARSE_JSONARRAY.getSdipErrorCode(),
        Integer
            .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                .getSdipErrorCodes().get(0)
                .replace("SDIP_", "")));

  }

  @Test
  void populate_ok() {
    soapBasicAuthModel.setRdf4jClient(rdf4jClient);
    String response = UnitTestHelper.readJsonFiles(UnitTestConstants.AUTHMODELTESTDATA);
    JsonArray daAnswer = JsonParser.parseString(response).getAsJsonArray();
    doReturn(daAnswer).when(rdf4jClient).selectSparqlOfg(any(), any());
    parameterModelFactory.when(() ->
        ParameterModelFactory.getParameter(any())).thenReturn(parameterModel);
    scriptModelFactory.when(() ->
        ScriptModelFactory.getScript(any())).thenReturn(scriptModel);
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();

    soapBasicAuthModel.populate("aSubjectIri", daParams, serviceArguments);

    verify(soapBasicAuthModel, times(1)).setSubjectIri(any());
    verify(soapBasicAuthModel, times(1)).setLabel(any());
    verify(soapBasicAuthModel, times(1)).setBindingName(any());
    verify(soapBasicAuthModel, times(1)).setScript(any());
    verify(soapBasicAuthModel, times(1)).setSoapOperation(any());
    verify(soapBasicAuthModel, times(1)).setWsdlFile(any());
    verify(soapBasicAuthModel, times(1)).setInputParameter(any());

  }

  @Test
  void populate_emptyResponse_nok() {
    soapBasicAuthModel.setRdf4jClient(rdf4jClient);
    doReturn(new JsonArray()).when(rdf4jClient).selectSparqlOfg(any(), any());
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      soapBasicAuthModel.populate("aSubjectIRI", daParams, serviceArguments);
    });
    assertEquals(SdipErrorCode.INVALID_JSONARRAY_RESPONSE.getSdipErrorCode(),
        Integer
            .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                .getSdipErrorCodes().get(0)
                .replace("SDIP_", "")));
  }

  @Test
  void test_run() {
    JsonObject result = new JsonObject();
    result.addProperty("value", "123");
    result.addProperty("nameSpaceURI", "aname");
    result.addProperty("localPart", "alocalpart");
    List<JsonObject> jsonObjects = new ArrayList<>();
    jsonObjects.add(result);
    soapBasicAuthModel.setScript(scriptModel);
    soapBasicAuthModel.setRdf4jClient(rdf4jClient);
    soapBasicAuthModel.setSoapClient(soapClient);
    soapBasicAuthModel.setInputParameter(parameterModel);
    doReturn("aSubjectIri").when(parameterModel).getSubjectIri();
    HashMap<String, String> hashMap = new HashMap<>();
    hashMap.put(SDOSConstants.PASSWORD, "pass");
    hashMap.put(SDOSConstants.USERNAME, "user");
    doReturn(hashMap).when(daParams).getValue(anyString());
    doReturn("aScript").when(scriptModel).getScript();
    doReturn("soapEnvelope").when(soapClient).createSoapRequest(any(), any(), any());
    doReturn("ok").when(soapClient).sendRequestGetResponse(any(), any());
    doReturn(jsonObjects).when(soapClient).getValueFromResponse(anyString(), anyString());

    soapBasicAuthModel.run(daParams);

    assertNotNull(soapBasicAuthModel.getSoapHeaders());
    verify(soapClient, times(1)).getValueFromResponse(any(), any());
    verify(soapClient, times(1)).createSoapRequest(any(), any(), any());
    verify(soapClient, times(1)).sendRequestGetResponse(any(), any());
  }

  @Test
  void test_run_nok() {
    JsonObject result = new JsonObject();
    List<JsonObject> jsonObjects = new ArrayList<>();
    jsonObjects.add(result);
    soapBasicAuthModel.setScript(scriptModel);
    soapBasicAuthModel.setRdf4jClient(rdf4jClient);
    soapBasicAuthModel.setSoapClient(soapClient);
    soapBasicAuthModel.setInputParameter(parameterModel);
    doReturn("aSubjectIri").when(parameterModel).getSubjectIri();
    HashMap<String, String> hashMap = new HashMap<>();
    hashMap.put(SDOSConstants.PASSWORD, "pass");
    hashMap.put(SDOSConstants.USERNAME, "user");
    doReturn(hashMap).when(daParams).getValue(anyString());
    doReturn("aScript").when(scriptModel).getScript();
    doReturn("soapEnvelope").when(soapClient).createSoapRequest(any(), any(), any());
    doReturn("ok").when(soapClient).sendRequestGetResponse(any(), any());
    doReturn(jsonObjects).when(soapClient).getValueFromResponse(anyString(), anyString());

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      soapBasicAuthModel.run(daParams);
    });
    assertEquals(SdipErrorCode.UNKNOWN_REASON_ERROR.getSdipErrorCode(),
        Integer
            .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                .getSdipErrorCodes().get(0)
                .replace("SDIP_", "")));
  }
}
