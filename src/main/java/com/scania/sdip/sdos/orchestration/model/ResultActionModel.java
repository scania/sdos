package com.scania.sdip.sdos.orchestration.model;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.scania.sdip.exceptions.IncidentException;
import com.scania.sdip.exceptions.SdipErrorCode;
import com.scania.sdip.sdos.model.ServiceArguments;
import com.scania.sdip.sdos.orchestration.Rdf4jClient;
import com.scania.sdip.sdos.orchestration.interfaces.IActionModel;
import com.scania.sdip.sdos.orchestration.interfaces.IConnectorModel;
import com.scania.sdip.sdos.orchestration.interfaces.IParameterMemory;
import com.scania.sdip.sdos.utils.SDOSConstants;
import com.scania.sdip.sdos.utils.Utility;
import java.util.HashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

abstract class ResultActionModel extends ActionModel implements IActionModel {

  private static final Logger LOGGER = LogManager.getLogger(ResultActionModel.class);

  public static final String SPARQL =
      SDOSConstants.PREFIX_RDF
          + SDOSConstants.PREFIX_RDFS
          + "PREFIX : <" + SDOSConstants.ORCHESTRATION_PREFIX + ">  \n"
          + "SELECT ?httpBody ?httpHeader ?system ?hasConnector ?connectortype ?nextaction ?nextactiontype ?inputparameter ?inputparametertype ?label\n"
          + "WHERE{ \n"
          + "BIND(<" + SDOSConstants.VARIABLE + "> AS ?subject)"
          + "?subject rdf:type :ResultAction  ;\n"
          + "  :httpHeader ?httpHeader  ;\n"
          + "  :hasSystem ?system ;\n"
          + "  :hasConnector ?hasConnector;\n"
          + "  rdfs:label ?label ."
          + "     OPTIONAL{?subject :httpBody ?httpBody  .}\n"
          + "     OPTIONAL{?subject :inputParameter ?inputparameter .\n"
          + "     {?inputparameter rdf:type :Parameter .\n"
          + "       BIND(:Parameter AS ?inputparametertype)}\n"
          + "     UNION { ?inputparameter rdf:type :StandardParameter .\n"
          + "       BIND(:StandardParameter AS ?inputparametertype)}\n"
          + "     UNION { ?inputparameter rdf:type :HTTPParameter .\n"
          + "       BIND(:HTTPParameter AS ?inputparametertype)}\n"
          + "     UNION { ?inputparameter rdf:type :BasicCredentialsParameter .\n"
          + "       BIND(:BasicCredentialsParameter AS ?inputparametertype)}\n"
          + "     }\n"
          + "    OPTIONAL{?subject :hasNextAction ?" + SDOSConstants.NEXT_ACTION + " .\n"
          + "        {?" + SDOSConstants.NEXT_ACTION + " rdf:type :Action .} \n"
          + SDOSConstants.UNION + SDOSConstants.NEXT_ACTION + " rdf:type :HTTPAction .\n"
          + "            BIND(:HTTPAction AS ?" + SDOSConstants.NEXT_ACTION_TYPE + SDOSConstants.CLOSING
          + SDOSConstants.UNION + SDOSConstants.NEXT_ACTION + " rdf:type :SOAPAction .\n"
          + "            BIND(:SOAPAction AS ?" + SDOSConstants.NEXT_ACTION_TYPE + SDOSConstants.CLOSING
          + SDOSConstants.UNION + SDOSConstants.NEXT_ACTION + " rdf:type :ScriptAction .\n"
          + "            BIND(:ScriptAction AS ?" + SDOSConstants.NEXT_ACTION_TYPE + SDOSConstants.CLOSING
          + SDOSConstants.UNION + SDOSConstants.NEXT_ACTION + " rdf:type :ResultAction . \n"
          + "            BIND(:ResultAction AS ?" + SDOSConstants.NEXT_ACTION_TYPE + SDOSConstants.CLOSING
          + SDOSConstants.UNION + SDOSConstants.NEXT_ACTION + " rdf:type :VirtualGraphAction .\n"
          + "            BIND(:VirtualGraphAction AS ?" + SDOSConstants.NEXT_ACTION_TYPE + ")}\n"
          + "        UNION { ?" + SDOSConstants.NEXT_ACTION + " rdf:type :SparqlConvertAction . \n"
          + "            BIND(:SparqlConvertAction AS ?" + SDOSConstants.NEXT_ACTION_TYPE + ")} \n"
          + "        UNION { ?" + SDOSConstants.NEXT_ACTION + " rdf:type :QueryAction . \n"
          + "            BIND(:QueryAction AS ?" + SDOSConstants.NEXT_ACTION_TYPE + ")} \n"
          + "    }\n"
          + " {?hasConnector rdf:type :Connector .} \n"
          + " UNION { ?hasConnector rdf:type :SOAPConnector .\n"
          + "   BIND(:SOAPConnector AS ?connectortype)} \n"
          + " UNION { ?hasConnector rdf:type :HTTPConnector .\n"
          + "   BIND(:HTTPConnector AS ?connectortype)} \n"
          + " }";

