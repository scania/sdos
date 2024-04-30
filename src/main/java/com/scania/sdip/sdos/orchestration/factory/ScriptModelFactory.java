package com.scania.sdip.sdos.orchestration.factory;

import com.scania.sdip.sdos.orchestration.interfaces.IScriptModel;
import com.scania.sdip.sdos.orchestration.model.GroovyScriptModel;
import com.scania.sdip.sdos.orchestration.model.PythonScriptModel;

public class ScriptModelFactory {

  private ScriptModelFactory(){
    //default constructor
  }
  public static IScriptModel getScript(String requestType) {

    if (requestType.contains("GroovyScript")) {
      return new GroovyScriptModel();
    } else if (requestType.contains("PythonScript")) {
      return new PythonScriptModel();
    } else {
      return null;
    }
  }
}
