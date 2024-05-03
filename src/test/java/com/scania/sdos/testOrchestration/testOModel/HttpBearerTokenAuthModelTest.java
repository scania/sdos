package com.scania.sdos.testOrchestration.testOModel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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
import com.scania.sdos.orchestration.factory.ParameterModelFactory;
import com.scania.sdos.orchestration.interfaces.IParameterModel;
import com.scania.sdos.orchestration.model.HttpBearerTokenAuthModel;
import com.scania.sdos.orchestration.model.HttpConnectorModel;
import com.scania.sdos.testUtils.UnitTestHelper;
import com.scania.sdos.utils.SDOSConstants;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.scania.sdos.testUtils.UnitTestConstants;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HttpBearerTokenAuthModelTest {

  public static final String ALABEL = "alabel";
  public static final String A_SUBJECT_IRI = "aSubjectIri";
  public static final String AN_INPUT_PARAMETER = "anInputParameter";
  public static final String AN_INPUT_PARAMETER_TYPE = "anInputParameterType";
  public static final String A_TOKEN = "aToken";
  private static final Logger LOGGER = LogManager.getLogger(HttpBearerTokenAuthModelTest.class);
  private static MockedStatic<ParameterModelFactory> parameterModelFactory;
  @Mock
  private HttpConnectorModel connectorModel;

  @Mock
  private ParameterMemory daParams;
  @Mock
  private ServiceArguments serviceArguments;

  @Mock
  private IParameterModel parameterModel;
  @Mock
  private OfgModelRepo ofgModelRepo;
  @Mock
  private Rdf4jClient rdf4jClient;

  @Spy
  private HttpBearerTokenAuthModel modelUnderTest;

  @BeforeAll
  static void beforeAll() {
    parameterModelFactory = mockStatic(ParameterModelFactory.class);
  }


  @AfterAll
  static void afterAll() {
    parameterModelFactory.close();
  }


  @BeforeEach
  void setUp() {
    reset(connectorModel);
    reset(modelUnderTest);
    reset(rdf4jClient);
    parameterModelFactory.reset();

  }


  @Test
  void populate_ok() {
    modelUnderTest.setRdf4jClient(rdf4jClient);
    String response = UnitTestHelper.readJsonFiles(UnitTestConstants.TOKENHTTPACTIONMODELTESTDATA);
    JsonArray daAnswer = JsonParser.parseString(response).getAsJsonArray();
    doReturn(daAnswer).when(rdf4jClient).selectSparqlOfg(any(), any());
    parameterModelFactory.when(() ->
        ParameterModelFactory.getParameter(any())).thenReturn(parameterModel);
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();
    modelUnderTest.populate(A_SUBJECT_IRI, daParams, serviceArguments);

    verify(modelUnderTest, times(1)).setSubjectIri(A_SUBJECT_IRI);
    verify(modelUnderTest, times(1)).setLabel(ALABEL);
    verify(modelUnderTest, times(1)).setInputParameter(any());
    parameterModelFactory.verify(() -> ParameterModelFactory.getParameter(AN_INPUT_PARAMETER_TYPE),
            times(1));
    verify(parameterModel, times(1))
        .populate(AN_INPUT_PARAMETER, daParams, serviceArguments);
    Assertions.assertEquals(parameterModel, modelUnderTest.getInputParameter());
    assertEquals(ALABEL, modelUnderTest.getLabel());
    assertEquals(A_SUBJECT_IRI, modelUnderTest.getSubjectIri());
  }

  @Test
  void populate_emptyResponse_nok() {
    modelUnderTest.setRdf4jClient(rdf4jClient);
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();
    // test empty json
    doReturn(new JsonArray()).when(rdf4jClient).selectSparqlOfg(any(), any());
    Throwable throwable = assertThrows(IncidentException.class, () -> {
      modelUnderTest.populate(A_SUBJECT_IRI, daParams, serviceArguments);
    });
    assertEquals(SdipErrorCode.INVALID_JSONARRAY_RESPONSE.getSdipErrorCode(),
        Integer
            .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                .getSdipErrorCodes().get(0)
                .replace("SDIP_", "")));

    // test Parameter.populate throws NullPointerException
    String response = UnitTestHelper.readJsonFiles(UnitTestConstants.TOKENHTTPACTIONMODELTESTDATA);
    JsonArray daAnswer = JsonParser.parseString(response).getAsJsonArray();
    doReturn(daAnswer).when(rdf4jClient).selectSparqlOfg(any(), any());
    parameterModelFactory.when(() ->
        ParameterModelFactory.getParameter(any())).thenReturn(parameterModel);
    doThrow(new NullPointerException("anullpointer")).when(parameterModel)
        .populate(any(), any(), any());
    throwable = assertThrows(IncidentException.class, () -> {
      modelUnderTest.populate(A_SUBJECT_IRI, daParams, serviceArguments);
    });
    assertEquals(SdipErrorCode.FAILED_TO_PARSE_JSONARRAY.getSdipErrorCode(),
        Integer
            .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                .getSdipErrorCodes().get(0)
                .replace("SDIP_", "")));

    // test Parameter.populate throws IncidentException
    parameterModelFactory.when(() ->
        ParameterModelFactory.getParameter(any())).thenReturn(parameterModel);
    IncidentException anException = new IncidentException("anErrorMessage",
        SdipErrorCode.UNKNOWN_REASON_ERROR, LOGGER, "UnknownReason", "anEmailAddress");
    doThrow(anException).when(parameterModel)
        .populate(any(), any(), any());
    throwable = assertThrows(IncidentException.class, () -> {
      modelUnderTest.populate(A_SUBJECT_IRI, daParams, serviceArguments);
    });
    assertEquals(SdipErrorCode.UNKNOWN_REASON_ERROR.getSdipErrorCode(),
        Integer
            .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                .getSdipErrorCodes().get(0)
                .replace("SDIP_", "")));

    // test Parameter.populate throws general exception
    parameterModelFactory.when(() ->
        ParameterModelFactory.getParameter(any())).thenReturn(parameterModel);
    Exception aGeneralException = new NotImplementedException("NotImplementedMessage");
    doThrow(aGeneralException).when(parameterModel)
        .populate(any(), any(), any());
    throwable = assertThrows(IncidentException.class, () -> {
      modelUnderTest.populate(A_SUBJECT_IRI, daParams, serviceArguments);
    });
    assertEquals(SdipErrorCode.UNKNOWN_PARSING_ERROR.getSdipErrorCode(),
        Integer
            .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                .getSdipErrorCodes().get(0)
                .replace("SDIP_", "")));

  }

  @Test
  void run_ok() {
    final String token = "aToken";
    modelUnderTest.setInputParameter(parameterModel);
    doReturn(A_SUBJECT_IRI).when(parameterModel).getSubjectIri();
    HashMap<String, List> hashMap = new HashMap<>();
    hashMap.put(SDOSConstants.TOKEN, Arrays.asList(token));
    doReturn(hashMap).when(daParams).getValue(A_SUBJECT_IRI);

    modelUnderTest.run(daParams);

    assertEquals(token, modelUnderTest.getToken());
  }

  @Test
  void test_run_nok() {
    modelUnderTest.setInputParameter(parameterModel);

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      modelUnderTest.run(daParams);
    });
    assertEquals(SdipErrorCode.UNKNOWN_REASON_ERROR.getSdipErrorCode(),
        Integer
            .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                .getSdipErrorCodes().get(0)
                .replace("SDIP_", "")));
  }

  @Test
  void addCredentials() {
    HashMap<String, String> reqHeader = new HashMap<>();
    modelUnderTest.setToken(A_TOKEN);
    HashMap<String, String> result = modelUnderTest.addCredentials(reqHeader, daParams);
    assertEquals(SDOSConstants.BEARER + " " + A_TOKEN, result.get(SDOSConstants.AUTHORIZATION));
  }

  @Test
  void addCredentials_throws() {
    HashMap<String, String> reqHeader = null;
    modelUnderTest.setToken(A_TOKEN);
    Throwable throwable = assertThrows(IncidentException.class, () -> {
      modelUnderTest.addCredentials(reqHeader, daParams);
    });
    assertEquals(SdipErrorCode.UNKNOWN_REASON_ERROR.getSdipErrorCode(),
        Integer
            .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                .getSdipErrorCodes().get(0)
                .replace("SDIP_", "")));
  }

}
