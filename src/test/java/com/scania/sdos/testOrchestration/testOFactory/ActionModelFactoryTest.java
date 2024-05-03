package com.scania.sdos.testOrchestration.testOFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.reset;

import com.scania.sdos.orchestration.ParameterMemory;
import com.scania.sdos.orchestration.factory.ActionModelFactory;
import com.scania.sdos.orchestration.model.HttpActionModel;
import com.scania.sdos.orchestration.model.QueryActionModel;
import com.scania.sdos.orchestration.model.ResultActionAsyncModel;
import com.scania.sdos.orchestration.model.ResultActionSyncModel;
import com.scania.sdos.orchestration.model.ScriptActionModel;
import com.scania.sdos.orchestration.model.SoapActionModel;
import com.scania.sdos.orchestration.model.SparqlConvertActionModel;
import com.scania.sdos.orchestration.model.VirtualGraphActionModel;
import com.scania.sdos.utils.SDOSConstants;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.NotImplementedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ActionModelFactoryTest {

  @Spy
  private ParameterMemory parameterMemory;

  @BeforeEach
  void setup() {
    reset(parameterMemory);
  }


  @Test
  void getAction_Soap() {
    Assertions.assertEquals(SoapActionModel.class,
        ActionModelFactory.getAction("SOAPAction", parameterMemory).getClass());
  }

  @Test
  void getAction_Http() {
    assertEquals(HttpActionModel.class,
            ActionModelFactory.getAction("HTTPAction", parameterMemory).getClass());
  }

  @Test
  void getAction_Script() {
    assertEquals(ScriptActionModel.class,
            ActionModelFactory.getAction("ScriptAction", parameterMemory).getClass());
  }

  @Test
  void getAction_Query() {
    assertEquals(QueryActionModel.class,
            ActionModelFactory.getAction("QueryAction", parameterMemory).getClass());
  }


  @Test
  void getAction_ResultAsync() {
    HashMap<String, List<String>> result = new HashMap<>();
    result.put(SDOSConstants.GRAPH, Collections.singletonList(""));
    result.put(SDOSConstants.SYNC, Collections.singletonList("False"));
    parameterMemory.putParameter(SDOSConstants.O_RESULT, result);
    assertEquals(ResultActionAsyncModel.class,
            ActionModelFactory.getAction("ResultAction", parameterMemory).getClass());
  }

  @Test
  void getAction_ResultSync() {
    HashMap<String, List<String>> result = new HashMap<>();
    result.put(SDOSConstants.GRAPH, Collections.singletonList(""));
    result.put(SDOSConstants.SYNC, Collections.singletonList("True"));
    parameterMemory.putParameter(SDOSConstants.O_RESULT, result);
    assertEquals(ResultActionSyncModel.class,
        ActionModelFactory.getAction("ResultAction", parameterMemory).getClass());
  }

  @Test
  void getAction_ResultUnknown() {
    HashMap<String, List<String>> result = new HashMap<>();
    result.put(SDOSConstants.GRAPH, Collections.singletonList(""));
    result.put(SDOSConstants.SYNC, Collections.singletonList("notABoolean"));
    parameterMemory.putParameter(SDOSConstants.O_RESULT, result);
    Throwable throwable = assertThrows(NotImplementedException.class, () -> {
      ActionModelFactory.getAction("ResultAction", parameterMemory);
    });
  }

  @Test
  void getAction_Virtual() {
    assertEquals(VirtualGraphActionModel.class,
        ActionModelFactory.getAction("VirtualGraphAction", parameterMemory).getClass());
  }

  @Test
  void getAction_SparqlConvert() {
    assertEquals(SparqlConvertActionModel.class,
        ActionModelFactory.getAction("SparqlConvertAction", parameterMemory).getClass());
  }

  @Test
  void getAction_Unknown() {
    assertNull(ActionModelFactory.getAction("UnknownAction", parameterMemory));
  }
}