package com.scania.sdip.sdos.orchestration.model;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.scania.sdip.exceptions.IncidentException;
import com.scania.sdip.exceptions.SdipErrorCode;
import com.scania.sdip.sdos.model.ServiceArguments;
import com.scania.sdip.sdos.orchestration.Rdf4jClient;
import com.scania.sdip.sdos.orchestration.interfaces.IParameterMemory;
import com.scania.sdip.sdos.orchestration.interfaces.IParameterModel;
import com.scania.sdip.sdos.orchestration.interfaces.IScriptModel;
import com.scania.sdip.sdos.services.config.SpringContext;
import com.scania.sdip.sdos.utils.SDOSConstants;
import com.scania.sdip.sdos.utils.Utility;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.scania.sdip.sdos.orchestration.JavaGroovy;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.LinkedHashMap;

public class GroovyScriptModel implements IScriptModel {

  private static final Logger LOGGER = LogManager.getLogger(GroovyScriptModel.class);

  private static final String SPARQL = SDOSConstants.PREFIX_RDF
          + SDOSConstants.PREFIX_RDFS
          + "PREFIX : <" + SDOSConstants.ORCHESTRATION_PREFIX + "> \n"
          + "SELECT ?script ?label\n"
          + "WHERE\n"
          + "{ \n"
          + "<" + SDOSConstants.VARIABLE + "> rdf:type :GroovyScript;\n"
          + "    :script ?script ;\n"
          + "     rdfs:label ?label .\n"
          + "}\n";

  private String subjectIri;
  private String script;
  private Rdf4jClient rdf4jClient;
  private String label;
  private JavaGroovy javaGroovy;

  @Override
  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public GroovyScriptModel() {
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
      JsonArray scriptJsonArray = rdf4jClient
              .selectSparqlOfg(getSparql(subjectIri, SPARQL), iParameterMemory.getOfgModelRepo());
      setScript(scriptJsonArray.get(0).getAsJsonObject().get(SDOSConstants.SCRIPT).getAsJsonObject()
              .get(SDOSConstants.VALUE).getAsString());
      setLabel(scriptJsonArray.get(0).getAsJsonObject().get(SDOSConstants.LABEL).getAsJsonObject()
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
  public String getScript() {
    return script;
  }

  public void setScript(String script) {
    this.script = script;
  }

  @Override
  public Map executeScript(IParameterMemory iParameterMemory, JsonLdContextModel context,
                           ServiceArguments serviceArguments, JsonLdContextModel contextModel,
                           List<IParameterModel> outputParameter, List<IParameterModel> inputParameter) {
    HashMap<String, HashMap<String, List<String>>> resList =null;
    try {
      HashMap<String, String> scriptInput = new LinkedHashMap<>();
      // If ScriptAction hasContext, use this contextModel over context passed from Task!!
      if (contextModel != null) {
        scriptInput.put(SDOSConstants.CONTEXT, contextModel.getContext());
      } else if (context != null) {
        scriptInput.put(SDOSConstants.CONTEXT, context.getContext());
      } else {
        throw new IncidentException(SdipErrorCode.INVALID_CONTEXT_DATA, LOGGER,
                SDOSConstants.INVALID_CONTEXT);
      }
      HashMap<String, String> hashMap = new HashMap<>();
      HashMap<String, LinkedList<HashMap<String, String>>> inputParamKey = new HashMap();
      for (IParameterModel i : outputParameter) {
        hashMap.put(i.getLabel(), i.getSubjectIri());
      }
      scriptInput.put(SDOSConstants.OUTPUTPARAMETERS, new Gson().toJson(hashMap, HashMap.class));
      extractInputParameter(inputParameter, iParameterMemory, inputParamKey);
      scriptInput.put(SDOSConstants.INPUTPARAMETERS,
              new Gson().toJson(inputParamKey, HashMap.class));
      if (!this.getScript().isEmpty()) {
        javaGroovy = SpringContext.getBean(JavaGroovy.class);
        resList = javaGroovy.runGroovyShell(scriptInput, this.getScript());
      }
    } catch (NullPointerException exception) {
      throw new IncidentException(SdipErrorCode.FAILED_TO_PARSE_JSONARRAY, LOGGER,
              exception.getMessage());
    } catch (IncidentException exception) {
      throw exception;
    }
    return resList;
  }
  private void extractInputParameter(List<IParameterModel> inputParameter,
                                     IParameterMemory iParameterMemory,
                                     HashMap<String, LinkedList<HashMap<String, String>>> inputParamKey){
    for (IParameterModel i : inputParameter) {
      for (String key : i.getKeys()) {
        List<String> value = iParameterMemory.getValue(i.getSubjectIri()).get(key);
        JsonArray jsonArrayContent = new JsonArray();
        HashMap<String, String> inputParamValue = new HashMap();
        for (String singleValue : value) {
          jsonArrayContent.add(singleValue);
        }
        mapInputParamKey( i,key,jsonArrayContent,inputParamKey,inputParamValue);
      }
    }
  }

  private void mapInputParamKey(IParameterModel i,String key, JsonArray jsonArrayContent,HashMap<String, LinkedList<HashMap<String, String>>> inputParamKey ,HashMap<String, String> inputParamValue){
    if (inputParamKey.containsKey(key)) {
      LinkedList<HashMap<String, String>> list = (inputParamKey.get(key));
      for (Map mapvalue : list) {
        if (!mapvalue.containsKey(i.getSubjectIri())) {
          HashMap map = new HashMap();
          map.put(i.getSubjectIri(), jsonArrayContent.toString());
          list.add(map);
        }
      }
      inputParamKey.put(key, list);
    } else {
      inputParamValue.put(i.getSubjectIri(), jsonArrayContent.toString());
      LinkedList<HashMap<String, String>> list = new LinkedList();
      list.add(inputParamValue);
      inputParamKey.put(key, list);
    }
  }
}
