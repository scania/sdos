package com.scania.sdip.sdos.orchestration.interfaces;

import com.scania.sdip.sdos.model.ServiceArguments;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public interface IParallelActionModel {

  default ExecutorService getExecutorService(int threadPoolSize) {
    ExecutorService executorService = Executors.newFixedThreadPool(threadPoolSize);
    return executorService;
  }

  String prepareRequest(IParameterMemory iParameterMemory, String requestEndpoint,
      String requestHttpBody,
      HashMap requestHeaders,
      HashMap requestQueryParams, HashMap formData, ServiceArguments serviceArguments,
      IParallelActionModel iParallelActionModel) throws IOException;

  default Set<Callable<String>> getCallableSet() {
    return new HashSet<>();
  }

  default void isThreadActive(ExecutorService executorService) {
    for (; ; ) {
      if (executorService instanceof ThreadPoolExecutor) {
        if (((ThreadPoolExecutor) executorService).getActiveCount() == 0) {
          executorService.shutdownNow();
          break;
        }
      }
    }
  }

  default int getThreadPoolSize(int iterations, ServiceArguments serviceArguments) {
    int threadPoolSize =
        (serviceArguments.getThreadPoolSize() != null && !serviceArguments
            .getThreadPoolSize().equals("0"))
            ? Integer.parseInt(serviceArguments.getThreadPoolSize()) : 50;
    threadPoolSize = (iterations < threadPoolSize) ? iterations : threadPoolSize;
    return threadPoolSize;
  }
}
