package com.scania.sdos.orchestration.model;

import com.google.gson.JsonArray;
import com.scania.sdip.exceptions.IncidentException;
import com.scania.sdip.exceptions.SdipErrorCode;
import com.scania.sdos.model.ServiceArguments;
import com.scania.sdos.orchestration.HttpClient;
import com.scania.sdos.orchestration.Rdf4jClient;
import com.scania.sdos.orchestration.interfaces.IActionModel;
import com.scania.sdos.orchestration.interfaces.IParameterMemory;
import com.scania.sdos.utils.SDOSConstants;
import com.scania.sdos.utils.Utility;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.parser.ParsedGraphQuery;
import org.eclipse.rdf4j.query.parser.ParsedQuery;
import org.eclipse.rdf4j.query.parser.ParsedTupleQuery;
import org.eclipse.rdf4j.query.parser.QueryParserUtil;

public class VirtualGraphActionModel extends ActionModel implements IActionModel {

  private static final Logger LOGGER = LogManager.getLogger(VirtualGraphActionModel.class);

  public static final String SPARQL_QUERY =
      SDOSConstants.PREFIX_RDF
          + SDOSConstants.PREFIX_RDFS
          + "PREFIX : <" + SDOSConstants.ORCHESTRATION_PREFIX + "> \n"
          + "SELECT ?sparql ?nextaction ?nextactiontype ?outputparameter ?outputparametertype ?label\n"
          + "WHERE\n"
          + "{ \n"
          + "BIND(<" + SDOSConstants.VARIABLE + "> AS ?subject)\n"
          + "?subject rdf:type :VirtualGraphAction ;\n"
          + " :sparql ?sparql ;\n"
          + " :outputParameter ?outputparameter;\n"
          + " rdfs:label ?label .\n"
          + "    OPTIONAL{?subject :hasNextAction ?" + SDOSConstants.NEXT_ACTION + " .\n"
          + "        {?" + SDOSConstants.NEXT_ACTION + " rdf:type :Action .} \n"
          + SDOSConstants.UNION + SDOSConstants.NEXT_ACTION + " rdf:type :HTTPAction .\n"
          + "            BIND(:HTTPAction AS ?" + SDOSConstants.NEXT_ACTION_TYPE + SDOSConstants.CLOSING
          + SDOSConstants.UNION + SDOSConstants.NEXT_ACTION + " rdf:type :SOAPAction .\n"
          + "            BIND(:SOAPAction AS ?" + SDOSConstants.NEXT_ACTION_TYPE + SDOSConstants.CLOSING
          + SDOSConstants.UNION + SDOSConstants.NEXT_ACTION + " rdf:type :ScriptAction .\n"
          + "            BIND(:ScriptAction AS ?" + SDOSConstants.NEXT_ACTION_TYPE + SDOSConstants.CLOSING
          + SDOSConstants.UNION + SDOSConstants.NEXT_ACTION + " rdf:type :ResultAction . \n"
          + "            BIND(:ResultAction AS ?" + SDOSConstants.NEXT_ACTION_TYPE + ")} \n"
          + SDOSConstants.UNION + SDOSConstants.NEXT_ACTION + " rdf:type :VirtualGraphAction .\n"
          + "            BIND(:VirtualGraphAction AS ?" + SDOSConstants.NEXT_ACTION_TYPE + ")}\n"
          + SDOSConstants.UNION + SDOSConstants.NEXT_ACTION + " rdf:type :SparqlConvertAction . \n"
          + "            BIND(:SparqlConvertAction AS ?" + SDOSConstants.NEXT_ACTION_TYPE + ")} \n"
          + "    }\n"
          + "{?outputparameter rdf:type :Parameter .\n"
          + "BIND(:Parameter AS ?outputparametertype)}\n"
          + "UNION { ?outputparameter rdf:type :StandardParameter .\n"
          + "BIND(:StandardParameter AS ?outputparametertype)}\n"
          + "UNION { ?outputparameter rdf:type :HTTPParameter .\n"
          + "BIND(:HTTPParameter AS ?outputparametertype)}\n"
          + "}";

  private String sparql;
  private Rdf4jClient rdf4jClient;
  private HttpClient httpClient;

  public VirtualGraphActionModel() {
    this.setRdf4jClient(new Rdf4jClient());
    this.setHttpClient(new HttpClient());
  }