  private String httpbody;
  private HashMap<String, String> httpHeader = new HashMap<>();
  private IConnectorModel connectorModel;
  private Rdf4jClient rdf4jClient;

  public ResultActionModel() {
    this.setRdf4jClient(new Rdf4jClient());
  }

  public Rdf4jClient getRdf4jClient() {
    return rdf4jClient;
  }

  public void setRdf4jClient(Rdf4jClient rdf4jClient) {
    this.rdf4jClient = rdf4jClient;
  }

  @Override
  public void populate(String subjectIri, IParameterMemory iParameterMemory,
      ServiceArguments serviceArguments) {
    try {
      Gson gson = new Gson();
      setSubjectIri(subjectIri);
      JsonArray jsonArray = rdf4jClient
          .selectSparqlOfg(getSparql(subjectIri, SPARQL), iParameterMemory.getOfgModelRepo());
      setLabel(jsonArray.get(0).getAsJsonObject().get(SDOSConstants.LABEL).getAsJsonObject()
          .get(SDOSConstants.VALUE).getAsString());
      if (jsonArray.get(0).getAsJsonObject().has(SDOSConstants.HTTPHEADER)) {
        setHttpHeader(gson.fromJson(
            jsonArray.get(0).getAsJsonObject().get(SDOSConstants.HTTPHEADER).getAsJsonObject()
                .get(SDOSConstants.VALUE).getAsString(), HashMap.class));
      }
      if (jsonArray.get(0).getAsJsonObject().has(SDOSConstants.HTTPBODY)) {
        setHttpbody(
            jsonArray.get(0).getAsJsonObject().get(SDOSConstants.HTTPBODY).getAsJsonObject()
                .get(SDOSConstants.VALUE).getAsString());
      }
      String connectorType = jsonArray.get(0).getAsJsonObject().get(SDOSConstants.CONNECTOR_TYPE)
          .getAsJsonObject().get(SDOSConstants.VALUE).getAsString();
      setConnectorModel(
          createPopulateConnector(iParameterMemory, serviceArguments, jsonArray, connectorType));
      populateInputParameter(iParameterMemory, serviceArguments, jsonArray);
      populateNextAction(iParameterMemory, serviceArguments, jsonArray);

    } catch (IllegalStateException | IndexOutOfBoundsException exception) {
      throw new IncidentException(exception, Utility.getErrorMessage(exception, label,
          subjectIri), SdipErrorCode.INVALID_JSONARRAY_RESPONSE, LOGGER,
          exception.getMessage());
    } catch (NullPointerException exception) {
      throw new IncidentException(exception, Utility.getErrorMessage(exception, label,
          subjectIri), SdipErrorCode.FAILED_TO_PARSE_JSONARRAY, LOGGER,
          exception.getMessage());
    } catch (IncidentException exception) {
      throw exception;
    } catch (Exception exception) {
      throw new IncidentException(exception, Utility.getErrorMessage(exception, label,
          subjectIri), SdipErrorCode.UNKNOWN_PARSING_ERROR, LOGGER,
          exception.getMessage());
    }
  }

  public HashMap<String, String> getHttpHeader() {
    return httpHeader;
  }

  public void setHttpHeader(HashMap<String, String> httpQueryParameter) {
    this.httpHeader = httpQueryParameter;
  }

  public void addHttpHeader(String key, String value) {
    this.httpHeader.put(key, value);
  }


  @Override
  public abstract void run(IParameterMemory iParameterMemory, JsonLdContextModel context,
      ServiceArguments serviceArguments);

  public IConnectorModel getConnectorModel() {
    return connectorModel;
  }

  public void setConnectorModel(IConnectorModel connectorModel) {
    this.connectorModel = connectorModel;
  }

  public String getHttpbody() {
    return httpbody;
  }

  public void setHttpbody(String httpbody) {
    this.httpbody = httpbody;
  }

}
