package com.scania.sdos.testStartup;

import com.scania.sdip.exceptions.IncidentException;
import com.scania.sdip.exceptions.SdipErrorCode;
import com.scania.sdos.model.ServiceArguments;
import com.scania.sdos.services.ApacheCommonsCliArgumentParser;
import com.scania.sdos.services.IArguments;
import com.scania.sdos.startup.IStartup;
import com.scania.sdos.startup.Startup;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class StartupTest {

  @Mock
  private ServiceArguments serviceArguments;

  @Test
  public void testToValidateProgramArgumentsPositive() {
    IArguments arguments = new ApacheCommonsCliArgumentParser()
        .parseArguments(
            new String[]{"-id", "SERVICE", "-b", "http://localhostt", "-r", "result", "-ofg",
                "ofg","-clientSecret","terdfgvbfhgyrtdgf","-clientScope",
                "api://12345/email","-tenantId","1874635761536"});
    IStartup startup = new Startup(arguments);
    try {
      serviceArguments.init(arguments);
    } catch (Exception e) {
      Assert.fail("Error was " + e);
    }
  }

  @Test
  public void testToValidateProgramArgumentsNegative() {
    IArguments arguments = new ApacheCommonsCliArgumentParser()
        .parseArguments(
            new String[]{"-id", "SERVICE", "-b", "notaURL", "-r", "result", "-ofg", "ofg",
                "-clientSecret","terdfgvbfhgyrtdgf","-clientScope",
                "api://12345/email","-tenantId","1874635761536"});
    ServiceArguments serviceArguments = new ServiceArguments();
    try {
      serviceArguments.init(arguments);
      Assert.fail("should be error");
    } catch (IncidentException e) {
      Assert.assertEquals("SDIP_" + SdipErrorCode.ARGUMENT_NOT_A_VALID_URL.getSdipErrorCode(),
              e.createHttpErrorResponse().getBody().getSdipErrorCodes().get(0));

    }

  }

  @Test
  public void testMissingMandatoryArgument() {
    try {
      IArguments arguments = new ApacheCommonsCliArgumentParser()
              .parseArguments(new String[]{"-c", "http://local"});
      Assert.fail("should be error");
    } catch (IncidentException e) {
      Assert.assertEquals("SDIP_" + SdipErrorCode.FAILED_TO_PARSE_ARGUMENTS.getSdipErrorCode(),
              e.createHttpErrorResponse().getBody().getSdipErrorCodes().get(0));
    }
  }

}
