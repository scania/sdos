package com.scania.sdip.sdos.startup;

import com.scania.sdip.sdos.services.IArguments;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Startup implements IStartup {

  private static final Logger LOGGER = LogManager.getLogger(Startup.class);
  private final IArguments arguments;

  public Startup(IArguments arguments) {
    this.arguments = arguments;
  }
}
