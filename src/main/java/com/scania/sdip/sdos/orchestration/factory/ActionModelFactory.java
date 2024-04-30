package com.scania.sdip.sdos.orchestration.factory;

import com.scania.sdip.sdos.orchestration.interfaces.IActionModel;
import com.scania.sdip.sdos.orchestration.interfaces.IParameterMemory;
import com.scania.sdip.sdos.orchestration.model.HttpActionModel;
import com.scania.sdip.sdos.orchestration.model.QueryActionModel;
import com.scania.sdip.sdos.orchestration.model.ResultActionAsyncModel;
import com.scania.sdip.sdos.orchestration.model.ResultActionSyncModel;
import com.scania.sdip.sdos.orchestration.model.ScriptActionModel;
import com.scania.sdip.sdos.orchestration.model.SoapActionModel;
import com.scania.sdip.sdos.orchestration.model.SparqlConvertActionModel;
import com.scania.sdip.sdos.orchestration.model.VirtualGraphActionModel;
import com.scania.sdip.sdos.utils.SDOSConstants;
import org.apache.commons.lang3.NotImplementedException;


public class ActionModelFactory {

  private ActionModelFactory(){
    //default constructor
  }

  public static IActionModel getAction(String requestType, IParameterMemory parameterMemory) {

    IActionModel actionModel = null;
    if (requestType.contains(SDOSConstants.SOAP_ACTION)) {
      actionModel = new SoapActionModel();
    } else if (requestType.contains(SDOSConstants.HTTP_ACTION)) {
      actionModel = new HttpActionModel();
    } else if (requestType.contains(SDOSConstants.SCRIPT_ACTION)) {
      actionModel = new ScriptActionModel();
    } else if (requestType.contains(SDOSConstants.RESULT_ACTION)) {
      actionModel = getResultActionModel(parameterMemory);
    } else if (requestType.contains(SDOSConstants.VIRTUAL_GRAPH_ACTION)) {
      actionModel = new VirtualGraphActionModel();
    } else if (requestType.contains(SDOSConstants.SPARQL_CONVERT_ACTION)) {
      actionModel = new SparqlConvertActionModel();
    } else if (requestType.contains(SDOSConstants.QUERY_ACTION)) {
      actionModel = new QueryActionModel();
    } else {
      actionModel = null;
    }

    return actionModel;
  }

  /**
   * Get correct ResultAction type depending on setting "SYNC" in parameter memory
   *
   * @param parameterMemory parameter memory
   * @return the correct ResultAction derivative
   */
  private static IActionModel getResultActionModel(IParameterMemory parameterMemory) {
    // Sync resultAction
    if (parameterMemory.getValue(SDOSConstants.O_RESULT).get(SDOSConstants.SYNC).get(0)
        .equals("True")) {
      return new ResultActionSyncModel();
    }
    // Async resultAction
    if (parameterMemory.getValue(SDOSConstants.O_RESULT).get(SDOSConstants.SYNC).get(0)
        .equals("False")) {
      return new ResultActionAsyncModel();
    }
    throw new NotImplementedException("ResultAction type not implemented");
  }
}