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
import com.scania.sdip.exceptions.SdipErrorParameter;
import com.scania.sdos.model.ServiceArguments;
import com.scania.sdos.orchestration.OfgModelRepo;
import com.scania.sdos.orchestration.ParameterMemory;
import com.scania.sdos.orchestration.Rdf4jClient;
import com.scania.sdos.orchestration.factory.AuthenticationModelFactory;
import com.scania.sdos.orchestration.model.SoapBasicAuthModel;
import com.scania.sdos.orchestration.model.SoapConnectorModel;
import com.scania.sdos.testUtils.UnitTestHelper;
import com.scania.sdos.testUtils.UnitTestConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
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
class SoapConnectorModelTest {

  private static final Logger LOGGER = LogManager.getLogger(SoapConnectorModelTest.class);

  private static MockedStatic authModelFactory;
  private Rdf4jClient rdf4jClient = Mockito.spy(Rdf4jClient.class);
  private String subjectUri = "http://testsubjecturi";
  @Mock
  private SoapBasicAuthModel authModel;
  @Mock
  private ParameterMemory daParams;
  @Mock
  private ServiceArguments serviceArguments;
  @Spy
  private SoapConnectorModel connectorModel;
  @Mock
  private OfgModelRepo ofgModelRepo;

  @BeforeAll
  static void beforeAll() {
    authModelFactory = mockStatic(AuthenticationModelFactory.class);
  }


  @AfterAll
  static void afterAll() {
    authModelFactory.close();
  }

  @BeforeEach
  void setUp() {
    authModelFactory.reset();
    reset(connectorModel);
  }

  @Test
  void populate_ok() {

    connectorModel.setRdf4jClient(rdf4jClient);

    // test successful case
    String response = UnitTestHelper.readJsonFiles(UnitTestConstants.SOAPCONNECTORMODELTESTDATA);
    JsonArray daAnswer = JsonParser.parseString(response).getAsJsonArray();
    doReturn(daAnswer).when(rdf4jClient).selectSparqlOfg(any(), any());
    authModelFactory.when(() ->
        AuthenticationModelFactory.getAuthModel(any())).thenReturn(authModel);
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();

    try {
      // run it!
      connectorModel.populate(subjectUri, daParams, serviceArguments);
      //check it!
      assertEquals(subjectUri, connectorModel.getSubjectIri());
      assertEquals("aWsdlFile", connectorModel.getWsdlFile());
      assertEquals("TrackerWebServiceSoapBinding", connectorModel.getBindingName());
      assertEquals(authModel, connectorModel.getHasAuthenticationMethod());
      assertEquals("aLabel", connectorModel.getLabel());

      verify(authModel, times(1))
          .populate("anAuthenticationMethod", daParams, serviceArguments);
    } catch (Exception e) {
      Assert.fail();
    }
  }

  @Test
  void populate_emptyResponse() {

    connectorModel.setRdf4jClient(rdf4jClient);

    // test empty response

    JsonArray daAnswer = JsonParser.parseString("[{}]").getAsJsonArray();
    doReturn(daAnswer).when(rdf4jClient).selectSparqlOfg(any(), any());

    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();

    // run it
    Throwable throwable = assertThrows(IncidentException.class, () -> {
      connectorModel.populate("1", daParams, serviceArguments);
    });
    assertEquals(SdipErrorCode.FAILED_TO_PARSE_JSONARRAY.getSdipErrorCode(),
        Integer
            .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                .getSdipErrorCodes().get(0)
                .replace("SDIP_", "")));
  }

  @Test
  void populate_authModelFactReturnsNull() {

    connectorModel.setRdf4jClient(rdf4jClient);

    // test empty response

    String response = UnitTestHelper.readJsonFiles(UnitTestConstants.SOAPCONNECTORMODELTESTDATA);
    JsonArray daAnswer = JsonParser.parseString(response).getAsJsonArray();
    doReturn(daAnswer).when(rdf4jClient).selectSparqlOfg(any(), any());

    authModelFactory.when(() ->
        AuthenticationModelFactory.getAuthModel(any())).thenReturn(null);
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();

    // run it
    Throwable throwable = assertThrows(IncidentException.class, () -> {
      connectorModel.populate("1", daParams, serviceArguments);
    });

    assertEquals(SdipErrorCode.FAILED_TO_PARSE_JSONARRAY.getSdipErrorCode(),
        Integer
            .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                .getSdipErrorCodes().get(0)
                .replace("SDIP_", "")));
  }

  @Test
  void populate_invalidPropertyJsonArray() {
    connectorModel.setRdf4jClient(rdf4jClient);
    JsonArray daAnswer = JsonParser.parseString("[{\"key\":\"value\"}]").getAsJsonArray();
    doReturn(daAnswer).when(rdf4jClient).selectSparqlOfg(any(), any());
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      connectorModel.populate("1", daParams, serviceArguments);
    });

    assertEquals(SdipErrorCode.FAILED_TO_PARSE_JSONARRAY.getSdipErrorCode(),
        Integer
            .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                .getSdipErrorCodes().get(0)
                .replace("SDIP_", "")));
  }

  @Test
  void populate_invalidJsonArrayResponse() {
    connectorModel.setRdf4jClient(rdf4jClient);
    doThrow(IllegalStateException.class).when(rdf4jClient)
        .selectSparqlOfg(any(), any());
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();
    Throwable throwable = assertThrows(IncidentException.class, () -> {
      connectorModel.populate("1", daParams, serviceArguments);
    });

    assertEquals(SdipErrorCode.INVALID_JSONARRAY_RESPONSE.getSdipErrorCode(),
        Integer
            .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                .getSdipErrorCodes().get(0)
                .replace("SDIP_", "")));

  }

  @Test
  void populate_unknownError() {
    connectorModel.setRdf4jClient(rdf4jClient);
    doThrow(RuntimeException.class).when(rdf4jClient).selectSparqlOfg(any(), any());
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();
    Throwable throwable = assertThrows(IncidentException.class, () -> {
      connectorModel.populate("1", daParams, serviceArguments);
    });
    assertEquals(SdipErrorCode.UNKNOWN_PARSING_ERROR.getSdipErrorCode(),
        Integer
            .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                .getSdipErrorCodes().get(0)
                .replace("SDIP_", "")));
  }

  @Test
  void populate_catchException() {
    connectorModel.setRdf4jClient(rdf4jClient);
    String message = "Host name may not be null";
    doThrow(new IncidentException(SdipErrorCode.UNKNOWN_REASON_ERROR, LOGGER, message,
        SdipErrorParameter.SUPPORTMAIL))
        .when(rdf4jClient).selectSparqlOfg(any(), any());
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      connectorModel.populate("1", daParams, serviceArguments);
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
}