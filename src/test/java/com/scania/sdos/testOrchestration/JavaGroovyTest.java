package com.scania.sdos.testOrchestration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import com.google.gson.JsonParser;
import com.scania.sdip.exceptions.IncidentException;
import com.scania.sdip.exceptions.SdipErrorCode;
import com.scania.sdos.orchestration.JavaGroovy;
import com.scania.sdos.testUtils.UnitTestHelper;
import com.scania.sdos.testUtils.UnitTestConstants;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.MissingPropertyException;
import groovy.lang.Script;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.codehaus.groovy.control.ErrorCollector;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class JavaGroovyTest {

  private HashMap binding = new HashMap();

  @Spy
  JavaGroovy javaGroovy;

  @Mock
  GroovyShell groovyShell;

  @Mock
  Script script;

  @BeforeEach
  void setUp() {
    reset(groovyShell);
    reset(script);
    reset(javaGroovy);
  }

  @Test
  void test_javaGroovy_runGroovyShellList() {
    LinkedHashMap map = new LinkedHashMap();
    map.put("localPart", "sessionID");
    map.put("nameSpaceURI", "http://ws.polarion.com/session");
    List result = new ArrayList();
    result.add(map);
    doReturn(groovyShell).when(javaGroovy).getGroovyShell(any());
    doReturn(script).when(groovyShell).parse(anyString());
    doReturn(result).when(script).run();

    try {
      List scriptResponse = javaGroovy.runGroovyShellList(binding, "");
      assertEquals("http://ws.polarion.com/session",
          ((HashMap) scriptResponse.get(0)).get("nameSpaceURI").toString());
      assertEquals("sessionID", ((HashMap) scriptResponse.get(0)).get("localPart").toString());
    } catch (Exception e) {
      Assert.fail();
    }
  }

  @Test
  void test_javaGroovy_runGroovyShell() {
    Map map = new LinkedHashMap();
    Map map2 = new LinkedHashMap();
    map2.put("httpBody", UnitTestConstants.VALUE);
    map.put("https://kg.scania.com/it/iris_orchestration/PARAM_SCRIPT1_OUTPUT", map2);

    doReturn(groovyShell).when(javaGroovy).getGroovyShell(any());
    doReturn(script).when(groovyShell).parse(anyString());
    doReturn(map).when(script).run();

    String response = UnitTestHelper.readJsonFiles(UnitTestConstants.GROOVYSCRIPTOUTPUT);
    String daAnswer = JsonParser.parseString(response).getAsJsonObject().toString()
        .replace("\\n", "").replace("\\t", "").replace(" ", "").trim();
    try {
      HashMap hashMap = javaGroovy.runGroovyShell(binding, "");
      String httpBody = ((LinkedHashMap) hashMap
          .get("https://kg.scania.com/it/iris_orchestration/PARAM_SCRIPT1_OUTPUT"))
          .get("httpBody")
          .toString();
      assertEquals(daAnswer, httpBody);
    } catch (Exception e) {
      Assert.fail();
    }
  }

  @Test
  void runGroovyShell_invalidScript() {
    ErrorCollector ec = mock(ErrorCollector.class);
    doReturn(groovyShell).when(javaGroovy).getGroovyShell(any());
    when(groovyShell.parse(anyString())).thenThrow(new MultipleCompilationErrorsException(ec));

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      javaGroovy.runGroovyShell(binding, "");
    });
    assertEquals(SdipErrorCode.SCRIPT_INVALID.getSdipErrorCode(),
            Integer
                    .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                            .getSdipErrorCodes().get(0)
                            .replace("SDIP_", "")));

  }

  @Test
  void runGroovyShell_missingPropertyScript() {
    doReturn(groovyShell).when(javaGroovy).getGroovyShell(any());
    when(groovyShell.parse(anyString())).thenThrow(new MissingPropertyException(""));

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      javaGroovy.runGroovyShell(binding, "");
    });
    assertEquals(SdipErrorCode.SCRIPT_PROPERTY_MISSING.getSdipErrorCode(),
            Integer
                    .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                            .getSdipErrorCodes().get(0)
                            .replace("SDIP_", "")));

  }

  @Test
  void runGroovyShell_outputErrorScript() {
    doReturn(groovyShell).when(javaGroovy).getGroovyShell(any());
    doReturn(script).when(groovyShell).parse(anyString());
    doThrow(ClassCastException.class).when(script).run();

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      javaGroovy.runGroovyShell(binding, "");
    });
    assertEquals(SdipErrorCode.SCRIPT_OUTPUT_ERROR.getSdipErrorCode(),
            Integer
                    .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                            .getSdipErrorCodes().get(0)
                            .replace("SDIP_", "")));

  }

  @Test
  void runGroovyShell_unknownErrorScript() {
    doReturn(groovyShell).when(javaGroovy).getGroovyShell(any());
    doReturn(script).when(groovyShell).parse(anyString());
    doThrow(NullPointerException.class).when(script).run();

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      javaGroovy.runGroovyShell(binding, "");
    });
    assertEquals(SdipErrorCode.UNKNOWN_REASON_ERROR.getSdipErrorCode(),
            Integer
                    .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                            .getSdipErrorCodes().get(0)
                            .replace("SDIP_", "")));

  }


  @Test
  void runGroovyShellList_invalidScript() {
    ErrorCollector ec = mock(ErrorCollector.class);
    doReturn(groovyShell).when(javaGroovy).getGroovyShell(any());
    when(groovyShell.parse(anyString())).thenThrow(new MultipleCompilationErrorsException(ec));

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      javaGroovy.runGroovyShellList(binding, "");
    });
    assertEquals(SdipErrorCode.SCRIPT_INVALID.getSdipErrorCode(),
            Integer
                    .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                            .getSdipErrorCodes().get(0)
                            .replace("SDIP_", "")));

  }

  @Test
  void runGroovyShellList_missingPropertyScript() {
    doReturn(groovyShell).when(javaGroovy).getGroovyShell(any());
    when(groovyShell.parse(anyString())).thenThrow(new MissingPropertyException(""));

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      javaGroovy.runGroovyShellList(binding, "");
    });
    assertEquals(SdipErrorCode.SCRIPT_PROPERTY_MISSING.getSdipErrorCode(),
            Integer
                    .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                            .getSdipErrorCodes().get(0)
                            .replace("SDIP_", "")));

  }

  @Test
  void runGroovyShellList_outputErrorScript() {
    doReturn(groovyShell).when(javaGroovy).getGroovyShell(any());
    doReturn(script).when(groovyShell).parse(anyString());
    doThrow(ClassCastException.class).when(script).run();

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      javaGroovy.runGroovyShellList(binding, "");
    });
    assertEquals(SdipErrorCode.SCRIPT_OUTPUT_ERROR.getSdipErrorCode(),
            Integer
                    .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                            .getSdipErrorCodes().get(0)
                            .replace("SDIP_", "")));

  }

  @Test
  void runGroovyShellList_unknownErrorScript() {
    doReturn(groovyShell).when(javaGroovy).getGroovyShell(any());
    doReturn(script).when(groovyShell).parse(anyString());
    doThrow(NullPointerException.class).when(script).run();

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      javaGroovy.runGroovyShellList(binding, "daScript");
    });
    assertEquals(SdipErrorCode.UNKNOWN_REASON_ERROR.getSdipErrorCode(),
            Integer
                    .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                            .getSdipErrorCodes().get(0)
                            .replace("SDIP_", "")));

  }

  @Test
  void getGroovyShell() {
    Binding binding = mock(Binding.class);
    assertTrue((javaGroovy.getGroovyShell(binding)) instanceof GroovyShell);
  }
}
