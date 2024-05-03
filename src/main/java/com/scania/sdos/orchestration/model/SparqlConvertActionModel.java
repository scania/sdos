package com.scania.sdos.orchestration.model;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.scania.sdip.exceptions.IncidentException;
import com.scania.sdip.exceptions.SdipErrorCode;
import com.scania.sdip.exceptions.SdipErrorParameter;
import com.scania.sdos.model.ServiceArguments;
import com.scania.sdos.orchestration.Rdf4jClient;
import com.scania.sdos.orchestration.interfaces.IActionModel;
import com.scania.sdos.orchestration.interfaces.IConnectorModel;
import com.scania.sdos.orchestration.interfaces.IParameterMemory;
import com.scania.sdos.orchestration.interfaces.IParameterModel;
import com.scania.sdos.utils.SDOSConstants;
import com.scania.sdos.utils.Utility;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.rdf4j.query.parser.ParsedGraphQuery;
import org.eclipse.rdf4j.query.parser.ParsedQuery;

public class SparqlConvertActionModel extends ActionModel implements IActionModel {

  private static final Logger LOGGER = LogManager.getLogger(SparqlConvertActionModel.class);

  public static final String SPARQL = SDOSConstants.PREFIX_RDF
      + SDOSConstants.PREFIX_RDFS
      + "PREFIX : <" + SDOSConstants.ORCHESTRATION_PREFIX + "> \n"
      + "SELECT ?" + SDOSConstants.HAS_CONNECTOR
      + " ?" + SDOSConstants.CONSTRUCT_SPARQL
      + " ?" + SDOSConstants.NEXT_ACTION
      + " ?" + SDOSConstants.NEXT_ACTION_TYPE
      + " ?" + SDOSConstants.INPUT_PARAMETER
      + " ?" + SDOSConstants.OUTPUT_PARAMETER
      + " ?" + SDOSConstants.INPUT_PARAMETER_TYPE
      + " ?" + SDOSConstants.OUTPUT_PARAMETER_TYPE
      + " ?" + SDOSConstants.LABEL + "\n "
      + "WHERE\n"
      + "{ \n"
      + "BIND(<" + SDOSConstants.VARIABLE + "> AS ?subject)"
      + "?subject rdf:type :SparqlConvertAction ;\n"
      + ":hasConnector ?" + SDOSConstants.HAS_CONNECTOR + ";\n"
      + ":inputParameter ?" + SDOSConstants.INPUT_PARAMETER + ";\n"
      + ":outputParameter ?" + SDOSConstants.OUTPUT_PARAMETER + ";\n"
      + ":constructSparql ?" + SDOSConstants.CONSTRUCT_SPARQL + ";\n"
      + "rdfs:label ?" + SDOSConstants.LABEL + ".\n"
      + "OPTIONAL{?subject :hasNextAction ?" + SDOSConstants.NEXT_ACTION + " ."
      + "{?" + SDOSConstants.NEXT_ACTION + " rdf:type :Action .} \n"
      + SDOSConstants.UNION + SDOSConstants.NEXT_ACTION + "  rdf:type :HTTPAction .\n"
      + "    BIND(:HTTPAction AS ?" + SDOSConstants.NEXT_ACTION_TYPE + " )} \n"
      + SDOSConstants.UNION + SDOSConstants.NEXT_ACTION + " rdf:type :SOAPAction .\n"
      + "    BIND(:SOAPAction AS ?" + SDOSConstants.NEXT_ACTION_TYPE + SDOSConstants.CLOSING
      + SDOSConstants.UNION + SDOSConstants.NEXT_ACTION + " rdf:type :ScriptAction .\n"
      + "    BIND(:ScriptAction AS ?" + SDOSConstants.NEXT_ACTION_TYPE + SDOSConstants.CLOSING
      + SDOSConstants.UNION + SDOSConstants.NEXT_ACTION + " rdf:type :ResultAction . \n"
      + "    BIND(:ResultAction AS ?" + SDOSConstants.NEXT_ACTION_TYPE + SDOSConstants.CLOSING
      + SDOSConstants.UNION + SDOSConstants.NEXT_ACTION + " rdf:type :VirtualGraphAction . \n"
      + "    BIND(:VirtualGraphAction AS ?" + SDOSConstants.NEXT_ACTION_TYPE + ")} \n"
      + SDOSConstants.UNION + SDOSConstants.NEXT_ACTION + " rdf:type :SparqlConvertAction . \n"
      + "    BIND(:SparqlConvertAction AS ?" + SDOSConstants.NEXT_ACTION_TYPE + ")} \n"
      + "{?" + SDOSConstants.INPUT_PARAMETER + " rdf:type :StandardParameter .\n"
      + "  BIND(:StandardParameter AS ?" + SDOSConstants.INPUT_PARAMETER_TYPE + ")}\n"
      + "{?" + SDOSConstants.OUTPUT_PARAMETER + " rdf:type :HTTPParameter .\n"
      + "  BIND(:HTTPParameter AS ?" + SDOSConstants.OUTPUT_PARAMETER_TYPE + ")}\n"
      + "}\n"
      + "}";

