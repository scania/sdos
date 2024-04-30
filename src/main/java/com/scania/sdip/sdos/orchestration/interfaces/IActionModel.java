package com.scania.sdip.sdos.orchestration.interfaces;

import com.scania.sdip.sdos.model.ServiceArguments;
import com.scania.sdip.sdos.orchestration.model.JsonLdContextModel;

public interface IActionModel extends IBaseModel {

  void run(IParameterMemory iParameterMemory, JsonLdContextModel context,
      ServiceArguments serviceArguments);

  IActionModel getNextAction();

}
