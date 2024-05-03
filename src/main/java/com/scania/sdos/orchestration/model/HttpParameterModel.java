package com.scania.sdos.orchestration.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.scania.sdip.exceptions.IncidentException;
import com.scania.sdip.exceptions.SdipErrorCode;
import com.scania.sdip.exceptions.SdipErrorParameter;
import com.scania.sdos.model.ServiceArguments;
import com.scania.sdos.orchestration.Rdf4jClient;
import com.scania.sdos.orchestration.interfaces.IParameterMemory;
import com.scania.sdos.orchestration.interfaces.IParameterModel;
import com.scania.sdos.utils.SDOSConstants;
import com.scania.sdos.utils.Utility;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HttpParameterModel implements IParameterModel {

  private static final Logger LOGGER = LogManager.getLogger(HttpParameterModel.class);
  
  public static final String SPARQL_GET_PARAMETERS =
          SDOSConstants.PREFIX_RDF +
                  SDOSConstants.PREFIX_RDFS +
                  "PREFIX : <" + SDOSConstants.ORCHESTRATION_PREFIX + "> \n" +
                  "SELECT ?endpoint ?httpBody ?httpHeader ?httpQueryParameter ?value ?label\n" +
                  "WHERE\n" +
                  "{ \n" +
                  "BIND(<" + SDOSConstants.VARIABLE + "> AS ?subject)\n" +
                  "?subject rdfs:label ?label .\n" +
                  "OPTIONAL{?subject :endpoint ?endpoint . } .\n" +
                  "OPTIONAL{?subject :httpBody ?httpBody . } .\n" +
                  "OPTIONAL{?subject :httpHeader ?httpHeader . } .\n" +
                  "OPTIONAL{?subject :httpQueryParameter ?httpQueryParameter . } .\n" +
                  "}";

  private String label;
  private String subjectIri;
  private String endpoint;
  private String httpBody;
  private String httpHeaders;
  private String httpQueryParameters;
  private Rdf4jClient rdf4jClient;

  public HttpParameterModel() {
    this.setRdf4jClient(new Rdf4jClient());
  }

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

  public void setSubjectIri(String subjectIri) {
    this.subjectIri = subjectIri;
  }

  @Override
  public List<String> getKeys() {
    List<String> params = new ArrayList<>();
    if (isHeaders()) {
      params.add(SDOSConstants.HTTPHEADER);
    }
    if (isQueryparameters()) {
      params.add(SDOSConstants.HTTPQUERYPARAM);
    }
    if (isEndpoint()) {
      params.add(SDOSConstants.ENDPOINT);
    }
    if (isBody()) {
      params.add(SDOSConstants.HTTPBODY);
    }
    return params;
  }

  @Override
  public HashMap getValue() {
    HashMap<String, List<String>> hashMap = new HashMap<>();
    if (isEndpoint()) {
      if (!getEndpoint().isEmpty()) {
        hashMap.put(SDOSConstants.ENDPOINT, Collections.singletonList(getEndpoint()));
      }
    }
    if (isBody()) {
      if (!getHttpBody().isEmpty()) {
        hashMap.put(SDOSConstants.HTTPBODY, Collections.singletonList(getHttpBody()));
      }
    }
    if (isHeaders()) {
      if (!getHttpHeaders().isEmpty()) {
        hashMap.put(SDOSConstants.HTTPHEADER, Collections.singletonList(getHttpHeaders()));
      }
    }
    if (isQueryparameters()) {
      if (!getHttpQueryParameters().isEmpty()) {
        hashMap.put(SDOSConstants.HTTPQUERYPARAM,
            Collections.singletonList(getHttpQueryParameters()));
      }
    }
    return hashMap;
  }

  private String getSparql(String subject, String sparql) {
    return sparql.replace(SDOSConstants.VARIABLE, subject);
  }

  @Override
  public void populate(String subjectIri, IParameterMemory iParameterMemory,
      ServiceArguments serviceArguments) {
    doPopulateHttpParamModel(subjectIri, iParameterMemory);
  }

  @Override
  public void populateHelp(String subjectIri, IParameterMemory iParameterMemory,
      ServiceArguments serviceArguments) {
    doPopulateHttpParamModel(subjectIri, iParameterMemory);
  }

  private void doPopulateHttpParamModel(String subjectIri, IParameterMemory iParameterMemory) {

    try {
      //TODO NEED TO ALLOW HARDCODED VALUES
      setSubjectIri(subjectIri);
      JsonArray taskJsonArray = rdf4jClient
              .selectSparqlOfg(getSparql(subjectIri, SPARQL_GET_PARAMETERS),
                   iParameterMemory.getOfgModelRepo());

      setLabel(taskJsonArray.get(0).getAsJsonObject().get(SDOSConstants.LABEL).getAsJsonObject()
          .get(SDOSConstants.VALUE).getAsString());

      iterateQueryResults(taskJsonArray);

      //TODO NEED TO BE SOMETHING, CHECK THAT ATLEAST ONE EXIST
      if (iParameterMemory.getValue(getSubjectIri())
          == null) {
        iParameterMemory.putParameter(getSubjectIri(), getValue());
      }
    } catch (IndexOutOfBoundsException | IllegalStateException exception) {
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
          subjectIri), SdipErrorCode.UNKNOWN_REASON_ERROR, LOGGER,
          exception.getMessage(), SdipErrorParameter.SUPPORTMAIL);
    }
  }

  private void iterateQueryResults(JsonArray results){
    if (results.get(0).getAsJsonObject().has(SDOSConstants.ENDPOINT)) {
      setEndpoint(
              results.get(0).getAsJsonObject().get(SDOSConstants.ENDPOINT).getAsJsonObject()
                      .get(SDOSConstants.VALUE).getAsString());
    }
    if (results.get(0).getAsJsonObject().has(SDOSConstants.HTTPBODY)) {
      setBody(results.get(0).getAsJsonObject().get(SDOSConstants.HTTPBODY).getAsJsonObject()
              .get(SDOSConstants.VALUE).getAsString());
    }
    if (results.get(0).getAsJsonObject().has(SDOSConstants.HTTPHEADER)) {
      setHeaders(
              results.get(0).getAsJsonObject().get(SDOSConstants.HTTPHEADER).getAsJsonObject()
                      .get(SDOSConstants.VALUE).getAsString());
    }
    if (results.get(0).getAsJsonObject().has(SDOSConstants.HTTPQUERYPARAM)) {
      setQueryparameters(
              results.get(0).getAsJsonObject().get(SDOSConstants.HTTPQUERYPARAM)
                      .getAsJsonObject()
                      .get(SDOSConstants.VALUE).getAsString());
    }
  }

  public void setEndpoint(String endpoint) {
    this.endpoint = endpoint;
  }

  public void setBody(String httpBody) {
    this.httpBody = httpBody;
  }

  public void setHeaders(String httpHeaders) {
    this.httpHeaders = httpHeaders;
  }

  public void setQueryparameters(String httpQueryParameters) {
    this.httpQueryParameters = httpQueryParameters;
  }

  @Override
  public JsonArray createUserInputHelp() {
    JsonArray jsonArray = new JsonArray();

    if (isEndpoint()) {
      JsonObject jsonObject = new JsonObject();
      jsonObject.addProperty(SDOSConstants.KEY, SDOSConstants.ENDPOINT);
      jsonObject.addProperty(SDOSConstants.VALUE, getEndpoint());
      jsonArray.add(jsonObject);
    }
    if (isBody()) {
      JsonObject jsonObject = new JsonObject();
      jsonObject.addProperty(SDOSConstants.KEY, SDOSConstants.HTTPBODY);
      jsonObject.addProperty(SDOSConstants.VALUE, getHttpBody());
      jsonArray.add(jsonObject);
    }
    if (isHeaders()) {
      JsonObject jsonObject = new JsonObject();
      jsonObject.addProperty(SDOSConstants.KEY, SDOSConstants.HTTPHEADER);
      jsonObject.addProperty(SDOSConstants.VALUE, getHttpHeaders());
      jsonArray.add(jsonObject);
    }
    if (isQueryparameters()) {
      JsonObject jsonObject = new JsonObject();
      jsonObject.addProperty(SDOSConstants.KEY, SDOSConstants.HTTPQUERYPARAM);
      jsonObject.addProperty(SDOSConstants.VALUE, getHttpQueryParameters());
      jsonArray.add(jsonObject);
    }
    return jsonArray;
  }

  public boolean isQueryparameters() {
    if (httpQueryParameters != null) {
      return true;
    } else {
      return false;
    }
  }

  public boolean isHeaders() {
    if (httpHeaders != null) {
      return true;
    } else {
      return false;
    }
  }

  public boolean isBody() {
    if (httpBody != null) {
      return true;
    } else {
      return false;
    }
  }

  public boolean isEndpoint() {
    if (endpoint != null) {
      return true;
    } else {
      return false;
    }
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getEndpoint() {
    return endpoint;
  }

  public String getHttpBody() {
    return httpBody;
  }

  public String getHttpHeaders() {
    return httpHeaders;
  }

  public String getHttpQueryParameters() {
    return httpQueryParameters;
  }
}
