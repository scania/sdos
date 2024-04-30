package com.scania.sdip.sdos.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema()
public class OrchestrationParameterModel {

  @NotNull  @NotEmpty
  String label;
  @NotNull @NotEmpty
  List<OrchestrationParameterKeyValModel> keyValuePairs;

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public List<OrchestrationParameterKeyValModel> getKeyValuePairs() {
    return keyValuePairs;
  }

  public void setKeyValuePairs(
      List<OrchestrationParameterKeyValModel> keyValuePairs) {
    this.keyValuePairs = keyValuePairs;
  }

  @Override
  public String toString() {
    return "OrchestrationParameterModel{" +
        "label='" + label + '\'' +
        ", keyValuePairs=" + keyValuePairs +
        '}';
  }
}
