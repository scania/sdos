package com.scania.sdos.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Schema()
public class OrchestrationParameterKeyValModel {

  private static final Logger LOGGER = LogManager
      .getLogger(OrchestrationParameterKeyValModel.class);

  @NotNull  @NotEmpty
  String key;
  @NotNull @NotEmpty
  String value;

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return "OrchestrationParameterKeyValModel{" +
        "key='" + key + '\'' +
        ", value='" + value + '\'' +
        '}';
  }
}
