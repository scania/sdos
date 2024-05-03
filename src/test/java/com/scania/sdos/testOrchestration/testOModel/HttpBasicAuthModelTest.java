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
import com.google.gson.JsonParser;
import com.scania.sdip.exceptions.IncidentException;
import com.scania.sdip.exceptions.SdipErrorCode;
import com.scania.sdos.model.ServiceArguments;
import com.scania.sdos.orchestration.OfgModelRepo;
import com.scania.sdos.orchestration.ParameterMemory;
import com.scania.sdos.orchestration.Rdf4jClient;
import com.scania.sdos.orchestration.factory.ParameterModelFactory;
import com.scania.sdos.orchestration.interfaces.IParameterModel;
import com.scania.sdos.orchestration.model.HttpBasicAuthModel;
import com.scania.sdos.orchestration.model.HttpConnectorModel;
import com.scania.sdos.testUtils.UnitTestHelper;
import com.scania.sdos.utils.SDOSConstants;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.scania.sdos.testUtils.UnitTestConstants;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HttpBasicAuthModelTest {

  private static MockedStatic parameterModelFactory;

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
  private HttpBasicAuthModel httpBasicAuthModel;

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
    reset(httpBasicAuthModel);
    reset(rdf4jClient);
    parameterModelFactory.reset();

  }

  @Test
  void populate_ok() {
    httpBasicAuthModel.setRdf4jClient(rdf4jClient);
    String response = UnitTestHelper.readJsonFiles(UnitTestConstants.BASICHTTPACTIONMODELTESTDATA);
    JsonArray daAnswer = JsonParser.parseString(response).getAsJsonArray();
    doReturn(daAnswer).when(rdf4jClient).selectSparqlOfg(any(), any());
    parameterModelFactory.when(() ->
        ParameterModelFactory.getParameter(any())).thenReturn(parameterModel);
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();
    httpBasicAuthModel.populate("aSubjectIri", daParams, serviceArguments);

    verify(httpBasicAuthModel, times(1)).setSubjectIri(any());
    verify(httpBasicAuthModel, times(1)).setLabel(any());
    verify(httpBasicAuthModel, times(1)).setInputParameter(any());

  }

  @Test
  void populate_emptyResponse_nok() {
    httpBasicAuthModel.setRdf4jClient(rdf4jClient);
    doReturn(new JsonArray()).when(rdf4jClient).selectSparqlOfg(any(), any());
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      httpBasicAuthModel.populate("aSubjectIRI", daParams, serviceArguments);
    });
    assertEquals(SdipErrorCode.INVALID_JSONARRAY_RESPONSE.getSdipErrorCode(),
        Integer
            .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                .getSdipErrorCodes().get(0)
                .replace("SDIP_", "")));
  }

  @Test
  void test_run() {
    httpBasicAuthModel.setInputParameter(parameterModel);
    doReturn("aSubjectIri").when(parameterModel).getSubjectIri();
    HashMap<String, List> hashMap = new HashMap<>();
    hashMap.put(SDOSConstants.PASSWORD, Arrays.asList("pass"));
    hashMap.put(SDOSConstants.USERNAME, Arrays.asList("user"));
    doReturn(hashMap).when(daParams).getValue(anyString());

    httpBasicAuthModel.run(daParams);

    assertNotNull(httpBasicAuthModel.getCredentials());
  }

  @Test
  void test_run_nok() {
    httpBasicAuthModel.setInputParameter(parameterModel);

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      httpBasicAuthModel.run(daParams);
    });
    assertEquals(SdipErrorCode.UNKNOWN_REASON_ERROR.getSdipErrorCode(),
        Integer
            .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                .getSdipErrorCodes().get(0)
                .replace("SDIP_", "")));
  }
}
