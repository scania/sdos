package com.scania.sdos.testController;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;

import com.google.gson.JsonObject;
import com.scania.sdip.exceptions.IncidentException;
import com.scania.sdos.controller.SdosController;
import com.scania.sdos.health.HealthCheck;
import com.scania.sdos.model.GetAllAvailableTasksRequest;
import com.scania.sdos.model.OrchestrationParameterKeyValModel;
import com.scania.sdos.model.OrchestrationParameterModel;
import com.scania.sdos.model.OrchestrationRequestModel;
import com.scania.sdos.model.ServiceArguments;
import com.scania.sdos.orchestration.model.ResultMetaDataModel;
import com.scania.sdos.services.ControllerService;
import com.scania.sdos.testUtils.UnitTestConstants;
import com.scania.sdos.testUtils.UnitTestHelper;
import com.scania.sdos.utils.SDOSConstants;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
public class SdosControllerTest {

  @Mock
  private ControllerService controllerService;

  @Spy
  private SdosController sdosController;

  @BeforeAll
  static void beforeAll() {

  }

  @AfterAll
  static void afterAll() {

  }

  @BeforeEach
  void setUp() {

    reset(controllerService);
    reset(sdosController);
  }

  @Test
  public void controllerTest_apiGateway() {
    // Arrange
    sdosController.setControllerService(controllerService);
    //Act&Assert
    try {
      ResponseEntity<String> entity = sdosController.apiGateway();
      assertEquals(HttpStatus.OK, entity.getStatusCode());
      Assertions.assertEquals(SDOSConstants.SUCCESSFUL, entity.getBody());
    } catch (IncidentException e) {
      fail();
    }
  }

  @Test
  public void healthCheckTest() {
    ServiceArguments service = new ServiceArguments();
    service.setServiceId("SDOS_TEST");

    Assertions.assertEquals("UP", new HealthCheck(service).health().getStatus().getCode());
    assertEquals("UP {serviceId=SDOS_TEST}", new HealthCheck(service).health().toString().trim());
  }

  @Test
  public void getTasks() {
    JsonObject queryList = new JsonObject();
    queryList.addProperty("key", "success");
    doReturn(queryList).when(controllerService).handleAllTasks(any());

    GetAllAvailableTasksRequest aReqModel = getGetAllAvailableTasksRequest();
    sdosController.setControllerService(controllerService);
    //With Header
    ResponseEntity<String> headerEntity = sdosController.getTasks("Bearer 12345sdos");
    assertEquals(HttpStatus.OK, headerEntity.getStatusCode());

    //Without Header
    ResponseEntity<String> entity = sdosController.getTasks(null);
    assertEquals(HttpStatus.OK, entity.getStatusCode());

  }

  @Test
  public void callOrchestration() {
    ResultMetaDataModel resultMetaDataModel = mock(ResultMetaDataModel.class);
    doReturn(resultMetaDataModel).when(controllerService).createResultMetaDataModel(any());
    Model aModel = UnitTestHelper.getLibraryModelForTest();
    doNothing().when(controllerService).handleOrchestration(any(), any(), any());
    doReturn("{\"key\":\"success\"}").when(controllerService).getResultGraphNameAsJson(any());
    OrchestrationRequestModel aReqModel = getOrchestrationRequestModel();
    sdosController.setControllerService(controllerService);
    doNothing().when(controllerService).validateAuthToken(any(), any());
    //With Header
    ResponseEntity<String> headerEntity = sdosController.callOrchestration("Bearer 12345sdos",
        aReqModel);
    assertEquals(HttpStatus.OK, headerEntity.getStatusCode());
    Model headerModel = UnitTestHelper.toModel(headerEntity.getBody(), RDFFormat.JSONLD);
    assertNotEquals(aModel, headerModel);

    //Without Header
    ResponseEntity<String> entity = sdosController.callOrchestration(null, aReqModel);
    assertEquals(HttpStatus.OK, entity.getStatusCode());
    Model bodyModel = UnitTestHelper.toModel(entity.getBody(), RDFFormat.JSONLD);
    assertNotEquals(aModel, bodyModel);
  }

