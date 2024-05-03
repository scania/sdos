package com.scania.sdos.services;

/**
 * interface for Configuration Consistency checker
 */
public interface IConfigurationConsistencyChecker {

  /**
   * run a sequence that contact SDCS to verify that configuration is set and updated
   */
  void run();
}
