package com.scania.sdos.services;

/**
 * The IArguments interface provides methods for retrieving command line arguments.
 */
public interface IArguments {

  /**
   * Retrieve the first argument, if any, of this option.
   *
   * @param key the key name of the option.
   * @return Value of the argument if option is set, and has an argument, otherwise null.
   */
  String getString(String key);
}
