package com.scania.sdip.sdos.orchestration.interfaces;

import com.google.gson.JsonArray;
import com.scania.sdip.sdos.model.ServiceArguments;

import java.util.List;
import java.util.Map;

public interface IParameterModel extends IBaseModel {

  String getSubjectIri();

  String getLabel();

  List<String> getKeys();

  Map getValue();

  JsonArray createUserInputHelp();

  /**
   * used when populating input parameters for a task. This method should query stardog, and not the
   * in-memory graph
   *
   * @param subjectIri       IRI of the Parameter
   * @param iParameterMemory parameterMemory
   * @param serviceArguments service arguments (including stardog query endpoint)
   */
  void populateHelp(String subjectIri, IParameterMemory iParameterMemory,
      ServiceArguments serviceArguments);

}
