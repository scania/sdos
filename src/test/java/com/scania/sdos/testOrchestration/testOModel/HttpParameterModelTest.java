package com.scania.sdos.testOrchestration.testOModel;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockStatic;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.scania.sdos.model.ServiceArguments;
import com.scania.sdos.orchestration.OfgModelRepo;
import com.scania.sdos.orchestration.ParameterMemory;
import com.scania.sdos.orchestration.Rdf4jClient;
import com.scania.sdos.orchestration.factory.ParameterModelFactory;
import com.scania.sdos.orchestration.interfaces.IParameterMemory;
import com.scania.sdos.orchestration.model.HttpParameterModel;
import com.scania.sdos.testUtils.UnitTestHelper;
import com.scania.sdos.utils.SDOSConstants;
import java.util.List;

import com.scania.sdos.testUtils.UnitTestConstants;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class HttpParameterModelTest {

  private static MockedStatic parameterModelFactory;
  private static MockedStatic parameterMemoryFactory;
  private Rdf4jClient rdf4jClient = Mockito.spy(Rdf4jClient.class);
  @Mock
  private ParameterMemory daParams;
  @Mock
  private ServiceArguments serviceArguments;
  @Mock
  private OfgModelRepo ofgModelRepo;
  @BeforeAll
  static void beforeAll() {
    parameterModelFactory = mockStatic(ParameterModelFactory.class);
    parameterMemoryFactory = mockStatic(IParameterMemory.class);
  }

  @AfterAll
  static void afterAll() {
    parameterModelFactory.close();
    parameterMemoryFactory.close();
  }

  @BeforeEach
  void setUp() {
    parameterModelFactory.reset();
    parameterMemoryFactory.reset();
  }

  @Test
  void populate_ok() {
    HttpParameterModel httpParameterModel = new HttpParameterModel();
    httpParameterModel.setRdf4jClient(rdf4jClient);

    //Testing if condition inside Populate
    String response = UnitTestHelper.readJsonFiles(UnitTestConstants.HTTPPARAMETERMODELTESTDATA);
    JsonArray daAnswer = JsonParser.parseString(response).getAsJsonArray();
    doReturn(daAnswer).when(rdf4jClient).selectSparqlOfg(any(), any());
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();

    httpParameterModel.populate("aSubjectIRI", daParams, serviceArguments);
    assertEquals("aSubjectIRI", httpParameterModel.getSubjectIri());
    assertTrue(httpParameterModel.isEndpoint());
    assertTrue(httpParameterModel.isHeaders());
    assertTrue(httpParameterModel.isBody());
    assertTrue(httpParameterModel.isQueryparameters());
    assertEquals("TestEndpoint", httpParameterModel.getEndpoint());
    assertEquals("TestBody", httpParameterModel.getHttpBody());

  }

  @Test
  void getKeys_ok() {
    HttpParameterModel httpParameterModel = new HttpParameterModel();
    httpParameterModel.setRdf4jClient(rdf4jClient);
    String response = UnitTestHelper.readJsonFiles(UnitTestConstants.HTTPPARAMETERMODELTESTDATA2);
    JsonArray daAnswer = JsonParser.parseString(response).getAsJsonArray();
    doReturn(daAnswer).when(rdf4jClient).selectSparqlOfg(any(), any());
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();

    httpParameterModel.populate("aSubjectIRI", daParams, serviceArguments);
    List<String> keys = httpParameterModel.getKeys();
    assertEquals("label", httpParameterModel.getLabel());
    assertTrue(keys.contains("endpoint"));
    assertTrue(keys.contains("httpBody"));
    assertTrue(keys.contains("httpHeader"));
    assertTrue(keys.contains("httpQueryParameter"));
  }

  @Test
  void createUserInputHelp_ok() {
    HttpParameterModel httpParameterModel = new HttpParameterModel();
    httpParameterModel.setRdf4jClient(rdf4jClient);
    String response = UnitTestHelper.readJsonFiles(UnitTestConstants.HTTPPARAMETERMODELTESTDATA2);
    JsonArray daAnswer = JsonParser.parseString(response).getAsJsonArray();
    doReturn(daAnswer).when(rdf4jClient).selectSparqlOfg(any(), any());
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();

    httpParameterModel.populate("aSubjectIRI", daParams, serviceArguments);
    JsonArray userInputHelp = httpParameterModel.createUserInputHelp();
    assertEquals(4, userInputHelp.size());

    assertEquals("TestEndpoint",
        userInputHelp.get(0).getAsJsonObject().get(SDOSConstants.VALUE).getAsString());
    assertEquals(SDOSConstants.ENDPOINT,
        userInputHelp.get(0).getAsJsonObject().get(SDOSConstants.KEY).getAsString());
    assertEquals("TestBody",
        userInputHelp.get(1).getAsJsonObject().get(SDOSConstants.VALUE).getAsString());
    assertEquals(SDOSConstants.HTTPBODY,
        userInputHelp.get(1).getAsJsonObject().get(SDOSConstants.KEY).getAsString());
    assertEquals("TestHeader",
        userInputHelp.get(2).getAsJsonObject().get(SDOSConstants.VALUE).getAsString());
    assertEquals(SDOSConstants.HTTPHEADER,
        userInputHelp.get(2).getAsJsonObject().get(SDOSConstants.KEY).getAsString());
    assertEquals("TestQueryParameter",
        userInputHelp.get(3).getAsJsonObject().get(SDOSConstants.VALUE).getAsString());
    assertEquals(SDOSConstants.HTTPQUERYPARAM,
        userInputHelp.get(3).getAsJsonObject().get(SDOSConstants.KEY).getAsString());

  }
}
