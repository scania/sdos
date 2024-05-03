package com.scania.sdos.services;


import static com.scania.sdos.utils.SDOSConstants.RESULTGRAPH;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.JsonObject;
import com.scania.sdip.exceptions.IncidentException;
import com.scania.sdip.exceptions.SdipErrorCode;
import com.scania.sdos.jwt.JwtTokenFilter;
import com.scania.sdos.model.OrchestrationParameterKeyValModel;
import com.scania.sdos.model.OrchestrationParameterModel;
import com.scania.sdos.model.OrchestrationRequestModel;
import com.scania.sdos.model.ServiceArguments;
import com.scania.sdos.orchestration.ActionRunner;
import com.scania.sdos.orchestration.OfgModelRepo;
import com.scania.sdos.orchestration.ParameterMemory;
import com.scania.sdos.orchestration.Rdf4jClient;
import com.scania.sdos.orchestration.factory.HelpModelFactory;
import com.scania.sdos.orchestration.factory.MetaDataModelFactory;
import com.scania.sdos.orchestration.factory.OfgModelRepoFactory;
import com.scania.sdos.orchestration.interfaces.IParameterMemory;
import com.scania.sdos.orchestration.interfaces.ITaskModel;
import com.scania.sdos.orchestration.model.HelpModel;
import com.scania.sdos.orchestration.model.JsonLdContextModel;
import com.scania.sdos.orchestration.model.MetaDataModel;
import com.scania.sdos.orchestration.model.ResultMetaDataModel;
import com.scania.sdos.orchestration.model.TaskModel;
import com.scania.sdos.utils.SDOSConstants;
import com.scania.sdos.utils.StateEnum;
import com.scania.sdos.utils.Utility;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.rdf4j.model.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


/**
 * This is the layer between RestController and backend services
 */

@Service
public class ControllerService {

  private static final Logger logger = LogManager.getLogger(ControllerService.class);
  private ObjectMapper objectMapper = new ObjectMapper();
  private ServiceArguments serviceArguments;
  private Rdf4jClient rdf4jClient;

  private JwtTokenFilter jwtTokenFilter;

  @Autowired
  public ControllerService(ServiceArguments serviceArguments, JwtTokenFilter jwtTokenFilter) {
    this.serviceArguments = serviceArguments;
    this.rdf4jClient = new Rdf4jClient();
    this.jwtTokenFilter = jwtTokenFilter;
  }

  public ControllerService() {
  }

  public void setRdf4jClient(Rdf4jClient rdf4jClient) {
    this.rdf4jClient = rdf4jClient;
  }

  public void setServiceArguments(ServiceArguments serviceArguments) {
    this.serviceArguments = serviceArguments;
  }

  public void setJwtTokenFilter(JwtTokenFilter jwtTokenFilter) {
    this.jwtTokenFilter = jwtTokenFilter;
  }

  public ActionRunner getActionRunner() {
    return new ActionRunner();
  }

  public TaskModel getTaskModel() {
    return new TaskModel();
  }


  /**
   * runs the orchestration, builds the taskmodel and also runs the model after completion
   *
   * @param params user input model
   * @return a string with information of the executed task.
   */

  @Async
  public void handleOrchestration(OrchestrationRequestModel params,
                                  ResultMetaDataModel resultMetaDataModel, ParameterMemory iParameterMemory) {
    doHandleOrchestration(params, resultMetaDataModel, iParameterMemory);
  }

  public void doHandleOrchestration(OrchestrationRequestModel params,
      ResultMetaDataModel resultMetaDataModel,
      ParameterMemory iParameterMemory) {
    logger.info("Starting Orchestration Async Method for {}", resultMetaDataModel.getResultgraph());
    JsonLdContextModel jsonLdContextModel = Utility.getStaticJsonLdContextModel();
    initOrchestrationExecution(resultMetaDataModel, iParameterMemory, jsonLdContextModel);
    logger.info("Orchestration Initialization complete for {}",
        resultMetaDataModel.getResultgraph());
    OfgModelRepo ofgModelRepo = OfgModelRepoFactory.getModelRepo();
    // get OFG and put it in an in-memory repo
    Model ofgModel = getOrchestrationFlowGraphAsModel(params.getSubjectIri(), iParameterMemory);
    ofgModelRepo.addModel(ofgModel);
    iParameterMemory.setOfgModelRepo(ofgModelRepo);

    try {
      populateAndRunTaskModel(params, resultMetaDataModel, iParameterMemory);

      resultMetaDataModel.setState(StateEnum.COMPLETE.toString());
      resultMetaDataModel.run(iParameterMemory, jsonLdContextModel, serviceArguments);
    } catch (IncidentException incidentException) {
      resultMetaDataModel.setState(StateEnum.FAILED.toString());
      resultMetaDataModel.run(iParameterMemory, jsonLdContextModel, serviceArguments);
      incidentException.setExecutionId(iParameterMemory.
          getValue(SDOSConstants.EXECUTION_REPORT).get(SDOSConstants.ID).get(0));
      throw incidentException;
    } finally {
      // remove in-memory repo for this task
      ofgModelRepo.removeModel();
      iParameterMemory.clear();
    }
  }

