package com.scania.sdos.orchestration.model;

import com.google.gson.JsonArray;
import com.scania.sdip.exceptions.IncidentException;
import com.scania.sdip.exceptions.SdipErrorCode;
import com.scania.sdos.model.ServiceArguments;
import com.scania.sdos.orchestration.Rdf4jClient;
import com.scania.sdos.orchestration.interfaces.IParameterMemory;
import com.scania.sdos.orchestration.interfaces.IParameterModel;
import com.scania.sdos.orchestration.interfaces.IScriptModel;
import com.scania.sdos.utils.SDOSConstants;
import com.scania.sdos.utils.Utility;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;

public class PythonScriptModel implements IScriptModel {

  private static final Logger LOGGER = LogManager.getLogger(PythonScriptModel.class);

  private static final String SPARQL =
          SDOSConstants.PREFIX_RDF
                  + SDOSConstants.PREFIX_RDFS
                  + "PREFIX : <" + SDOSConstants.ORCHESTRATION_PREFIX + "> \n"
                  + "SELECT ?script ?label\n"
                  + "WHERE\n"
                  + "{ \n"
                  + "    <"
                  + SDOSConstants.VARIABLE
                  + "> rdf:type :PythonScript;\n"
                  + "    :script ?script ;\n"
                  + "    rdfs:label ?label .\n"
                  + "}\n";

  public PythonScriptModel() {
    this.setRdf4jClient(new Rdf4jClient());
  }

  private String subjectIri;
  private String script;
  private Rdf4jClient rdf4jClient;
  private String label;

  @Override
  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
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
      JsonArray scriptJsonArray = rdf4jClient
              .selectSparqlOfg(getSparql(subjectIri, SPARQL), iParameterMemory.getOfgModelRepo());
      setScript(scriptJsonArray.get(0).getAsJsonObject().get(SDOSConstants.SCRIPT).getAsJsonObject()
              .get(SDOSConstants.VALUE)
              .getAsString());
      setLabel(scriptJsonArray.get(0).getAsJsonObject().get(SDOSConstants.LABEL).getAsJsonObject()
              .get(SDOSConstants.VALUE)
              .getAsString());
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
  public String getScript() {
    return script;
  }

  @Override
  public HashMap executeScript(IParameterMemory iParameterMemory, JsonLdContextModel context, ServiceArguments serviceArguments, JsonLdContextModel contextModel, List<IParameterModel> outputParameter, List<IParameterModel> inputParameter) {
    throw new NotImplementedException("Not Implemented");
  }

  public void setScript(String script) {
    this.script = script;
  }
}