  @Test
  public void callOrchestrationSyncAsJsonLd() {
    ResultMetaDataModel resultMetaDataModel = mock(ResultMetaDataModel.class);
    doReturn(resultMetaDataModel).when(controllerService).createResultMetaDataModel(any());
    Model aModel = UnitTestHelper.getLibraryModelForTest();
    doReturn(aModel).when(controllerService).handleOrchestrationSync(any(), any(), any());
    OrchestrationRequestModel aReqModel = getOrchestrationRequestModel();
    sdosController.setControllerService(controllerService);
    doNothing().when(controllerService).validateAuthToken(any(), any());
    ResponseEntity<String> entity = sdosController.callOrchestrationSyncAsJsonLd(null, aReqModel);
    assertEquals(HttpStatus.OK, entity.getStatusCode());
    Model bodyModel = UnitTestHelper.toModel(entity.getBody(), RDFFormat.JSONLD);
    assertEquals(aModel, bodyModel);
  }

  @Test
  public void callOrchestrationSyncAsJsonLd_withJwtTokenHeader() {
    ResultMetaDataModel resultMetaDataModel = mock(ResultMetaDataModel.class);
    doReturn(resultMetaDataModel).when(controllerService).createResultMetaDataModel(any());
    Model aModel = UnitTestHelper.getLibraryModelForTest();
    doReturn(aModel).when(controllerService).handleOrchestrationSync(any(), any(), any());
    OrchestrationRequestModel aReqModel = getOrchestrationRequestModel();
    sdosController.setControllerService(controllerService);
    doNothing().when(controllerService).validateAuthToken(any(), any());
    ResponseEntity<String> entity = sdosController.callOrchestrationSyncAsJsonLd("Bearer 12345sdos",
        aReqModel);
    assertEquals(HttpStatus.OK, entity.getStatusCode());
    Model bodyModel = UnitTestHelper.toModel(entity.getBody(), RDFFormat.JSONLD);
    assertEquals(aModel, bodyModel);
  }

  @Test
  public void callOrchestrationSyncAsRdfXml() {
    ResultMetaDataModel resultMetaDataModel = mock(ResultMetaDataModel.class);
    doReturn(resultMetaDataModel).when(controllerService).createResultMetaDataModel(any());
    Model aModel = UnitTestHelper.getLibraryModelForTest();
    doReturn(aModel).when(controllerService).handleOrchestrationSync(any(), any(), any());
    OrchestrationRequestModel aReqModel = getOrchestrationRequestModel();
    sdosController.setControllerService(controllerService);
    doNothing().when(controllerService).validateAuthToken(any(), any());
    ResponseEntity<String> entity = sdosController.callOrchestrationSyncAsRdfXml(null, aReqModel);
    assertEquals(HttpStatus.OK, entity.getStatusCode());
    Model bodyModel = UnitTestHelper.toModel(entity.getBody(), RDFFormat.RDFXML);
    assertEquals(aModel, bodyModel);
  }

  @Test
  public void callOrchestrationSyncAsRdfXml_withJwtTokenHeader() {
    ResultMetaDataModel resultMetaDataModel = mock(ResultMetaDataModel.class);
    doReturn(resultMetaDataModel).when(controllerService).createResultMetaDataModel(any());
    Model aModel = UnitTestHelper.getLibraryModelForTest();
    doReturn(aModel).when(controllerService).handleOrchestrationSync(any(), any(), any());
    OrchestrationRequestModel aReqModel = getOrchestrationRequestModel();
    sdosController.setControllerService(controllerService);
    doNothing().when(controllerService).validateAuthToken(any(), any());
    ResponseEntity<String> entity = sdosController.callOrchestrationSyncAsRdfXml("Bearer 12345sdos",
        aReqModel);
    assertEquals(HttpStatus.OK, entity.getStatusCode());
    Model bodyModel = UnitTestHelper.toModel(entity.getBody(), RDFFormat.RDFXML);
    assertEquals(aModel, bodyModel);
  }

  @Test
  public void callOrchestrationSyncAsNTriples() {
    ResultMetaDataModel resultMetaDataModel = mock(ResultMetaDataModel.class);
    doReturn(resultMetaDataModel).when(controllerService).createResultMetaDataModel(any());
    Model aModel = UnitTestHelper.getLibraryModelForTest();
    doReturn(aModel).when(controllerService).handleOrchestrationSync(any(), any(), any());
    OrchestrationRequestModel aReqModel = getOrchestrationRequestModel();
    sdosController.setControllerService(controllerService);
    doNothing().when(controllerService).validateAuthToken(any(), any());
    ResponseEntity<String> entity = sdosController.callOrchestrationSyncAsNTriples(null, aReqModel);
    assertEquals(HttpStatus.OK, entity.getStatusCode());
    Model bodyModel = UnitTestHelper.toModel(entity.getBody(), RDFFormat.NTRIPLES);
    assertEquals(aModel, bodyModel);
  }

