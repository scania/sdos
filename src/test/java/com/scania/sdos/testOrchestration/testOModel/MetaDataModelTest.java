package com.scania.sdos.testOrchestration.testOModel;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.scania.sdip.exceptions.IncidentException;
import com.scania.sdip.exceptions.SdipErrorCode;
import com.scania.sdip.exceptions.SdipErrorParameter;
import com.scania.sdos.model.ServiceArguments;
import com.scania.sdos.orchestration.OfgModelRepo;
import com.scania.sdos.orchestration.ParameterMemory;
import com.scania.sdos.orchestration.Rdf4jClient;
import com.scania.sdos.orchestration.model.JsonLdContextModel;
import com.scania.sdos.orchestration.model.MetaDataModel;
import com.scania.sdos.testUtils.UnitTestConstants;
import com.scania.sdos.testUtils.UnitTestHelper;
import com.scania.sdos.utils.SDOSConstants;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
public class MetaDataModelTest {

  @Mock
  private Logger logger;
  @Mock
  private ParameterMemory daParams;
  @Mock
  private ServiceArguments serviceArguments;
  @Mock
  private Rdf4jClient rdf4jClient;
  @Spy
  private MetaDataModel metaDataModel;
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
    reset(metaDataModel);
    reset(rdf4jClient);
    reset(serviceArguments);
  }

  @Test
  void populate_ok_run_with_token() {
    metaDataModel.setRdf4jClient(rdf4jClient);
    JsonLdContextModel context = Mockito.mock(JsonLdContextModel.class);
    String response = UnitTestHelper.readJsonFiles(UnitTestConstants.METADATAMODELTESTDATA);
    JsonArray daAnswer = JsonParser.parseString(response).getAsJsonArray();
    doReturn(daAnswer).when(rdf4jClient).selectSparqlOfg(any(), any());

    HashMap<String, List<String>> parameterMap = new HashMap<>();
    parameterMap.put(SDOSConstants.BEARER_TOKEN, Collections.singletonList(UnitTestConstants.DUMMY_JWT_TOKEN));
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();
    doReturn(parameterMap).when(daParams).getValue(SDOSConstants.BEARER_TOKEN);
    metaDataModel.populate("subjectIri", daParams, serviceArguments);

    assertEquals("testContributor", metaDataModel.getContributor());
    assertEquals("ravigyan.singh@scania.com", metaDataModel.getCreator());
    assertEquals("testDescription", metaDataModel.getDescription());
    assertEquals("SEMIPUBLIC", metaDataModel.getGraphType());
    assertEquals("testTitle", metaDataModel.getTitle());
    assertEquals("testLabel", metaDataModel.getLabel());
    assertEquals("testInformationResponsible", metaDataModel.getInformationResponsible());

    //Test run method
    HashMap<String, List<String>> map = new HashMap();
    map.put(SDOSConstants.ID, Collections.singletonList("Graph"));
    daParams.putParameter(SDOSConstants.EXECUTION_REPORT, map);
    doReturn("").when(metaDataModel).getSparql_INSERT(any());
    doNothing().when(rdf4jClient).executeUpdateSparql(anyString(), anyString(), any());
    metaDataModel.run(daParams, context, serviceArguments);
    verify(rdf4jClient, times(1)).executeUpdateSparql(anyString(), anyString(), eq(daParams));

    //Test Invalid Graph type  Exception
    response = UnitTestHelper.readJsonFiles(UnitTestConstants.METADATAMODELEXCEPTIONDATA);
    JsonArray daAnswer1 = JsonParser.parseString(response).getAsJsonArray();
    doReturn(daAnswer1).when(rdf4jClient).selectSparqlOfg(any(), any());

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      metaDataModel.populate("subjectIri", daParams, serviceArguments);
      ;
    });

    assertEquals(SdipErrorCode.UNKNOWN_PARSING_ERROR.getSdipErrorCode(),
        Integer
            .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                .getSdipErrorCodes().get(0)
                .replace("SDIP_", "")));

    //Test Invalid token exception
    String invalidJwtToken = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ";
    HashMap<String, List<String>> parameterMapException = new HashMap<>();
    parameterMapException.put(SDOSConstants.BEARER_TOKEN, Collections.singletonList(invalidJwtToken));
    doReturn(parameterMapException).when(daParams).getValue(SDOSConstants.BEARER_TOKEN);

    Throwable throwableTokenException = assertThrows(IncidentException.class, () -> {
      metaDataModel.populate("subjectIri", daParams, serviceArguments);
      ;
    });
    assertEquals(SdipErrorCode.INVALID_JSONARRAY_RESPONSE.getSdipErrorCode(),
            Integer.parseInt(((IncidentException) throwableTokenException).createHttpErrorResponse().getBody()
                    .getSdipErrorCodes().get(0)
                    .replace("SDIP_", "")));


    //Test Invalid token exception
    HashMap<String, List<String>> parameterMapException2 = new HashMap<>();
    parameterMapException2.put(SDOSConstants.BEARER_TOKEN, null);
    doReturn(parameterMapException2).when(daParams).getValue(SDOSConstants.BEARER_TOKEN);

    assertThrows(IncidentException.class, () -> {
      metaDataModel.populate("subjectIri", daParams, serviceArguments);
      ;
    });
  }



    @Test
  void populate_emptyResponse() {
    metaDataModel.setRdf4jClient(rdf4jClient);
    JsonArray daAnswer = JsonParser.parseString("[]").getAsJsonArray();
    doReturn(daAnswer).when(rdf4jClient).selectSparqlOfg(any(), any());
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      metaDataModel.populate("test", daParams, serviceArguments);
    });
    assertEquals(SdipErrorCode.INVALID_JSONARRAY_RESPONSE.getSdipErrorCode(),
        Integer
            .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                .getSdipErrorCodes().get(0)
                .replace("SDIP_", "")));
  }


  @Test
  void populate_invalidJsonArrayResponse() {
    metaDataModel.setRdf4jClient(rdf4jClient);
    doThrow(IllegalStateException.class).when(rdf4jClient).selectSparqlOfg(any(), any());
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      metaDataModel.populate("test", daParams, serviceArguments);
    });
    assertEquals(SdipErrorCode.INVALID_JSONARRAY_RESPONSE.getSdipErrorCode(),
        Integer
            .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                .getSdipErrorCodes().get(0)
                .replace("SDIP_", "")));
  }

  @Test
  void populate_invalidPropertyJsonArray() {
    metaDataModel.setRdf4jClient(rdf4jClient);
    JsonArray daAnswer = JsonParser.parseString("[{\"key\":\"value\"}]").getAsJsonArray();
    doReturn(daAnswer).when(rdf4jClient).selectSparqlOfg(any(), any());
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      metaDataModel.populate("test", daParams, serviceArguments);
    });
    assertEquals(SdipErrorCode.FAILED_TO_PARSE_JSONARRAY.getSdipErrorCode(),
        Integer
            .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                .getSdipErrorCodes().get(0)
                .replace("SDIP_", "")));
  }

  @Test
  void populate_unknownError() {
    metaDataModel.setRdf4jClient(rdf4jClient);
    doThrow(RuntimeException.class).when(rdf4jClient).selectSparqlOfg(any(), any());
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      metaDataModel.populate("test", daParams, serviceArguments);
    });
    assertEquals(SdipErrorCode.UNKNOWN_PARSING_ERROR.getSdipErrorCode(),
        Integer
            .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                .getSdipErrorCodes().get(0)
                .replace("SDIP_", "")));
  }

  @Test
  void populate_catchException() {
    metaDataModel.setRdf4jClient(rdf4jClient);
    String message = "Host name may not be null";
    doThrow(new IncidentException(SdipErrorCode.UNKNOWN_REASON_ERROR, logger, message,
        SdipErrorParameter.SUPPORTMAIL))
        .when(rdf4jClient).selectSparqlOfg(any(), any());
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      metaDataModel.populate("test", daParams, serviceArguments);
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
  void test_getSparql_INSERT() {
    metaDataModel.setRdf4jClient(rdf4jClient);
    metaDataModel.setSubjectIri("testIri");
    metaDataModel.setDescription("testDescription");
    metaDataModel.setCreator("testUser1@scania.com");
    metaDataModel.setContributor("testUser2@scania.com");
    metaDataModel.setInformationResponsible("testUser3@scania.com");
    metaDataModel.setGraphType("private");
    HashMap exec_report = new HashMap();
    exec_report.put("Id",Collections.singletonList("http://resultGraph"));
    doReturn(exec_report).when(daParams).getValue("ExecutionReport");

    String insertQuery = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
            "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
            "PREFIX : <https://kg.scania.com/it/iris_orchestration/> \n" +
            "PREFIX core: <http://kg.scania.com/core/> \n" +
            "INSERT DATA\n" +
            "{ \n" +
            "GRAPH <http://resultGraph> { \n" +
            "  <testIri> rdf:type core:Metadata ;\n" +
            "       rdfs:label \"MetaData\" ;\n" +
            "       core:graphType \"private\" ;\n" +
            "       core:creator \"testUser1@scania.com\" ;\n" +
            "       core:contributor \"testUser2@scania.com\" ;\n" +
            "       core:informationResponsible \"testUser3@scania.com\" ;\n" +
            "       :description \"testDescription\" .\n" +
            "       \n" +
            "}}";

    assertEquals(insertQuery, metaDataModel.getSparql_INSERT(daParams));
  }

  @Test
  void test_run_exception() {
    metaDataModel.setRdf4jClient(rdf4jClient);
    JsonLdContextModel jsonContext = Mockito.mock(JsonLdContextModel.class);
    doReturn("").when(metaDataModel).getSparql_INSERT(any());
    doThrow(RuntimeException.class).when(rdf4jClient).executeUpdateSparql(eq(""),anyString(),any());

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      metaDataModel.run(daParams,jsonContext,serviceArguments);
    });
    assertEquals(SdipErrorCode.UNKNOWN_REASON_ERROR.getSdipErrorCode(),
            Integer
                    .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                            .getSdipErrorCodes().get(0)
                            .replace(UnitTestConstants.SDIP, "")));


  }

  @Test
  void test_run_incidentException() {
    metaDataModel.setRdf4jClient(rdf4jClient);
    JsonLdContextModel jsonContext = Mockito.mock(JsonLdContextModel.class);
    doReturn("").when(metaDataModel).getSparql_INSERT(any());
    doThrow(new IncidentException(SdipErrorCode.MALFORMED_SPARQL_QUERY,logger,"testQuery")).when(rdf4jClient)
            .executeUpdateSparql(eq(""),anyString(),any());

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      metaDataModel.run(daParams,jsonContext,serviceArguments);
    });
    assertTrue(throwable instanceof IncidentException);
    assertEquals(SdipErrorCode.MALFORMED_SPARQL_QUERY.getSdipErrorCode(),
            Integer
                    .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                            .getSdipErrorCodes().get(0)
                            .replace(UnitTestConstants.SDIP, "")));


  }


}
