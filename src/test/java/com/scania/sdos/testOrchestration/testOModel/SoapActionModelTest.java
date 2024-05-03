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
import com.scania.sdip.exceptions.SdipErrorParameter;
import com.scania.sdos.model.ServiceArguments;
import com.scania.sdos.orchestration.OfgModelRepo;
import com.scania.sdos.orchestration.ParameterMemory;
import com.scania.sdos.orchestration.Rdf4jClient;
import com.scania.sdos.orchestration.SoapClient;
import com.scania.sdos.orchestration.factory.ActionModelFactory;
import com.scania.sdos.orchestration.factory.ConnectorModelFactory;
import com.scania.sdos.orchestration.factory.ParameterModelFactory;
import com.scania.sdos.orchestration.interfaces.IActionModel;
import com.scania.sdos.orchestration.interfaces.IParameterModel;
import com.scania.sdos.orchestration.model.JsonLdContextModel;
import com.scania.sdos.orchestration.model.SoapActionModel;
import com.scania.sdos.orchestration.model.SoapBasicAuthModel;
import com.scania.sdos.orchestration.model.SoapConnectorModel;
import com.scania.sdos.testUtils.UnitTestHelper;
import com.scania.sdos.utils.SDOSConstants;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.scania.sdos.testUtils.UnitTestConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
class SoapActionModelTest {

  private static final Logger LOGGER = LogManager.getLogger(SoapActionModel.class);
  private static MockedStatic<? extends ActionModelFactory> actionModelFactory;
  private static MockedStatic<? extends ConnectorModelFactory> connectorModelFactory;
  private static MockedStatic<? extends ParameterModelFactory> parameterModelFactory;
  private static MockedStatic<? extends SoapBasicAuthModel> authModelFactory;
  private String SOAP_CONNECTOR = "https://kg.scania.com/it/iris_orchestration/Connector_SOAP1";
  private String subjectUri = "http://testsubjecturi";

  @Mock
  SoapConnectorModel connectorModel;

  @Mock
  IActionModel iActionModel;

  @Mock
  ParameterMemory daParams;

  @Mock
  ServiceArguments serviceArguments;

  @Mock
  StringBuilder sb;

  @Mock
  IParameterModel parameterModel;
  @Mock
  IParameterModel outputParameterModel;

  @Mock
  Rdf4jClient rdf4jClient;

  @Mock
  SoapClient soapClient;

  @Mock
  SoapBasicAuthModel authModel;

  @Spy
  SoapActionModel soapActionModel;
  @Spy
  ParameterMemory daParamsSpy;
  @Mock
  private JsonLdContextModel jsonLdContextModel;
  @Mock
  private OfgModelRepo ofgModelRepo;
  @BeforeAll
  static void beforeAll() {
    actionModelFactory = mockStatic(ActionModelFactory.class);
    connectorModelFactory = mockStatic(ConnectorModelFactory.class);
    parameterModelFactory = mockStatic(ParameterModelFactory.class);
    authModelFactory = mockStatic(SoapBasicAuthModel.class);
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
    reset(authModel);
    reset(rdf4jClient);
    reset(soapActionModel);
    connectorModelFactory.reset();
    actionModelFactory.reset();
    parameterModelFactory.reset();
    reset(daParamsSpy);
  }

