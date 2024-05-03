package com.scania.sdos.orchestration;

import com.scania.sdos.orchestration.interfaces.IParameterMemory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ParameterMemory implements IParameterMemory {

  //TODO MAYBE SAVE MEMORY AS OBJECT THAT USES MODELS INSTEAD OF HASHMAP<String,LIST<STring>)
  //EX HASHMAP<SUBJECTIRI>,IOFGMODEL> this will save all info on one place and not duplicate when populating
  //                   SUBJECTIRI       PARAMETERNAME    CONTENTS
  private final HashMap<String, HashMap<String, List<String>>> parametersStore;//TODO MAYBE CHANGE TO BYTE[] FOR MEMORY EFFIENCY
  private final ReentrantReadWriteLock lock;

  private OfgModelRepo ofgModelRepo;
  public ParameterMemory() {
    parametersStore = new HashMap<>();
    lock = new ReentrantReadWriteLock();

  }

  public static ParameterMemory getParameterMemoryObject(){
    return new ParameterMemory();
  }

  @Override
  public void putParameter(String key, HashMap<String, List<String>> value) {
    if (getValue(key) != null) {
      HashMap<String, List<String>> listHashMap = getValue(key);
      for (Map.Entry<String, List<String>> entry : listHashMap.entrySet()) {
        List<String> stringList = entry.getValue();
        List<String> stringList2 = value.get(entry.getKey());
        List<String> result = new ArrayList<>(stringList.size() + stringList2.size());
        result.addAll(stringList);
        result.addAll(stringList2);
        value.put(entry.getKey(), result);
      }
    }
    parametersStore.put(key, value);
  }

  @Override
  public void replaceParameter(String key, HashMap<String, List<String>> value) {
    lock.writeLock().lock();
    try {
      parametersStore.put(key, value);
    } finally {
      lock.writeLock().unlock();
    }
  }
  @Override
  public HashMap<String, List<String>> getValue(String key) {
    return parametersStore.get(key);
  }

  @Override
  public void clear() {
    parametersStore.clear();
  }

  public OfgModelRepo getOfgModelRepo() {
    return ofgModelRepo;
  }

  public void setOfgModelRepo(OfgModelRepo ofgModelRepo) {
    this.ofgModelRepo = ofgModelRepo;
  }
}
