package com.scania.sdip.sdos.orchestration.factory;

import com.scania.sdip.sdos.orchestration.interfaces.IConnectorModel;
import com.scania.sdip.sdos.orchestration.model.HttpConnectorModel;
import com.scania.sdip.sdos.orchestration.model.SoapConnectorModel;

public class ConnectorModelFactory {

  private ConnectorModelFactory(){
    //default constructor
  }

  public static IConnectorModel getConnector(String connectorType) {
    if (connectorType.contains("SOAPConnector")) {
      SoapConnectorModel soapConnectorModel = new SoapConnectorModel();
      return soapConnectorModel;
    } else if (connectorType.contains("HTTPConnector")) {
      HttpConnectorModel httpConnectorModel = new HttpConnectorModel();
      return httpConnectorModel;
    } else {
      return null;
    }
  }
}
