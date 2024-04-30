package com.scania.sdip.sdos.orchestration.factory;

import com.scania.sdip.sdos.orchestration.interfaces.IAuthModel;
import com.scania.sdip.sdos.orchestration.model.HttpBasicAuthModel;
import com.scania.sdip.sdos.orchestration.model.HttpBearerTokenAuthModel;
import com.scania.sdip.sdos.orchestration.model.SoapBasicAuthModel;

public class AuthenticationModelFactory {

  private AuthenticationModelFactory(){
    //default constructor
  }

  public static IAuthModel getAuthModel(String requestType) {
    if (requestType.contains("SOAPBasicAuthenticationMethod")) {
      SoapBasicAuthModel authModel = new SoapBasicAuthModel();
      return authModel;
    } else if (requestType.contains("HTTPBasicAuthenticationMethod")) {
      HttpBasicAuthModel authModel = new HttpBasicAuthModel();
      return authModel;
    } else if (requestType.contains("HTTPBearerTokenAuthenticationMethod")) {
      HttpBearerTokenAuthModel authModel = new HttpBearerTokenAuthModel();
      return authModel;
    } else {
      return null;
    }
  }
}