  private String sparqlConvert;
  private HttpConnectorModel hasConnector;
  private Rdf4jClient rdf4jClient;
  private String tmpGraphName;

  public SparqlConvertActionModel() {
    this.setRdf4jClient(new Rdf4jClient());
  }

  public String generateNewTmpGraphName() {
    tmpGraphName = "http://convertTmpGraph_" + Utility.getUUID4String();
    return tmpGraphName;
  }

  public String getSparqlConvert() {
    return sparqlConvert;
  }

  public void setSparqlConvert(String sparqlConvert) {
    this.sparqlConvert = sparqlConvert;
  }

  public void setHasConnector(HttpConnectorModel hasConnector) {
    this.hasConnector = hasConnector;
  }

  public IConnectorModel getHasConnector() {
    return hasConnector;
  }

  public String getTmpGraphName() {
    return tmpGraphName;
  }

  public void setRdf4jClient(Rdf4jClient rdf4jClient) {
    this.rdf4jClient = rdf4jClient;
  }

  @Override
  public void run(IParameterMemory iParameterMemory, JsonLdContextModel context,
                  ServiceArguments serviceArguments) {
    try {
      JsonObject tmpGraph = new JsonObject();
      for (IParameterModel inputParameterModel : inputParameter) {
        HashMap<String, List<String>> inputParamVal = iParameterMemory
            .getValue(inputParameterModel.getSubjectIri());

        // For value in inputparameter
        if (inputParamVal != null && !inputParamVal.isEmpty()) {
          Integer iterations = Utility.getActionIteration(inputParamVal, inputParameterModel);
          for (int i = 0; i < iterations; i++) {
            // - create tmpGraph from JSON-LD using context
            // TODO: populate context from hasContext and use it!!!!
            JsonArray jsonArray_val = new Gson().fromJson(
                inputParamVal.get(((StandardParameterModel) inputParameterModel).getParamName()).get(i),
                JsonArray.class);
            tmpGraph.add("@context", new Gson().fromJson(context.getContext(), JsonObject.class));
            tmpGraph.add("@graph", jsonArray_val);
          }

        }

        // -  save in tmpGraphName
        rdf4jClient.saveJsonLdToGraph(tmpGraph, getTmpGraphName(),
            serviceArguments.getStardogResultUpdateEndpoint(),
                iParameterMemory);
        // -  run construct query
        String result = runConstructQueryOnTmpGraph(sparqlConvert,
            serviceArguments.getStardogResultQueryEndpoint(),
                iParameterMemory);

        // -  save in outputParameter
        HashMap<String, List<String>> outputMemoryValue = new HashMap<>();
        outputMemoryValue
            .put(outputParameter.get(0).getKeys().get(0), Collections.singletonList(result));
        iParameterMemory.putParameter(outputParameter.get(0).getSubjectIri(), outputMemoryValue);

        // - remove tmp-graph tmpGraphName
        rdf4jClient.executeUpdateSparql("CLEAR GRAPH <" + getTmpGraphName() + ">",
            serviceArguments.getStardogResultUpdateEndpoint(), iParameterMemory);
      }
    } catch (JsonSyntaxException exception) {
      throw new IncidentException(SdipErrorCode.INVALID_JSONARRAY_RESPONSE, LOGGER,
          exception.getMessage());
    } catch (IncidentException exception) {
      throw exception;
    } catch (Exception exception) {
      throw new IncidentException(exception, SdipErrorCode.UNKNOWN_REASON_ERROR, LOGGER,
          exception.getMessage(), SdipErrorParameter.SUPPORTMAIL);
    }
  }

