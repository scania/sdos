package com.scania.sdos.testOrchestration.testOModel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.scania.sdip.exceptions.IncidentException;
import com.scania.sdip.exceptions.SdipErrorCode;
import com.scania.sdip.exceptions.SdipErrorParameter;
import com.scania.sdos.model.ServiceArguments;
import com.scania.sdos.orchestration.OfgModelRepo;
import com.scania.sdos.orchestration.ParameterMemory;
import com.scania.sdos.orchestration.Rdf4jClient;
import com.scania.sdos.orchestration.model.SparqlQueryParameterModel;
import com.scania.sdos.testUtils.UnitTestConstants;
import com.scania.sdos.testUtils.UnitTestHelper;
import com.scania.sdos.utils.SDOSConstants;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SparqlQueryParameterModelTest {

    private static final Logger LOGGER = LogManager.getLogger(SparqlQueryParameterModelTest.class);

    private String subjectUri = "http://testsubjecturi";

    @Mock
    private ParameterMemory daParams;
    @Mock
    private ServiceArguments serviceArguments;
    @Mock
    private List listMock;
    @Mock
    private HashMap hashMapMock;
    @Spy
    private SparqlQueryParameterModel modelUnderTest;
    @Mock
    private Rdf4jClient rdf4jClient;
    @Mock
    private OfgModelRepo ofgModelRepo;

    @BeforeEach
    void setUp() {
        reset(rdf4jClient);
        reset(hashMapMock);
        reset(listMock);
        reset(modelUnderTest);
        reset(daParams);
    }

    void setupEndpointMock() {
        when(daParams.getValue(SDOSConstants.OFG_ENDPOINT)).thenReturn(hashMapMock);
        when(hashMapMock.get(any())).thenReturn(listMock);
        when(listMock.get(anyInt())).thenReturn(UnitTestConstants.ENDPOINT);
    }

    @Test
    void populate_ok_() {
        ParameterMemory parameterMemory = new ParameterMemory();
        modelUnderTest.setRdf4jClient(rdf4jClient);
        String response = UnitTestHelper.readJsonFiles("SparqlQueryParameterModelTestData.json");
        JsonArray daAnswer = JsonParser.parseString(response).getAsJsonArray();
        doReturn(daAnswer).when(rdf4jClient).selectSparqlOfg(any(), any());

        parameterMemory.setOfgModelRepo(ofgModelRepo);

        modelUnderTest.populate(subjectUri, parameterMemory, serviceArguments);
        Assertions.assertEquals(UnitTestConstants.SPARQLQUERY, modelUnderTest.getQuery());
        assertEquals("param_query_part", modelUnderTest.getLabel());
        Assertions.assertEquals(UnitTestConstants.SPARQLQUERY, parameterMemory.getValue(subjectUri).get(UnitTestConstants.QUERY).get(0));
    }

    @Test
    void populate_emptyResponse() {
        modelUnderTest.setRdf4jClient(rdf4jClient);
        JsonArray daAnswer = JsonParser.parseString("[]").getAsJsonArray();
        doReturn(daAnswer).when(rdf4jClient).selectSparqlOfg(any(), any());
        daParams.setOfgModelRepo(ofgModelRepo);
        doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();
        Throwable throwable = assertThrows(IncidentException.class, () -> {
            modelUnderTest.populate(subjectUri, daParams, serviceArguments);
        });
        assertEquals(SdipErrorCode.INVALID_JSONARRAY_RESPONSE.getSdipErrorCode(),
            Integer
                .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                    .getSdipErrorCodes().get(0)
                    .replace(UnitTestConstants.SDIP, "")));
    }

    @Test
    void populate_invalidJsonArrayResponse() {
        modelUnderTest.setRdf4jClient(rdf4jClient);
        doThrow(IllegalStateException.class).when(rdf4jClient).selectSparqlOfg(any(), any());
        daParams.setOfgModelRepo(ofgModelRepo);
        doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();
        Throwable throwable = assertThrows(IncidentException.class, () -> {
            modelUnderTest.populate("1", daParams, serviceArguments);
        });
        assertEquals(SdipErrorCode.INVALID_JSONARRAY_RESPONSE.getSdipErrorCode(),
            Integer
                .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                    .getSdipErrorCodes().get(0)
                    .replace(UnitTestConstants.SDIP, "")));
    }

    @Test
    void populate_invalidPropertyJsonArray() {
        modelUnderTest.setRdf4jClient(rdf4jClient);
        JsonArray daAnswer = JsonParser.parseString("[{\"key\":\"value\"}]").getAsJsonArray();
        doReturn(daAnswer).when(rdf4jClient).selectSparqlOfg(any(), any());
        daParams.setOfgModelRepo(ofgModelRepo);
        doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();
        Throwable throwable = assertThrows(IncidentException.class, () -> {
            modelUnderTest.populate("1", daParams, serviceArguments);
        });
        assertEquals(SdipErrorCode.FAILED_TO_PARSE_JSONARRAY.getSdipErrorCode(),
            Integer
                .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                    .getSdipErrorCodes().get(0)
                    .replace(UnitTestConstants.SDIP, "")));
    }

    @Test
    void populate_unknownError() {
        modelUnderTest.setRdf4jClient(rdf4jClient);
        doThrow(RuntimeException.class).when(rdf4jClient).selectSparqlOfg(any(), any());
        daParams.setOfgModelRepo(ofgModelRepo);
        doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();
        Throwable throwable = assertThrows(IncidentException.class, () -> {
            modelUnderTest.populate("1", daParams, serviceArguments);
        });
        assertEquals(SdipErrorCode.UNKNOWN_PARSING_ERROR.getSdipErrorCode(),
            Integer
                .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                    .getSdipErrorCodes().get(0)
                    .replace(UnitTestConstants.SDIP, "")));
    }

    @Test
    void populate_catchException() {
        modelUnderTest.setRdf4jClient(rdf4jClient);
        String message = "Host name may not be null";
        doThrow(new IncidentException(SdipErrorCode.UNKNOWN_REASON_ERROR, LOGGER, message,
            SdipErrorParameter.SUPPORTMAIL))
            .when(rdf4jClient).selectSparqlOfg(any(), any());
        daParams.setOfgModelRepo(ofgModelRepo);
        doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();

        Throwable throwable = assertThrows(IncidentException.class, () -> {
            modelUnderTest.populate("1", daParams, serviceArguments);
        });

        assertEquals(SdipErrorCode.UNKNOWN_REASON_ERROR.getSdipErrorCode(),
            Integer
                .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                    .getSdipErrorCodes().get(0)
                    .replace(UnitTestConstants.SDIP, "")));
        assertEquals(2, ((IncidentException) throwable).getErrorBindings().get(0).getArgs().length);
        Object[] args = ((IncidentException) throwable).getErrorBindings().get(0).getArgs();
        assertEquals(message, args[0]);
        assertEquals(SdipErrorParameter.SUPPORTMAIL, args[1]);
    }

    @Test
    void test_getKey() {
        Assertions.assertEquals(UnitTestConstants.QUERY,modelUnderTest.getKeys().get(0));
    }

    @Test
    void test_getValue() {
        modelUnderTest.setQuery(UnitTestConstants.SPARQLQUERY);
        Assertions.assertEquals(UnitTestConstants.SPARQLQUERY,modelUnderTest.getValue().get(UnitTestConstants.QUERY).get(0));

    }

}
