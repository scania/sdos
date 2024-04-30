package com.scania.sdip.sdos.orchestration.model;

import com.google.gson.JsonArray;
import com.scania.sdip.exceptions.IncidentException;
import com.scania.sdip.exceptions.SdipErrorCode;
import com.scania.sdip.exceptions.SdipErrorParameter;
import com.scania.sdip.sdos.model.ServiceArguments;

import com.scania.sdip.sdos.orchestration.Rdf4jClient;
import com.scania.sdip.sdos.orchestration.factory.ScriptModelFactory;
import com.scania.sdip.sdos.orchestration.interfaces.IParameterMemory;

import com.scania.sdip.sdos.orchestration.interfaces.IScriptModel;
import com.scania.sdip.sdos.utils.SDOSConstants;
import com.scania.sdip.sdos.utils.Utility;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ScriptActionModel extends ActionModel {

  private static final Logger LOGGER = LogManager.getLogger(ScriptActionModel.class);

  public static final String SPARQL =
          SDOSConstants.PREFIX_RDF
                  + SDOSConstants.PREFIX_RDFS
                  + "PREFIX : <" + SDOSConstants.ORCHESTRATION_PREFIX + "> \n"
                  + "SELECT ?script ?scripttype ?nextaction ?nextactiontype ?inputparameter ?outputparameter ?inputparametertype ?outputparametertype ?label"
                  + " ?" + SDOSConstants.CONTEXT
                  + "\n "
                  + "WHERE\n"
                  + "{ \n"
                  + "BIND(<" + SDOSConstants.VARIABLE + "> AS ?subject)"
                  + "?subject rdf:type :ScriptAction ;\n"
                  + " :hasScript ?script ;\n"
                  + " :inputParameter ?inputparameter ;\n"
                  + " :outputParameter ?outputparameter;\n"
                  + " rdfs:label ?label .\n"
                  + "OPTIONAL{?subject :" + SDOSConstants.HAS_CONTEXT + " ?" + SDOSConstants.CONTEXT + "}\n"
                  + "     OPTIONAL{?subject :hasNextAction ?nextaction ."
                  + "     {?nextaction rdf:type :Action .} \n"
                  + "     UNION { ?nextaction rdf:type :HTTPAction .\n"
                  + "       BIND(:HTTPAction AS ?nextactiontype)} \n"
                  + "     UNION { ?nextaction rdf:type :SOAPAction .\n"
                  + "       BIND(:SOAPAction AS ?nextactiontype)} \n"
                  + "     UNION { ?nextaction rdf:type :ScriptAction .\n"
                  + "       BIND(:ScriptAction AS ?nextactiontype)} \n"
                  + "     UNION { ?nextaction rdf:type :ResultAction . \n"
                  + "       BIND(:ResultAction AS ?nextactiontype)} \n"
                  + "     UNION { ?nextaction rdf:type :VirtualGraphAction .\n"
                  + "       BIND(:VirtualGraphAction AS ?nextactiontype)}\n"
                  + "     UNION { ?nextaction rdf:type :QueryAction .\n"
                  + "       BIND(:QueryAction AS ?nextactiontype)}\n"
                  + "}\n"
                  + "{?script rdf:type :Script .} \n"
                  + " UNION { ?script rdf:type :GroovyScript .\n"
                  + "   BIND(:GroovyScript AS ?scripttype)} \n"
                  + " UNION { ?script rdf:type :PythonScript .\n"
                  + "   BIND(:PythonScript AS ?scripttype)} \n"
                  + " {?inputparameter rdf:type :Parameter .\n"
                  + " BIND(:Parameter AS ?inputparametertype)}\n"
                  + " UNION { ?inputparameter rdf:type :StandardParameter .\n"
                  + "   BIND(:StandardParameter AS ?inputparametertype)}\n"
                  + " UNION { ?inputparameter rdf:type :HTTPParameter .\n"
                  + "   BIND(:HTTPParameter AS ?inputparametertype)}\n"
                  + " UNION { ?inputparameter rdf:type :SparqlQueryParameter .\n"
                  + "   BIND(:SparqlQueryParameter AS ?inputparametertype)}\n"
                  + " {?outputparameter rdf:type :Parameter .\n"
                  + " BIND(:Parameter AS ?outputparametertype)}\n"
                  + " UNION { ?outputparameter rdf:type :StandardParameter .\n"
                  + "   BIND(:StandardParameter AS ?outputparametertype)}\n"
                  + " UNION { ?outputparameter rdf:type :HTTPParameter .\n"
                  + "   BIND(:HTTPParameter AS ?outputparametertype)}\n"
                  + " UNION { ?outputparameter rdf:type :SparqlQueryParameter .\n"
                  + "   BIND(:SparqlQueryParameter AS ?outputparametertype)}\n"
                  + "}\n";

  public ScriptActionModel() {
    this.setRdf4jClient(new Rdf4jClient());
  }

  private IScriptModel script;
  private Rdf4jClient rdf4jClient;
  private JsonLdContextModel contextModel;

  public void setRdf4jClient(Rdf4jClient rdf4jClient) {
    this.rdf4jClient = rdf4jClient;
  }

  public void setContextModel(JsonLdContextModel contextModel) {
    this.contextModel = contextModel;
  }

  public JsonLdContextModel getContextModel() {
    return contextModel;
  }

  @Override
  public void run(IParameterMemory iParameterMemory, JsonLdContextModel context,
                  ServiceArguments serviceArguments) {
    try {
      Map<String, HashMap<String, List<String>>> resList = script.executeScript(iParameterMemory, context, serviceArguments,
              contextModel, outputParameter, inputParameter);
      //TODO VERIFY OUTPUTPARAMETER MATCH RESULTS FROM SCRIPTS
      for (Map.Entry<String, HashMap<String, List<String>>> set : resList.entrySet()) {
        iParameterMemory.putParameter(set.getKey(), set.getValue());
      }
    } catch (IncidentException exception) {
      throw exception;
    } catch (Exception exception) {
      throw new IncidentException(SdipErrorCode.UNKNOWN_REASON_ERROR, LOGGER,
              exception.getMessage(), SdipErrorParameter.SUPPORTMAIL);
    }
  }

  @Override
  public void populate(String subjectIri, IParameterMemory iParameterMemory,
                       ServiceArguments serviceArguments) {
    try {
      setSubjectIri(subjectIri);
      JsonArray jsonArray = rdf4jClient
              .selectSparqlOfg(getSparql(subjectIri, SPARQL), iParameterMemory.getOfgModelRepo());

      IScriptModel iScriptModel = ScriptModelFactory.getScript(
              jsonArray.get(0).getAsJsonObject().get(SDOSConstants.SCRIPT_TYPE).getAsJsonObject()
                      .get(SDOSConstants.VALUE)
                      .getAsString());
      iScriptModel.populate(
              jsonArray.get(0).getAsJsonObject().get(SDOSConstants.SCRIPT).getAsJsonObject()
                      .get(SDOSConstants.VALUE)
                      .getAsString(), iParameterMemory, serviceArguments);
      setScript(iScriptModel);
      if (jsonArray.get(0).getAsJsonObject().has(SDOSConstants.CONTEXT)) {
        setContextModel(getNewJsonLdContextModel());
        contextModel.populate(
                jsonArray.get(0).getAsJsonObject().get(SDOSConstants.CONTEXT).getAsJsonObject()
                        .get(SDOSConstants.VALUE)
                        .getAsString(), iParameterMemory, serviceArguments);
      }
      populateInputParameter(iParameterMemory, serviceArguments, jsonArray);
      populateOutputParameter(iParameterMemory, serviceArguments, jsonArray);
      setLabel(jsonArray.get(0).getAsJsonObject().get(SDOSConstants.LABEL).getAsJsonObject()
              .get(SDOSConstants.VALUE).getAsString());
      populateNextAction(iParameterMemory, serviceArguments, jsonArray);

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
              subjectIri), SdipErrorCode.UNKNOWN_REASON_ERROR, LOGGER,
              exception.getMessage(), SdipErrorParameter.SUPPORTMAIL);
    }
  }

  public JsonLdContextModel getNewJsonLdContextModel() {
    return new JsonLdContextModel();
  }

  public IScriptModel getScript() {
    return script;
  }

  public void setScript(IScriptModel script) {
    this.script = script;
  }
}
