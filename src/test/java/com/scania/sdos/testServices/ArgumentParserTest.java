package com.scania.sdos.testServices;

import com.scania.sdos.services.ApacheCommonsCliArgumentParser;
import com.scania.sdos.services.IArgumentParser;
import com.scania.sdos.services.IArguments;
import org.junit.Assert;
import org.junit.jupiter.api.Test;


public class ArgumentParserTest {

  @Test
  public void parseArgs() {
    IArgumentParser argumentParser = new ApacheCommonsCliArgumentParser();

    IArguments arguments = argumentParser.parseArguments(
        new String[]{"-b", "stardogurl", "-id", "idtest", "-r", "result", "-ofg", "ofg","-clientSecret","terdfgvbfhgyrtdgf","-clientScope","api://12345/email","-tenantId",
        "1874635761536"});

    Assert.assertEquals("stardogurl", arguments.getString("stardogBaseUrl"));
    Assert.assertEquals("result", arguments.getString("resultDb"));
    Assert.assertEquals("ofg", arguments.getString("ofgDb"));
    Assert.assertEquals("idtest", arguments.getString("serviceId"));
    Assert.assertEquals("terdfgvbfhgyrtdgf", arguments.getString("sdosClientSecret"));
    Assert.assertEquals("api://12345/email", arguments.getString("stardogClientScope"));
    Assert.assertEquals("1874635761536", arguments.getString("azureTenantId"));

  }
}
