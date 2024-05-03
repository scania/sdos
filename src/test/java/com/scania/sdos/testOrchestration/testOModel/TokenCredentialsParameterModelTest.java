package com.scania.sdos.testOrchestration.testOModel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.scania.sdip.exceptions.IncidentException;
import com.scania.sdip.exceptions.SdipErrorCode;
import com.scania.sdos.model.ServiceArguments;
import com.scania.sdos.orchestration.OfgModelRepo;
import com.scania.sdos.orchestration.ParameterMemory;
import com.scania.sdos.orchestration.Rdf4jClient;
import com.scania.sdos.orchestration.model.TokenCredentialsParameterModel;
import com.scania.sdos.testUtils.UnitTestConstants;
import com.scania.sdos.testUtils.UnitTestHelper;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TokenCredentialsParameterModelTest {

    private String subjectIri = "aSubjectIRI";

    @Mock
    private ParameterMemory daParams;
    @Mock
    private ServiceArguments serviceArguments;
    @Mock
    private Rdf4jClient rdf4jClient;
    @Spy
    private TokenCredentialsParameterModel tokenCredentialsParameterModel;
    @Mock
    private OfgModelRepo ofgModelRepo;

    @BeforeEach
    void setUp() {
        reset(daParams);
        reset(rdf4jClient);
        reset(tokenCredentialsParameterModel);
        reset(serviceArguments);
    }

    @Test
    void getKeys_getValues_ok() {
        tokenCredentialsParameterModel.setToken("testtoken");
        List<String> keys = tokenCredentialsParameterModel.getKeys();
        HashMap<String, String> hashMap = tokenCredentialsParameterModel.getValue();
        assertTrue(keys.contains(UnitTestConstants.TOKEN));
        assertEquals("testtoken",hashMap.get(UnitTestConstants.TOKEN));
    }

    @Test
    void populate_ok() {
        tokenCredentialsParameterModel.setRdf4jClient(rdf4jClient);
        String response = UnitTestHelper.readJsonFiles(UnitTestConstants.TOKENCREDENTIALsPARAMETERGETDATA);
        JsonArray daAnswer = JsonParser.parseString(response).getAsJsonArray();
        doReturn(daAnswer).when(rdf4jClient).selectSparqlOfg(any(), any());
        daParams.setOfgModelRepo(ofgModelRepo);
        doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();
        tokenCredentialsParameterModel.populate(subjectIri, daParams, serviceArguments);

        assertEquals("param_token_credentials", tokenCredentialsParameterModel.getLabel());
        assertEquals(subjectIri, tokenCredentialsParameterModel.getSubjectIri());
        verify(tokenCredentialsParameterModel, times(1)).setSubjectIri(any());
        verify(tokenCredentialsParameterModel, times(1)).setLabel(any());
    }

    @Test
    void populate_emptyResponse_nok() {
        tokenCredentialsParameterModel.setRdf4jClient(rdf4jClient);
        doReturn(new JsonArray()).when(rdf4jClient).selectSparqlOfg(any(), any());
        daParams.setOfgModelRepo(ofgModelRepo);
        doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();

        Throwable throwable = assertThrows(IncidentException.class, () -> {
            tokenCredentialsParameterModel.populate("aSubjectIRI", daParams, serviceArguments);
        });
        assertEquals(SdipErrorCode.INVALID_JSONARRAY_RESPONSE.getSdipErrorCode(),
            Integer
                .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                    .getSdipErrorCodes().get(0)
                    .replace(UnitTestConstants.SDIP, "")));
    }

    @Test
    void populate_invalidPropertyJsonArray() {
        tokenCredentialsParameterModel.setRdf4jClient(rdf4jClient);
        JsonArray daAnswer = JsonParser.parseString("[{\"key\":\"value\"}]").getAsJsonArray();
        doReturn(daAnswer).when(rdf4jClient).selectSparqlOfg(any(), any());
        daParams.setOfgModelRepo(ofgModelRepo);
        doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();

        Throwable throwable = assertThrows(IncidentException.class, () -> {
            tokenCredentialsParameterModel.populate(subjectIri, daParams, serviceArguments);
        });
        assertEquals(SdipErrorCode.FAILED_TO_PARSE_JSONARRAY.getSdipErrorCode(),
            Integer
                .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                    .getSdipErrorCodes().get(0)
                    .replace(UnitTestConstants.SDIP, "")));
    }

    @Test
    void populate_invalidJsonArrayResponse() {
        tokenCredentialsParameterModel.setRdf4jClient(rdf4jClient);
        doThrow(IllegalStateException.class).when(rdf4jClient).selectSparqlOfg(any(), any());
        daParams.setOfgModelRepo(ofgModelRepo);
        doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();

        Throwable throwable = assertThrows(IncidentException.class, () -> {
            tokenCredentialsParameterModel.populate(subjectIri, daParams, serviceArguments);
        });
        assertEquals(SdipErrorCode.INVALID_JSONARRAY_RESPONSE.getSdipErrorCode(),
            Integer
                .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                    .getSdipErrorCodes().get(0)
                    .replace(UnitTestConstants.SDIP, "")));
    }

    @Test
    void populate_catchException() {
        tokenCredentialsParameterModel.setRdf4jClient(rdf4jClient);
        doThrow(RuntimeException.class).when(rdf4jClient).selectSparqlOfg(any(), any());
        daParams.setOfgModelRepo(ofgModelRepo);
        doReturn(ofgModelRepo).when(daParams).getOfgModelRepo();

        Throwable throwable = assertThrows(IncidentException.class, () -> {
            tokenCredentialsParameterModel.populate(subjectIri, daParams, serviceArguments);
        });
        assertEquals(SdipErrorCode.UNKNOWN_PARSING_ERROR.getSdipErrorCode(),
            Integer
                .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                    .getSdipErrorCodes().get(0)
                    .replace(UnitTestConstants.SDIP, "")));
    }

    @Test
    void createUserInputHelp() {
        String expected = "[{\"key\":\"token\",\"value\":\"\"}]";
        JsonArray userInputHelp = tokenCredentialsParameterModel.createUserInputHelp();
        assertEquals(1, userInputHelp.size());
        assertEquals(expected, userInputHelp.toString());
    }
}
