package com.scania.sdos.testOrchestration.testOInterface;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.reset;

import com.scania.sdos.model.ServiceArguments;
import com.scania.sdos.orchestration.interfaces.IParallelActionModel;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class IParallelActionModelTest {

  @Spy
  private IParallelActionModel iParallelActionModel;

  @BeforeEach
  void setUp() {
    reset(iParallelActionModel);
  }


  @Test
  void test_checkExecutorService() {
    try {
      ExecutorService executorService = iParallelActionModel.getExecutorService(10);
      Assert.assertEquals(10, ((ThreadPoolExecutor) executorService).getCorePoolSize());
    } catch (Exception e) {
      Assertions.fail(e.getMessage());
    }
  }

  @Test
  void test_checkThreadCount() {
    ServiceArguments serviceArguments = Mockito.mock(ServiceArguments.class);
    doReturn("40").when(serviceArguments).getThreadPoolSize();
    try {
      Assert.assertEquals(10, iParallelActionModel.getThreadPoolSize(10, serviceArguments));
    } catch (Exception e) {
      Assertions.fail(e.getMessage());
    }

    doReturn("0").when(serviceArguments).getThreadPoolSize();
    try {
      Assert.assertEquals(50, iParallelActionModel.getThreadPoolSize(470, serviceArguments));
    } catch (Exception e) {
      Assertions.fail(e.getMessage());
    }
  }

  @Test
  void test_getCallables() {
    try {
      Assert.assertEquals(true, iParallelActionModel.getCallableSet() instanceof HashSet);
    } catch (Exception e) {
      Assertions.fail(e.getMessage());
    }
  }

}
