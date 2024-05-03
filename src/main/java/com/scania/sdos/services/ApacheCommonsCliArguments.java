package com.scania.sdos.services;

import org.apache.commons.cli.CommandLine;

/**
 * The ApacheCommonsCliArgments class stores the command line arguments provided for the various
 * options.
 */
public class ApacheCommonsCliArguments implements IArguments {

  private final CommandLine cmd;

  public ApacheCommonsCliArguments(CommandLine cmd) {
    this.cmd = cmd;
  }

  @Override
  public String getString(String key) {
    return cmd.getOptionValue(key);
  }
}
