package com.scania.sdos.orchestration.interfaces;

import com.scania.sdos.model.ServiceArguments;
import com.scania.sdos.orchestration.model.JsonLdContextModel;

import java.util.Map;
import java.util.List;

public interface IScriptModel extends IBaseModel {

  String getScript();
  Map executeScript(IParameterMemory iParameterMemory, JsonLdContextModel context,
                    ServiceArguments serviceArguments, JsonLdContextModel contextModel,
                    List<IParameterModel> outputParameter, List<IParameterModel> inputParameter);
}