  @Test
  public void callOrchestrationSyncAsNTriples_withJwtTokenHeader() {
    ResultMetaDataModel resultMetaDataModel = mock(ResultMetaDataModel.class);
    doReturn(resultMetaDataModel).when(controllerService).createResultMetaDataModel(any());
    Model aModel = UnitTestHelper.getLibraryModelForTest();
    doReturn(aModel).when(controllerService).handleOrchestrationSync(any(), any(), any());
    OrchestrationRequestModel aReqModel = getOrchestrationRequestModel();
    sdosController.setControllerService(controllerService);
    doNothing().when(controllerService).validateAuthToken(any(), any());
    ResponseEntity<String> entity = sdosController.callOrchestrationSyncAsNTriples(
        "Bearer 12345sdos", aReqModel);
    assertEquals(HttpStatus.OK, entity.getStatusCode());
    Model bodyModel = UnitTestHelper.toModel(entity.getBody(), RDFFormat.NTRIPLES);
    assertEquals(aModel, bodyModel);
  }

  @Test
  public void callOrchestrationSyncAsTurtle() {
    ResultMetaDataModel resultMetaDataModel = mock(ResultMetaDataModel.class);
    doReturn(resultMetaDataModel).when(controllerService).createResultMetaDataModel(any());
    Model aModel = UnitTestHelper.getLibraryModelForTest();
    doReturn(aModel).when(controllerService).handleOrchestrationSync(any(), any(), any());
    OrchestrationRequestModel aReqModel = getOrchestrationRequestModel();
    sdosController.setControllerService(controllerService);
    doNothing().when(controllerService).validateAuthToken(any(), any());
    ResponseEntity<String> entity = sdosController.callOrchestrationSyncAsTurtle(null, aReqModel);
    assertEquals(HttpStatus.OK, entity.getStatusCode());
    Model bodyModel = UnitTestHelper.toModel(entity.getBody(), RDFFormat.TURTLE);
    assertEquals(aModel, bodyModel);
  }

  @Test
  public void callOrchestrationSyncAsTurtle_withJwtTokenHeader() {
    ResultMetaDataModel resultMetaDataModel = mock(ResultMetaDataModel.class);
    doReturn(resultMetaDataModel).when(controllerService).createResultMetaDataModel(any());
    Model aModel = UnitTestHelper.getLibraryModelForTest();
    doReturn(aModel).when(controllerService).handleOrchestrationSync(any(), any(), any());
    OrchestrationRequestModel aReqModel = getOrchestrationRequestModel();
    sdosController.setControllerService(controllerService);
    doNothing().when(controllerService).validateAuthToken(any(), any());
    ResponseEntity<String> entity = sdosController.callOrchestrationSyncAsTurtle("Bearer 12345sdos",
        aReqModel);
    assertEquals(HttpStatus.OK, entity.getStatusCode());
    Model bodyModel = UnitTestHelper.toModel(entity.getBody(), RDFFormat.TURTLE);
    assertEquals(aModel, bodyModel);
  }

  @Test
  public void callOrchestrationSyncAsN3() {
    ResultMetaDataModel resultMetaDataModel = mock(ResultMetaDataModel.class);
    doReturn(resultMetaDataModel).when(controllerService).createResultMetaDataModel(any());
    Model aModel = UnitTestHelper.getLibraryModelForTest();
    doReturn(aModel).when(controllerService).handleOrchestrationSync(any(), any(), any());
    OrchestrationRequestModel aReqModel = getOrchestrationRequestModel();
    sdosController.setControllerService(controllerService);
    doNothing().when(controllerService).validateAuthToken(any(), any());
    ResponseEntity<String> entity = sdosController.callOrchestrationSyncAsN3(null, aReqModel);
    assertEquals(HttpStatus.OK, entity.getStatusCode());
    Model bodyModel = UnitTestHelper.toModel(entity.getBody(), RDFFormat.N3);
    assertEquals(aModel, bodyModel);
  }

