package com.scania.sdos.testOrchestration;

import static org.eclipse.rdf4j.model.util.Values.iri;
import static org.eclipse.rdf4j.model.util.Values.literal;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.complexible.stardog.rdf4j.StardogRepository;
import com.complexible.stardog.rdf4j.StardogRepositoryConnection;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.scania.sdip.exceptions.IncidentException;
import com.scania.sdip.exceptions.SdipErrorCode;
import com.scania.sdos.model.ServiceArguments;
import com.scania.sdos.orchestration.OfgModelRepo;
import com.scania.sdos.orchestration.ParameterMemory;
import com.scania.sdos.orchestration.Rdf4jClient;
import com.scania.sdos.orchestration.interfaces.IParameterMemory;
import com.scania.sdos.testUtils.UnitTestConstants;
import com.scania.sdos.utils.SDOSConstants;
import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.query.Dataset;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.parser.ParsedQuery;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.WriterConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

//@RunWith(MockitoJUnitRunner.Silent.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class Rdf4jClientTest {

  @Mock
  private ParameterMemory parameterMemoryMock;
  @Mock
  private SPARQLRepository sparqlRepoMock;
  @Spy
  private Rdf4jClient rdf4jClient;
  @Mock
  private OfgModelRepo ofgModelRepo;

  @BeforeEach
  void setUp() {
    reset(rdf4jClient);
  }

  @Test
  void selectSparqlOfg(){
    JsonArray anArray = mock(JsonArray.class);
    Repository repo = mock(Repository.class) ;
    String aQuery = "a query";

    doReturn(repo).when(ofgModelRepo).getRepository();
    doReturn(anArray).when(rdf4jClient).executeSelectSparqlQuery(any(), any(), any(),any());

    //Run it!
    rdf4jClient.selectSparqlOfg(aQuery, ofgModelRepo);

    // check it
    verify(rdf4jClient, times(1))
            .executeSelectSparqlQuery(ofgModelRepo.getRepository(), aQuery, "in-memory", null);
  }
  @Test
  void selectSparqlOrchestration_ok() {
    JsonArray anArray = mock(JsonArray.class);
    String aQuery = "a query";
    doReturn(anArray).when(rdf4jClient).executeSelectSparql(any(), any(), any());

    //Run it!
    rdf4jClient.selectSparqlOrchestration(aQuery, "anEndPoint", parameterMemoryMock);

    // check it
    verify(rdf4jClient, times(1))
            .executeSelectSparql(aQuery, "anEndPoint", parameterMemoryMock);
  }

  @Test
  void doSelectSparql_ok() {
    JsonArray anArray = mock(JsonArray.class);
    String aQuery = "a query with a " + SDOSConstants.NAMEDGRAPH + " in it";
    doReturn(anArray).when(rdf4jClient).executeSelectSparql(any(), any(), any());

    //Run it!
    rdf4jClient.doSelectSparql(aQuery, "anEndpoint", parameterMemoryMock);

    // check it
    verify(rdf4jClient, times(1))
            .executeSelectSparql(aQuery, "anEndpoint", parameterMemoryMock);
  }

  @Test
  void executeUpdateSparql_emptyEndpoint() {
    Throwable throwable = assertThrows(IncidentException.class, () -> {
      rdf4jClient.executeUpdateSparql(UnitTestConstants.DELETE_SPARQL, null, parameterMemoryMock);
    });
    assertEquals(SdipErrorCode.SPARQL_ENDPOINT_EMPTY.getSdipErrorCode(),
            Integer
                    .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                            .getSdipErrorCodes().get(0)
                            .replace("SDIP_", "")));

  }

  @Test
  void test_getStardogRepository() throws MalformedURLException {
    ServiceArguments serviceArguments = mock(ServiceArguments.class);
    doReturn("result").when(serviceArguments).getResultDB();
    URL url = new URL("http://localhost:5820");
    doReturn(url).when(serviceArguments).getStardogBaseUrl();
    Repository repository = rdf4jClient.getStardogRepository(serviceArguments);
    assertEquals(true, repository instanceof StardogRepository);
  }

  @Test
  void executeUpdateinvalidQuery() {
    String query = "SELECT*WHERE{?s ?p ?o}";
    SPARQLRepository repository = mock(SPARQLRepository.class);
    RepositoryConnection conn = mock(RepositoryConnection.class);
    //TODO RDF¤J DOESNT THROW MALFORMED_QUERY EVEN IF QUERY IS NOT SELECT SPARQL...

    when(parameterMemoryMock.getValue(SDOSConstants.OBO_TOKEN)).thenReturn(createMockBearerTokenParameterMap());
    doReturn(repository).when(rdf4jClient).getSparqlRepo(any());
    doReturn(conn).when(repository).getConnection();
    doThrow(MalformedQueryException.class).when(conn).prepareUpdate(query);
    Throwable throwable = assertThrows(IncidentException.class, () -> {
      rdf4jClient.executeUpdateSparql(query, "http://endpoint", parameterMemoryMock);
    });
    assertEquals(SdipErrorCode.MALFORMED_SPARQL_QUERY.getSdipErrorCode(),
            Integer
                    .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                            .getSdipErrorCodes().get(0)
                            .replace("SDIP_", "")));
  }

  @Test
  void executeUpdateSparql_repositoryError() {
    when(parameterMemoryMock.getValue(SDOSConstants.OBO_TOKEN)).thenReturn(createMockBearerTokenParameterMap());
    doReturn(sparqlRepoMock).when(rdf4jClient).getSparqlRepo(any());
    doThrow(RepositoryException.class).when(sparqlRepoMock).getConnection();

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      rdf4jClient.executeUpdateSparql("invalid_query", "endpoint", parameterMemoryMock);
    });
    assertEquals(SdipErrorCode.RDFSTORE_COMMUNICATION_ERROR.getSdipErrorCode(),
            Integer
                    .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                            .getSdipErrorCodes().get(0)
                            .replace("SDIP_", "")));
  }

  @Test
  void executeUpdateSparql_unknownError() {
    when(parameterMemoryMock.getValue(SDOSConstants.OBO_TOKEN)).thenReturn(createMockBearerTokenParameterMap());
    doReturn(sparqlRepoMock).when(rdf4jClient).getSparqlRepo(any());
    RepositoryConnection repo = Mockito.spy(RepositoryConnection.class);
    doReturn(null).when(sparqlRepoMock).getConnection();
    Throwable throwable = assertThrows(IncidentException.class, () -> {
      rdf4jClient.executeUpdateSparql(UnitTestConstants.SPARQLQUERY, "endpoint", parameterMemoryMock);
    });
    assertEquals(SdipErrorCode.UNKNOWN_REASON_ERROR.getSdipErrorCode(),
            Integer
                    .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                            .getSdipErrorCodes().get(0)
                            .replace("SDIP_", "")));
  }

  @Test
  void doConstructSparql_emptyEndpoint() {
    Throwable throwable = assertThrows(IncidentException.class, () -> {
      rdf4jClient.doConstructSparql(UnitTestConstants.SPARQLQUERY, null, parameterMemoryMock);
    });
    assertEquals(SdipErrorCode.SPARQL_ENDPOINT_EMPTY.getSdipErrorCode(),
            Integer
                    .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                            .getSdipErrorCodes().get(0)
                            .replace("SDIP_", "")));

  }

  @Test
  void doConstructSparql_invalidQuery() {
    when(parameterMemoryMock.getValue(SDOSConstants.OBO_TOKEN)).thenReturn(createMockBearerTokenParameterMap());
    Throwable throwable = assertThrows(IncidentException.class, () -> {
      rdf4jClient.doConstructSparql("invalid_query", "http://endpoint", parameterMemoryMock);
    });
    assertEquals(SdipErrorCode.DOMAIN_NOT_ANSWERING.getSdipErrorCode(),
            Integer
                    .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                            .getSdipErrorCodes().get(0)
                            .replace("SDIP_", "")));
  }


  @Test
  void doConstructSparql_repositoryError() {
    when(parameterMemoryMock.getValue(SDOSConstants.OBO_TOKEN)).thenReturn(createMockBearerTokenParameterMap());
    doReturn(sparqlRepoMock).when(rdf4jClient).getSparqlRepo(any());
    doThrow(RepositoryException.class).when(sparqlRepoMock).getConnection();

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      rdf4jClient.doConstructSparql("invalid_query", "endpoint", parameterMemoryMock);
    });
    assertEquals(SdipErrorCode.RDFSTORE_COMMUNICATION_ERROR.getSdipErrorCode(),
            Integer
                    .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                            .getSdipErrorCodes().get(0)
                            .replace("SDIP_", "")));
  }

  @Test
  void doConstructSparql_unknownError() {
    when(parameterMemoryMock.getValue(SDOSConstants.OBO_TOKEN)).thenReturn(createMockBearerTokenParameterMap());
    doReturn(sparqlRepoMock).when(rdf4jClient).getSparqlRepo(any());
    RepositoryConnection repo = Mockito.spy(RepositoryConnection.class);
    doReturn(null).when(sparqlRepoMock).getConnection();
    Throwable throwable = assertThrows(IncidentException.class, () -> {
      rdf4jClient.doConstructSparql(UnitTestConstants.SPARQLQUERY, "endpoint", parameterMemoryMock);
    });
    assertEquals(SdipErrorCode.UNKNOWN_REASON_ERROR.getSdipErrorCode(),
            Integer
                    .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                            .getSdipErrorCodes().get(0)
                            .replace("SDIP_", "")));
  }

  @Test
  void doConstructSparql_withJwtTokenExists() {
    String aQuery = "a query";
    String aGraph = "anotherGraph";
    HashMap<String, String> anEndpoint = new HashMap<>();
    anEndpoint.put(SDOSConstants.GRAPH, aGraph);
    String aJsonResult = "[{"
            + "\"this\":\"is\","
            + "\"a\":\"JSON\""
            + "}]";
    String aJsonString = "{\n"
            + "    \"results\": {\n"
            + "        \"bindings\":"
            + aJsonResult
            + "    }\n"
            + "}\n";
    RepositoryConnection aConn = mock(RepositoryConnection.class);
    GraphQuery aGraphQuery = mock(GraphQuery.class);
    StringWriter aStringWriter = mock(StringWriter.class);
    RDFWriter aWriter = mock(RDFWriter.class);
    WriterConfig aWriterConfig = mock(WriterConfig.class);
    doReturn(sparqlRepoMock).when(rdf4jClient).getSparqlRepo(any());
    doReturn(aConn).when(sparqlRepoMock).getConnection();
    doReturn(aGraphQuery).when(aConn).prepareGraphQuery(any(), any());
    doReturn(aStringWriter).when(rdf4jClient).getStringWriter();
    doReturn(aWriter).when(rdf4jClient).getRioWriter(any(), any());
    doReturn(aWriterConfig).when(aWriter).getWriterConfig();
    doReturn(aWriterConfig).when(aWriterConfig).set(any(), any());
    doNothing().when(aGraphQuery).evaluate(any());
    doReturn(aJsonResult).when(aStringWriter).toString();

    doNothing().when(sparqlRepoMock).setAdditionalHttpHeaders(anyMap());
    HashMap<String, List<String>> tokenMap = new HashMap<>();
    tokenMap.put(SDOSConstants.BEARER_TOKEN, Collections.singletonList(UnitTestConstants.DUMMY_JWT_TOKEN));
    tokenMap.put(SDOSConstants.VALUE, Collections.singletonList(UnitTestConstants.DUMMY_JWT_TOKEN));
    when(parameterMemoryMock.getValue(SDOSConstants.OBO_TOKEN)).thenReturn(tokenMap);

    Map<String, String> testHeader = new HashMap<>();
    testHeader.put("Authorization", "Bearer " + UnitTestConstants.DUMMY_JWT_TOKEN);

    String result = rdf4jClient.doConstructSparql(aQuery, "anEndpoint", parameterMemoryMock);

    verify(sparqlRepoMock).setAdditionalHttpHeaders(eq(testHeader));
  }

  @Test
  void executeSparql_emptyEndpoint() {
    Throwable throwable = assertThrows(IncidentException.class, () -> {
      rdf4jClient.executeSelectSparql(UnitTestConstants.SPARQLQUERY, null, parameterMemoryMock);
    });
    assertEquals(SdipErrorCode.SPARQL_ENDPOINT_EMPTY.getSdipErrorCode(),
            Integer
                    .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                            .getSdipErrorCodes().get(0)
                            .replace("SDIP_", "")));

  }

  @Test
  void executeSparql_invalidQuery() {
    String query = "DELETE\n"
            + " { ?book ?p ?v }\n"
            + "WHERE\n"
            + " { ?book dc:date ?date .\n"
            + "   FILTER ( ?date > \"1970-01-01T00:00:00-02:00\"^^xsd:dateTime )\n"
            + "   ?book ?p ?v\n"
            + " }";
    SPARQLRepository repository = mock(SPARQLRepository.class);
    RepositoryConnection conn = mock(RepositoryConnection.class);
    //TODO RDF¤J DOESNT THROW MALFORMED_QUERY EVEN IF QUERY IS NOT SELECT SPARQL...

    when(parameterMemoryMock.getValue(SDOSConstants.OBO_TOKEN)).thenReturn(createMockBearerTokenParameterMap());
    doReturn(repository).when(rdf4jClient).getSparqlRepo(any());
    doReturn(conn).when(repository).getConnection();
    doThrow(MalformedQueryException.class).when(conn).prepareTupleQuery(query);
    Throwable throwable = assertThrows(IncidentException.class, () -> {
      rdf4jClient.executeSelectSparql(query, "http://endpoint", parameterMemoryMock);
    });
    assertEquals(SdipErrorCode.MALFORMED_SPARQL_QUERY.getSdipErrorCode(),
            Integer
                    .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                            .getSdipErrorCodes().get(0)
                            .replace("SDIP_", "")));
  }

  @Test
  void executeSparql_repositoryError() {
    when(parameterMemoryMock.getValue(SDOSConstants.OBO_TOKEN)).thenReturn(createMockBearerTokenParameterMap());
    doReturn(sparqlRepoMock).when(rdf4jClient).getSparqlRepo(any());
    doThrow(RepositoryException.class).when(sparqlRepoMock).getConnection();

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      rdf4jClient.executeSelectSparql("invalid_query", "endpoint", parameterMemoryMock);
    });
    assertEquals(SdipErrorCode.RDFSTORE_COMMUNICATION_ERROR.getSdipErrorCode(),
            Integer
                    .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                            .getSdipErrorCodes().get(0)
                            .replace("SDIP_", "")));
  }

  @Test
  void executeSparql_unknownError() {
    when(parameterMemoryMock.getValue(SDOSConstants.OBO_TOKEN)).thenReturn(createMockBearerTokenParameterMap());
    doReturn(sparqlRepoMock).when(rdf4jClient).getSparqlRepo(any());
    RepositoryConnection repo = Mockito.spy(RepositoryConnection.class);
    doReturn(null).when(sparqlRepoMock).getConnection();
    Throwable throwable = assertThrows(IncidentException.class, () -> {
      rdf4jClient.executeSelectSparql(UnitTestConstants.SPARQLQUERY, "endpoint", parameterMemoryMock);
    });
    assertEquals(SdipErrorCode.UNKNOWN_REASON_ERROR.getSdipErrorCode(),
            Integer
                    .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                            .getSdipErrorCodes().get(0)
                            .replace("SDIP_", "")));
  }

  @Test
  void doConstructSparql_ok() throws Exception {
    String aQuery = "a query";
    String aGraph = "anotherGraph";
    HashMap<String, String> anEndpoint = new HashMap<>();
    anEndpoint.put(SDOSConstants.GRAPH, aGraph);
    String aJsonResult = "[{"
            + "\"this\":\"is\","
            + "\"a\":\"JSON\""
            + "}]";
    String aJsonString = "{\n"
            + "    \"results\": {\n"
            + "        \"bindings\":"
            + aJsonResult
            + "    }\n"
            + "}\n";
    RepositoryConnection aConn = mock(RepositoryConnection.class);
    GraphQuery aGraphQuery = mock(GraphQuery.class);
    StringWriter aStringWriter = mock(StringWriter.class);
    RDFWriter aWriter = mock(RDFWriter.class);
    WriterConfig aWriterConfig = mock(WriterConfig.class);
    when(parameterMemoryMock.getValue(SDOSConstants.OBO_TOKEN)).thenReturn(createMockBearerTokenParameterMap());
    doReturn(sparqlRepoMock).when(rdf4jClient).getSparqlRepo(any());
    doReturn(aConn).when(sparqlRepoMock).getConnection();
    doReturn(aGraphQuery).when(aConn).prepareGraphQuery(any(), any());
    doReturn(aStringWriter).when(rdf4jClient).getStringWriter();
    doReturn(aWriter).when(rdf4jClient).getRioWriter(any(), any());
    doReturn(aWriterConfig).when(aWriter).getWriterConfig();
    doReturn(aWriterConfig).when(aWriterConfig).set(any(), any());
    doNothing().when(aGraphQuery).evaluate(any());
    doReturn(aJsonResult).when(aStringWriter).toString();

    // run it!
    String result = rdf4jClient.doConstructSparql(aQuery, "anEndpoint", parameterMemoryMock);

    // check it!
    assertEquals(aJsonResult, result);
    verify(aConn, times(1))
            .prepareGraphQuery(QueryLanguage.SPARQL, aQuery);
    verify(rdf4jClient, times(1))
            .getRioWriter(RDFFormat.JSONLD, aStringWriter);
    verify(aGraphQuery, times(1)).evaluate(aWriter);
  }

  @Test
  void doConstructSparql_withReasoning_ok() throws Exception {
    String aQuery = "a query";
    String aGraph = "anotherGraph";
    HashMap<String, String> anEndpoint = new HashMap<>();
    anEndpoint.put(SDOSConstants.GRAPH, aGraph);
    String aJsonResult = "[{"
            + "\"this\":\"is\","
            + "\"a\":\"JSON\""
            + "}]";
    String aJsonString = "{\n"
            + "    \"results\": {\n"
            + "        \"bindings\":"
            + aJsonResult
            + "    }\n"
            + "}\n";
    Repository stardogRepository = mock(StardogRepository.class);
    ServiceArguments serviceArguments = mock(ServiceArguments.class);
    IParameterMemory daParams = mock(IParameterMemory.class);
    RepositoryConnection aConn = mock(StardogRepositoryConnection.class);
    Dataset dataset = mock(Dataset.class);
    GraphQuery aGraphQuery = mock(GraphQuery.class);
    StringWriter aStringWriter = mock(StringWriter.class);
    RDFWriter aWriter = mock(RDFWriter.class);
    WriterConfig aWriterConfig = mock(WriterConfig.class);
    doReturn(stardogRepository).when(rdf4jClient).getStardogRepository(any());
    doReturn(aConn).when(stardogRepository).getConnection();
    doReturn(dataset).when(rdf4jClient).getDataSet(any());
    doReturn(aGraphQuery).when(aConn).prepareGraphQuery(QueryLanguage.SPARQL, "aQuery");
    doReturn(aStringWriter).when(rdf4jClient).getStringWriter();
    doReturn(aWriter).when(rdf4jClient).getRioWriter(any(), any());
    doReturn(aWriterConfig).when(aWriter).getWriterConfig();
    doReturn(aWriterConfig).when(aWriterConfig).set(any(), any());
    doNothing().when(aGraphQuery).evaluate(any());
    doReturn(aJsonResult).when(aStringWriter).toString();
    HashMap map = new HashMap();
    map.put("Id", Collections.singletonList("test"));
    doReturn(map).when(daParams).getValue(any());

    // run it!
    String result = rdf4jClient
            .doConstructSparql("aQuery", serviceArguments, daParams, true);

    // check it!
//    assertEquals(aJsonResult, result);
    verify(aConn, times(1))
            .prepareGraphQuery(any(), any());
    verify(rdf4jClient, times(1))
            .getRioWriter(RDFFormat.JSONLD, aStringWriter);
    verify(aGraphQuery, times(1)).evaluate(aWriter);
    verify(rdf4jClient, times(1))
            .getDataSet(any());

  }

  @Test
  void executeSelectSparql_ok() {
    String aQuery = "a query";
    String aGraph = "anotherGraph";
    HashMap<String, String> anEndpoint = new HashMap<>();
    anEndpoint.put(SDOSConstants.GRAPH, aGraph);
    anEndpoint.put(UnitTestConstants.ENDPOINT, "");
    String aJsonResult = "[{"
            + "\"this\":\"is\","
            + "\"a\":\"JSON\""
            + "}]";
    String aJsonString = "{\n"
            + "    \"results\": {\n"
            + "        \"bindings\":"
            + aJsonResult
            + "    }\n"
            + "}\n";
    RepositoryConnection aConn = mock(RepositoryConnection.class);
    TupleQuery aTupleQuery = mock(TupleQuery.class);
    ByteArrayOutputStream anArrayOutputStream = mock(ByteArrayOutputStream.class);
    when(parameterMemoryMock.getValue(SDOSConstants.OBO_TOKEN)).thenReturn(createMockBearerTokenParameterMap());
    doReturn(sparqlRepoMock).when(rdf4jClient).getSparqlRepo(any());
    doReturn(aConn).when(sparqlRepoMock).getConnection();
    doReturn(aTupleQuery).when(aConn).prepareTupleQuery(any());
    doNothing().when(aTupleQuery).evaluate(any());
    doReturn(anArrayOutputStream).when(rdf4jClient).getByteArrayOutputStream();
    doReturn(aJsonString).when(anArrayOutputStream).toString();

    // run it!
    JsonArray result = rdf4jClient.executeSelectSparql(aQuery, "anEndpoint", parameterMemoryMock);

    // check it!
    assertEquals(aJsonResult, result.toString());
    verify(aConn, times(1))
            .prepareTupleQuery(aQuery);
  }

  @Test
  void test_getDataSet() {
    String graphName = "http://testGraph";
    Dataset dataset = rdf4jClient.getDataSet(graphName);
    assertEquals(graphName, ((Set) dataset.getNamedGraphs()).iterator().next().toString());
  }

  @Test
  void executeSelectSparql_withReasoning_ok() {
    String aQuery = "a query";
    String aGraph = "anotherGraph";
    HashMap<String, String> anEndpoint = new HashMap<>();
    anEndpoint.put(SDOSConstants.GRAPH, aGraph);
    anEndpoint.put(UnitTestConstants.ENDPOINT, "");
    String aJsonResult = "[{"
            + "\"this\":\"is\","
            + "\"a\":\"JSON\""
            + "}]";
    String aJsonString = "{\n"
            + "    \"results\": {\n"
            + "        \"bindings\":"
            + aJsonResult
            + "    }\n"
            + "}\n";

    Repository stardogRepository = mock(StardogRepository.class);
    ServiceArguments serviceArguments = mock(ServiceArguments.class);
    IParameterMemory daParams = mock(IParameterMemory.class);
    Dataset dataset = mock(Dataset.class);
    RepositoryConnection aConn = mock(StardogRepositoryConnection.class);
    TupleQuery aTupleQuery = mock(TupleQuery.class);
    ByteArrayOutputStream anArrayOutputStream = mock(ByteArrayOutputStream.class);
    doReturn(stardogRepository).when(rdf4jClient).getStardogRepository(any());
    doReturn(dataset).when(rdf4jClient).getDataSet(any());
    doReturn(aConn).when(stardogRepository).getConnection();
    doReturn(aTupleQuery).when(aConn).prepareTupleQuery(any());
    doNothing().when(aTupleQuery).evaluate(any());
    doReturn(anArrayOutputStream).when(rdf4jClient).getByteArrayOutputStream();
    doReturn(aJsonString).when(anArrayOutputStream).toString();
    HashMap map = new HashMap();
    map.put("Id", Collections.singletonList("test"));
    doReturn(map).when(daParams).getValue(any());

    // run it!
    JsonArray result = rdf4jClient
            .doSelectSparql(aQuery, serviceArguments, daParams, true);

    // check it!
    assertEquals(aJsonResult, result.toString());
    verify(aConn, times(1))
            .prepareTupleQuery(aQuery);
    verify(rdf4jClient, times(1))
            .getDataSet("test");
  }

  @Test
  void saveJsonLdToGraph_ok() {
    ModelBuilder modelBuilderMock = mock(ModelBuilder.class);
    Model modelMock = mock(Model.class);

    String graphName = "http://aGraph";
    JsonArray jsonArray_val = new Gson().fromJson(
            " [\n"
                    + "    {\n"
                    + "      \"@id\": \"http://example.org/library\",\n"
                    + "      \"@type\": \"ex:Library\",\n"
                    + "      \"ex:contains\": \"http://example.org/library/the-republic\"\n"
                    + "    },\n"
                    + "    {\n"
                    + "      \"@id\": \"http://example.org/library/the-republic\",\n"
                    + "      \"@type\": \"ex:Book\",\n"
                    + "      \"dc11:creator\": \"Plato\",\n"
                    + "      \"dc11:title\": \"The Republic\",\n"
                    + "      \"ex:contains\": \"http://example.org/library/the-republic#introduction\"\n"
                    + "    },\n"
                    + "    {\n"
                    + "      \"@id\": \"http://example.org/library/the-republic#introduction\",\n"
                    + "      \"@type\": \"ex:Chapter\",\n"
                    + "      \"dc11:description\": \"An introductory chapter on The Republic.\",\n"
                    + "      \"dc11:title\": \"The Introduction\"\n"
                    + "    }\n"
                    + "  ]",
            JsonArray.class);
    JsonObject tmpGraph = new JsonObject();
    String aContext = " {\n"
            + "    \"dc11\": \"http://purl.org/dc/elements/1.1/\",\n"
            + "    \"ex\": \"http://example.org/vocab#\",\n"
            + "    \"xsd\": \"http://www.w3.org/2001/XMLSchema#\",\n"
            + "    \"ex:contains\": {\n"
            + "      \"@type\": \"@id\"\n"
            + "    }\n"
            + "  }";
    String anEndpoint = "http://anEndPoint";
    tmpGraph.add("@context", new Gson().fromJson(aContext, JsonObject.class));
    tmpGraph.add("@graph", jsonArray_val);
    doReturn(modelBuilderMock).when(rdf4jClient).getModelBuilder();
    doReturn(modelBuilderMock).when(modelBuilderMock).namedGraph((String) any());
    doReturn(modelBuilderMock).when(modelBuilderMock)
            .add((Resource) any(), (IRI) any(), (Object) any());
    doReturn(modelMock).when(modelBuilderMock).build();

    doNothing().when(rdf4jClient).executeAddModel(any(), any(), any());

    // Run it!
    rdf4jClient.saveJsonLdToGraph(tmpGraph, graphName, anEndpoint, parameterMemoryMock);

    // Check it!
    verify(rdf4jClient, times(1)).executeAddModel(modelMock, anEndpoint, parameterMemoryMock);

    verify(modelBuilderMock, times(1))
            .add(iri("http://example.org/library/the-republic#introduction"),
                    iri("http://purl.org/dc/elements/1.1/description"),
                    literal("An introductory chapter on The Republic."));
    verify(modelBuilderMock, times(1))
            .add(iri("http://example.org/library/the-republic#introduction"),
                    iri("http://purl.org/dc/elements/1.1/title"),
                    literal("The Introduction"));
    verify(modelBuilderMock, times(1))
            .add(iri("http://example.org/library/the-republic#introduction"),
                    iri("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
                    iri("http://example.org/vocab#Chapter"));
    verify(modelBuilderMock, times(1))
            .add(iri("http://example.org/library/the-republic"),
                    iri("http://example.org/vocab#contains"),
                    iri("http://example.org/library/the-republic#introduction"));
    verify(modelBuilderMock, times(1))
            .add(iri("http://example.org/library/the-republic"),
                    iri("http://purl.org/dc/elements/1.1/creator"),
                    literal("Plato"));
    verify(modelBuilderMock, times(1))
            .add(iri("http://example.org/library/the-republic"),
                    iri("http://purl.org/dc/elements/1.1/title"), literal("The Republic"));
    verify(modelBuilderMock, times(1))
            .add(iri("http://example.org/library/the-republic"),
                    iri("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
                    iri("http://example.org/vocab#Book"));
    verify(modelBuilderMock, times(1))
            .add(iri("http://example.org/library"),
                    iri("http://example.org/vocab#contains"),
                    iri("http://example.org/library/the-republic"));
    verify(modelBuilderMock, times(1))
            .add(iri("http://example.org/library"),
                    iri("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
                    iri("http://example.org/vocab#Library"));
    verify(modelBuilderMock, times(10)).namedGraph(graphName);
  }

  @Test
  void executeAddModel_ok() {
    RepositoryConnection aConn = mock(RepositoryConnection.class);
    Model modelMock = mock(Model.class);

    when(parameterMemoryMock.getValue(SDOSConstants.OBO_TOKEN)).thenReturn(createMockBearerTokenParameterMap());
    doReturn(sparqlRepoMock).when(rdf4jClient).getSparqlRepo(any());
    doReturn(aConn).when(sparqlRepoMock).getConnection();
    doNothing().when(aConn).add((Model) any());
    doNothing().when(sparqlRepoMock).shutDown();

    // Run it!
    rdf4jClient.executeAddModel(modelMock, UnitTestConstants.ENDPOINT, parameterMemoryMock);
    // Check it!
    verify(sparqlRepoMock, times(1)).getConnection();
    verify(aConn, times(1)).add(modelMock);
    verify(sparqlRepoMock, times(1)).shutDown();
  }

  @Test
  void executeAddModel_throwsRdfStoreCommunicationError() {
    RepositoryConnection aConn = mock(RepositoryConnection.class);
    Model modelMock = mock(Model.class);

    when(parameterMemoryMock.getValue(SDOSConstants.OBO_TOKEN)).thenReturn(createMockBearerTokenParameterMap());
    doReturn(sparqlRepoMock).when(rdf4jClient).getSparqlRepo(any());
    doReturn(aConn).when(sparqlRepoMock).getConnection();
    doThrow(RepositoryException.class).when(aConn).add((Model) any());
    doNothing().when(sparqlRepoMock).shutDown();

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      rdf4jClient.executeAddModel(modelMock, UnitTestConstants.ENDPOINT, parameterMemoryMock);
    });
    assertEquals(SdipErrorCode.RDFSTORE_COMMUNICATION_ERROR.getSdipErrorCode(),
            Integer
                    .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                            .getSdipErrorCodes().get(0)
                            .replace("SDIP_", "")));
  }

  @Test
  void isSparql_ok() {
    ParsedQuery result = rdf4jClient.isSPARQL("SELECT * WHERE {?s ?p ?o}");
    assertNotNull(result);
  }

  @Test
  void isSparql_nok() {
    ParsedQuery result = rdf4jClient.isSPARQL("not a query");
    assertNull(result);
  }

  private HashMap<String, List<String>> createMockBearerTokenParameterMap() {
    HashMap<String, List<String>> parameterMap = new HashMap<>();
    parameterMap.put(SDOSConstants.BEARER_TOKEN, Collections.singletonList(UnitTestConstants.DUMMY_JWT_TOKEN));
    parameterMap.put(SDOSConstants.VALUE, Collections.singletonList(UnitTestConstants.DUMMY_JWT_TOKEN));
    return parameterMap;
  }

}
