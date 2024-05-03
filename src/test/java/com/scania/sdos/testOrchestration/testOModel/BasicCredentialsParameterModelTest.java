package com.scania.sdos.testOrchestration.testOModel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
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
import com.scania.sdos.orchestration.model.BasicCredentialsParameterModel;
import com.scania.sdos.testUtils.UnitTestHelper;
import com.scania.sdos.utils.SDOSConstants;
import java.util.HashMap;
import java.util.List;

import com.scania.sdos.testUtils.UnitTestConstants;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BasicCredentialsParameterModelTest {

  @Mock
  private ParameterMemory daParams;
  @Mock
  private ServiceArguments serviceArguments;
  @Spy
  private Rdf4jClient rdf4jClient;
  @Spy
  private BasicCredentialsParameterModel parameterModel;
  @Mock
  private OfgModelRepo ofgModelRepo;
  @BeforeAll
  static void beforeAll() {
  }

  @AfterAll
  static void afterAll() {
  }

  @BeforeEach
  void setUp() {
    reset(daParams);
    reset(rdf4jClient);
    reset(parameterModel);
    reset(serviceArguments);
  }

  @Test
  void getKeys_ok() {
    parameterModel.setRdf4jClient(rdf4jClient);
    String response = UnitTestHelper.readJsonFiles(UnitTestConstants.BASICCREDPARAMETERMODELGETDATA);
    JsonArray daAnswer = JsonParser.parseString(response).getAsJsonArray();
    doReturn(daAnswer).when(rdf4jClient).selectSparqlOfg(any(), any());
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();
    parameterModel.populate("aSubjectIRI", daParams, serviceArguments);
    List<String> keys = parameterModel.getKeys();
    assertEquals("param_credentials", parameterModel.getLabel());
    assertTrue(keys.contains(SDOSConstants.USERNAME));
    assertTrue(keys.contains(SDOSConstants.PASSWORD));
  }

  @Test
  void getValue_ok() {
    parameterModel.setRdf4jClient(rdf4jClient);
    String response = UnitTestHelper.readJsonFiles(UnitTestConstants.BASICCREDPARAMETERMODELGETDATA);
    JsonArray daAnswer = JsonParser.parseString(response).getAsJsonArray();
    doReturn(daAnswer).when(rdf4jClient).selectSparqlOfg(any(), any());
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();
    parameterModel.populate("aSubjectIRI", daParams, serviceArguments);
    HashMap<String, String> hashMap = parameterModel.getValue();
    assertEquals("param_credentials", parameterModel.getLabel());
    assertTrue(hashMap.containsKey(SDOSConstants.USERNAME));
    assertTrue(hashMap.containsKey(SDOSConstants.PASSWORD));
  }

  @Test
  void populate_ok() {
    parameterModel.setRdf4jClient(rdf4jClient);
    String response = UnitTestHelper.readJsonFiles(UnitTestConstants.BASICCREDPARAMETERMODELGETDATA);
    JsonArray daAnswer = JsonParser.parseString(response).getAsJsonArray();
    doReturn(daAnswer).when(rdf4jClient).selectSparqlOfg(any(), any());
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();
    parameterModel.populate("aSubjectIRI", daParams, serviceArguments);

    assertEquals("param_credentials", parameterModel.getLabel());
    assertEquals("aSubjectIRI", parameterModel.getSubjectIri());
    verify(parameterModel, times(1)).setSubjectIri(any());
    verify(parameterModel, times(1)).setLabel(any());
  }

  @Test
  void populate_emptyResponse_nok() {
    parameterModel.setRdf4jClient(rdf4jClient);
    doReturn(new JsonArray()).when(rdf4jClient).selectSparqlOfg(any(), any());
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      parameterModel.populate("aSubjectIRI", daParams, serviceArguments);
    });
    assertEquals(SdipErrorCode.INVALID_JSONARRAY_RESPONSE.getSdipErrorCode(),
            Integer
                    .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                            .getSdipErrorCodes().get(0)
                            .replace("SDIP_", "")));
  }


  @Test
  void createUserInputHelp() {
    String expected = "[{\"key\":\"username\",\"value\":\"\"},{\"key\":\"password\",\"value\":\"\"}]";
    JsonArray userInputHelp = parameterModel.createUserInputHelp();
    assertEquals(2, userInputHelp.size());
    assertEquals(expected, userInputHelp.toString());
  }

}