  @Test
  void populate_ok() {
    soapActionModel.setRdf4jClient(rdf4jClient);
    // test successful case
    String response = UnitTestHelper.readJsonFiles(UnitTestConstants.SOAPACTIONMODELTESTDATA);
    JsonArray daAnswer = JsonParser.parseString(response).getAsJsonArray();
    doReturn(daAnswer).when(rdf4jClient).selectSparqlOfg(any(), any());
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
      soapActionModel.populate(subjectUri, daParams, serviceArguments);
      // TODO: maybe remove hardcoded values?
      assertEquals(subjectUri, soapActionModel.getSubjectIri());
      assertEquals("aSoapOpera", soapActionModel.getSoapOperation());
      assertEquals(connectorModel, soapActionModel.getSoapConnectorModel());
      Assertions.assertEquals(iActionModel, soapActionModel.getNextAction());
      assertEquals("aLabel", soapActionModel.getLabel());
      verify(iActionModel, times(1))
          .populate("next/action/IRI", daParams, serviceArguments);
      verify(connectorModel, times(1))
          .populate("aConnectorURI", daParams, serviceArguments);
      verify(parameterModel, times(1))
          .populate(UnitTestConstants.PARAM_POLARION_WORKITEM, daParams, serviceArguments);
      verify(parameterModel, times(1))
          .populate(UnitTestConstants.PARAM_POLARION_RAW, daParams, serviceArguments);
      actionModelFactory.verify(
          () -> ActionModelFactory.getAction("aNextActionType", daParams),
              times(1)
      );

      connectorModelFactory.verify(
          () -> ConnectorModelFactory.getConnector("SOAPConnector"), times(1));
      parameterModelFactory.verify(
          () -> ParameterModelFactory
              .getParameter("https://kg.scania.com/it/iris_orchestration/StandardParameter"),
              times(1));
      parameterModelFactory.verify(
          () -> ParameterModelFactory
              .getParameter("https://kg.scania.com/it/iris_orchestration/Parameter"),
              times(1));
    } catch (Exception e) {
      Assert.fail();
    }
  }


  @Test
  void populate_emptyResponse() {
    soapActionModel.setRdf4jClient(rdf4jClient);
    JsonArray daAnswer = JsonParser.parseString("[{}]").getAsJsonArray();
    doReturn(daAnswer).when(rdf4jClient).selectSparqlOfg(any(), any());
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      soapActionModel.populate("", daParams, serviceArguments);
    });
    assertEquals(SdipErrorCode.FAILED_TO_PARSE_JSONARRAY.getSdipErrorCode(),
        Integer
            .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                .getSdipErrorCodes().get(0)
                .replace("SDIP_", "")));
  }


  @Test
  void populate_invalidJsonArrayResponse() {
    soapActionModel.setRdf4jClient(rdf4jClient);
    doThrow(IllegalStateException.class).when(rdf4jClient)
        .selectSparqlOfg(any(), any());
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      soapActionModel.populate("", daParams, serviceArguments);
    });
    assertEquals(SdipErrorCode.INVALID_JSONARRAY_RESPONSE.getSdipErrorCode(),
        Integer
            .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                .getSdipErrorCodes().get(0)
                .replace("SDIP_", "")));
  }


  @Test
  void populate_actionModelFactoryReturnsNull() {
    soapActionModel.setRdf4jClient(rdf4jClient);
    String response = UnitTestHelper.readJsonFiles(UnitTestConstants.SOAPACTIONMODELTESTDATA);
    JsonArray daAnswer = JsonParser.parseString(response).getAsJsonArray();
    doReturn(daAnswer).when(rdf4jClient).selectSparqlOfg(any(), any());
    actionModelFactory.when(() ->
        ActionModelFactory.getAction(any(), any())).thenReturn(null);
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      soapActionModel.populate("", daParams, serviceArguments);
    });

    assertEquals(SdipErrorCode.FAILED_TO_PARSE_JSONARRAY.getSdipErrorCode(),
        Integer
            .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                .getSdipErrorCodes().get(0)
                .replace("SDIP_", "")));

  }


  @Test
  void populate_connectorModelFactoryReturnsNull() {
    soapActionModel.setRdf4jClient(rdf4jClient);
    String response = UnitTestHelper.readJsonFiles(UnitTestConstants.SOAPACTIONMODELTESTDATA);
    JsonArray daAnswer = JsonParser.parseString(response).getAsJsonArray();
    doReturn(daAnswer).when(rdf4jClient).selectSparqlOfg(any(), any());
    connectorModelFactory.when(() ->
        ConnectorModelFactory.getConnector(any())).thenReturn(null);
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      soapActionModel.populate("", daParams, serviceArguments);
    });

    assertEquals(SdipErrorCode.FAILED_TO_PARSE_JSONARRAY.getSdipErrorCode(),
        Integer
            .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                .getSdipErrorCodes().get(0)
                .replace("SDIP_", "")));
  }


  @Test
  void populate_catchIncidentException() {
    soapActionModel.setRdf4jClient(rdf4jClient);
    doThrow(new IncidentException(SdipErrorCode.SOAP_ERROR, LOGGER, "Test_Message",
        SdipErrorParameter.SUPPORTMAIL)).
        when(rdf4jClient).selectSparqlOfg(any(), any());
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      soapActionModel.populate("1", daParams, serviceArguments);
    });

    assertEquals(SdipErrorCode.SOAP_ERROR.getSdipErrorCode(),
        Integer
            .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                .getSdipErrorCodes().get(0)
                .replace("SDIP_", "")));
  }


  @Test
  void run_returnsNull() {
    soapActionModel.setSoapClient(soapClient);
    soapActionModel.setInputParameter(null);
    soapActionModel.setSoapConnectorModel(connectorModel);
    doNothing().when(soapClient).init(any(), any());
    Throwable throwable = assertThrows(IncidentException.class, () -> {
      soapActionModel.run(daParams, jsonLdContextModel, serviceArguments);
    });
    assertEquals(SdipErrorCode.UNKNOWN_REASON_ERROR.getSdipErrorCode(),
        Integer
            .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                .getSdipErrorCodes().get(0)
                .replace("SDIP_", "")));
  }


  @Test
  void run_catchIncidentException() {
    soapActionModel.setSoapClient(soapClient);
    soapActionModel.setSoapConnectorModel(connectorModel);
    doThrow(new IncidentException(SdipErrorCode.SOAP_RESPONSE_WSDL_NOK, LOGGER, "Test_Message",
        SdipErrorParameter.SUPPORTMAIL))
        .when(soapClient).init(any(), any());
    Throwable throwable = assertThrows(IncidentException.class, () -> {
      soapActionModel.run(daParams, jsonLdContextModel, serviceArguments);
    });

    assertEquals(SdipErrorCode.SOAP_RESPONSE_WSDL_NOK.getSdipErrorCode(),
        Integer
            .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                .getSdipErrorCodes().get(0)
                .replace("SDIP_", "")));
  }

  @Test
  void test_run() {

    HashMap<String, List<String>> inputParam = new HashMap<>();
    inputParam.put("KEY", Collections.singletonList("VALUE"));

    StringBuilder stringBuilder = new StringBuilder();
    soapActionModel.setRdf4jClient(rdf4jClient);
    soapActionModel.setSoapConnectorModel(connectorModel);
    soapActionModel.setInputParameter(parameterModel);
    soapActionModel.setOutputParameter(outputParameterModel);
    soapActionModel.setRdf4jClient(rdf4jClient);
    soapActionModel.setSoapClient(soapClient);
    soapActionModel.setSoapOperation("aOperation");
    List<String> inputKeys = new ArrayList<>();
    inputKeys.add("KEY");

    doNothing().when(soapClient).init(any(), any());
    doReturn("http://subjectIRI").when(parameterModel).getSubjectIri();
    doReturn(inputParam).when(daParamsSpy).getValue("http://subjectIRI");
    doReturn(inputKeys).when(parameterModel).getKeys();
    doReturn("aENVELOPE").when(soapClient).createSoapRequest(any(), any(), any());
    doReturn("OK").when(soapClient).sendRequestGetResponse(any(), any());
    doReturn(authModel).when(connectorModel).getHasAuthenticationMethod();
    doNothing().when(authModel).run(any());
    doReturn(inputKeys).when(outputParameterModel).getKeys();
    doReturn("http://outputIRI").when(outputParameterModel).getSubjectIri();

    try {
      soapActionModel.run(daParamsSpy, jsonLdContextModel, serviceArguments);
      verify(authModel, times(1)).run(any());
      verify(soapClient, times(1)).createSoapRequest(any(), any(), any());
      verify(soapClient, times(1)).sendRequestGetResponse(any(), any());
      assertEquals("OK",
          daParamsSpy.getValue("http://outputIRI").get("KEY").get(0));
    } catch (Exception e) {
      Assertions.fail(e.getMessage());
    }
  }

  @Test
  void test_run_multiple() {

    List<String> content = new ArrayList<>();
    content.add("value0");
    content.add("value1");
    content.add("value2");
    HashMap<String, List<String>> inputParam = new HashMap<>();
    inputParam.put("KEY", content);

    StringBuilder stringBuilder = new StringBuilder();
    soapActionModel.setRdf4jClient(rdf4jClient);
    soapActionModel.setSoapConnectorModel(connectorModel);
    soapActionModel.setInputParameter(parameterModel);
    soapActionModel.setOutputParameter(outputParameterModel);
    soapActionModel.setRdf4jClient(rdf4jClient);
    soapActionModel.setSoapClient(soapClient);
    soapActionModel.setSoapOperation("aOperation");
    List<String> inputKeys = new ArrayList<>();
    inputKeys.add("KEY");

    doNothing().when(soapClient).init(any(), any());
    doReturn("http://subjectIRI").when(parameterModel).getSubjectIri();
    doReturn(inputParam).when(daParams).getValue("http://subjectIRI");
    doReturn(inputKeys).when(parameterModel).getKeys();
    doReturn("aENVELOPE").when(soapClient).createSoapRequest(any(), any(), any());
    doReturn("OK").when(soapClient).sendRequestGetResponse(any(), any());

    doReturn(inputKeys).when(outputParameterModel).getKeys();
    doReturn("http://outputIRI").when(outputParameterModel).getSubjectIri();

    try {
      soapActionModel.run(daParams, jsonLdContextModel, serviceArguments);
      verify(soapClient, times(3)).createSoapRequest(any(), any(), any());
    } catch (Exception e) {
      Assertions.fail(e.getMessage());
    }
  }

  @Test
  void populate_getActionVirtualGraphAction() {
    soapActionModel.setRdf4jClient(rdf4jClient);
    // test successful case
    String response = UnitTestHelper.readJsonFiles(UnitTestConstants.SOAPACTIONMODELTESTDATA_VIRTUALGRAPHACTION);
    JsonArray daAnswer = JsonParser.parseString(response).getAsJsonArray();
    doReturn(daAnswer).when(rdf4jClient).selectSparqlOfg(any(), any());
    actionModelFactory.when(() ->
        ActionModelFactory.getAction(any(), any())).thenReturn(iActionModel);
    connectorModelFactory.when(() ->
        ConnectorModelFactory.getConnector(any())).thenReturn(connectorModel);
    parameterModelFactory.when(() ->
        ParameterModelFactory.getParameter(any())).thenReturn(parameterModel);
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();
    // run it!
    soapActionModel.populate("next/action/IRI", daParams, serviceArguments);

    verify(iActionModel, times(1))
        .populate("https://atest#daactiona", daParams, serviceArguments);
    actionModelFactory.verify(
        () -> ActionModelFactory.getAction("https://atest#VirtualGraphAction", daParams),
            times(1)
    );
  }

  @Test
  void populate_missingNextActionType() {
    //reset(doThrow());
    // Testing that the SPARQL-query consists of a UNION for VirtualGraphAction as nextactiontype
    soapActionModel.setRdf4jClient(rdf4jClient);
    String sparql = SoapActionModel.SPARQL.replace(SDOSConstants.VARIABLE, "aSubjectIri");
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
      soapActionModel.populate("https://atest#VirtualGraphAction", daParams, serviceArguments);
    });
    assertEquals(SdipErrorCode.FAILED_TO_PARSE_JSONARRAY.getSdipErrorCode(),
            Integer.parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                    .getSdipErrorCodes().get(0)
                    .replace("SDIP_", "")));
  }
}
