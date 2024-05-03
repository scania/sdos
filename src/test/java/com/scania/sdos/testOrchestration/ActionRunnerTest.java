package com.scania.sdos.testOrchestration;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.scania.sdip.exceptions.IncidentException;
import com.scania.sdip.exceptions.SdipErrorCode;
import com.scania.sdip.exceptions.SdipErrorParameter;
import com.scania.sdos.model.ServiceArguments;
import com.scania.sdos.orchestration.ActionRunner;
import com.scania.sdos.orchestration.ParameterMemory;
import com.scania.sdos.orchestration.interfaces.IActionModel;
import com.scania.sdos.orchestration.model.JsonLdContextModel;
import com.scania.sdos.utils.SDOSConstants;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ActionRunnerTest {

  private static final Logger LOGGER = LogManager.getLogger(ActionRunnerTest.class);

  @Mock
  private ParameterMemory parameterMemory;
  @Mock
  private ServiceArguments serviceArguments;
  @Mock
  private IActionModel iActionModel;
  @Mock
  private JsonLdContextModel jsonLdContextModel;
  @Spy
  private ActionRunner actionRunner;


  private HashMap<String, List<String>> getReportId() {
    HashMap map = new HashMap();
    map.put("Id", Collections.singletonList("http://test"));
    return map;
  }

  @Test
  void test_actionRunner() {
    StringBuilder sbb = new StringBuilder();
    doNothing().when(iActionModel).run(any(), any(), any());
    doReturn(getReportId()).when(parameterMemory).getValue(SDOSConstants.EXECUTION_REPORT);
    doReturn(null).when(iActionModel).getNextAction();
    actionRunner.run(iActionModel, parameterMemory, jsonLdContextModel, serviceArguments);
    verify(iActionModel, times(1))
        .run(parameterMemory, jsonLdContextModel, serviceArguments);
    verify(iActionModel, times(1))
        .getNextAction();
    /**
     * getSubjectIri method will be called twice because of the new logs
     */
    verify(iActionModel, times(2))
        .getSubjectIri();
    /**
     * getLabel method will be called twice because of the new logs
     */
    verify(iActionModel, times(2))
        .getLabel();
    assertNotNull(sbb);
    assertTrue(sbb.toString().contains(""));
  }

  @Test
  void test_actionRunner_nextAction() {
    IActionModel iActionModel1 = mock(IActionModel.class);
    StringBuilder sbb = new StringBuilder();
    doNothing().when(iActionModel).run(any(), any(), any());
    doReturn(iActionModel1).when(iActionModel).getNextAction();
    doNothing().when(iActionModel1).run(any(), any(), any());
    doReturn(null).when(iActionModel1).getNextAction();
    doReturn(getReportId()).when(parameterMemory).getValue(SDOSConstants.EXECUTION_REPORT);
    actionRunner.run(iActionModel, parameterMemory, jsonLdContextModel, serviceArguments);
    verify(iActionModel, times(1))
        .run(parameterMemory, jsonLdContextModel, serviceArguments);
    verify(iActionModel, times(2))
        .getNextAction();
    /**
     * getSubjectIri method will be called twice because of the new logs
     */
    verify(iActionModel, times(2))
        .getSubjectIri();
    /**
     * getLabel method will be called twice because of the new logs
     */
    verify(iActionModel, times(2))
        .getLabel();
    verify(iActionModel1, times(1))
        .run(parameterMemory, jsonLdContextModel, serviceArguments);
    verify(iActionModel1, times(1))
        .getNextAction();

    /**
     * getSubjectIri method will be called twice because of the new logs
     */
    verify(iActionModel1, times(2))
        .getSubjectIri();
    /**
     * getLabel method will be called twice because of the new logs
     */
    verify(iActionModel1, times(2))
        .getLabel();
    assertNotNull(sbb);
    assertTrue(sbb.toString().contains(""));
  }

  @Test
  void actionRunner_catchException() {
    StringBuilder sbb = new StringBuilder();
    String message = "Host name may not be null";
    doReturn("actionLabel").when(iActionModel).getLabel();
    doReturn("testIRI").when(iActionModel).getSubjectIri();
    doThrow(new IncidentException(SdipErrorCode.UNKNOWN_REASON_ERROR, LOGGER, message,
        SdipErrorParameter.SUPPORTMAIL))
        .when(iActionModel).run(any(), any(), any());
    doReturn(getReportId()).when(parameterMemory).getValue(SDOSConstants.EXECUTION_REPORT);

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      actionRunner.run(iActionModel, parameterMemory, jsonLdContextModel, serviceArguments);
    });

    assertEquals("actionLabel, testIRI", ((IncidentException)throwable).getActionDetails());
    assertEquals(SdipErrorCode.UNKNOWN_REASON_ERROR.getSdipErrorCode(),
            Integer
                    .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                            .getSdipErrorCodes().get(0)
                            .replace("SDIP_", "")));
    assertEquals(2, ((IncidentException) throwable).getErrorBindings().get(0).getArgs().length);
    Object[] args = ((IncidentException) throwable).getErrorBindings().get(0).getArgs();
    assertEquals(message, args[0]);
    assertEquals(SdipErrorParameter.SUPPORTMAIL, args[1]);
    assertNotNull(sbb);
    assertFalse(sbb.toString().contains("success"));

  }

}
