package com.scania.sdos.orchestration;

import static org.eclipse.rdf4j.model.util.Values.iri;
import com.complexible.stardog.api.ConnectionConfiguration;
import com.complexible.stardog.rdf4j.StardogRepository;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.scania.sdip.exceptions.IncidentException;
import com.scania.sdip.exceptions.SdipErrorCode;
import com.scania.sdip.exceptions.SdipErrorParameter;
import com.scania.sdos.jwt.JwtTokenUtil;
import com.scania.sdos.model.ServiceArguments;
import com.scania.sdos.orchestration.interfaces.IParameterMemory;
import com.scania.sdos.utils.SDOSConstants;
import com.scania.sdos.utils.Utility;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.query.Dataset;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.query.UpdateExecutionException;
import org.eclipse.rdf4j.query.impl.SimpleDataset;
import org.eclipse.rdf4j.query.parser.ParsedQuery;
import org.eclipse.rdf4j.query.parser.QueryParserUtil;
import org.eclipse.rdf4j.query.resultio.sparqljson.SPARQLResultsJSONWriter;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.JSONLDMode;
import org.eclipse.rdf4j.rio.helpers.JSONLDSettings;

/**
 * This class hold RDF4j client functions
 */
public class Rdf4jClient {

  private static final Logger LOGGER = LogManager.getLogger(Rdf4jClient.class);
  private static final Gson gson = new Gson();
  /*TODO refactor this class as a spring component and all ocurrensences of this class uses should be refactored.
   * since this class is not a spring component yet, we are using credentials a constants.
   * once it is refactored as a spring component,
   * then we must get the credentials from application.properties or use planned future jwt functionality */
  private static String STARDOG_USER = "username";

  private static String STARDOG_PASS = "password";

  private static String aws_session_token = "gfhvbdfdter547uthgy";

  private static String aws_temporary_access_key_id = "12345";

  private static String azure_function_key = "azure_function_key";

  private static String baiducloud_api_accesskey = "baiducloud_api_accesskey";

  private static String cratesio_api_token = "cratesio_api_token";



  public Rdf4jClient() {
    //default  constructor
  }

  /**
   * function to do sparql SELECT on in-memory Repo containing the OFG
   *
   * @param query          SELECT sparql
   * @param ofgModelRepo  current ofgmodel
   * @return a JsonArray of the query results
   */
  public JsonArray selectSparqlOfg(String query, OfgModelRepo ofgModelRepo) {
    return executeSelectSparqlQuery(ofgModelRepo.getRepository(), query, "in-memory", null);
  }

  /**
   * function to do sparql SELECT
   *
   * @param query    SELECT sparql
   * @param endPoint sparql endPoint
   * @return a JsonArray of the query results
   */
  public JsonArray selectSparqlOrchestration(String query, String endPoint, IParameterMemory iParameterMemory) {
    return executeSelectSparql(query, endPoint, iParameterMemory);
  }

  /**
   * function to do sparql CONSTRUCT
   *
   * @param query    CONSTRUCT sparql
   * @param endPoint sparql endPoint
   * @return resulting String of a GRAPH in JSON-LD format
   */
  public String doConstructSparql(String query, String endPoint, IParameterMemory iParameterMemory) {
    SPARQLRepository repository = getSparqlRepo(endPoint);
    configureAuthOnRepo(repository, iParameterMemory);

    return executeConstructSparql(repository, query, endPoint, Utility.getStaticString());
  }

  public String doConstructSparql(String query, ServiceArguments serviceArguments,
                                  IParameterMemory iParameterMemory, boolean enableReasoner) {
    if (enableReasoner) {
      Repository repository = getStardogRepository(serviceArguments);
      String graphName = iParameterMemory.getValue(SDOSConstants.EXECUTION_REPORT)
              .get(SDOSConstants.ID).get(0);
      return executeConstructSparql(repository, query, serviceArguments.getStardogQueryEndpoint(),
              graphName);
    } else {
      return doConstructSparql(query, serviceArguments.getStardogResultEndpoint(), iParameterMemory);
    }
  }

  public String executeConstructSparql(Repository repository, String query, String endPoint,
                                       String graphName) {
    try (RepositoryConnection conn = repository.getConnection()) {
      GraphQuery graphQuery = conn.prepareGraphQuery(QueryLanguage.SPARQL, query);
      graphQuery.setMaxExecutionTime(0);
      if (repository instanceof StardogRepository) {
        graphQuery.setDataset(getDataSet(graphName));
      }
      StringWriter sw = getStringWriter();
      RDFWriter writer = getRioWriter(RDFFormat.JSONLD, sw);
      writer.getWriterConfig().set(JSONLDSettings.JSONLD_MODE, JSONLDMode.COMPACT);
      writer.getWriterConfig().set(JSONLDSettings.OPTIMIZE, true);
      writer.getWriterConfig().set(JSONLDSettings.COMPACT_ARRAYS, true);
      graphQuery.evaluate(writer);
      return sw.toString();
    } catch (QueryEvaluationException e) {
      throw new IncidentException(e, SdipErrorCode.DOMAIN_NOT_ANSWERING, LOGGER, e.getMessage());
    } catch (RepositoryException e) {
      throw new IncidentException(e, SdipErrorCode.RDFSTORE_COMMUNICATION_ERROR, LOGGER,
              e.getMessage(), endPoint,
              SdipErrorParameter.SUPPORTMAIL);
    } catch (Exception e) {
      throw new IncidentException(e, SdipErrorCode.UNKNOWN_REASON_ERROR, LOGGER, e.getMessage(),
              SdipErrorParameter.SUPPORTMAIL);

    } finally {
      repository.shutDown();
    }
  }

