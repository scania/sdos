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
import com.scania.sdos.orchestration.interfaces.IParameterModel;
import com.scania.sdos.orchestration.model.JsonLdContextModel;
import com.scania.sdos.testUtils.UnitTestHelper;
import com.scania.sdos.utils.Utility;

import com.scania.sdos.testUtils.UnitTestConstants;
import org.apache.commons.lang3.NotImplementedException;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JsonLdContextModelTest {

  @Spy
  private JsonLdContextModel modelUnderTest;
  @Mock
  private Rdf4jClient rdf4jClient;
  @Mock
  private ParameterMemory daParams;
  @Mock
  private ServiceArguments serviceArguments;
  @Mock
  private IParameterModel parameterModel;
  @Mock
  private OfgModelRepo ofgModelRepo;
  @BeforeEach
  void setUp() {
    reset(modelUnderTest);
    reset(rdf4jClient);
    reset(serviceArguments);
    reset(daParams);
  }

  @AfterEach
  void tearDown() {
  }

  @Test
  void populate_ok() {
    modelUnderTest.setRdf4jClient(rdf4jClient);
    // test successful case
    String response = UnitTestHelper.readJsonFiles(UnitTestConstants.JSONLDCONTEXTMODELTESTDATA);
    JsonArray daAnswer = JsonParser.parseString(response).getAsJsonArray();
    doReturn(daAnswer).when(rdf4jClient).selectSparqlOfg(any(), any());
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();

    try {
      // run it!
      modelUnderTest.populate(UnitTestConstants.SUBJECTURI, daParams, serviceArguments);
      Assertions.assertEquals(UnitTestConstants.SUBJECTURI, modelUnderTest.getSubjectIri());
      Assertions.assertEquals(UnitTestConstants.ALABEL, modelUnderTest.getLabel());
      Assertions.assertEquals(UnitTestConstants.TEST_CONTEXT_2.replace(" ", "").replace("\\n", ""),
          modelUnderTest.getContext().replace("\\n", "").replace("\\", "").replace(" ", ""));


    } catch (Exception e) {
      Assert.fail();
    }
  }

  @Test
  void populate_emptyResponse() {
    modelUnderTest.setRdf4jClient(rdf4jClient);
    JsonArray daAnswer = JsonParser.parseString("[{}]").getAsJsonArray();
    doReturn(daAnswer).when(rdf4jClient).selectSparqlOfg(any(), any());
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      modelUnderTest.populate("anIri", daParams, serviceArguments);
    });
    assertEquals(SdipErrorCode.FAILED_TO_PARSE_JSONARRAY.getSdipErrorCode(),
        Integer
            .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                .getSdipErrorCodes().get(0)
                .replace("SDIP_", "")));
  }

  @Test
  void populate_invalidPropertyJsonArray() {
    modelUnderTest.setRdf4jClient(rdf4jClient);
    JsonArray daAnswer = JsonParser.parseString("[{\"key\":\"value\"}]").getAsJsonArray();
    doReturn(daAnswer).when(rdf4jClient).selectSparqlOfg(any(), any());
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      modelUnderTest.populate(UnitTestConstants.SUBJECTURI, daParams, serviceArguments);
    });
    assertEquals(SdipErrorCode.FAILED_TO_PARSE_JSONARRAY.getSdipErrorCode(),
        Integer
            .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                .getSdipErrorCodes().get(0)
                .replace("SDIP_", "")));
  }

  @Test
  void populate_invalidJsonArrayResponse() {
    modelUnderTest.setRdf4jClient(rdf4jClient);
    doThrow(IllegalStateException.class).when(rdf4jClient).selectSparqlOfg(any(), any());
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      modelUnderTest.populate("anIri", daParams, serviceArguments);
    });
    assertEquals(SdipErrorCode.INVALID_JSONARRAY_RESPONSE.getSdipErrorCode(),
        Integer
            .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                .getSdipErrorCodes().get(0)
                .replace("SDIP_", "")));
  }

  @Test
  void populate_malformedQuery() {
    modelUnderTest.setRdf4jClient(rdf4jClient);
    Exception ex = new NotImplementedException("it happens");
    doThrow(
        new IncidentException(ex, Utility.getErrorMessage(ex, "",
            ""), SdipErrorCode.MALFORMED_SPARQL_QUERY, null,
            "an error")).
        when(rdf4jClient).selectSparqlOfg(any(), any());
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      modelUnderTest.populate("anIri", daParams, serviceArguments);
    });
    assertEquals(SdipErrorCode.MALFORMED_SPARQL_QUERY.getSdipErrorCode(),
        Integer
            .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                .getSdipErrorCodes().get(0)
                .replace("SDIP_", "")));
  }

  @Test
  void populate_unknownParsingError() {
    modelUnderTest.setRdf4jClient(rdf4jClient);
    doThrow(
        new NotImplementedException(
            "It's your kids Marty! Something has to be done with your kids!")).when(
        rdf4jClient).selectSparqlOfg(any(), any());
    daParams.setOfgModelRepo(ofgModelRepo);
    doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      modelUnderTest.populate("anIri", daParams, serviceArguments);
    });
    assertEquals(SdipErrorCode.UNKNOWN_PARSING_ERROR.getSdipErrorCode(),
        Integer
            .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                .getSdipErrorCodes().get(0)
                .replace("SDIP_", "")));
  }
}