  private ITaskModel populateAndRunTaskModel(OrchestrationRequestModel params,
                                             ResultMetaDataModel resultMetaDataModel,
                                             ParameterMemory iParameterMemory) {
    TaskModel taskModel = getTaskModel();
    MetaDataModel metaDataModel = taskModel.getMetaDataModel();

    logger.info("Populating TaskModel for {}", resultMetaDataModel.getResultgraph());
    taskModel.populate(params.getSubjectIri(), getParametersAsHashmap(params.getParameters()),
        iParameterMemory, serviceArguments);
    logger.info("Populating TaskModel Complete  for " + resultMetaDataModel.getResultgraph());

    metaDataModel.run(iParameterMemory, taskModel.getContext(), serviceArguments);
    ActionRunner actionRunner = getActionRunner();
    actionRunner
        .run(taskModel.getNextAction(), iParameterMemory, taskModel.getContext(),
            serviceArguments);
    return taskModel;
  }


  /***
   * Creates a ResultMetaDataModel
   *
   * @return a constructed ResultMetaDataModel with resultgraph name and timestamp
   */
  public ResultMetaDataModel createResultMetaDataModel(ParameterMemory iParameterMemory) {
    ResultMetaDataModel resultMetaDataModel = MetaDataModelFactory.getModel();
    resultMetaDataModel.populate(resultMetaDataModel.getSubjectIri(), iParameterMemory,
        serviceArguments);

    HashMap<String, List<String>> map = new HashMap<>();
    map.put(SDOSConstants.ID, Collections.singletonList(resultMetaDataModel.getResultgraph()));
    iParameterMemory.putParameter(SDOSConstants.EXECUTION_REPORT, map);
    return resultMetaDataModel;
  }

  /***
   *
   * @param resultMetaDataModel
   * @return a Json string representing the resultgraph name
   */
  public String getResultGraphNameAsJson(ResultMetaDataModel resultMetaDataModel) {
    try {
      ObjectNode json = objectMapper.createObjectNode();
      json.put(RESULTGRAPH, resultMetaDataModel.getResultgraph());
      return objectMapper.writeValueAsString(json);
    } catch (JsonProcessingException jpe) {
      throw new IncidentException(jpe, SdipErrorCode.EMPTY_DOCUMENT, logger);
    }
  }

  /***
   * Initializaes an Orchestration
   *
   * @param resultMetaDataModel
   */
  private void initOrchestrationExecution(ResultMetaDataModel resultMetaDataModel,
      ParameterMemory iParameterMemory,
      JsonLdContextModel jsonLdContextModel) {
    HashMap<String, List<String>> result = new HashMap<>();
    result.put(SDOSConstants.GRAPH,
        Collections.singletonList(resultMetaDataModel.getResultgraph()));
    result.put(SDOSConstants.SYNC, Collections.singletonList("False"));
    iParameterMemory.putParameter(SDOSConstants.O_RESULT, result);
    resultMetaDataModel.run(iParameterMemory, jsonLdContextModel, serviceArguments);
  }


  private void initOrchestrationExecutionSync(ResultMetaDataModel resultMetaDataModel,
      ParameterMemory iParameterMemory) {
    HashMap<String, List<String>> result = new HashMap<>();
    result.put(SDOSConstants.GRAPH, Collections.singletonList(""));
    result.put(SDOSConstants.SYNC, Collections.singletonList("True"));
    iParameterMemory.putParameter(SDOSConstants.O_RESULT, result);
  }

  public JsonObject handleAllTasks(ParameterMemory iParameterMemory) {
    try {
      Model ofgModel = getAllTaskAsModel(iParameterMemory);
      OfgModelRepo ofgModelRepo = OfgModelRepoFactory.getModelRepo();
      ofgModelRepo.addModel(ofgModel);
      iParameterMemory.setOfgModelRepo(ofgModelRepo);

      HelpModel helpModel = HelpModelFactory.getInstance();
      JsonObject queryList = helpModel.populate(iParameterMemory, serviceArguments);

      return queryList;
    } finally {
      iParameterMemory.clear();
    }
  }