  /**
   * function to do sparql select
   *
   * @param query    SELECT sparql
   * @param endPoint sparql endPoint
   * @return a JsonArray of the query results
   */
  public JsonArray doSelectSparql(String query, String endPoint, IParameterMemory iParameterMemory) {
    return executeSelectSparql(query, endPoint, iParameterMemory);
  }

  public JsonArray doSelectSparql(String query,
                                  ServiceArguments serviceArguments, IParameterMemory iParameterMemory,
                                  boolean enableReasoner) {
    if (enableReasoner) {
      Repository repository = getStardogRepository(serviceArguments);
      String graphName = iParameterMemory.getValue(SDOSConstants.EXECUTION_REPORT)
              .get(SDOSConstants.ID).get(0);
      return executeSelectSparqlQuery(repository, query, serviceArguments.getStardogResultEndpoint(),
              graphName);
    } else {
      return doSelectSparql(query, serviceArguments.getStardogResultEndpoint(), iParameterMemory);
    }

  }

  /**
   * function to execute sparql SELECT towards a sparql endPoint
   *
   * @param query    SELECT sparql
   * @param endPoint sparql endPoint
   * @return a JsonArray of the query results
   */
  public JsonArray executeSelectSparql(String query, String endPoint, IParameterMemory iParameterMemory) {
    SPARQLRepository repository = getSparqlRepo(endPoint);
    configureAuthOnRepo(repository, iParameterMemory);
    return executeSelectSparqlQuery(repository, query, endPoint, Utility.getStaticString());
  }

  public JsonArray executeSelectSparqlQuery(Repository repository, String query, String endPoint,
                                            String graphName) {

    try (RepositoryConnection conn = repository.getConnection()) {
      TupleQuery tupleQuery = conn.prepareTupleQuery(query);//TODO DOESNT THROW MALFORMED QUERY
      tupleQuery.setMaxExecutionTime(0);
      if (repository instanceof StardogRepository) {
        tupleQuery.setDataset(getDataSet(graphName));
      }
      OutputStream output = getByteArrayOutputStream();
      tupleQuery.evaluate(getSparqlResultWriter(output));
      JsonObject json = gson.fromJson(output.toString(), JsonObject.class);
      JsonArray jsonArray = json.get(SDOSConstants.RESULTS).getAsJsonObject()
              .get(SDOSConstants.BINDINGS).getAsJsonArray();

      return jsonArray;
    } catch (MalformedQueryException exception) {
      throw new IncidentException(exception, Utility.getErrorMessage(exception, "",
              ""), SdipErrorCode.MALFORMED_SPARQL_QUERY, LOGGER, exception.getMessage());
    } catch (QueryEvaluationException exception) {
      throw new IncidentException(exception, Utility.getErrorMessage(exception, "",
              ""), SdipErrorCode.DOMAIN_NOT_ANSWERING, LOGGER, exception.getMessage());
    } catch (RepositoryException exception) {
      throw new IncidentException(exception, Utility.getErrorMessage(exception, "",
              ""), SdipErrorCode.RDFSTORE_COMMUNICATION_ERROR, LOGGER,
              exception.getMessage(), endPoint, SdipErrorParameter.SUPPORTMAIL);
    } catch (Exception exception) {
      throw new IncidentException(exception, Utility.getErrorMessage(exception, "",
              ""), SdipErrorCode.UNKNOWN_REASON_ERROR, LOGGER, exception.getMessage(),
              SdipErrorParameter.SUPPORTMAIL);

    } finally {
      // don't call shutdown on in-memory repo
      if (!(repository instanceof SailRepository)) {
        repository.shutDown();
      }
    }
  }

  public Dataset getDataSet(String graphName) {
    Dataset dataset = new SimpleDataset();
    ((SimpleDataset) dataset).addNamedGraph(iri(graphName));
    return dataset;
  }