  /**
   * Execute the supplied CONSTRUCT on the temporary graph
   *
   * @param query    supplied CONSTRUCT query
   * @param endPoint SPARQL QUERY endpoint
   * @return Resulting GRAPH in JSON-LD format
   */
  private String runConstructQueryOnTmpGraph(String query, String endPoint, IParameterMemory iParameterMemory) {
    ParsedQuery parsedQuery1 = Rdf4jClient.isSPARQL(query);
    if (parsedQuery1 instanceof ParsedGraphQuery) {
      LOGGER.debug("User executing Construct SPARQL {} ", query);
    } else {
      throw new IncidentException(SdipErrorCode.INVALID_SPARQL_QUERY, LOGGER,
          query, "invalid", SdipErrorParameter.SUPPORTMAIL);
    }
    // Add graph to query

    // Could this be done differently using RDF4J???
    // replace where statement with where-graph
    String queryWithGraph = query.replaceAll("[wW][hH][eE][rR][eE][ \n]*\\{",
        "WHERE { GRAPH <" + getTmpGraphName() + "> {");
    // close added statement
    queryWithGraph += "}";
    ParsedQuery parsedQuery = Rdf4jClient.isSPARQL(queryWithGraph);
    if (parsedQuery instanceof ParsedGraphQuery) {
      LOGGER.debug("Construct SPARQL on tmp-graph: {} ", queryWithGraph);
    } else {
      throw new IncidentException(SdipErrorCode.INVALID_SPARQL_QUERY, LOGGER,
          query, "invalid", SdipErrorParameter.SUPPORTMAIL);
    }
    return rdf4jClient.doConstructSparql(queryWithGraph, endPoint, iParameterMemory);
  }

  @Override
  public void populate(String subjectIri, IParameterMemory iParameterMemory,
      ServiceArguments serviceArguments) {
    generateNewTmpGraphName();
    try {
      setSubjectIri(subjectIri);
      JsonArray actionModelJsonArray = rdf4jClient
          .selectSparqlOfg(getSparql(subjectIri, SPARQL), iParameterMemory.getOfgModelRepo());

      setLabel(
          actionModelJsonArray.get(0).getAsJsonObject().get(SDOSConstants.LABEL).getAsJsonObject()
              .get(SDOSConstants.VALUE).getAsString());
      setSparqlConvert(
          actionModelJsonArray.get(0).getAsJsonObject().get(SDOSConstants.CONSTRUCT_SPARQL)
              .getAsJsonObject()
              .get(SDOSConstants.VALUE).getAsString());

      setHasConnector(
          (HttpConnectorModel) createPopulateConnector(iParameterMemory, serviceArguments,
              actionModelJsonArray, SDOSConstants.HTTP_CONNECTOR));

      populateInputParameter(iParameterMemory, serviceArguments, actionModelJsonArray);
      populateOutputParameter(iParameterMemory, serviceArguments, actionModelJsonArray);

      populateNextAction(iParameterMemory, serviceArguments, actionModelJsonArray);

    } catch (IllegalStateException exception) {
      throw new IncidentException(exception, SdipErrorCode.INVALID_JSONARRAY_RESPONSE, LOGGER,
          exception.getMessage());
    } catch (NullPointerException exception) {
      throw new IncidentException(exception, SdipErrorCode.FAILED_TO_PARSE_JSONARRAY, LOGGER,
          exception.getMessage());
    } catch (IncidentException exception) {
      throw exception;
    } catch (Exception exception) {
      throw new IncidentException(exception, SdipErrorCode.UNKNOWN_REASON_ERROR, LOGGER,
          exception.getMessage(), SdipErrorParameter.SUPPORTMAIL);
    }
  }
}