  private HashMap<String, HashMap<String, String>> getParametersAsHashmap(
      List<OrchestrationParameterModel> modelList) {
    HashMap<String, HashMap<String, String>> hashMap = new HashMap<>();
    for (OrchestrationParameterModel paramModel : modelList) {
      HashMap<String, String> keyVal = new HashMap<>();
      String label = paramModel.getLabel();
      for (OrchestrationParameterKeyValModel keyValModel : paramModel.getKeyValuePairs()) {
        keyVal.put(keyValModel.getKey(), keyValModel.getValue());
      }
      hashMap.put(label, keyVal);

    }
    return hashMap;
  }

  public Model handleOrchestrationSync(OrchestrationRequestModel params,
      ResultMetaDataModel resultMetaDataModel,
      ParameterMemory iParameterMemory) {

    ITaskModel taskModel;
    logger.info("Starting Orchestration Sync Method for {}", resultMetaDataModel.getResultgraph());
    initOrchestrationExecutionSync(resultMetaDataModel, iParameterMemory);
    logger.info(
        "Orchestration Initialization complete for " + resultMetaDataModel.getResultgraph());
    // get OFG and put it in an in-memory repo
    Model ofgModel = getOrchestrationFlowGraphAsModel(params.getSubjectIri(), iParameterMemory);
    OfgModelRepo ofgModelRepo = OfgModelRepoFactory.getModelRepo();
    ofgModelRepo.addModel(ofgModel);
    iParameterMemory.setOfgModelRepo(ofgModelRepo);

    Model result = null;
    try {
      // TODO: Check that only one data source is used in the flow! (e.g. one Http/Soap/VirtualAction)
      // TODO: Check that only one resultAction is in flow!
      taskModel = populateAndRunTaskModel(params, resultMetaDataModel, iParameterMemory);

      if (taskModel != null) {
        // get result from resultAction saved in parameterMemory, and convert to RDF4J Model
        String orchestrationResultJsonLd = iParameterMemory
            .getValue(SDOSConstants.SYNC_RESULT).get(SDOSConstants.GRAPH).get(0);
        result = Utility.getJsonldData(orchestrationResultJsonLd);
      }
    } catch (IncidentException incidentException) {
      incidentException.setExecutionId(iParameterMemory.
          getValue(SDOSConstants.EXECUTION_REPORT).get(SDOSConstants.ID).get(0));
      throw incidentException;
    } finally {
      // remove in-memory repo for this task
      ofgModelRepo.removeModel();
      iParameterMemory.clear();
    }

    return result;
  }

  /**
   * Get the Orchestration Flow Graph as an RDF(4J) Model object for given Task
   *
   * @param taskSubjectIri Subject IRI of Task
   * @return OFG in RDF Model format
   */
  public Model getOrchestrationFlowGraphAsModel(String taskSubjectIri,
      IParameterMemory iParameterMemory) {
    String ofg = SDOSConstants.PREFIX_RDF
        + "PREFIX : <" + SDOSConstants.ORCHESTRATION_PREFIX + "> \n"
        + "CONSTRUCT {\n"
        + "    ?s ?p ?o\n"
        + "}\n"
        + "{\n"
        + "    BIND(<"
        + taskSubjectIri
        + "> AS ?taskIri)\n"
        + "    GRAPH ?GraphName {\n"
        + "        ?taskIri rdf:type :Task .\n"
        + "        ?s ?p ?o\n"
        + "    }\n"
        + "}";

    String ofgString = rdf4jClient.doConstructSparql(ofg,
        serviceArguments.getStardogQueryEndpoint(),
        iParameterMemory);
    Model resultOfg = Utility.getJsonldData(ofgString);
    return resultOfg;
  }

  public Model getAllTaskAsModel(IParameterMemory iParameterMemory) {
    String queryAllTask = SDOSConstants.GETALLTASKS_QUERY;
    String allTask = rdf4jClient.doConstructSparql(queryAllTask,
        serviceArguments.getStardogQueryEndpoint(),
        iParameterMemory);
    Model resultAllTask = Utility.getJsonldData(allTask);
    return resultAllTask;
  }

  public void validateAuthToken(String token, ParameterMemory parameterMemory) {
    if (jwtTokenFilter.validateJwtToken(token)) {
      jwtTokenFilter.jwtProcess(token, true, parameterMemory);
    }
  }

}