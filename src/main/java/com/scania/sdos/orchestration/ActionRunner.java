package com.scania.sdos.orchestration;

import com.scania.sdip.exceptions.IncidentException;
import com.scania.sdos.model.ServiceArguments;
import com.scania.sdos.orchestration.interfaces.IActionModel;
import com.scania.sdos.orchestration.interfaces.IParameterMemory;
import com.scania.sdos.orchestration.model.JsonLdContextModel;
import com.scania.sdos.utils.SDOSConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Class that iterate the TaskModels run functions executing the orchestration flow
 */
public class ActionRunner {

  private static final Logger logger = LogManager.getLogger(ActionRunner.class);

  public void run(IActionModel action, IParameterMemory iParameterMemory,
                  JsonLdContextModel context,
                  ServiceArguments serviceArguments) {
    try {
      String id = iParameterMemory.
          getValue(SDOSConstants.EXECUTION_REPORT).get(SDOSConstants.ID).get(0);
      logger.info(id + "-Running Action " + action.getLabel() + ", " + action.getSubjectIri());
      action.run(iParameterMemory, context, serviceArguments);
      logger.info(id + "-Completed Action " + action.getLabel() + ", " + action.getSubjectIri());
    } catch (IncidentException e) {
      e.setActionDetails(action.getLabel() + ", " + action.getSubjectIri());
      throw e;
    }
    if (action.getNextAction() != null) {
      run(action.getNextAction(), iParameterMemory, context, serviceArguments);
    }
  }
}
