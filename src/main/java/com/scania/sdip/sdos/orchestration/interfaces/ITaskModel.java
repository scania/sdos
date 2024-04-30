package com.scania.sdip.sdos.orchestration.interfaces;

import com.scania.sdip.sdos.model.ServiceArguments;
import com.scania.sdip.sdos.orchestration.model.JsonLdContextModel;
import java.util.HashMap;
import java.util.List;

public interface ITaskModel {

  JsonLdContextModel getContext();

  String getLabel();

  IActionModel getNextAction();

  /**
   * Get subject IRI of model
   *
   * @return string representing the subject IRI of model
   */
  String getSubjectIri();

  List<IParameterModel> getInputParameters();

  /**
   * populate model
   *
   * @param iParameterMemory hashmap of parameters
   */
  void populate(String subjectIri, HashMap<String, HashMap<String, String>> userInput,
                IParameterMemory iParameterMemory, ServiceArguments serviceArguments);


  void populateOnlyTask(String subjectIri, IParameterMemory iParameterMemory,
                        ServiceArguments serviceArguments);

}