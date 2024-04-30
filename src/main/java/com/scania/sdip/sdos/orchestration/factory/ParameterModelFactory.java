package com.scania.sdip.sdos.orchestration.factory;

import com.scania.sdip.sdos.orchestration.interfaces.IParameterModel;
import com.scania.sdip.sdos.orchestration.model.BasicCredentialsParameterModel;
import com.scania.sdip.sdos.orchestration.model.HttpParameterModel;
import com.scania.sdip.sdos.orchestration.model.StandardParameterModel;
import com.scania.sdip.sdos.orchestration.model.TokenCredentialsParameterModel;
import com.scania.sdip.sdos.orchestration.model.SparqlQueryParameterModel;
import com.scania.sdip.sdos.utils.SDOSConstants;

public class ParameterModelFactory {

  private ParameterModelFactory(){
    //default constructor
  }
  public static IParameterModel getParameter(String connectorType) {

    IParameterModel parameterModel = null;
    if (connectorType.contains(SDOSConstants.HTTPPARAMETER)) {
      parameterModel = new HttpParameterModel();
    } else if (connectorType.contains(SDOSConstants.STANDARDPARAMETER)) {
      parameterModel = new StandardParameterModel();
    } else if (connectorType.contains(SDOSConstants.BASICCREDENTIALSPARAMETER)) {
      parameterModel = new BasicCredentialsParameterModel();
    } else if (connectorType.contains(SDOSConstants.TOKENCREDENTIALSPARAMETER)) {
      parameterModel = new TokenCredentialsParameterModel();
    } else if (connectorType.contains(SDOSConstants.SPARQLQUERYPARAMETER)) {
      parameterModel = new SparqlQueryParameterModel();
    } else {
      parameterModel = null;
    }
    return parameterModel;
  }
}
