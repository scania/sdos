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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BasicCredentialsParameterModel implements IParameterModel {

  private static final Logger LOGGER = LogManager.getLogger(BasicCredentialsParameterModel.class);

  public static final String SPARQL_GET_PARAMETERS =
          SDOSConstants.PREFIX_RDF +
                  SDOSConstants.PREFIX_RDFS +
                  "PREFIX : <" + SDOSConstants.ORCHESTRATION_PREFIX + "> \n" +
                  "SELECT ?label\n" +
                  "WHERE\n" +
                  "{ \n" +
                  "BIND(<" + SDOSConstants.VARIABLE + "> AS ?subject)\n" +
                  "?subject rdf:type :BasicCredentialsParameter .\n" +
                  "?subject rdfs:label ?label .\n" +
                  "}";

  public BasicCredentialsParameterModel() {
    this.setRdf4jClient(new Rdf4jClient());
  }

  private String label;
  private String subjectIri;
  private String username;
  private String password;
  private Rdf4jClient rdf4jClient;


  public void setRdf4jClient(Rdf4jClient rdf4jClient) {
    this.rdf4jClient = rdf4jClient;
  }

  @Override
  public String getSubjectIri() {
    return subjectIri;
  }

  @Override
  public String getLabel() {
    return label;
  }


  @Override
  public List<String> getKeys() {
    List<String> keys = new ArrayList<>();
    keys.add(SDOSConstants.USERNAME);
    keys.add(SDOSConstants.PASSWORD);
    return keys;
  }

  @Override
  public HashMap getValue() {
    HashMap<String, String> hashMap = new HashMap<>();
    hashMap.put(SDOSConstants.USERNAME, getUsername());
    hashMap.put(SDOSConstants.PASSWORD, getPassword());
    return hashMap;
  }

  public void setSubjectIri(String subjectIri) {
    this.subjectIri = subjectIri;
  }


  @Override
  public void populate(String subjectIri, IParameterMemory iParameterMemory,
                       ServiceArguments serviceArguments) {
    doPopulateBasicCredential(subjectIri, iParameterMemory);
  }

  @Override
  public void populateHelp(String subjectIri, IParameterMemory iParameterMemory,
                           ServiceArguments serviceArguments) {
    doPopulateBasicCredential(subjectIri, iParameterMemory);
  }

  private void doPopulateBasicCredential(String subjectIri, IParameterMemory iParameterMemory) {

    try {
      setSubjectIri(subjectIri);
      JsonArray taskJsonArray = rdf4jClient.selectSparqlOfg(getSparql(subjectIri, SPARQL_GET_PARAMETERS),
              iParameterMemory.getOfgModelRepo());

      setLabel(taskJsonArray.get(0).getAsJsonObject().get(SDOSConstants.LABEL).getAsJsonObject()
              .get(SDOSConstants.VALUE).getAsString());
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

  @Override
  public JsonArray createUserInputHelp() {
    JsonArray jsonArray = new JsonArray();
    JsonObject usernameObject = new JsonObject();
    usernameObject.addProperty(SDOSConstants.KEY, SDOSConstants.USERNAME);
    usernameObject.addProperty(SDOSConstants.VALUE, "");
    jsonArray.add(usernameObject);
    JsonObject passwordObject = new JsonObject();
    passwordObject.addProperty(SDOSConstants.KEY, SDOSConstants.PASSWORD);
    passwordObject.addProperty(SDOSConstants.VALUE, "");
    jsonArray.add(passwordObject);

    return jsonArray;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }
}