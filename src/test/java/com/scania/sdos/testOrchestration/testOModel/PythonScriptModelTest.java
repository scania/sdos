package com.scania.sdos.testOrchestration.testOModel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.scania.sdip.exceptions.IncidentException;
import com.scania.sdip.exceptions.SdipErrorCode;
import com.scania.sdos.model.ServiceArguments;
import com.scania.sdos.orchestration.OfgModelRepo;
import com.scania.sdos.orchestration.ParameterMemory;
import com.scania.sdos.orchestration.Rdf4jClient;
import com.scania.sdos.orchestration.model.PythonScriptModel;
import com.scania.sdos.testUtils.UnitTestHelper;
import com.scania.sdos.testUtils.UnitTestConstants;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PythonScriptModelTest {

  private static final String no_Value_Script = "[\n"
      + "  {\n"
      + "    \"script\": {\n"
      + "      \"type\": \"literal\",\n"
      + "      \"value\": \"\"\n"
      + "    },\n"
      + "    \"label\": {\n"
      + "      \"type\" : \"literal\",\n"
      + "      \"value\": \"\"\n"
      + "    }\n"
      + "  }\n"
      + "]";

  @Mock
  private ParameterMemory daParams;
  @Mock
  private ServiceArguments serviceArguments;
  @Spy
  private Rdf4jClient rdf4jClient;
  @Spy
  private PythonScriptModel scriptModel;
  @Mock
  private OfgModelRepo ofgModelRepo;

  @BeforeEach
  void setUp() {
    reset(rdf4jClient);
    reset(scriptModel);
  }


  @Test
  void populate_ok() {
    scriptModel.setRdf4jClient(rdf4jClient);

    // test successful case
    String response = UnitTestHelper.readJsonFiles(UnitTestConstants.PYTHONSCRIPTMODELTESTDATA);
    JsonArray daAnswer = JsonParser.parseString(response).getAsJsonArray();
    doReturn(daAnswer).when(rdf4jClient).selectSparqlOfg(any(), any());
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();

    try {
      scriptModel.populate(UnitTestConstants.SUBJECTURI, daParams, serviceArguments);
      assertEquals("ok", scriptModel.getScript());
      Assertions.assertEquals(UnitTestConstants.SUBJECTURI, scriptModel.getSubjectIri());
      assertEquals("aLabel", scriptModel.getLabel());
    } catch (Exception e) {
      Assert.fail();
    }
  }

  @Test
  void populate_emptyResponse() {
    scriptModel.setRdf4jClient(rdf4jClient);

    // test empty response
    JsonArray daAnswer = JsonParser.parseString("[]").getAsJsonArray();
    doReturn(daAnswer).when(rdf4jClient).selectSparqlOfg(any(), any());
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      scriptModel.populate("1", daParams, serviceArguments);
    });
    assertEquals(SdipErrorCode.INVALID_JSONARRAY_RESPONSE.getSdipErrorCode(),
        Integer
            .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                .getSdipErrorCodes().get(0)
                .replace("SDIP_", "")));
  }

  @Test
  void populate_emptyScript() {
    scriptModel.setRdf4jClient(rdf4jClient);
    // test empty script
    JsonArray daAnswer = JsonParser.parseString(no_Value_Script).getAsJsonArray();
    doReturn(daAnswer).when(rdf4jClient).selectSparqlOfg(any(), any());
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();

    try {
      scriptModel.populate(UnitTestConstants.SUBJECTURI, daParams, serviceArguments);
      assertEquals("", scriptModel.getScript());
      assertEquals("", scriptModel.getLabel());
    } catch (Exception e) {
      Assert.fail();
    }
  }

  @Test
  void populate_noScript() {
    scriptModel.setRdf4jClient(rdf4jClient);
    JsonArray daAnswer = JsonParser.parseString("[{\"key\":\"value\"}]").getAsJsonArray();
    doReturn(daAnswer).when(rdf4jClient).selectSparqlOfg(any(), any());
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      scriptModel.populate("1", daParams, serviceArguments);
    });
    assertEquals(SdipErrorCode.FAILED_TO_PARSE_JSONARRAY.getSdipErrorCode(),
        Integer
            .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                .getSdipErrorCodes().get(0)
                .replace("SDIP_", "")));
  }

  @Test
  void populate_emptyLabel() {
    scriptModel.setRdf4jClient(rdf4jClient);
    String response = UnitTestHelper.readJsonFiles(UnitTestConstants.GROOVYSCRIPTMODELTESTDATA2);
    JsonArray daAnswer = JsonParser.parseString(response).getAsJsonArray();
    doReturn(daAnswer).when(rdf4jClient).selectSparqlOfg(any(), any());
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();

    scriptModel.populate("anIri", daParams, serviceArguments);
    assertEquals("", scriptModel.getLabel());
  }

  @Test
  void populate_invalidJsonArrayResponse() {
    scriptModel.setRdf4jClient(rdf4jClient);
    doThrow(IllegalStateException.class).when(rdf4jClient).selectSparqlOfg(any(), any());
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      scriptModel.populate("1", daParams, serviceArguments);
    });
    assertEquals(SdipErrorCode.INVALID_JSONARRAY_RESPONSE.getSdipErrorCode(),
        Integer
            .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                .getSdipErrorCodes().get(0)
                .replace("SDIP_", "")));
  }

  @Test
  void populate_invalidPropertyJsonArray() {
    scriptModel.setRdf4jClient(rdf4jClient);
    JsonArray daAnswer = JsonParser.parseString("[{\"key\":\"value\"}]").getAsJsonArray();
    doReturn(daAnswer).when(rdf4jClient).selectSparqlOfg(any(), any());
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      scriptModel.populate("1", daParams, serviceArguments);
    });
    assertEquals(SdipErrorCode.FAILED_TO_PARSE_JSONARRAY.getSdipErrorCode(),
        Integer
            .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                .getSdipErrorCodes().get(0)
                .replace("SDIP_", "")));
  }

}
