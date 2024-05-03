package com.scania.sdos.orchestration.factory;

import com.scania.sdos.orchestration.interfaces.IParameterModel;
import com.scania.sdos.orchestration.model.BasicCredentialsParameterModel;
import com.scania.sdos.orchestration.model.HttpParameterModel;
import com.scania.sdos.orchestration.model.StandardParameterModel;
import com.scania.sdos.orchestration.model.TokenCredentialsParameterModel;
import com.scania.sdos.orchestration.model.SparqlQueryParameterModel;
import com.scania.sdos.utils.SDOSConstants;

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