  @Test
  public void callOrchestrationSyncAsN3_withJwtTokenHeader() {
    ResultMetaDataModel resultMetaDataModel = mock(ResultMetaDataModel.class);
    doReturn(resultMetaDataModel).when(controllerService).createResultMetaDataModel(any());
    Model aModel = UnitTestHelper.getLibraryModelForTest();
    doReturn(aModel).when(controllerService).handleOrchestrationSync(any(), any(), any());
    OrchestrationRequestModel aReqModel = getOrchestrationRequestModel();
    sdosController.setControllerService(controllerService);
    doNothing().when(controllerService).validateAuthToken(any(), any());
    ResponseEntity<String> entity = sdosController.callOrchestrationSyncAsN3("Bearer 12345sdos",
        aReqModel);
    assertEquals(HttpStatus.OK, entity.getStatusCode());
    Model bodyModel = UnitTestHelper.toModel(entity.getBody(), RDFFormat.N3);
    assertEquals(aModel, bodyModel);
  }

  @Test
  public void callOrchestrationSyncAsNQuads() {
    ResultMetaDataModel resultMetaDataModel = mock(ResultMetaDataModel.class);
    doReturn(resultMetaDataModel).when(controllerService).createResultMetaDataModel(any());
    Model aModel = UnitTestHelper.getLibraryModelForTest();
    doReturn(aModel).when(controllerService).handleOrchestrationSync(any(), any(), any());
    OrchestrationRequestModel aReqModel = getOrchestrationRequestModel();
    sdosController.setControllerService(controllerService);
    doNothing().when(controllerService).validateAuthToken(any(), any());
    ResponseEntity<String> entity = sdosController.callOrchestrationSyncAsNQuads(null, aReqModel);
    assertEquals(HttpStatus.OK, entity.getStatusCode());
    Model bodyModel = UnitTestHelper.toModel(entity.getBody(), RDFFormat.NQUADS);
    assertEquals(aModel, bodyModel);
  }

  @Test
  public void callOrchestrationSyncAsNQuads_withJwtTokenHeader() {
    ResultMetaDataModel resultMetaDataModel = mock(ResultMetaDataModel.class);
    doReturn(resultMetaDataModel).when(controllerService).createResultMetaDataModel(any());
    Model aModel = UnitTestHelper.getLibraryModelForTest();
    doReturn(aModel).when(controllerService).handleOrchestrationSync(any(), any(), any());
    OrchestrationRequestModel aReqModel = getOrchestrationRequestModel();
    sdosController.setControllerService(controllerService);
    doNothing().when(controllerService).validateAuthToken(any(), any());
    ResponseEntity<String> entity = sdosController.callOrchestrationSyncAsNQuads("Bearer 12345sdos",
        aReqModel);
    assertEquals(HttpStatus.OK, entity.getStatusCode());
    Model bodyModel = UnitTestHelper.toModel(entity.getBody(), RDFFormat.NQUADS);
    assertEquals(aModel, bodyModel);
  }


  private GetAllAvailableTasksRequest getGetAllAvailableTasksRequest() {
    GetAllAvailableTasksRequest model = new GetAllAvailableTasksRequest();
    model.setSparqlEndpoint("sdos");
    return model;
  }

  private OrchestrationRequestModel getOrchestrationRequestModel() {
    OrchestrationParameterKeyValModel keyValModel = new OrchestrationParameterKeyValModel();
    keyValModel.setKey("aKey");
    keyValModel.setValue("aVal");
    List<OrchestrationParameterKeyValModel> keyValPairs = new ArrayList<>();
    keyValPairs.add(keyValModel);
    OrchestrationParameterModel parameterModel = new OrchestrationParameterModel();
    parameterModel.setLabel("aLabel");
    parameterModel.setKeyValuePairs(keyValPairs);
    List<OrchestrationParameterModel> parameterModels = new ArrayList<>();
    parameterModels.add(parameterModel);
    OrchestrationRequestModel requestModel = new OrchestrationRequestModel();
    requestModel.setSubjectIri("aSubjectIri");
    requestModel.setParameters(parameterModels);
    return requestModel;
  }

  private HttpHeaders getTokenHeader() {
    HttpHeaders header = new HttpHeaders();
    header.add(HttpHeaders.AUTHORIZATION,
        " " + SDOSConstants.BEARER + " " + UnitTestConstants.DUMMY_JWT_TOKEN);
    return header;
  }

}