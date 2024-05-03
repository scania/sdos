package com.scania.sdos.testServices;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import com.google.gson.JsonObject;
import com.scania.sdip.exceptions.IncidentException;
import com.scania.sdip.exceptions.SdipErrorCode;
import com.scania.sdip.exceptions.SdipErrorParameter;
import com.scania.sdos.jwt.JwtTokenFilter;
import com.scania.sdos.model.GetAllAvailableTasksRequest;
import com.scania.sdos.model.OrchestrationRequestModel;
import com.scania.sdos.model.ServiceArguments;
import com.scania.sdos.orchestration.ActionRunner;
import com.scania.sdos.orchestration.OfgModelRepo;
import com.scania.sdos.orchestration.ParameterMemory;
import com.scania.sdos.orchestration.Rdf4jClient;
import com.scania.sdos.orchestration.factory.HelpModelFactory;
import com.scania.sdos.orchestration.factory.MetaDataModelFactory;
import com.scania.sdos.orchestration.factory.OfgModelRepoFactory;
import com.scania.sdos.orchestration.interfaces.IParameterMemory;
import com.scania.sdos.orchestration.model.HelpModel;
import com.scania.sdos.orchestration.model.MetaDataModel;
import com.scania.sdos.orchestration.model.ResultMetaDataModel;
import com.scania.sdos.orchestration.model.TaskModel;
import com.scania.sdos.services.ControllerService;
import com.scania.sdos.testUtils.UnitTestConstants;
import com.scania.sdos.testUtils.UnitTestHelper;
import com.scania.sdos.utils.SDOSConstants;
import com.scania.sdos.utils.StateEnum;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.UnsupportedRDFormatException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ControllerServiceTest {


  private static final Logger LOGGER = LogManager.getLogger(ControllerServiceTest.class);
  private static MockedStatic ofgModelRepoStatic;
  private static MockedStatic metaDataModelStatic;
  private static MockedStatic paramMemoryObject;
  private static MockedStatic modelFactory;
  private static MockedStatic rioObject;
  @Spy
  private ControllerService controllerService;
  @Mock
  private OrchestrationRequestModel requestModel;
  @Mock
  private ResultMetaDataModel metaModel;
  @Mock
  private MetaDataModel metaDataModel;
  @Mock
  private TaskModel dasModel;
  @Mock
  private ActionRunner daActionRunner;
  @Spy
  private ParameterMemory parameterMemory;
  @Mock
  private OfgModelRepo ofgModelRepo;
  @Mock
  private HelpModelFactory helpModelFactory;
  @Mock
  private HelpModel helpModel;
  @Mock
  private Rdf4jClient rdf4jClient;
  @Mock
  private ServiceArguments serviceArguments;

  @Mock
  private JwtTokenFilter jwtTokenFilter;

  @BeforeAll
  static void beforeAll() {
    ofgModelRepoStatic = mockStatic(OfgModelRepoFactory.class);
    metaDataModelStatic = mockStatic(MetaDataModelFactory.class);
    paramMemoryObject = mockStatic(ParameterMemory.class);
    modelFactory = mockStatic(HelpModelFactory.class);
    rioObject = mockStatic(Rio.class);
  }


  @AfterAll
  static void afterAll() {
    ofgModelRepoStatic.close();
    metaDataModelStatic.close();
    modelFactory.close();
    rioObject.close();
  }

  @BeforeEach
  void setup() {
    reset(requestModel);
    reset(metaModel);
    reset(parameterMemory);
    ofgModelRepoStatic.reset();
    metaDataModelStatic.reset();
    modelFactory.reset();
    rioObject.reset();
    reset(ofgModelRepo);
    reset(helpModelFactory);
    reset(rdf4jClient);
    controllerService.setRdf4jClient(rdf4jClient);
  }


  @Test
  void test_handleAllTasks(){
    JsonObject queryList = new JsonObject();
    queryList.addProperty("key", "value");

    GetAllAvailableTasksRequest model = new GetAllAvailableTasksRequest();
    model.setSparqlEndpoint("sdos");
    controllerService.setServiceArguments(serviceArguments);
    Model rdfModel = mock(Model.class);
    doReturn(rdfModel).when(controllerService)
            .getAllTaskAsModel(any(IParameterMemory.class));
    modelFactory.when(() ->
            HelpModelFactory.getInstance()).thenReturn(helpModel);
    ofgModelRepoStatic.when(() ->
            OfgModelRepoFactory.getModelRepo()).thenReturn(ofgModelRepo);
    doNothing().when(ofgModelRepo).addModel(any());
    doReturn(queryList).when(helpModel).populate(any(), any());

    JsonObject obj = controllerService.handleAllTasks(parameterMemory);
    assertEquals("value", obj.get("key").getAsString());
  }

  @Test
  void test_handleOrchestrationSync_ok() {
    metaModel.setResultgraph("Graph");
    doReturn("Graph").when(metaModel).getResultgraph();
    doReturn(dasModel).when(controllerService).getTaskModel();
    doReturn(metaDataModel).when(dasModel).getMetaDataModel();
    doReturn(daActionRunner).when(controllerService).getActionRunner();
    doNothing().when(dasModel).populate(any(), any(), any(), any());
    doNothing().when(daActionRunner).run(any(), any(), any(), any());
    HashMap<String, List<String>> aParam = new HashMap<>();
    aParam.put(SDOSConstants.GRAPH, Collections.singletonList(UnitTestConstants.LIBRARY_JSON_LD));
    parameterMemory.putParameter(SDOSConstants.SYNC_RESULT, aParam);
    HashMap<String, List<String>> map = new HashMap();
    map.put(SDOSConstants.ID, Collections.singletonList("Graph"));
    parameterMemory.putParameter(SDOSConstants.EXECUTION_REPORT, map);

    doReturn("aSubjectIri").when(requestModel).getSubjectIri();
    Model rdfModel = mock(Model.class);
    doReturn(rdfModel).when(controllerService)
            .getOrchestrationFlowGraphAsModel(any(), any(IParameterMemory.class));
    ofgModelRepoStatic.when(() ->
            OfgModelRepoFactory.getModelRepo()).thenReturn(ofgModelRepo);
    doNothing().when(ofgModelRepo).addModel(any());
    doNothing().when(ofgModelRepo).removeModel();

    Model result = controllerService.handleOrchestrationSync(requestModel, metaModel,
            parameterMemory);
    Assertions.assertEquals(UnitTestHelper.getLibraryModelForTest(), result);
  }

  @Test
  void test_handleOrchestrationSync_populateThrows() {
    controllerService.setServiceArguments(serviceArguments);
    doReturn(dasModel).when(controllerService).getTaskModel();
    doReturn("aSubjectIri").when(requestModel).getSubjectIri();
    Model rdfModel = mock(Model.class);
    doReturn(rdfModel).when(controllerService)
            .getOrchestrationFlowGraphAsModel(any(), any(IParameterMemory.class));
    ofgModelRepoStatic.when(() ->
            OfgModelRepoFactory.getModelRepo()).thenReturn(ofgModelRepo);
    doNothing().when(ofgModelRepo).addModel(any());
    doNothing().when(ofgModelRepo).removeModel();

    String message = "aMessage";
    doThrow(new IncidentException(SdipErrorCode.UNKNOWN_REASON_ERROR, LOGGER, message,
            SdipErrorParameter.SUPPORTMAIL)).when(dasModel).populate(any(), any(), any(), any());
    metaDataModelStatic.when(() ->
            MetaDataModelFactory.getModel()).thenReturn(metaModel);
    ResultMetaDataModel aResultMetaDataModel = controllerService.createResultMetaDataModel(
            parameterMemory);
    Throwable throwable = assertThrows(IncidentException.class, () -> {
      controllerService.handleOrchestrationSync(requestModel, aResultMetaDataModel,
              parameterMemory);
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
  void test_handleOrchestrationSync_resultNotJsonLd_RdfException() {
    controllerService.setServiceArguments(serviceArguments);

    doReturn(dasModel).when(controllerService).getTaskModel();
    doReturn(metaDataModel).when(dasModel).getMetaDataModel();
    doReturn(daActionRunner).when(controllerService).getActionRunner();
    doNothing().when(dasModel).populate(any(), any(), any(), any());
    doNothing().when(daActionRunner).run(any(), any(), any(), any());
    HashMap<String, List<String>> aParam = new HashMap<>();
    aParam.put(SDOSConstants.GRAPH, Collections.singletonList("Not a JSON-LD"));
    parameterMemory.putParameter(SDOSConstants.SYNC_RESULT, aParam);
    HashMap<String, List<String>> map = new HashMap();
    map.put(SDOSConstants.ID, Collections.singletonList("Graph"));
    parameterMemory.putParameter(SDOSConstants.EXECUTION_REPORT, map);
    metaDataModelStatic.when(() ->
            MetaDataModelFactory.getModel()).thenReturn(metaModel);
    ResultMetaDataModel aResultMetaDataModel = controllerService.createResultMetaDataModel(
            parameterMemory);
    doReturn("aSubjectIri").when(requestModel).getSubjectIri();
    Model rdfModel = mock(Model.class);
    doReturn(rdfModel).when(controllerService)
            .getOrchestrationFlowGraphAsModel(any(), any(IParameterMemory.class));
    ofgModelRepoStatic.when(() ->
            OfgModelRepoFactory.getModelRepo()).thenReturn(ofgModelRepo);
    doNothing().when(ofgModelRepo).addModel(any());
    doNothing().when(ofgModelRepo).removeModel();
    rioObject.when(() ->
            Rio.parse(any(InputStream.class), any())).thenThrow(new RDFParseException("Could not parse JSONLD"));
    Throwable throwable = assertThrows(IncidentException.class, () -> {
      controllerService.handleOrchestrationSync(requestModel, aResultMetaDataModel,
              parameterMemory);
    });

    assertEquals(SdipErrorCode.RDF_STORE_RESPONSE_NOT_JSON_LD.getSdipErrorCode(),
            Integer
                    .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                            .getSdipErrorCodes().get(0)
                            .replace("SDIP_", "")));
    assertEquals(1, ((IncidentException) throwable).getErrorBindings().get(0).getArgs().length);
    Object[] args = ((IncidentException) throwable).getErrorBindings().get(0).getArgs();
    assertTrue(((String) args[0]).contains("Could not parse JSONLD"));

  }

  @Test
  void test_handleOrchestrationSync_resultNotJsonLd_IoException() {
    controllerService.setServiceArguments(serviceArguments);

    doReturn(dasModel).when(controllerService).getTaskModel();
    doReturn(metaDataModel).when(dasModel).getMetaDataModel();
    doReturn(daActionRunner).when(controllerService).getActionRunner();
    doNothing().when(dasModel).populate(any(), any(), any(), any());
    doNothing().when(daActionRunner).run(any(), any(), any(), any());
    HashMap<String, List<String>> aParam = new HashMap<>();
    aParam.put(SDOSConstants.GRAPH, Collections.singletonList("Not a JSON-LD"));
    parameterMemory.putParameter(SDOSConstants.SYNC_RESULT, aParam);
    HashMap<String, List<String>> map = new HashMap();
    map.put(SDOSConstants.ID, Collections.singletonList("Graph"));
    parameterMemory.putParameter(SDOSConstants.EXECUTION_REPORT, map);
    metaDataModelStatic.when(() ->
            MetaDataModelFactory.getModel()).thenReturn(metaModel);
    ResultMetaDataModel aResultMetaDataModel = controllerService.createResultMetaDataModel(
            parameterMemory);
    doReturn("aSubjectIri").when(requestModel).getSubjectIri();
    Model rdfModel = mock(Model.class);
    doReturn(rdfModel).when(controllerService)
            .getOrchestrationFlowGraphAsModel(any(), any(IParameterMemory.class));
    ofgModelRepoStatic.when(() ->
            OfgModelRepoFactory.getModelRepo()).thenReturn(ofgModelRepo);
    doNothing().when(ofgModelRepo).addModel(any());
    doNothing().when(ofgModelRepo).removeModel();

    rioObject.when(() ->
            Rio.parse(any(InputStream.class), any())).thenThrow(new IOException("Could not parse JSONLD"));
    Throwable throwableIo = assertThrows(IncidentException.class, () -> {
      controllerService.handleOrchestrationSync(requestModel, aResultMetaDataModel,
              parameterMemory);
    });

    assertEquals(SdipErrorCode.IO_ERROR.getSdipErrorCode(),
            Integer
                    .parseInt(((IncidentException) throwableIo).createHttpErrorResponse().getBody()
                            .getSdipErrorCodes().get(0)
                            .replace("SDIP_", "")));
    assertEquals(1, ((IncidentException) throwableIo).getErrorBindings().get(0).getArgs().length);
    Object[] argsIo = ((IncidentException) throwableIo).getErrorBindings().get(0).getArgs();
    assertTrue(((String) argsIo[0]).contains("Could not parse JSONLD"));
  }

  @Test
  void getOrchestrationFlowGraphAsModel_ok() {
    String taskIri = "anIri";
    String expectedQuery = SDOSConstants.PREFIX_RDF
            + "PREFIX : <" + SDOSConstants.ORCHESTRATION_PREFIX + "> \n"
            + "CONSTRUCT {\n"
            + "    ?s ?p ?o\n"
            + "}\n"
            + "{\n"
            + "    BIND(<"
            + taskIri
            + "> AS ?taskIri)\n"
            + "    GRAPH ?GraphName {\n"
            + "        ?taskIri rdf:type :Task .\n"
            + "        ?s ?p ?o\n"
            + "    }\n"
            + "}";
    String endpoint = "anEndpoint";

    doReturn(endpoint).when(serviceArguments).getStardogQueryEndpoint();
    controllerService.setServiceArguments(serviceArguments);
    String response = UnitTestHelper.readJsonFiles(UnitTestConstants.GRAPH_AS_MODEL);
    Model rdfModel = mock(Model.class);
    doReturn(response).when(rdf4jClient)
            .doConstructSparql(any(), any(), any(IParameterMemory.class));
    rioObject.when(() ->
           Rio.parse(any(InputStream.class), any())).thenReturn(rdfModel);
    Model result = controllerService.getOrchestrationFlowGraphAsModel(taskIri, parameterMemory);
    assertNotNull(result);
    verify(rdf4jClient, times(1)).doConstructSparql(expectedQuery, endpoint, parameterMemory);
  }

  @Test
  void getOrchestrationFlowGraphAsModel_throws() {

    doReturn("anEndpoint").when(serviceArguments).getStardogQueryEndpoint();
    controllerService.setServiceArguments(serviceArguments);

    rioObject.when(() ->
            Rio.parse(any(InputStream.class), any())).thenThrow(new RDFParseException("test"));

    doReturn("null").when(rdf4jClient).doConstructSparql(any(), any(), any(IParameterMemory.class));

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      controllerService.getOrchestrationFlowGraphAsModel("anIri", parameterMemory);
    });
    assertEquals(SdipErrorCode.RDF_STORE_RESPONSE_NOT_JSON_LD.getSdipErrorCode(),
            Integer
                    .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                            .getSdipErrorCodes().get(0)
                            .replace("SDIP_", "")));
    rioObject.reset();

    rioObject.when(() ->
            Rio.parse(any(InputStream.class), any())).thenThrow(new UnsupportedRDFormatException("test"));

    doReturn("null").when(rdf4jClient).doConstructSparql(any(), any(), any(IParameterMemory.class));

    Throwable throwable1 = assertThrows(IncidentException.class, () -> {
      controllerService.getOrchestrationFlowGraphAsModel("anIri", parameterMemory);
    });
    assertEquals(SdipErrorCode.RDF_STORE_RESPONSE_NOT_JSON_LD.getSdipErrorCode(),
            Integer
                    .parseInt(((IncidentException) throwable1).createHttpErrorResponse().getBody()
                            .getSdipErrorCodes().get(0)
                            .replace("SDIP_", "")));

    rioObject.reset();

    rioObject.when(() ->
            Rio.parse(any(InputStream.class), any())).thenThrow(new IOException("test"));

    doReturn("null").when(rdf4jClient).doConstructSparql(any(), any(), any(IParameterMemory.class));

    Throwable throwable2 = assertThrows(IncidentException.class, () -> {
      controllerService.getOrchestrationFlowGraphAsModel("anIri", parameterMemory);
    });
    assertEquals(SdipErrorCode.IO_ERROR.getSdipErrorCode(),
            Integer
                    .parseInt(((IncidentException) throwable2).createHttpErrorResponse().getBody()
                            .getSdipErrorCodes().get(0)
                            .replace("SDIP_", "")));
    rioObject.reset();
  }
  @Test
  void getAllTaskAsModel_ok() {
    rioObject.reset();
    String taskIri = "anIri";
    String expectedQuery = UnitTestConstants.CONSTRUCT_QUERY;
    String endpoint = "anEndpoint";

    doReturn(endpoint).when(serviceArguments).getStardogQueryEndpoint();
    controllerService.setServiceArguments(serviceArguments);
    String response = UnitTestHelper.readJsonFiles(UnitTestConstants.CONSTRUCT_QUERY_RESPONSE);
    Model rdfModel = mock(Model.class);
    doReturn(response).when(rdf4jClient)
            .doConstructSparql(any(), any(), any(IParameterMemory.class));
    rioObject.when(() ->
            Rio.parse(any(InputStream.class), any())).thenReturn(rdfModel);
    Model result = controllerService.getAllTaskAsModel(parameterMemory);
    assertNotNull(result);
    verify(rdf4jClient, times(1)).doConstructSparql(expectedQuery, endpoint, parameterMemory);

  }
  @Test
  void getAllTaskAsModel_throws() {

    doReturn("anEndpoint").when(serviceArguments).getStardogQueryEndpoint();
    controllerService.setServiceArguments(serviceArguments);

    rioObject.when(() ->
            Rio.parse(any(InputStream.class), any())).thenThrow(new RDFParseException("test"));

    doReturn("null").when(rdf4jClient).doConstructSparql(any(), any(), any(IParameterMemory.class));

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      controllerService.getAllTaskAsModel(parameterMemory);
    });
    assertEquals(SdipErrorCode.RDF_STORE_RESPONSE_NOT_JSON_LD.getSdipErrorCode(),
            Integer
                    .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                            .getSdipErrorCodes().get(0)
                            .replace("SDIP_", "")));
    rioObject.reset();

    rioObject.when(() ->
            Rio.parse(any(InputStream.class), any())).thenThrow(new UnsupportedRDFormatException("test"));

    doReturn("null").when(rdf4jClient).doConstructSparql(any(), any(), any(IParameterMemory.class));

    Throwable throwable1 = assertThrows(IncidentException.class, () -> {
      controllerService.getAllTaskAsModel(parameterMemory);
    });
    assertEquals(SdipErrorCode.RDF_STORE_RESPONSE_NOT_JSON_LD.getSdipErrorCode(),
            Integer
                    .parseInt(((IncidentException) throwable1).createHttpErrorResponse().getBody()
                            .getSdipErrorCodes().get(0)
                            .replace("SDIP_", "")));

    rioObject.reset();

    rioObject.when(() ->
            Rio.parse(any(InputStream.class), any())).thenThrow(new IOException("test"));

    doReturn("null").when(rdf4jClient).doConstructSparql(any(), any(), any(IParameterMemory.class));

    Throwable throwable2 = assertThrows(IncidentException.class, () -> {
      controllerService.getAllTaskAsModel(parameterMemory);
    });
    assertEquals(SdipErrorCode.IO_ERROR.getSdipErrorCode(),
            Integer
                    .parseInt(((IncidentException) throwable2).createHttpErrorResponse().getBody()
                            .getSdipErrorCodes().get(0)
                            .replace("SDIP_", "")));
  }

  @Test
  void doHandleOrchestration_ok() {
    metaModel.setResultgraph("Graph");

    doReturn(dasModel).when(controllerService).getTaskModel();
    doReturn(metaDataModel).when(dasModel).getMetaDataModel();
    doReturn(daActionRunner).when(controllerService).getActionRunner();
    doNothing().when(dasModel).populate(any(), any(), any(), any());
    doNothing().when(daActionRunner).run(any(), any(), any(), any());
    doReturn("Graph").when(metaModel).getResultgraph();
    String aSubjectIri = "aSubjectIri";
    doReturn(aSubjectIri).when(requestModel).getSubjectIri();
    Model rdfModel = mock(Model.class);
    doReturn(rdfModel).when(controllerService)
            .getOrchestrationFlowGraphAsModel(any(), any(IParameterMemory.class));
    ofgModelRepoStatic.when(() ->
            OfgModelRepoFactory.getModelRepo()).thenReturn(ofgModelRepo);
    doNothing().when(ofgModelRepo).addModel(any());
    doNothing().when(ofgModelRepo).removeModel();
    HashMap<String, List<String>> map = new HashMap();
    map.put(SDOSConstants.ID, Collections.singletonList("Graph"));
    parameterMemory.putParameter(SDOSConstants.EXECUTION_REPORT, map);
    controllerService.doHandleOrchestration(requestModel, metaModel, parameterMemory);
    verify(parameterMemory, times(1)).clear();
    verify(metaModel).setState(StateEnum.COMPLETE.toString());
    verify(metaModel, times(2)).run(any(), any(), any()); // run once in constructor as well
    verify(ofgModelRepo, times(1)).addModel(rdfModel);
    verify(ofgModelRepo, times(1)).removeModel();
    //verify(parameterMemory, times(1)).putParameter(eq(SDOSConstants.CURRENT_TASK), any());
  }

  @Test
  void doHandleOrchestration_populateThrows() {

    doReturn(dasModel).when(controllerService).getTaskModel();
    doThrow(new IncidentException(SdipErrorCode.UNKNOWN_REASON_ERROR, LOGGER, "amessage",
            SdipErrorParameter.SUPPORTMAIL)).when(dasModel).populate(any(), any(), any(), any());
    String aSubjectIri = "aSubjectIri";
    doReturn(aSubjectIri).when(requestModel).getSubjectIri();
    Model rdfModel = mock(Model.class);
    doReturn(rdfModel).when(controllerService)
            .getOrchestrationFlowGraphAsModel(any(), any(IParameterMemory.class));
    ofgModelRepoStatic.when(() ->
            OfgModelRepoFactory.getModelRepo()).thenReturn(ofgModelRepo);
    doNothing().when(ofgModelRepo).addModel(any());
    doNothing().when(ofgModelRepo).removeModel();
    HashMap<String, List<String>> execId = new HashMap<String, List<String>>() {{
      put(SDOSConstants.ID, Arrays.asList("anId"));
    }};
    parameterMemory.putParameter(SDOSConstants.EXECUTION_REPORT, execId);
    Throwable throwable = assertThrows(IncidentException.class, () -> {
      controllerService.doHandleOrchestration(requestModel, metaModel, parameterMemory);
    });
    verify(metaModel).setState(StateEnum.FAILED.toString());
    verify(parameterMemory, times(1)).clear();
    verify(ofgModelRepo, times(1)).addModel(rdfModel);
    verify(ofgModelRepo, times(1)).removeModel();
    //verify(parameterMemory, times(1)).putParameter(eq(SDOSConstants.CURRENT_TASK), any());
  }

  @Test
  void getResultGraphNameAsJson(){
    String result = controllerService.getResultGraphNameAsJson(metaModel);
    assertEquals("{\"resultgraph\":null}", result);
  }

  @Test
  void test_validateAuthToken(){
    controllerService.setJwtTokenFilter(jwtTokenFilter);
    doReturn(true).when(jwtTokenFilter).validateJwtToken(any());
    doNothing().when(jwtTokenFilter).jwtProcess(anyString(),anyBoolean(),any());
    controllerService.validateAuthToken("testToken",parameterMemory);
    verify(jwtTokenFilter, times(1)).validateJwtToken(any());
    verify(jwtTokenFilter, times(1)).jwtProcess(anyString(),anyBoolean(),any());
  }

}