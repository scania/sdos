package com.scania.sdos.testOrchestration.testOModel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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
import com.scania.sdos.orchestration.factory.AuthenticationModelFactory;
import com.scania.sdos.orchestration.interfaces.IAuthModel;
import com.scania.sdos.orchestration.model.HttpConnectorModel;
import com.scania.sdos.testUtils.UnitTestHelper;
import com.scania.sdos.utils.Utility;
import com.scania.sdos.testUtils.UnitTestConstants;
import org.junit.jupiter.api.AfterAll;
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
class HttpConnectorModelTest {

  static final String VARIABLE = "VARIABLE";

  private static MockedStatic utilityMock;
  private static MockedStatic authenticationModelFactory;
  private Rdf4jClient rdf4jClient = Mockito.spy(Rdf4jClient.class);

  @Mock
  private ParameterMemory daParams;

  @Mock
  private ServiceArguments serviceArguments;

  @Mock
  private IAuthModel iAuthModel;
  @Mock
  private OfgModelRepo ofgModelRepo;

  @Spy
  private HttpConnectorModel connectorModel;

  @BeforeAll
  static void beforeAll() {
    utilityMock = mockStatic(Utility.class);
    authenticationModelFactory = mockStatic(AuthenticationModelFactory.class);
  }


  @AfterAll
  static void afterAll() {
    utilityMock.close();
    authenticationModelFactory.close();
  }

  @BeforeEach
  void setUp() {
    reset(connectorModel);
    utilityMock.reset();
    authenticationModelFactory.reset();
  }

  @Test
  void populate_ok() {
    connectorModel.setRdf4jClient(rdf4jClient);
    // test successful case
    String response = UnitTestHelper.readJsonFiles(UnitTestConstants.HTTPCONNECTORMODELTESTDATA_AUTH);
    JsonArray daAnswer = JsonParser.parseString(response).getAsJsonArray();
    doReturn(daAnswer).when(rdf4jClient).selectSparqlOfg(any(), any());
    authenticationModelFactory.when(() ->
        AuthenticationModelFactory.getAuthModel(any())).thenReturn(iAuthModel);
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();
    // run it!
    connectorModel.populate("aSubjectIri", daParams, serviceArguments);
    //check it!
    verify(iAuthModel, times(1)).populate(any(), any(), any());
    assertEquals("aSubjectIri", connectorModel.getSubjectIri());
    assertEquals("anEpicURL", connectorModel.getBaseUrl());
    assertEquals("aLabel", connectorModel.getLabel());
  }


  @Test
  void populate_emptyResponse() {
    HttpConnectorModel connectorModel = new HttpConnectorModel();
    connectorModel.setRdf4jClient(rdf4jClient);
    // test empty response
    // TODO: this case should throw a meaningful exception
    JsonArray daAnswer = JsonParser.parseString("[{}]").getAsJsonArray();
    doReturn(daAnswer).when(rdf4jClient).selectSparqlOfg(any(), any());
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();

    // run it
    Throwable throwable = assertThrows(IncidentException.class, () -> {
      connectorModel.populate("aSubjectIri", daParams, serviceArguments);
    });
    assertEquals(SdipErrorCode.FAILED_TO_PARSE_JSONARRAY.getSdipErrorCode(),
        Integer
            .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                .getSdipErrorCodes().get(0)
                .replace("SDIP_", "")));
  }

}
