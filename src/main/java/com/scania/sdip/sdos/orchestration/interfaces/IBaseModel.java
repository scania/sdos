package com.scania.sdip.sdos.orchestration.interfaces;

import com.scania.sdip.sdos.model.ServiceArguments;

public interface IBaseModel {

  /**
   * Get subject IRI of model
   *
   * @return string representing the subject IRI of model
   */
  String getSubjectIri();

  String getLabel();

  /**
   * populate model
   *
   * @param subjectIri       of subject
   * @param iParameterMemory hashmap of parameters
   * @param serviceArguments
   */
  void populate(String subjectIri, IParameterMemory iParameterMemory, ServiceArguments serviceArguments);

}
