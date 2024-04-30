package com.scania.sdip.sdos.orchestration.factory;

import com.scania.sdip.sdos.orchestration.model.HelpModel;

public class HelpModelFactory {

  private static final HelpModel helpModel = new HelpModel();

  private HelpModelFactory(){
    //default constructor
  }

  public static HelpModel getInstance() {
    if (helpModel != null) {
      return helpModel;
    } else {
      return new HelpModel();
    }
  }
}
