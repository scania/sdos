package com.scania.sdos.orchestration.model;

import com.google.gson.JsonArray;
import com.scania.sdip.exceptions.IncidentException;
import com.scania.sdip.exceptions.SdipErrorCode;
import com.scania.sdip.exceptions.SdipErrorParameter;
import com.scania.sdos.model.ServiceArguments;
import com.scania.sdos.orchestration.Rdf4jClient;
import com.scania.sdos.orchestration.factory.ParameterModelFactory;
import com.scania.sdos.orchestration.interfaces.IAuthModel;
import com.scania.sdos.orchestration.interfaces.IParameterMemory;
import com.scania.sdos.orchestration.interfaces.IParameterModel;
import com.scania.sdos.utils.SDOSConstants;
import com.scania.sdos.utils.Utility;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HttpBearerTokenAuthModel implements IAuthModel {


  public static final String SPARQL = SDOSConstants.PREFIX_RDF
      + SDOSConstants.PREFIX_RDFS
      + "PREFIX : <" + SDOSConstants.ORCHESTRATION_PREFIX + "> \n"
      + "SELECT ?label ?inputparameter ?inputparametertype \n"
      + "WHERE \n"
      + "{ \n"
      + "    BIND(<" + SDOSConstants.VARIABLE + "> AS ?subject)\n"
      + "    ?subject rdf:type :HTTPBearerTokenAuthenticationMethod ;\n"
      + "    rdfs:label  ?label  ; \n"
      + "    :inputParameter ?inputparameter .\n"
      + "    {?inputparameter rdf:type :Parameter  .\n"
      + "    BIND(:Parameter AS ?inputparametertype)}\n"
      + "    UNION { ?inputparameter rdf:type :TokenCredentialsParameter .\n"
      + "    BIND(:TokenCredentialsParameter AS ?inputparametertype)}\n"
      + "}";
  private static final Logger LOGGER = LogManager.getLogger(HttpBearerTokenAuthModel.class);
  private String subjectIri;
  private String label;
  private String token;
  private IParameterModel inputParameter;
  private Rdf4jClient rdf4jClient;

  public HttpBearerTokenAuthModel() {
    this.setRdf4jClient(new Rdf4jClient());
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public void setRdf4jClient(Rdf4jClient rdf4jClient) {
    this.rdf4jClient = rdf4jClient;
  }

  @Override
  public String getSubjectIri() {
    return subjectIri;
  }

  public void setSubjectIri(String iri) {
    subjectIri = iri;
  }

  @Override
  public void populate(String subjectIri, IParameterMemory iParameterMemory,
      ServiceArguments serviceArguments) {
    try {
      setSubjectIri(subjectIri);
      JsonArray jsonArray = rdf4jClient
          .selectSparqlOfg(getSparql(subjectIri, SPARQL), iParameterMemory.getOfgModelRepo());
      setLabel(jsonArray.get(0).getAsJsonObject().get(SDOSConstants.LABEL).getAsJsonObject()
          .get(SDOSConstants.VALUE).getAsString());

      IParameterModel iParameterModel = ParameterModelFactory.getParameter(
          jsonArray.get(0).getAsJsonObject().get(SDOSConstants.INPUT_PARAMETER_TYPE)
              .getAsJsonObject()
              .get(SDOSConstants.VALUE).getAsString());

      iParameterModel.populate(
          jsonArray.get(0).getAsJsonObject().get(SDOSConstants.INPUT_PARAMETER).getAsJsonObject()
              .get(SDOSConstants.VALUE).getAsString(), iParameterMemory, serviceArguments);
      setInputParameter(iParameterModel);
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

  private String getSparql(String subject, String query) {
    return query.replace(SDOSConstants.VARIABLE, subject);
  }

  public void run(IParameterMemory iParameterMemory) {
    try {
      setToken(iParameterMemory.getValue(inputParameter.getSubjectIri()).get(SDOSConstants.TOKEN)
          .get(0));
    } catch (Exception exception) {
      throw new IncidentException(SdipErrorCode.UNKNOWN_REASON_ERROR, LOGGER,
          exception.getMessage(), SdipErrorParameter.SUPPORTMAIL);
    }
  }


  public IParameterModel getInputParameter() {
    return inputParameter;
  }

  public void setInputParameter(IParameterModel inputParameter) {
    this.inputParameter = inputParameter;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public HashMap addCredentials(HashMap requestHeaders, IParameterMemory iParameterMemory) {

    try {

      requestHeaders.put(SDOSConstants.AUTHORIZATION, SDOSConstants.BEARER + " " + getToken());

      return requestHeaders;

    } catch (Exception exception) {
      throw new IncidentException(SdipErrorCode.UNKNOWN_REASON_ERROR, LOGGER,
          exception.getMessage(), SdipErrorParameter.SUPPORTMAIL);
    }
  }
}

