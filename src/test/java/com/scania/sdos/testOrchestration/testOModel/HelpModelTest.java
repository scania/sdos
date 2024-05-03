package com.scania.sdos.testOrchestration.testOModel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.scania.sdip.exceptions.IncidentException;
import com.scania.sdip.exceptions.SdipErrorCode;
import com.scania.sdip.exceptions.SdipErrorParameter;
import com.scania.sdos.model.ServiceArguments;
import com.scania.sdos.orchestration.OfgModelRepo;
import com.scania.sdos.orchestration.ParameterMemory;
import com.scania.sdos.orchestration.Rdf4jClient;
import com.scania.sdos.orchestration.interfaces.IParameterModel;
import com.scania.sdos.orchestration.model.HelpModel;
import com.scania.sdos.orchestration.model.TaskModel;
import com.scania.sdos.testUtils.UnitTestHelper;
import java.util.ArrayList;
import java.util.List;

import com.scania.sdos.testUtils.UnitTestConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HelpModelTest {

  private static final Logger LOGGER = LogManager.getLogger(HelpModel.class);

  @Mock
  private ParameterMemory daParams;

  @Mock
  private ServiceArguments serviceArguments;
  @Mock
  private Rdf4jClient rdf4jClient;
  @Spy
  private HelpModel helpModel;
  @Mock
  private OfgModelRepo ofgModelRepo;
  @BeforeEach
  void setUp() {
    reset(rdf4jClient);
    reset(helpModel);
  }

  @Test
  void populate_ok() {
    helpModel.setRdf4jClient(rdf4jClient);
    TaskModel taskModelMock = mock(TaskModel.class);
    IParameterModel iParameterModelMock1 = mock(IParameterModel.class);
    IParameterModel iParameterModelMock2 = mock(IParameterModel.class);
    List<IParameterModel> iParameterModels = new ArrayList<>();
    iParameterModels.add(iParameterModelMock1);
    iParameterModels.add(iParameterModelMock2);
    String standardParamHelp = UnitTestHelper.readJsonFiles(UnitTestConstants.PARAMETERMODELINPUTHELP);
    JsonArray standardParamHelpInput = JsonParser.parseString(standardParamHelp).getAsJsonArray();
    String httpParamHelp = UnitTestHelper.readJsonFiles(UnitTestConstants.HTTPPARAMETERMODELINPUTHELP);
    JsonArray httpParamHelpInput = JsonParser.parseString(httpParamHelp).getAsJsonArray();
    String sparqlResponse = UnitTestHelper.readJsonFiles(UnitTestConstants.HELPMODELTESTDATA);
    JsonArray helpModelsparqlResponse = JsonParser.parseString(sparqlResponse).getAsJsonArray();
    String expectedString = UnitTestHelper.readJsonFiles(UnitTestConstants.HELPMODELTESTOBJECT);
    JsonObject expected = JsonParser.parseString(expectedString).getAsJsonObject();

    doReturn(helpModelsparqlResponse).when(rdf4jClient)
        .selectSparqlOfg(any(), any());
    doReturn(taskModelMock).when(helpModel).getTaskModel();
    doNothing().when(taskModelMock).populateOnlyTask(any(), any(), any());
    doReturn("label").when(taskModelMock).getLabel();
    doReturn(iParameterModels).when(taskModelMock).getInputParameters();
    doReturn("label1").when(iParameterModelMock1).getLabel();
    doReturn(standardParamHelpInput).when(iParameterModelMock1).createUserInputHelp();
    doReturn("label2").when(iParameterModelMock2).getLabel();
    doReturn(httpParamHelpInput).when(iParameterModelMock2).createUserInputHelp();

    JsonObject result = helpModel.populate(daParams, serviceArguments);
    verify(taskModelMock, times(1)).populateOnlyTask(any(), any(), any());
    verify(taskModelMock, times(2)).getLabel();
    verify(taskModelMock, times(1)).getInputParameters();
    verify(iParameterModelMock1, times(1)).createUserInputHelp();
    verify(iParameterModelMock2, times(1)).createUserInputHelp();
    assertEquals(expected, result);
  }

  @Test
  void populate_emptyResponse() {
    helpModel.setRdf4jClient(rdf4jClient);
    JsonArray daAnswer = JsonParser.parseString("[{}]").getAsJsonArray();
    doReturn(daAnswer).when(rdf4jClient).selectSparqlOfg(any(), any());

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      helpModel.populate(daParams, serviceArguments);
    });
    assertEquals(SdipErrorCode.FAILED_TO_PARSE_JSONARRAY.getSdipErrorCode(),
        Integer
            .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                .getSdipErrorCodes().get(0)
                .replace("SDIP_", "")));
  }

  @Test
  void populate_invalidJsonArrayResponse() {
    helpModel.setRdf4jClient(rdf4jClient);
    doThrow(IllegalStateException.class).when(rdf4jClient).selectSparqlOfg(any(), any());
    Throwable throwable = assertThrows(IncidentException.class, () -> {
      helpModel.populate(daParams, serviceArguments);
    });
    assertEquals(SdipErrorCode.INVALID_JSONARRAY_RESPONSE.getSdipErrorCode(),
        Integer
            .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                .getSdipErrorCodes().get(0)
                .replace("SDIP_", "")));
  }

  @Test
  void populate_invalidPropertyJsonArray() {
    helpModel.setRdf4jClient(rdf4jClient);
    JsonArray daAnswer = JsonParser.parseString("[{\"key\":\"value\"}]").getAsJsonArray();
    doReturn(daAnswer).when(rdf4jClient).selectSparqlOfg(any(), any());
    Throwable throwable = assertThrows(IncidentException.class, () -> {
      helpModel.populate(daParams, serviceArguments);
    });
    assertEquals(SdipErrorCode.FAILED_TO_PARSE_JSONARRAY.getSdipErrorCode(),
        Integer
            .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                .getSdipErrorCodes().get(0)
                .replace("SDIP_", "")));
  }

  @Test
  void populate_catchGenericException() {
    helpModel.setRdf4jClient(rdf4jClient);
    doThrow(RuntimeException.class).when(rdf4jClient).selectSparqlOfg(any(), any());
    Throwable throwable = assertThrows(IncidentException.class, () -> {
      helpModel.populate(daParams, serviceArguments);
    });
    assertEquals(SdipErrorCode.UNKNOWN_PARSING_ERROR.getSdipErrorCode(),
        Integer
            .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                .getSdipErrorCodes().get(0)
                .replace("SDIP_", "")));
  }

  @Test
  void populate_catchException() {
    helpModel.setRdf4jClient(rdf4jClient);
    String message = "Host name may not be null";
    doThrow(new IncidentException(SdipErrorCode.UNKNOWN_REASON_ERROR, LOGGER, message,
        SdipErrorParameter.SUPPORTMAIL))
        .when(rdf4jClient).selectSparqlOfg(any(), any());

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      helpModel.populate(daParams, serviceArguments);
    });

    assertEquals(SdipErrorCode.UNKNOWN_REASON_ERROR.getSdipErrorCode(),
        Integer
            .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                .getSdipErrorCodes().get(0)
                .replace("SDIP_", "")));
    assertEquals(2, ((IncidentException) throwable).getErrorBindings().get(0).getArgs().length);
    Object[] args = ((IncidentException) throwable).getErrorBindings().get(0).getArgs();
    assertEquals(message, args[0]);
    assertEquals(SdipErrorParameter.SUPPORTMAIL, args[1]);
  }
}
