package com.scania.sdos.orchestration.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.scania.sdos.model.ServiceArguments;
import com.scania.sdos.orchestration.factory.ActionModelFactory;
import com.scania.sdos.orchestration.factory.ConnectorModelFactory;
import com.scania.sdos.orchestration.factory.ParameterModelFactory;
import com.scania.sdos.orchestration.interfaces.IActionModel;
import com.scania.sdos.orchestration.interfaces.IConnectorModel;
import com.scania.sdos.orchestration.interfaces.IParameterMemory;
import com.scania.sdos.orchestration.interfaces.IParameterModel;
import com.scania.sdos.utils.SDOSConstants;

import java.util.ArrayList;
import java.util.List;

public abstract class ActionModel implements IActionModel {

  protected String subjectIri;
  protected String label;
  protected IActionModel nextAction;

  protected List<IParameterModel> inputParameter = new ArrayList();
  protected List<IParameterModel> outputParameter = new ArrayList();

  public List<IParameterModel> getInputParameter() {
    return inputParameter;
  }

  public void setInputParameter(
      IParameterModel inputParameter) {
    this.inputParameter.add(inputParameter);
  }

  public List<IParameterModel> getOutputParameter() {
    return outputParameter;
  }

  public void setOutputParameter(
      IParameterModel outputParameter) {
    this.outputParameter.add(outputParameter);
  }

  public void setSubjectIri(String subjectIri) {
    this.subjectIri = subjectIri;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public void setNextAction(IActionModel nextAction) {
    this.nextAction = nextAction;
  }

  @Override

  public IActionModel getNextAction() {
    return nextAction;
  }

  @Override
  public String getSubjectIri() {
    return this.subjectIri;
  }

  @Override
  public String getLabel() {
    return this.label;
  }


  protected String getSparql(String subject, String sparql) {
    return sparql.replace(SDOSConstants.VARIABLE, subject);
  }

  /**
   * Populate and set nextAction if it exists
   *
   * @param iParameterMemory     Internal parameter memory
   * @param serviceArguments     The given service arguments
   * @param actionModelJsonArray json array containing the Connector from the sparql-response
   */
  protected void populateNextAction(IParameterMemory iParameterMemory,
                                    ServiceArguments serviceArguments,
                                    JsonArray actionModelJsonArray) {
    if (actionModelJsonArray.get(0).getAsJsonObject().has(SDOSConstants.NEXT_ACTION_TYPE) &&
        actionModelJsonArray.get(0).getAsJsonObject().has(SDOSConstants.NEXT_ACTION)) {
      IActionModel iActionModel = ActionModelFactory.getAction(
          actionModelJsonArray.get(0).getAsJsonObject().get(SDOSConstants.NEXT_ACTION_TYPE)
              .getAsJsonObject().get(SDOSConstants.VALUE)
              .getAsString(), iParameterMemory);
      iActionModel.populate(
          actionModelJsonArray.get(0).getAsJsonObject().get(SDOSConstants.NEXT_ACTION)
              .getAsJsonObject()
              .get(SDOSConstants.VALUE)
              .getAsString(), iParameterMemory, serviceArguments);
      setNextAction(iActionModel);
    }
  }

  /**
   * Populate and set outputParameter if it exists
   *
   * @param iParameterMemory   Internal parameter memory
   * @param serviceArguments   The given service arguments
   * @param parameterJsonArray json element containing the parameter from the sparql-response
   */
  protected void populateOutputParameter(IParameterMemory iParameterMemory,
      ServiceArguments serviceArguments,
      JsonArray parameterJsonArray) {
    for (JsonElement parameterJsonElement : parameterJsonArray) {
      if (parameterJsonElement.getAsJsonObject().has(SDOSConstants.OUTPUT_PARAMETER_TYPE)
          && parameterJsonElement.getAsJsonObject().has(SDOSConstants.OUTPUT_PARAMETER)) {
        IParameterModel outputparameterModel = createPopulateParameterModel(
            iParameterMemory, serviceArguments, SDOSConstants.OUTPUT_PARAMETER_TYPE,
            SDOSConstants.OUTPUT_PARAMETER, parameterJsonElement
        );
        setOutputParameter(outputparameterModel);
      }
    }
  }

  /**
   * Populate and set inputParameter if it exists
   *
   * @param iParameterMemory   Internal parameter memory
   * @param serviceArguments   The given service arguments
   * @param parameterJsonArray json element containing the parameter from the sparql-response
   */
  protected void populateInputParameter(IParameterMemory iParameterMemory,
      ServiceArguments serviceArguments,
      JsonArray parameterJsonArray) {
    for (JsonElement parameterJsonElement : parameterJsonArray) {

      if (parameterJsonElement.getAsJsonObject().has(SDOSConstants.INPUT_PARAMETER_TYPE)
          && parameterJsonElement.getAsJsonObject().has(SDOSConstants.INPUT_PARAMETER)) {

        IParameterModel inputparameterModel = createPopulateParameterModel(iParameterMemory,
            serviceArguments,
            SDOSConstants.INPUT_PARAMETER_TYPE, SDOSConstants.INPUT_PARAMETER, parameterJsonElement
        );
        setInputParameter(inputparameterModel);
      }
    }
  }

  /**
   * Generic creation and populating a StandardParameterModel
   *
   * @param iParameterMemory     Internal parameter memory
   * @param serviceArguments     The given service arguments
   * @param parameterType        Type of the parameter in the json structure (e.g.
   *                             "inputparametertype" or "outputparametertype")
   * @param parameterName        The key of the parameter in the json structure (e.g.
   *                             "inputparameter" or "outputparameter")
   * @param parameterJsonElement json element containing the parameter from the sparql-response
   * @return populated IParameterModel
   */
  protected IParameterModel createPopulateParameterModel(IParameterMemory iParameterMemory,
      ServiceArguments serviceArguments, String parameterType, String parameterName,
      JsonElement parameterJsonElement) {
    IParameterModel parameterModel = ParameterModelFactory.getParameter(
        parameterJsonElement.getAsJsonObject().get(parameterType).getAsJsonObject()
            .get(SDOSConstants.VALUE)
            .getAsString());

    parameterModel.populate(
        parameterJsonElement.getAsJsonObject().get(parameterName).getAsJsonObject()
            .get(SDOSConstants.VALUE)
            .getAsString(), iParameterMemory, serviceArguments);
    return parameterModel;
  }

  /**
   * Generic creation and populating a IConnectorModel
   *
   * @param iParameterMemory     Internal parameter memory
   * @param serviceArguments     The given service arguments
   * @param actionModelJsonArray json array containing the Connector from the sparql-response
   * @return Populated IConnectorModel
   */
  protected IConnectorModel createPopulateConnector(IParameterMemory iParameterMemory,
                                                    ServiceArguments serviceArguments,
                                                    JsonArray actionModelJsonArray,
                                                    String connectorType) {
    IConnectorModel iConnectorModel = ConnectorModelFactory
        .getConnector(connectorType);
    iConnectorModel.populate(
        actionModelJsonArray.get(0).getAsJsonObject().get(SDOSConstants.HAS_CONNECTOR)
            .getAsJsonObject()
            .get(SDOSConstants.VALUE)
            .getAsString(), iParameterMemory, serviceArguments);
    return iConnectorModel;
  }
}