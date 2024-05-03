package com.scania.sdos.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema()
public class OrchestrationRequestModel {

  @NotNull  @NotEmpty
  @Schema(description = "Subject IRI of wanted Task to run")
  String subjectIri;

  @NotNull @NotEmpty
  @Schema(description = "Parameters to run the first action in an orchestration flow.")
  List<OrchestrationParameterModel> parameters;

  public List<OrchestrationParameterModel> getParameters() {
    return parameters;
  }

  public void setParameters(List<OrchestrationParameterModel> parameters) {
    this.parameters = parameters;
  }

  public String getSubjectIri() {
    return subjectIri;
  }

  public void setSubjectIri(String subjectIri) {
    this.subjectIri = subjectIri;
  }


  @Override
  public String toString() {
    return "OrchestrationRequestModel{" +
        ", subjectIri='" + subjectIri + '\'' +
        ", parameters=" + parameters +
        '}';
  }
}
