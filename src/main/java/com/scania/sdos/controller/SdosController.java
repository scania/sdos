package com.scania.sdos.controller;

import static com.scania.sdos.utils.Utility.modelToJsonLd;
import static com.scania.sdos.utils.Utility.modelToN3;
import static com.scania.sdos.utils.Utility.modelToNQuads;
import static com.scania.sdos.utils.Utility.modelToNTriples;
import static com.scania.sdos.utils.Utility.modelToRdfXml;
import static com.scania.sdos.utils.Utility.modelToTurtle;

import com.google.gson.JsonObject;
import com.scania.sdos.model.OrchestrationRequestModel;
import com.scania.sdos.orchestration.ParameterMemory;
import com.scania.sdos.orchestration.model.ResultMetaDataModel;
import com.scania.sdos.services.ControllerService;
import com.scania.sdos.utils.SDOSConstants;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.rdf4j.model.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
public class SdosController {

  private static final Logger LOGGER = LogManager.getLogger(SdosController.class);
  private ControllerService controllerService;

  @Autowired
  public SdosController(ControllerService controllerService) {
    this.controllerService = controllerService;
  }

  public SdosController() {
  }

  public void setControllerService(ControllerService controllerService) {
    this.controllerService = controllerService;
  }

  @GetMapping(value = "/getAllAvailableTasks")
  @Operation(summary = "Returns all available tasks and their parameters.")
  @ResponseBody
  public ResponseEntity<String> getTasks(
      @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = true) String token) {
    ParameterMemory parameterMemory = ParameterMemory.getParameterMemoryObject();
    controllerService.validateAuthToken(token, parameterMemory);
    JsonObject queryList = controllerService.handleAllTasks(parameterMemory);
    return new ResponseEntity<>(queryList.toString(), HttpStatus.OK);
  }


  @PostMapping(value = "/runOrchestration")
  @Operation(summary = "run a Orchestration flow graph.")
  @ResponseBody
  public ResponseEntity<String> callOrchestration(
      @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = true) String token,
      @RequestBody() @Valid OrchestrationRequestModel param) {
    ParameterMemory parameterMemory = ParameterMemory.getParameterMemoryObject();

    controllerService.validateAuthToken(token, parameterMemory);
    ResultMetaDataModel resultMetaDataModel = controllerService.createResultMetaDataModel(
        parameterMemory);
    controllerService.handleOrchestration(param, resultMetaDataModel, parameterMemory);
    return new ResponseEntity<>(controllerService.getResultGraphNameAsJson(resultMetaDataModel),
        HttpStatus.OK);


  }


  /**
   * Put request on context path for API gateway
   *
   * @return SUCCESSFUL Response
   */
  @Operation(hidden = true)
  @PutMapping(value = "/")
  public ResponseEntity<String> apiGateway() {
    LOGGER.debug("Received request from API Gateway.");
    return ResponseEntity.status(HttpStatus.OK).body(SDOSConstants.SUCCESSFUL);
  }

  @PostMapping(value = "/runOrchestrationSync", produces = {"application/ld+json"})
  @Operation(summary = "run a Orchestration flow graph synchronous and get result in response.")
  @ResponseBody
  public ResponseEntity<String> callOrchestrationSyncAsJsonLd(
      @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = true) String token,
      @RequestBody() @Valid OrchestrationRequestModel param) {
    ParameterMemory parameterMemory = ParameterMemory.getParameterMemoryObject();
    controllerService.validateAuthToken(token, parameterMemory);
    ResultMetaDataModel resultMetaDataModel = controllerService.createResultMetaDataModel(
        parameterMemory);
    Model result = controllerService.handleOrchestrationSync(param, resultMetaDataModel,
        parameterMemory);
    return new ResponseEntity<>(modelToJsonLd(result), HttpStatus.OK);
  }

  @PostMapping(value = "/runOrchestrationSync", produces = {"application/rdf+xml",
      "application/xml", "text/xml"})
  @Operation(summary = "run a Orchestration flow graph synchronous and get result in response.")
  @ResponseBody
  public ResponseEntity<String> callOrchestrationSyncAsRdfXml(
      @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = true) String token,
      @RequestBody() @Valid OrchestrationRequestModel param) {
    ParameterMemory parameterMemory = ParameterMemory.getParameterMemoryObject();
    controllerService.validateAuthToken(token, parameterMemory);
    ResultMetaDataModel resultMetaDataModel = controllerService.createResultMetaDataModel(
        parameterMemory);
    Model result = controllerService.handleOrchestrationSync(param, resultMetaDataModel,
        parameterMemory);

    return new ResponseEntity<>(modelToRdfXml(result), HttpStatus.OK);
  }

  @PostMapping(value = "/runOrchestrationSync", produces = {"application/n-triples", "text/plain"})
  @Operation(summary = "run a Orchestration flow graph synchronous and get result in response.")
  @ResponseBody
  public ResponseEntity<String> callOrchestrationSyncAsNTriples(
      @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = true) String token,
      @RequestBody() @Valid OrchestrationRequestModel param) {
    ParameterMemory parameterMemory = ParameterMemory.getParameterMemoryObject();
    controllerService.validateAuthToken(token, parameterMemory);
    ResultMetaDataModel resultMetaDataModel = controllerService.createResultMetaDataModel(
        parameterMemory);
    Model result = controllerService.handleOrchestrationSync(param, resultMetaDataModel,
        parameterMemory);

    return new ResponseEntity<>(modelToNTriples(result), HttpStatus.OK);
  }

  @PostMapping(value = "/runOrchestrationSync", produces = {"text/turtle", "application/x-turtle"})
  @Operation(summary = "run a Orchestration flow graph synchronous and get result in response.")
  @ResponseBody
  public ResponseEntity<String> callOrchestrationSyncAsTurtle(
      @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = true) String token,
      @RequestBody() @Valid OrchestrationRequestModel param) {
    ParameterMemory parameterMemory = ParameterMemory.getParameterMemoryObject();
    controllerService.validateAuthToken(token, parameterMemory);
    ResultMetaDataModel resultMetaDataModel = controllerService.createResultMetaDataModel(
        parameterMemory);
    Model result = controllerService.handleOrchestrationSync(param, resultMetaDataModel,
        parameterMemory);

    return new ResponseEntity<>(modelToTurtle(result), HttpStatus.OK);
  }

  @PostMapping(value = "/runOrchestrationSync", produces = {"text/n3", "text/rdf+n3"})
  @Operation(summary = "run a Orchestration flow graph synchronous and get result in response.")
  @ResponseBody
  public ResponseEntity<String> callOrchestrationSyncAsN3(
      @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = true) String token,
      @RequestBody() @Valid OrchestrationRequestModel param) {
    ParameterMemory parameterMemory = ParameterMemory.getParameterMemoryObject();
    controllerService.validateAuthToken(token, parameterMemory);
    ResultMetaDataModel resultMetaDataModel = controllerService.createResultMetaDataModel(
        parameterMemory);
    Model result = controllerService.handleOrchestrationSync(param, resultMetaDataModel,
        parameterMemory);

    return new ResponseEntity<>(modelToN3(result), HttpStatus.OK);
  }

  @PostMapping(value = "/runOrchestrationSync", produces = {"application/n-quads", "text/x-nquads",
      "text/nquads"})
  @Operation(summary = "run a Orchestration flow graph synchronous and get result in response.")
  @ResponseBody
  public ResponseEntity<String> callOrchestrationSyncAsNQuads(
      @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = true) String token,
      @RequestBody() @Valid OrchestrationRequestModel param) {
    ParameterMemory parameterMemory = ParameterMemory.getParameterMemoryObject();
    controllerService.validateAuthToken(token, parameterMemory);
    ResultMetaDataModel resultMetaDataModel = controllerService.createResultMetaDataModel(
        parameterMemory);
    Model result = controllerService.handleOrchestrationSync(param, resultMetaDataModel,
        parameterMemory);

    return new ResponseEntity<>(modelToNQuads(result), HttpStatus.OK);
  }
}