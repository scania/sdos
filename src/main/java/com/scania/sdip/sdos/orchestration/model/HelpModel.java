package com.scania.sdip.sdos.orchestration.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.scania.sdip.exceptions.IncidentException;
import com.scania.sdip.exceptions.SdipErrorCode;
import com.scania.sdip.sdos.model.ServiceArguments;
import com.scania.sdip.sdos.orchestration.Rdf4jClient;
import com.scania.sdip.sdos.orchestration.interfaces.IParameterMemory;
import com.scania.sdip.sdos.orchestration.interfaces.IParameterModel;
import com.scania.sdip.sdos.orchestration.interfaces.ITaskModel;
import com.scania.sdip.sdos.utils.SDOSConstants;
import com.scania.sdip.sdos.utils.Utility;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class HelpModel {

  private static final Logger LOGGER = LogManager.getLogger(HelpModel.class);

  private static final String SPARQL_GET_TASKS =
      SDOSConstants.PREFIX_RDF
          + SDOSConstants.PREFIX_RDFS
          + "PREFIX : <" + SDOSConstants.ORCHESTRATION_PREFIX + "> \n"
          + "SELECT ?task \n"
          + "WHERE\n"
          + "{ \n"
          + "?task rdf:type :Task .\n"
          + "}";

  public HelpModel() {
    this.setRdf4jClient(new Rdf4jClient());
  }

  private Rdf4jClient rdf4jClient;
  private String label;
  private String subjectIri;

  public void setLabel(String label) { this.label = label; }

  public void setSubjectIri(String subjectIri) { this.subjectIri = subjectIri; }

  public static String getSparql() {
    return SPARQL_GET_TASKS;
  }

  public void setRdf4jClient(Rdf4jClient rdf4jClient) {
    this.rdf4jClient = rdf4jClient;
  }

  public JsonObject populate(IParameterMemory iParameterMemory, ServiceArguments serviceArguments) {

    try {
      JsonObject resultObject = new JsonObject();
      JsonArray jsonArray = new JsonArray();
      JsonArray taskJsonArray = rdf4jClient
          .selectSparqlOfg(getSparql(), iParameterMemory.getOfgModelRepo());

      for (JsonElement element : taskJsonArray) {
        JsonObject taskObject = new JsonObject();
        JsonArray parameters = new JsonArray();
        ITaskModel taskModel = getTaskModel();
        taskModel.populateOnlyTask(
            element.getAsJsonObject().get(SDOSConstants.TASK).getAsJsonObject()
                .get(SDOSConstants.VALUE).getAsString(), iParameterMemory, serviceArguments);
        setLabel(taskModel.getLabel());
        setSubjectIri(taskModel.getSubjectIri());

        taskObject.addProperty(SDOSConstants.LABEL, taskModel.getLabel());
        taskObject.addProperty(SDOSConstants.SUBJECT_IRI, taskModel.getSubjectIri());

        List<IParameterModel> iParameterModels = taskModel.getInputParameters();
        for (IParameterModel parameterModel : iParameterModels) {
          JsonObject paramObject = new JsonObject();
          paramObject.addProperty(SDOSConstants.LABEL, parameterModel.getLabel());
          paramObject.add("keyValuePairs", parameterModel.createUserInputHelp());
          parameters.add(paramObject);
        }
        taskObject.add("parameters", parameters);
        jsonArray.add(taskObject);
      }
      resultObject.add("tasks", jsonArray);
      return resultObject;
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

  public ITaskModel getTaskModel() {
    return new TaskModel();
  }

}