  @Override
  public void run(IParameterMemory iParameterMemory, JsonLdContextModel context,
                  ServiceArguments serviceArguments) {

    //verify inputs
    //TODO right now the OFG decides the result graph to insert to,
    //TODO WE NEED TO RESTRICT USER ACCESS TO BE 100% sure we can control where namedgraph is mentioned
    //TODO THIS WILL AFFECT HTTPACTION ASWELL
    //action
    String result = runSelectOrConstructSparql(getActionSparql(), iParameterMemory,
        serviceArguments);
    //output
    HashMap<String, List<String>> outputMemoryValue = new HashMap<>();
    outputMemoryValue.put(outputParameter.get(0).getKeys().get(0),
        Collections.singletonList(result));
    iParameterMemory.putParameter(outputParameter.get(0).getSubjectIri(), outputMemoryValue);
  }

  @Override
  public void populate(String subjectIri, IParameterMemory iParameterMemory,
      ServiceArguments serviceArguments) {
    try {
      setSubjectIri(subjectIri);
      JsonArray jsonArray = rdf4jClient
          .selectSparqlOfg(getSparql(subjectIri, SPARQL_QUERY), iParameterMemory.getOfgModelRepo());
      setLabel(jsonArray.get(0).getAsJsonObject().get(SDOSConstants.LABEL).getAsJsonObject()
          .get(SDOSConstants.VALUE).getAsString());

      populateOutputParameter(iParameterMemory, serviceArguments, jsonArray);

      setActionSparql(
          jsonArray.get(0).getAsJsonObject().get(SDOSConstants.ACTION_SPARQL).getAsJsonObject()
              .get(SDOSConstants.VALUE)
              .getAsString());

      populateNextAction(iParameterMemory, serviceArguments, jsonArray);

    } catch (IllegalStateException | IndexOutOfBoundsException exception) {
      throw new IncidentException(exception, Utility.getErrorMessage(exception,label,
          subjectIri), SdipErrorCode.INVALID_JSONARRAY_RESPONSE, LOGGER,
          exception.getMessage());
    } catch (NullPointerException exception) {
      throw new IncidentException(exception, Utility.getErrorMessage(exception,label,
          subjectIri), SdipErrorCode.FAILED_TO_PARSE_JSONARRAY, LOGGER,
          exception.getMessage());
    } catch (IncidentException exception) {
      throw exception;
    } catch (Exception exception) {
      throw new IncidentException(exception, Utility.getErrorMessage(exception,label,
          subjectIri), SdipErrorCode.UNKNOWN_PARSING_ERROR, LOGGER,
          exception.getMessage());
    }
  }

  public Rdf4jClient getRdf4jClient() {
    return rdf4jClient;
  }

  public void setRdf4jClient(Rdf4jClient rdf4jClient) {
    this.rdf4jClient = rdf4jClient;
  }

  public String getActionSparql() {
    return sparql;
  }

  public void setActionSparql(String sparql) {
    this.sparql = sparql;
  }

  public HttpClient getHttpClient() {
    return httpClient;
  }

  public void setHttpClient(HttpClient httpClient) {
    this.httpClient = httpClient;
  }

  public String runSelectOrConstructSparql(String query, IParameterMemory iParameterMemory,
      ServiceArguments serviceArguments) {
    try {
      ParsedQuery parsedQuery = QueryParserUtil.parseQuery(QueryLanguage.SPARQL, query, null);
      if (parsedQuery instanceof ParsedTupleQuery) {
        JsonArray jsonArray = rdf4jClient
            .doSelectSparql(query, serviceArguments.getStardogQueryEndpoint(), iParameterMemory);
        return jsonArray.toString();
      } else if (parsedQuery instanceof ParsedGraphQuery) {
        return rdf4jClient.doConstructSparql(query, serviceArguments.getStardogQueryEndpoint(), iParameterMemory);
      } else {
        throw new NotImplementedException();
      }
    } catch (MalformedQueryException queryException) {
      throw new IncidentException(queryException, SdipErrorCode.MALFORMED_SPARQL_QUERY, LOGGER,
          queryException.getMessage());
    } catch (IllegalStateException | IndexOutOfBoundsException queryException) {
      throw new IncidentException(queryException, SdipErrorCode.INVALID_JSONARRAY_RESPONSE, LOGGER,
          queryException.getMessage());
    } catch (NullPointerException queryException) {
      throw new IncidentException(queryException, SdipErrorCode.FAILED_TO_PARSE_JSONARRAY, LOGGER,
          queryException.getMessage());
    } catch (IncidentException queryException) {
      throw queryException;
    } catch (Exception queryException) {
      throw new IncidentException(queryException, SdipErrorCode.UNKNOWN_PARSING_ERROR, LOGGER,
          queryException.getMessage());
    }
  }
}
