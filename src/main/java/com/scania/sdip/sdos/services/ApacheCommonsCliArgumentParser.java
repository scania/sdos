package com.scania.sdip.sdos.services;

import com.scania.sdip.exceptions.IncidentException;
import com.scania.sdip.exceptions.SdipErrorCode;
import java.util.Arrays;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The ApacheCommonsCliArgumentParser class parses the command line options and arguments provided
 * at startup, and verifies that required arguments are available. It also provides the user with a
 * "help" message describing the command line options, when requested by the user.
 */
public class ApacheCommonsCliArgumentParser implements IArgumentParser {

  private static final Logger logger = LoggerFactory
          .getLogger(ApacheCommonsCliArgumentParser.class);

  @Override
  public IArguments parseArguments(String[] args) {

    Options options = new Options();
//SDCS CONNECTION IS CURRENTLY DISABLED DUE TO NOT USED BUT MAYBE WILL BE
//    Option option = new Option("c", "configurationUrl", true,
//        "The Configuration service base-url on the form http://scania.com/sdcs, this option is mandatory.");
//    option.setRequired(true);
//    options.addOption(option);

    Option option = new Option("id", "serviceId", true,
            "A unique identifier for this service, this option is mandatory.");
    option.setRequired(false);
    options.addOption(option);

    option = new Option("b", "stardogBaseUrl", true,
            "the baseUrl for a stardog endpoint, this option is mandatory.");
    option.setRequired(true);
    options.addOption(option);

    option = new Option("ofg", "ofgDb", true,
            "A unique identifier for orchestration database name in stardog, this option is mandatory.");
    option.setRequired(true);
    options.addOption(option);

    option = new Option("r", "resultDb", true,
            "A unique identifier for result database name in stardog, this option is mandatory.");
    option.setRequired(true);
    options.addOption(option);

    option = new Option("tpSize", "threadPoolSize", true,
            "the thread pool size to initialize the Thread Executor, this option is optional.");
    option.setRequired(false);
    options.addOption(option);

    option = new Option("clientSecret", "sdosClientSecret", true,
            "SDOS Azure app client secret used for Azure communication, this option is mandatory.");
    option.setRequired(true);
    options.addOption(option);

    option = new Option("clientScope", "stardogClientScope", true,
            "Stardog client scope that SDOS can use for token exchange, this option is mandatory.");
    option.setRequired(true);
    options.addOption(option);

    option = new Option("tenantId", "AzureTenantId", true,
            "Tenant id used for Azure communication, this option is mandatory.");
    option.setRequired(true);
    options.addOption(option);

    options.addOption(option);
    option = new Option("h", "help", false, "");
    options.addOption(option);

    if (Arrays.stream(args).anyMatch(arg -> "--help".equals(arg) || "-h".equals(arg))) {
      printHelp(options);
      return null;
    }

    CommandLineParser parser = new DefaultParser();
    try {
      CommandLine cmd = parser.parse(options, args);
      ApacheCommonsCliArguments cliArguments = new ApacheCommonsCliArguments(cmd);

      return cliArguments;
    } catch (ParseException e) {
      printHelp(options);
      throw new IncidentException(SdipErrorCode.FAILED_TO_PARSE_ARGUMENTS, logger, e.getMessage());
    }

  }

  private void printHelp(Options options) {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("sdos", "", options, "", true);
  }
}
