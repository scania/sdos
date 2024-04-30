package com.scania.sdip.sdos;


import com.scania.sdip.exceptions.IncidentException;
import com.scania.sdip.sdos.model.ServiceArguments;
import com.scania.sdip.sdos.services.ApacheCommonsCliArgumentParser;
import com.scania.sdip.sdos.services.IArguments;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.context.support.GenericWebApplicationContext;

@SpringBootApplication
@ComponentScan(basePackages = {"com.scania.sdip", "com.scania.sdip.sdos"})
@EnableAsync
public class SdosApplication {

  private static IArguments arguments;

  private final ServiceArguments serviceArguments;

  @Autowired
  public SdosApplication(GenericWebApplicationContext context, ServiceArguments serviceArguments) {
    this.serviceArguments = serviceArguments;
    try {
      if (arguments != null) {
        this.serviceArguments.init(arguments);
      }
    } catch (IncidentException e) {
      e.printToLog();
      e.runExit();
    }
  }

  public static void main(String[] args) {
    try {
      arguments = new ApacheCommonsCliArgumentParser().parseArguments(args);
      if (arguments == null) {
        return;
      }
      SpringApplication.run(SdosApplication.class, args);

    } catch (IncidentException e) {
      e.printToLog();
      e.runExit();
    }
  }
}
