package com.scania.sdos.orchestration.interfaces;

import com.scania.sdos.orchestration.OfgModelRepo;

import java.util.HashMap;
import java.util.List;

/**
 * public interface over the internal memory of input and output parameter between actions
 */
public interface IParameterMemory {

  void putParameter(String key, HashMap<String, List<String>> value);
  void replaceParameter(String key, HashMap<String, List<String>> value);
  HashMap<String, List<String>> getValue(String key);
  void clear();
  OfgModelRepo getOfgModelRepo();
  void setOfgModelRepo(OfgModelRepo ofgModelRepo);
}
