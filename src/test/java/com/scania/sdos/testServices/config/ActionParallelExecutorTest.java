package com.scania.sdos.testServices.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.scania.sdos.model.ServiceArguments;
import com.scania.sdos.orchestration.ParameterMemory;
import com.scania.sdos.orchestration.interfaces.IParallelActionModel;
import com.scania.sdos.orchestration.interfaces.IParameterMemory;
import com.scania.sdos.services.config.ActionParallelExecutor;
import java.io.IOException;
import java.util.HashMap;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ActionParallelExecutorTest {

  private static MockedStatic parameterMemoryFactory;

  @Mock
  private ParameterMemory daParams;
  @Mock
  private ServiceArguments serviceArguments;
  @Mock
  private IParallelActionModel iParallelActionModel;

  @BeforeAll
  static void beforeAll() {
    parameterMemoryFactory = mockStatic(IParameterMemory.class);
  }

  @AfterAll
  static void afterAll() {
    parameterMemoryFactory.close();
  }

  @BeforeEach
  void setUp() {
    parameterMemoryFactory.reset();
    reset(iParallelActionModel);
  }

  private ActionParallelExecutor getInstance() {
    HashMap requestHeader = new HashMap();
    requestHeader.put("content-type", "application/json");

    HashMap requestParam = new HashMap();
    requestParam.put("testKey", "testvalue");
    ActionParallelExecutor modelUnderTest = ActionParallelExecutor
        .getParallelExecutorInstance(daParams, "testEndPoint", "testHttpBody",
            requestHeader, requestParam, new HashMap(), serviceArguments, iParallelActionModel);
    return modelUnderTest;
  }

  @Test
  void test_getExecutorInstance() {
    try {
      ActionParallelExecutor modelUnderTest = getInstance();
      assertEquals("testEndPoint", modelUnderTest.getRequestEndpoint());
      assertEquals("testHttpBody", modelUnderTest.getRequestHttpBody());
      assertEquals("application/json",
          ((HashMap) modelUnderTest.getRequestHeaders()).get("content-type"));
      assertEquals("testvalue",
          ((HashMap) modelUnderTest.getRequestQueryParams()).get("testKey"));
    } catch (Exception e) {
      Assertions.fail(e.getMessage());
    }
  }

  @Test
  void test_call() throws IOException {
    ActionParallelExecutor modelUnderTest = getInstance();
    doReturn("apiResponse").when(iParallelActionModel)
        .prepareRequest(any(), any(), any(), any(), any(), any(), any(), any());
    try {
      assertEquals("apiResponse", modelUnderTest.call());
      verify(iParallelActionModel, times(1))
          .prepareRequest(any(), any(), any(), any(), any(), any(), any(), any());
    } catch (Exception e) {
      Assertions.fail(e.getMessage());
    }
  }
}
