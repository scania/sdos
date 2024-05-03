package com.scania.sdos.orchestration.model;

import com.google.gson.JsonArray;
import com.scania.sdip.exceptions.IncidentException;
import com.scania.sdip.exceptions.SdipErrorCode;
import com.scania.sdos.model.ServiceArguments;
import com.scania.sdos.orchestration.Rdf4jClient;
import com.scania.sdos.orchestration.interfaces.IBaseModel;
import com.scania.sdos.orchestration.interfaces.IParameterMemory;
import com.scania.sdos.utils.SDOSConstants;
import com.scania.sdos.utils.Utility;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class JsonLdContextModel implements IBaseModel {

  private static final Logger LOGGER = LogManager.getLogger(JsonLdContextModel.class);

  public static final String SPARQL =

      SDOSConstants.PREFIX_RDF +
          SDOSConstants.PREFIX_RDFS +
          "PREFIX : <" + SDOSConstants.ORCHESTRATION_PREFIX + "> \n" +
          "SELECT ?context ?label \n" +
          "WHERE\n" +
          "{ \n" +
          "  <" + SDOSConstants.VARIABLE + "> rdf:type :JsonLdContext ;\n" +
          "    rdfs:label ?label ;\n" +
          "    :context ?context .\n" +
          "}";

  private String subjectIri;
  private String label;
  private String context;
  private Rdf4jClient rdf4jClient;

  public JsonLdContextModel() {
    this.setRdf4jClient(new Rdf4jClient());
  }

  @Override
  public String getSubjectIri() {
    return subjectIri;
  }

  public void setSubjectIri(String subjectIri) {
    this.subjectIri = subjectIri;
  }

  @Override
  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getContext() {
    return context;
  }

  public void setContext(String context) {
    this.context = context;
  }

  public void setRdf4jClient(Rdf4jClient rdf4jClient) {
    this.rdf4jClient = rdf4jClient;
  }

  @Override
  public void populate(String subjectIri, IParameterMemory iParameterMemory,
      ServiceArguments serviceArguments) {
    try {

      setSubjectIri(subjectIri);
      JsonArray contextArray = rdf4jClient
          .selectSparqlOfg(getSparql(subjectIri, SPARQL), iParameterMemory.getOfgModelRepo());
      if (contextArray.size() >= 1) {
        setContext(
            contextArray.get(0).getAsJsonObject().get(SDOSConstants.CONTEXT).getAsJsonObject()
                .get(SDOSConstants.VALUE)
                .getAsString().replace("\n", ""));
      }
      setLabel(contextArray.get(0).getAsJsonObject().get(SDOSConstants.LABEL).getAsJsonObject()
          .get(SDOSConstants.VALUE).getAsString());
    } catch (IllegalStateException exception) {
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

  private String getSparql(String subject, String sparql) {
    return sparql.replace(SDOSConstants.VARIABLE, Optional.ofNullable(subject).orElse(""));
  }
}
