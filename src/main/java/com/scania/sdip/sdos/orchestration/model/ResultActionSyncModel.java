package com.scania.sdip.sdos.orchestration.model;

import com.scania.sdip.exceptions.IncidentException;
import com.scania.sdip.exceptions.SdipErrorCode;
import com.scania.sdip.exceptions.SdipErrorParameter;
import com.scania.sdip.sdos.model.ServiceArguments;
import com.scania.sdip.sdos.orchestration.interfaces.IParameterMemory;
import com.scania.sdip.sdos.orchestration.interfaces.IParameterModel;
import com.scania.sdip.sdos.utils.SDOSConstants;
import com.scania.sdip.sdos.utils.Utility;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ResultActionSyncModel extends ResultActionModel {

  private static final Logger LOGGER = LogManager.getLogger(ResultActionSyncModel.class);

  @Override
  public void run(IParameterMemory iParameterMemory, JsonLdContextModel context,
      ServiceArguments serviceArguments) {
    try {
      // only handle conversions created by flow.
      // If action doesn't have any inputParameter then it's static data in httpBody
      //  (i.e. ontology or other data already in RDF), and not handled here.
      if (getInputParameter() != null && getInputParameter().size() != 0) {
        for (IParameterModel inputParameterModel : inputParameter) {
          HashMap<String, List<String>> memoryValue = iParameterMemory
              .getValue(inputParameterModel.getSubjectIri());

          if (memoryValue != null && !memoryValue.isEmpty()) {
            Integer iterations = Utility.getActionIteration(memoryValue, inputParameterModel);
            mapActionIteration(iterations,inputParameterModel,memoryValue,iParameterMemory);
          }

        }
      }
    } catch (Exception exception) {
      throw new IncidentException(exception, SdipErrorCode.UNKNOWN_REASON_ERROR, LOGGER,
          exception.getMessage(), SdipErrorParameter.SUPPORTMAIL);
    }
  }

  private void mapActionIteration(Integer iterations,IParameterModel inputParameterModel,HashMap<String, List<String>> memoryValue,IParameterMemory iParameterMemory){
    for (int i = 0; i < iterations; i++) {
      String requestBody = getHttpbody();
      for (String key : inputParameterModel.getKeys()) {
        if (key.equals(SDOSConstants.HTTPBODY)) {
          requestBody = memoryValue.get(SDOSConstants.HTTPBODY).get(i);
        }
      }
      // -  save requestBody in sync result
      HashMap<String, List<String>> outputMemoryValue = new HashMap<>();
      outputMemoryValue
              .put(SDOSConstants.GRAPH, Collections.singletonList(requestBody));
      iParameterMemory.putParameter(SDOSConstants.SYNC_RESULT, outputMemoryValue);
    }
  }
}
