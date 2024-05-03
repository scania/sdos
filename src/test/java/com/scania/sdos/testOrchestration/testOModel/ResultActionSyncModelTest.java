package com.scania.sdos.testOrchestration.testOModel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.reset;
import com.scania.sdip.exceptions.IncidentException;
import com.scania.sdip.exceptions.SdipErrorCode;
import com.scania.sdos.model.ServiceArguments;
import com.scania.sdos.orchestration.ParameterMemory;
import com.scania.sdos.orchestration.Rdf4jClient;
import com.scania.sdos.orchestration.factory.ActionModelFactory;
import com.scania.sdos.orchestration.factory.ConnectorModelFactory;
import com.scania.sdos.orchestration.factory.ParameterModelFactory;
import com.scania.sdos.orchestration.interfaces.IActionModel;
import com.scania.sdos.orchestration.interfaces.IConnectorModel;
import com.scania.sdos.orchestration.interfaces.IParameterMemory;
import com.scania.sdos.orchestration.interfaces.IParameterModel;
import com.scania.sdos.orchestration.model.JsonLdContextModel;
import com.scania.sdos.orchestration.model.ResultActionSyncModel;
import com.scania.sdos.utils.SDOSConstants;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ResultActionSyncModelTest {

  private static final Logger LOGGER = LogManager.getLogger(ResultActionSyncModel.class);
  private static MockedStatic actionModelFactory;
  private static MockedStatic connectorModelFactory;
  private static MockedStatic parameterModelFactory;
  private static MockedStatic parameterMemoryFactory;
  private String subjectUri = "http://testsubjecturi";

  @Mock
  private IConnectorModel connectorModel;
  @Mock
  private IActionModel iActionModel;
  @Spy
  private ParameterMemory daParams;
  @Mock
  private ServiceArguments serviceArguments;
  @Mock
  private IParameterModel parameterModel;
  @Mock
  private JsonLdContextModel jsonLdContextModel;

  @Spy
  private ResultActionSyncModel modelUnderTest;
  @Spy
  private Rdf4jClient rdf4jClient;
  @BeforeAll
  static void beforeAll() {
    actionModelFactory = mockStatic(ActionModelFactory.class);
    connectorModelFactory = mockStatic(ConnectorModelFactory.class);
    parameterModelFactory = mockStatic(ParameterModelFactory.class);
    parameterMemoryFactory = mockStatic(IParameterMemory.class);

  }


  @AfterAll
  static void afterAll() {
    actionModelFactory.close();
    connectorModelFactory.close();
    parameterModelFactory.close();
    parameterMemoryFactory.close();
  }

  @BeforeEach
  void setUp() {
    reset(connectorModel);
    reset(rdf4jClient);
    reset(modelUnderTest);
    connectorModelFactory.reset();
    actionModelFactory.reset();
    parameterModelFactory.reset();
    parameterMemoryFactory.reset();
  }

  @Test
  void run_catchIncidentException() {
    modelUnderTest.setInputParameter(parameterModel);
    doThrow(new NullPointerException("anullpointer")).when(daParams).getValue(any());
    Throwable throwable = assertThrows(IncidentException.class, () -> {
      modelUnderTest.run(daParams, jsonLdContextModel, serviceArguments);
    });
    assertEquals(SdipErrorCode.UNKNOWN_REASON_ERROR.getSdipErrorCode(),
        Integer
            .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                .getSdipErrorCodes().get(0)
                .replace("SDIP_", "")));
  }

  @Test
  void run_ok() {
    HashMap<String, List<String>> inputParam = new HashMap<>();
    inputParam.put(SDOSConstants.HTTPBODY, Collections.singletonList("abody"));
    inputParam.put(SDOSConstants.ENDPOINT, Collections.singletonList("/endpoint"));
    inputParam.put(SDOSConstants.HTTPHEADER,
        Collections.singletonList("aHeader"));
    inputParam.put(SDOSConstants.HTTPQUERYPARAM,
        Collections.singletonList("aQueryParam"));
    modelUnderTest.setInputParameter(parameterModel);
    List<String> inputKeys = new ArrayList<>();
    inputKeys.add(SDOSConstants.HTTPHEADER);
    inputKeys.add(SDOSConstants.HTTPQUERYPARAM);
    inputKeys.add(SDOSConstants.HTTPBODY);

    doReturn("http://subjectIRI").when(parameterModel).getSubjectIri();
    doReturn(inputKeys).when(parameterModel).getKeys();
    doReturn(inputParam).when(daParams).getValue("http://subjectIRI");

    modelUnderTest.run(daParams, jsonLdContextModel, serviceArguments);
    assertEquals("abody",
        daParams.getValue(SDOSConstants.SYNC_RESULT).get(SDOSConstants.GRAPH).get(0));

  }

}