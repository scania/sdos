package com.scania.sdip.sdos.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Schema
public class GetAllAvailableTasksRequest {

  @NotNull @NotEmpty
  @Schema(description = "Orchestration flow sparql endpoint") //TODO REPLACE WITH DOMAINID?
  String sparqlEndpoint;


  @Override
  public String toString() {
    return "GetAllAvailableTasksRequest{" +
        "sparqlEndpoint='" + sparqlEndpoint + '\'' +
        '}';
  }

  public String getSparqlEndpoint() {
    return sparqlEndpoint;
  }

  public void setSparqlEndpoint(String sparqlEndpoint) {
    this.sparqlEndpoint = sparqlEndpoint;
  }

}
