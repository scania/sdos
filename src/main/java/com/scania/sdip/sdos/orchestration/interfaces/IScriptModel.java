package com.scania.sdip.sdos.orchestration.interfaces;

import com.scania.sdip.sdos.model.ServiceArguments;
import com.scania.sdip.sdos.orchestration.model.JsonLdContextModel;

import java.util.Map;
import java.util.List;

public interface IScriptModel extends IBaseModel {

  String getScript();
  Map executeScript(IParameterMemory iParameterMemory, JsonLdContextModel context,
                    ServiceArguments serviceArguments, JsonLdContextModel contextModel,
                    List<IParameterModel> outputParameter, List<IParameterModel> inputParameter);
}
