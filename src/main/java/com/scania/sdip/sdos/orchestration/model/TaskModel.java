package com.scania.sdip.sdos.orchestration.model;


import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.scania.sdip.exceptions.IncidentException;
import com.scania.sdip.exceptions.SdipErrorCode;
import com.scania.sdip.sdos.model.ServiceArguments;
import com.scania.sdip.sdos.orchestration.Rdf4jClient;
import com.scania.sdip.sdos.orchestration.factory.ActionModelFactory;
import com.scania.sdip.sdos.orchestration.factory.ParameterModelFactory;
import com.scania.sdip.sdos.orchestration.interfaces.IActionModel;
import com.scania.sdip.sdos.orchestration.interfaces.IParameterMemory;
import com.scania.sdip.sdos.orchestration.interfaces.IParameterModel;
import com.scania.sdip.sdos.orchestration.interfaces.ITaskModel;
import com.scania.sdip.sdos.utils.SDOSConstants;
import com.scania.sdip.sdos.utils.Utility;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class TaskModel implements ITaskModel {


  private static final Logger LOGGER = LogManager.getLogger(TaskModel.class);

  public static final String SPARQL_GET_SMALL_TASK =

      SDOSConstants.PREFIX_RDF +
          SDOSConstants.PREFIX_RDFS +
          "PREFIX : <" + SDOSConstants.ORCHESTRATION_PREFIX + "> \n" +
          "SELECT ?task ?inputparameter ?inputparametertype ?label\n" +
          "WHERE\n" +
          "{ \n" +
          "?task rdf:type :Task .\n" +
          "?task :subjectIri ?<" + SDOSConstants.VARIABLE + "> .\n" +
          "?task rdfs:label ?label .\n" +
          "OPTIONAL{?task :inputParameter ?inputparameter .\n" +
          "{?inputparameter rdf:type :Parameter .\n" +
          "BIND(:Parameter AS ?inputparametertype)}\n" +
          "UNION { ?inputparameter rdf:type :StandardParameter .\n" +
          "BIND(:StandardParameter AS ?inputparametertype)}\n" +
          "UNION { ?inputparameter rdf:type :HTTPParameter .\n" +
          "BIND(:HTTPParameter AS ?inputparametertype)}\n" +
          "UNION { ?inputparameter rdf:type :BasicCredentialsParameter .\n" +
          "BIND(:BasicCredentialsParameter AS ?inputparametertype)}\n" +
          "UNION { ?inputparameter rdf:type :TokenCredentialsParameter .\n" +
          "BIND(:TokenCredentialsParameter AS ?inputparametertype)}}\n" +
          "}";


  public static final String SPARQL_GET_TASK =

      SDOSConstants.PREFIX_RDF +

          SDOSConstants.PREFIX_RDFS +

          "PREFIX : <" + SDOSConstants.ORCHESTRATION_PREFIX + "> \n" +
          "SELECT ?task ?label ?inputparameter ?inputparametertype ?nextaction ?nextactiontype ?resultMetaData ?context\n"
          +
          "WHERE\n" +
          "{ \n" +
          "?task rdf:type :Task .\n" +
          "?task :subjectIri ?<" + SDOSConstants.VARIABLE + "> .\n" +
          "?task rdfs:label ?label .\n" +
          "?task :" + SDOSConstants.HAS_CONTEXT + " ?" + SDOSConstants.CONTEXT + " .\n" +
          "OPTIONAL{?task :inputParameter ?inputparameter .\n" +
          "{?inputparameter rdf:type :Parameter .\n" +
          "BIND(:Parameter AS ?inputparametertype)}\n" +
          "UNION { ?inputparameter rdf:type :StandardParameter .\n" +
          "BIND(:StandardParameter AS ?inputparametertype)}\n" +
          "UNION { ?inputparameter rdf:type :HTTPParameter .\n" +
          "BIND(:HTTPParameter AS ?inputparametertype)}\n" +
          "UNION { ?inputparameter rdf:type :BasicCredentialsParameter .\n" +
          "BIND(:BasicCredentialsParameter AS ?inputparametertype)}\n" +
          "UNION { ?inputparameter rdf:type :TokenCredentialsParameter .\n" +
          "BIND(:TokenCredentialsParameter AS ?inputparametertype)}}\n" +
          "?task :hasResultMetaData ?resultMetaData .\n" +
          "?task :hasAction ?nextaction .\n" +
          "{?nextaction rdf:type :Action .}\n" +
          "UNION { ?nextaction rdf:type :HTTPAction .\n" +
          "BIND(:HTTPAction AS ?nextactiontype)}\n" +
          "UNION { ?nextaction rdf:type :SOAPAction .\n" +
          "BIND(:SOAPAction AS ?nextactiontype)}\n" +
          "UNION { ?nextaction rdf:type :ScriptAction .\n" +
          "BIND(:ScriptAction AS ?nextactiontype)}\n" +
              "UNION { ?nextaction rdf:type :ResultAction .\n" +
              "BIND(:ResultAction AS ?nextactiontype)}\n" +
              "UNION { ?nextaction rdf:type :VirtualGraphAction .\n" +
              "BIND(:VirtualGraphAction AS ?nextactiontype)}\n" +
              "UNION { ?nextaction rdf:type :SparqlConvertAction . \n" +
              "BIND(:SparqlConvertAction AS ?nextactiontype)} \n" +
              "UNION { ?nextaction rdf:type :QueryAction . \n" +
              "BIND(:QueryAction AS ?nextactiontype)} \n" +
              "}";

  public TaskModel() {
    this.setRdf4jClient(new Rdf4jClient());
    this.setMetaDataModel(new MetaDataModel());
  }
  private String subjectIri;

  private JsonLdContextModel jsonLdContextModel;
  private String label;
  private IActionModel nextAction;
  private List<IParameterModel> inputParameter = new ArrayList<>();
  private Rdf4jClient rdf4jClient;
  private MetaDataModel metaDataModel;


  public void setMetaDataModel(MetaDataModel metaDataModel) {
    this.metaDataModel = metaDataModel;
  }

  public MetaDataModel getMetaDataModel() {
    return metaDataModel;
  }

  public void setRdf4jClient(Rdf4jClient rdf4jClient) {
    this.rdf4jClient = rdf4jClient;
  }

  @Override
  public String getSubjectIri() {
    return subjectIri;
  }

  public void setSubjectIri(String subjectIri) {
    this.subjectIri = subjectIri;
  }

  public void populate(String subjectIri, HashMap<String, HashMap<String, String>> userInput,
      IParameterMemory iParameterMemory, ServiceArguments serviceArguments) {
    try {
      JsonArray taskJsonArray = rdf4jClient
          .selectSparqlOfg(getSparql(subjectIri, SPARQL_GET_TASK),
                  iParameterMemory.getOfgModelRepo());
      setSubjectIri(subjectIri);

      setLabel(taskJsonArray.get(0).getAsJsonObject().get(SDOSConstants.LABEL).getAsJsonObject()
          .get(SDOSConstants.VALUE).getAsString());
      processTaskJsonArray(taskJsonArray, iParameterMemory, serviceArguments, userInput);


      IActionModel iActionModel = ActionModelFactory.getAction(
          taskJsonArray.get(0).getAsJsonObject().get(SDOSConstants.NEXT_ACTION_TYPE)
              .getAsJsonObject()
              .get(SDOSConstants.VALUE).getAsString(), iParameterMemory);
      iActionModel.populate(
          taskJsonArray.get(0).getAsJsonObject().get(SDOSConstants.NEXT_ACTION).getAsJsonObject()
              .get(SDOSConstants.VALUE).getAsString(), iParameterMemory, serviceArguments);
      setNextAction(iActionModel);
      metaDataModel.populate(
          taskJsonArray.get(0).getAsJsonObject().get(SDOSConstants.RESULTMETADATA).getAsJsonObject()
              .get(SDOSConstants.VALUE).getAsString(), iParameterMemory, serviceArguments);
      setContext(getNewJsonLdContextModel());

      jsonLdContextModel.populate(
          taskJsonArray.get(0).getAsJsonObject().get(SDOSConstants.CONTEXT).getAsJsonObject()
              .get(SDOSConstants.VALUE).getAsString(), iParameterMemory, serviceArguments);

    } catch (IllegalStateException exception) {
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

  private void processTaskJsonArray(JsonArray taskJsonArray, IParameterMemory iParameterMemory,
                                    ServiceArguments serviceArguments,
                                    HashMap<String, HashMap<String, String>> userInput){
    for (JsonElement i : taskJsonArray) {
      IParameterModel inputParameterModel = ParameterModelFactory.getParameter(
              i.getAsJsonObject().get(SDOSConstants.INPUT_PARAMETER_TYPE).getAsJsonObject()
                      .get(SDOSConstants.VALUE)
                      .getAsString());
      inputParameterModel.populate(
              i.getAsJsonObject().get(SDOSConstants.INPUT_PARAMETER).getAsJsonObject()
                      .get(SDOSConstants.VALUE)
                      .getAsString(), iParameterMemory, serviceArguments);
      setInputParameter(inputParameterModel);
      if (iParameterMemory.getValue(inputParameterModel.getSubjectIri()) == null) {
        HashMap<String, String> temp = userInput.get(inputParameterModel.getLabel());
        HashMap<String, List<String>> listHashMap = new HashMap<>();

        for (Entry<String, String> entry : temp.entrySet()) {
          listHashMap.put(entry.getKey(), Collections.singletonList(entry.getValue()));
        }
        iParameterMemory.putParameter(inputParameterModel.getSubjectIri(), listHashMap);
      }
    }
  }

  public void populateOnlyTask(String subjectIri, IParameterMemory iParameterMemory,
      ServiceArguments serviceArguments) {
    try {
      JsonArray taskJsonArray = rdf4jClient
          .selectSparqlOfg(getSparql(subjectIri, SPARQL_GET_SMALL_TASK),
               iParameterMemory.getOfgModelRepo());
      setSubjectIri(subjectIri);

      setLabel(taskJsonArray.get(0).getAsJsonObject().get(SDOSConstants.LABEL).getAsJsonObject()
          .get(SDOSConstants.VALUE).getAsString());
      for (JsonElement i : taskJsonArray) {
        IParameterModel inputParameterModel = ParameterModelFactory.getParameter(
            i.getAsJsonObject().get(SDOSConstants.INPUT_PARAMETER_TYPE).getAsJsonObject()
                .get(SDOSConstants.VALUE)
                .getAsString());
        inputParameterModel.populateHelp(
            i.getAsJsonObject().get(SDOSConstants.INPUT_PARAMETER).getAsJsonObject()
                .get(SDOSConstants.VALUE)
                .getAsString(), iParameterMemory, serviceArguments);
        setInputParameter(inputParameterModel);
      }
    } catch (IllegalStateException exception) {
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
    return sparql.replace(SDOSConstants.VARIABLE, Optional.ofNullable(subject).orElse(""));
  }

  @Override
  public JsonLdContextModel getContext() {
    return jsonLdContextModel;
  }


  public void setContext(JsonLdContextModel jsonLdContextModel) {
    this.jsonLdContextModel = jsonLdContextModel;
  }

  public JsonLdContextModel getNewJsonLdContextModel() {
    return new JsonLdContextModel();
  }

  @Override
  public IActionModel getNextAction() {
    return nextAction;
  }


  public void setNextAction(IActionModel nextAction) {
    this.nextAction = nextAction;
  }

  public List<IParameterModel> getInputParameters() {
    return inputParameter;
  }

  public void setInputParameter(IParameterModel inputParameter) {
    this.inputParameter.add(inputParameter);
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

}