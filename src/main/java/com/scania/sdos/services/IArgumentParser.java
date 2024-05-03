package com.scania.sdos.services;

/**
 * The IArgumentParser interface provides a method for parsing command line options and arguments.
 */
public interface IArgumentParser {

  /**
   * Returns an {@link IArguments} object into which the command line options and arguments provided
   * by the user have been parsed.
   *
   * @param args is a list of String arguments.
   * @return an {@link IArguments} object that holds the command line options and provided
   * arguments.
   */
  IArguments parseArguments(String[] args);

}
