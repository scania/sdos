package com.scania.sdip.sdos.orchestration.model;

import com.google.gson.JsonArray;
import com.scania.sdip.exceptions.IncidentException;
import com.scania.sdip.exceptions.SdipErrorCode;
import com.scania.sdip.sdos.model.ServiceArguments;
import com.scania.sdip.sdos.orchestration.Rdf4jClient;
import com.scania.sdip.sdos.orchestration.factory.AuthenticationModelFactory;
import com.scania.sdip.sdos.orchestration.interfaces.IAuthModel;
import com.scania.sdip.sdos.orchestration.interfaces.IConnectorModel;
import com.scania.sdip.sdos.orchestration.interfaces.IParameterMemory;
import com.scania.sdip.sdos.utils.SDOSConstants;
import com.scania.sdip.sdos.utils.Utility;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SoapConnectorModel implements IConnectorModel {

  private static final Logger LOGGER = LogManager.getLogger(SoapConnectorModel.class);

  public static final String SPARQL =
      SDOSConstants.PREFIX_RDF
          + SDOSConstants.PREFIX_RDFS
          + "PREFIX : <" + SDOSConstants.ORCHESTRATION_PREFIX + "> \n"
          + "SELECT  ?bindingName ?hasAuthenticationMethod ?hasAuthenticationMethodType ?wsdlFile ?label\n"
          + "  WHERE  \n"
          + "  { <" + SDOSConstants.VARIABLE + "> :bindingName  ?bindingName ;\n"
          + "      rdf:type :SOAPConnector;\n"
          + "  rdfs:label ?label;\n"
          + "      :wsdlFile ?wsdlFile.\n"
          + "  OPTIONAL{ <" + SDOSConstants.VARIABLE
          + "> :hasAuthenticationMethod ?hasAuthenticationMethod . \n" +
          "{?hasAuthenticationMethod rdf:type :SOAPAuthenticationMethod . } \n" +
          "UNION { ?hasAuthenticationMethod rdf:type :SOAPBasicAuthenticationMethod . \n" +
          "BIND(:SOAPBasicAuthenticationMethod AS ?hasAuthenticationMethodType)}} \n" +
          "  }\n";
  private String subjectIri;
  private IAuthModel hasAuthenticationMethod;
  private String wsdlFile;
  private String bindingName;
  private Rdf4jClient rdf4jClient;
  private String label;

  @Override
  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public SoapConnectorModel() {
    this.setRdf4jClient(new Rdf4jClient());
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

      setWsdlFile(jsonArray.get(0).getAsJsonObject().get(SDOSConstants.WSDLFILE).getAsJsonObject()
          .get(SDOSConstants.VALUE).getAsString());
      setBindingName(
          jsonArray.get(0).getAsJsonObject().get(SDOSConstants.BINDING_NAME).getAsJsonObject()
              .get(SDOSConstants.VALUE).getAsString());
      if (jsonArray.get(0).getAsJsonObject().get(SDOSConstants.HAS_AUTH_METHOD) != null) {
        hasAuthenticationMethod = AuthenticationModelFactory.getAuthModel(
            jsonArray.get(0).getAsJsonObject().get(SDOSConstants.HAS_AUTH_METHOD_TYPE)
                .getAsJsonObject()
                .get(SDOSConstants.VALUE)
                .getAsString());
        setLabel(jsonArray.get(0).getAsJsonObject().get(SDOSConstants.LABEL).getAsJsonObject()
            .get(SDOSConstants.VALUE).getAsString());
        hasAuthenticationMethod.populate(
            jsonArray.get(0).getAsJsonObject().get(SDOSConstants.HAS_AUTH_METHOD).getAsJsonObject()
                .get(SDOSConstants.VALUE)
                .getAsString(), iParameterMemory, serviceArguments);
      }
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

  public IAuthModel getHasAuthenticationMethod() {
    return hasAuthenticationMethod;
  }

  public void setHasAuthenticationMethod(IAuthModel hasAuthenticationMethod) {
    this.hasAuthenticationMethod = hasAuthenticationMethod;
  }

  public String getWsdlFile() {
    return wsdlFile;
  }

  public void setWsdlFile(String wsdlFile) {
    this.wsdlFile = wsdlFile;
  }

  public String getBindingName() {
    return bindingName;
  }

  public void setBindingName(String bindingName) {
    this.bindingName = bindingName;
  }
}