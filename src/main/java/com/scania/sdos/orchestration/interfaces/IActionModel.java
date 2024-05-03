package com.scania.sdos.orchestration.interfaces;

import com.scania.sdos.model.ServiceArguments;
import com.scania.sdos.orchestration.model.JsonLdContextModel;

public interface IActionModel extends IBaseModel {

  void run(IParameterMemory iParameterMemory, JsonLdContextModel context,
      ServiceArguments serviceArguments);

  IActionModel getNextAction();

}
