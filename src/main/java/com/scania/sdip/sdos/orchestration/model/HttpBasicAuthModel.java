package com.scania.sdip.sdos.orchestration.model;


import com.google.gson.JsonArray;
import com.scania.sdip.exceptions.IncidentException;
import com.scania.sdip.exceptions.SdipErrorCode;
import com.scania.sdip.exceptions.SdipErrorParameter;
import com.scania.sdip.sdos.model.ServiceArguments;
import com.scania.sdip.sdos.orchestration.Rdf4jClient;
import com.scania.sdip.sdos.orchestration.factory.ParameterModelFactory;
import com.scania.sdip.sdos.orchestration.interfaces.IAuthModel;
import com.scania.sdip.sdos.orchestration.interfaces.IParameterMemory;
import com.scania.sdip.sdos.orchestration.interfaces.IParameterModel;
import com.scania.sdip.sdos.utils.SDOSConstants;
import com.scania.sdip.sdos.utils.Utility;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class HttpBasicAuthModel implements IAuthModel {

  private static final Logger LOGGER = LogManager.getLogger(HttpBasicAuthModel.class);
  
  public static final String SPARQL = SDOSConstants.PREFIX_RDF
      + SDOSConstants.PREFIX_RDFS
      + "PREFIX : <" + SDOSConstants.ORCHESTRATION_PREFIX + "> \n"
      + "SELECT ?label ?inputparameter ?inputparametertype \n"
      + "WHERE \n"
      + "{ \n"
      + "    BIND(<" + SDOSConstants.VARIABLE + "> AS ?subject)\n"
      + "    ?subject rdf:type :HTTPBasicAuthenticationMethod ;\n"
      + "    rdfs:label  ?label  ; \n"
      + "    :inputParameter ?inputparameter .\n"
      + "    {?inputparameter rdf:type :Parameter  .\n"
      + "    BIND(:Parameter AS ?inputparametertype)}\n"
      + "    UNION { ?inputparameter rdf:type :BasicCredentialsParameter .\n"
      + "    BIND(:BasicCredentialsParameter AS ?inputparametertype)}\n"
      + "}";

  public HttpBasicAuthModel() {
    this.setRdf4jClient(new Rdf4jClient());
    this.setCredentialsProvider(new BasicCredentialsProvider());
  }


  private String subjectIri;
  private String label;
  private CredentialsProvider credentialsProvider;
  private IParameterModel inputParameter;
  private Rdf4jClient rdf4jClient;

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

  private String getSparql(String subject, String query) {
    return query.replace(SDOSConstants.VARIABLE, subject);
  }

  public void run(IParameterMemory iParameterMemory) {
    try {
      HashMap<String, List<String>> memoryValue = iParameterMemory
          .getValue(inputParameter.getSubjectIri());

      credentialsProvider.setCredentials(AuthScope.ANY,
          new UsernamePasswordCredentials(
              memoryValue.get(SDOSConstants.USERNAME).get(0),
              memoryValue.get(SDOSConstants.PASSWORD).get(0)));
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

  public CredentialsProvider getCredentials() {
    return credentialsProvider;
  }

  public void setCredentialsProvider(CredentialsProvider credentialsProvider) {
    this.credentialsProvider = credentialsProvider;
  }
  public HashMap addCredentials(HashMap requestHeaders, IParameterMemory iParameterMemory) {

    try {
      HashMap<String, List<String>> memoryValue = iParameterMemory
              .getValue(inputParameter.getSubjectIri());

      String base64Creds = new String(Base64.encodeBase64((memoryValue.get(SDOSConstants.USERNAME).get(0)+":" +
              memoryValue.get(SDOSConstants.PASSWORD).get(0)).getBytes()));

      requestHeaders.put(SDOSConstants.AUTHORIZATION, SDOSConstants.BASIC+" "+ base64Creds);

      return requestHeaders;

    } catch (Exception exception) {
      throw new IncidentException(SdipErrorCode.UNKNOWN_REASON_ERROR, LOGGER,
              exception.getMessage(), SdipErrorParameter.SUPPORTMAIL);
    }
  }

}