  public void executeUpdateSparql(String query, String endPoint, IParameterMemory iParameterMemory) {
    SPARQLRepository repository = getSparqlRepo(endPoint);
    configureAuthOnRepo(repository, iParameterMemory);

    try (RepositoryConnection conn = repository.getConnection()) {
      Update prepareUpdate = conn.prepareUpdate(query);
      prepareUpdate.setMaxExecutionTime(0);
      prepareUpdate.execute();
    } catch (MalformedQueryException e) {
      throw new IncidentException(e, SdipErrorCode.MALFORMED_SPARQL_QUERY, LOGGER, e.getMessage());
    } catch (UpdateExecutionException e) {
      throw new IncidentException(e, SdipErrorCode.DOMAIN_NOT_ANSWERING, LOGGER, e.getMessage());
    } catch (RepositoryException e) {
      throw new IncidentException(e, SdipErrorCode.RDFSTORE_COMMUNICATION_ERROR, LOGGER,
              e.getMessage(), endPoint,
              SdipErrorParameter.SUPPORTMAIL);
    } catch (Exception e) {
      throw new IncidentException(e, SdipErrorCode.UNKNOWN_REASON_ERROR, LOGGER, e.getMessage(),
              SdipErrorParameter.SUPPORTMAIL);

    } finally {
      repository.shutDown();
    }
  }

  public SPARQLRepository getSparqlRepo(String repo) {

    try {
      return new SPARQLRepository(repo);
    } catch (IllegalArgumentException e) {
      throw new IncidentException(e, SdipErrorCode.SPARQL_ENDPOINT_EMPTY, LOGGER, e.getMessage());
    }
  }

  public StardogRepository getStardogRepository(ServiceArguments serviceArguments) {
    return new StardogRepository(ConnectionConfiguration
            .to(serviceArguments.getResultDB())
            .reasoning(true)
            .server(serviceArguments.getStardogBaseUrl().toString())
            .credentials(STARDOG_USER, STARDOG_PASS));
  }

  public SPARQLResultsJSONWriter getSparqlResultWriter(OutputStream outputStream) {
    return new SPARQLResultsJSONWriter(outputStream);
  }

  public ByteArrayOutputStream getByteArrayOutputStream() {
    return new ByteArrayOutputStream();
  }

  public StringWriter getStringWriter() {
    return new StringWriter();
  }

  public RDFWriter getRioWriter(RDFFormat format, Writer sw) {
    return Rio.createWriter(format, sw);
  }

  /**
   * Save a JSON-LD to triple store in the supplied named graph
   *
   * @param jsonld    Graph in JSON-LD to save
   * @param graphName Name of the named graph
   * @param endPoint  UPDATE endpoint of the triple store
   */
  public void saveJsonLdToGraph(JsonObject jsonld, String graphName, String endPoint, IParameterMemory iParameterMemory) {
    ModelBuilder builder = getModelBuilder();
    try {
      InputStream is = IOUtils.toInputStream(jsonld.toString(), "UTF-8");
      Model tmpModel = Rio.parse(is, RDFFormat.JSONLD);

      for (Statement statement : tmpModel) {
        builder.namedGraph(graphName).add(
                statement.getSubject(), statement.getPredicate(), statement.getObject()
        );
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    Model model = builder.namedGraph(graphName).build();
    executeAddModel(model, endPoint, iParameterMemory);
  }

  public ModelBuilder getModelBuilder() {
    return new ModelBuilder();
  }

  /**
   * Add model to triple store
   *
   * @param modelToAdd model containing statements to add
   * @param endPoint   UPDATE endpoint of triple store
   */
  public void executeAddModel(Model modelToAdd, String endPoint, IParameterMemory iParameterMemory) {
    SPARQLRepository repository = getSparqlRepo(endPoint);
    configureAuthOnRepo(repository, iParameterMemory);
    try (RepositoryConnection conn = repository.getConnection()) {
      conn.add(modelToAdd);
    } catch (RepositoryException e) {
      throw new IncidentException(e, SdipErrorCode.RDFSTORE_COMMUNICATION_ERROR, LOGGER,
              e.getMessage(), endPoint,
              SdipErrorParameter.SUPPORTMAIL);
    } finally {
      repository.shutDown();
    }
  }

  /**
   * Validate query is a valid SPARQL query
   *
   * @param queryString query to validate
   * @return ParsedQuery representation of queryString if query is valid, else null
   */
  public static ParsedQuery isSPARQL(String queryString) {

    ParsedQuery query = null;
    try {
      query = QueryParserUtil.parseQuery(QueryLanguage.SPARQL, queryString, null);
    } catch (MalformedQueryException queryException) {
      LOGGER.error("Invalid SPARQL : {} ", queryException.getMessage());
    }
    return query;
  }

  private void configureAuthOnRepo(SPARQLRepository repository, IParameterMemory iParameterMemory) {
    if (JwtTokenUtil.oboTokenExists(iParameterMemory)) {
      Map<String, String> header = new HashMap<>();
      header.put(SDOSConstants.AUTHORIZATION, SDOSConstants.BEARER + " " + JwtTokenUtil.getOboToken(iParameterMemory));
      repository
              .setAdditionalHttpHeaders(header);
    } else {
      throw new IncidentException(SdipErrorCode.MISSING_OBO_TOKEN_ERROR, LOGGER);
    }
  }

}
