package com.scania.sdos.testUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.annotation.PreDestroy;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.jupiter.api.Test;


/**
 * JUnit test to validate logging framework.
 */
public class Log4jTest {

  private static String CONFIG_FILE = "log4j2-test.xml";
  private static String TEST_LOG_FILE = "logs/sdip-sdos-test.log";
  private static String ERROR_MSG = "Error Message Logged Successfully!";
  private static String LOG_DIR = "logs";
  private FileAlterationMonitor monitor;

  @BeforeClass
  public static void init() {
    LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
    System.setProperty("log4j.configurationFile", CONFIG_FILE);
    ctx.reconfigure();
  }

  @Test
  public void isConfigurationFileExist() throws NullPointerException {
    try {
      File configFile =
          FileUtils.getFile(getClass().getClassLoader().getResource(CONFIG_FILE).getPath());
      Assert.assertTrue(configFile.exists());
    } catch (NullPointerException e) {
      Assert.fail("Exception Should not be thrown!");
    }
  }

  @Test
  public void isLogFileCreated() {
    FileAlterationObserver observer = new FileAlterationObserver(LOG_DIR);
    monitor = new FileAlterationMonitor(10);
    FileAlterationListener fal = new FileAlterationListenerAdaptor() {

      @Override
      public void onFileCreate(File file) {
        Assert.assertTrue(file.getName().contains("sdip-sdos-test"));
      }

      @Override
      public void onFileDelete(File file) {
      }
    };

    observer.addListener(fal);
    monitor.addObserver(observer);
    try {
      monitor.start();
    } catch (Exception e) {
      Assert.fail("Exception Should not be thrown!");
    }
  }

  @Test
  public void addLogging() {
    Logger logger = LogManager.getLogger(Log4jTest.class);
    logger.error(ERROR_MSG);
    logger.fatal("Fatal Message Logged!");
    logger.info("Info Message Logged!");
    logger.trace("Trace Message Logged!");
    logger.warn("Warn Message Logged!");
  }

  @PreDestroy
  public void testAppender() {
    try {
      Thread.sleep(10000);
      String loggedData = readFromFile(TEST_LOG_FILE);
      Assert.assertTrue(loggedData.contains(ERROR_MSG));
    } catch (NullPointerException | InterruptedException e) {
      Assert.fail("Exception Should not be thrown!");
    } catch (Exception e) {
      Assert.fail("Exception Should not be thrown!");
    }
  }

  /**
   * To read log files
   *
   * @param filePath : Test log file path
   * @return content
   * @throws Exception while reading file
   */
  private String readFromFile(String filePath) throws Exception {
    final File testLogFile = new File(filePath);
    final InputStream targetStream = new FileInputStream(testLogFile);
    StringBuilder resultStringBuilder = new StringBuilder();
    try (BufferedReader br = new BufferedReader(new InputStreamReader(targetStream))) {
      String line;
      while ((line = br.readLine()) != null) {
        resultStringBuilder.append(line).append("\n");
      }
    }
    return resultStringBuilder.toString();
  }

  @Test
  public void closeAll() {

    if ((monitor != null)) {
      try {
        monitor.stop();
      } catch (Exception e) {
        Assert.fail("Exception Should not be thrown!");
      }
    }
  }
}
