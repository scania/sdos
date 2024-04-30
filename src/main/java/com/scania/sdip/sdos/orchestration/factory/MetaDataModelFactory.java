package com.scania.sdip.sdos.orchestration.factory;

import com.scania.sdip.sdos.orchestration.model.ResultMetaDataModel;

public class MetaDataModelFactory {

    private MetaDataModelFactory(){
        //default constructor
    }

  /**
   * Return  ResultMetaDataModel instance.
   *
   * @return the ResultMetaDataModel
   */
  public static ResultMetaDataModel getModel() {
       return new ResultMetaDataModel();
  }
}