package com.scania.sdip.sdos.orchestration.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.scania.sdip.exceptions.IncidentException;
import com.scania.sdip.exceptions.SdipErrorCode;
import com.scania.sdip.sdos.model.ServiceArguments;
import com.scania.sdip.sdos.orchestration.Rdf4jClient;
import com.scania.sdip.sdos.orchestration.interfaces.IParameterMemory;
import com.scania.sdip.sdos.orchestration.interfaces.IParameterModel;
import com.scania.sdip.sdos.utils.SDOSConstants;
import com.scania.sdip.sdos.utils.Utility;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StandardParameterModel implements IParameterModel {

  private static final Logger LOGGER = LogManager.getLogger(StandardParameterModel.class);
  
  public static final String SPARQL_GET_PARAMETERS =
      SDOSConstants.PREFIX_RDF +
          SDOSConstants.PREFIX_RDFS +
          "PREFIX : <" + SDOSConstants.ORCHESTRATION_PREFIX + "> \n" +
          "SELECT ?paramName ?label\n" +
          "WHERE\n" +
          "{ \n" +
          "BIND(<" + SDOSConstants.VARIABLE + "> AS ?subject)\n" +
          "?subject rdfs:label ?label ;\n" +
          "         :paramName ?paramName .\n" +
          "}";

  public StandardParameterModel() {
    this.setRdf4jClient(new Rdf4jClient());
  }

  private String label;
  private String subjectIri;
  private String paramName;
  private Rdf4jClient rdf4jClient;

  public void setRdf4jClient(Rdf4jClient rdf4jClient) {
    this.rdf4jClient = rdf4jClient;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  @Override
  public String getSubjectIri() {
    return subjectIri;
  }

  @Override
  public String getLabel() {
    return label;
  }

  public void setSubjectIri(String subjectIri) {
    this.subjectIri = subjectIri;
  }

  @Override
  public List<String> getKeys() {
    return Collections.singletonList(paramName);
  }

  @Override
  public Map getValue() {
    return Collections.EMPTY_MAP;
  }

  @Override
  public void populate(String subjectIri, IParameterMemory iParameterMemory,
      ServiceArguments serviceArguments) {
    doPopulate(subjectIri, iParameterMemory);
  }

  @Override
  public void populateHelp(String subjectIri, IParameterMemory iParameterMemory,
      ServiceArguments serviceArguments) {
    doPopulate(subjectIri, iParameterMemory);
  }

  private void doPopulate(String subjectIri, IParameterMemory iParameterMemory) {
    try {
      setSubjectIri(subjectIri);
      JsonArray taskJsonArray = rdf4jClient.selectSparqlOfg(getSparql(subjectIri, SPARQL_GET_PARAMETERS), iParameterMemory.getOfgModelRepo());

      setLabel(taskJsonArray.get(0).getAsJsonObject().get(SDOSConstants.LABEL).getAsJsonObject()
          .get(SDOSConstants.VALUE).getAsString());
      setParamName(taskJsonArray.get(0).getAsJsonObject().get(SDOSConstants.PARAM_NAME)
          .getAsJsonObject().get(SDOSConstants.VALUE).getAsString());

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

  private String getSparql(String subject, String sparql) {
    return sparql.replace(SDOSConstants.VARIABLE, subject);
  }

  public String getParamName() {
    return paramName;
  }

  public void setParamName(String paramName) {
    this.paramName = paramName;
  }

  @Override
  public JsonArray createUserInputHelp() {
    JsonArray jsonArray = new JsonArray();
    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty(SDOSConstants.KEY, paramName);
    jsonObject.addProperty(SDOSConstants.VALUE, "");
    jsonArray.add(jsonObject);
    return jsonArray;
  }

}