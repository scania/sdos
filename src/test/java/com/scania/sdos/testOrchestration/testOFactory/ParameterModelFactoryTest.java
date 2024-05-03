package com.scania.sdos.testOrchestration.testOFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.reset;
import com.scania.sdos.orchestration.ParameterMemory;
import com.scania.sdos.orchestration.factory.ParameterModelFactory;
import com.scania.sdos.orchestration.model.BasicCredentialsParameterModel;
import com.scania.sdos.orchestration.model.HttpParameterModel;
import com.scania.sdos.orchestration.model.StandardParameterModel;
import com.scania.sdos.orchestration.model.TokenCredentialsParameterModel;
import com.scania.sdos.orchestration.model.SparqlQueryParameterModel;
import com.scania.sdos.utils.SDOSConstants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ParameterModelFactoryTest {

    @Spy
    private ParameterMemory parameterMemory;

    @BeforeEach
    void setup() {
        reset(parameterMemory);
    }

    @Test
    void getHttpParameter() {
        Assertions.assertEquals(HttpParameterModel.class,
                ParameterModelFactory.getParameter(SDOSConstants.HTTPPARAMETER).getClass());
    }

    @Test
    void getStandardParameter() {
        Assertions.assertEquals(StandardParameterModel.class,
                ParameterModelFactory.getParameter(SDOSConstants.STANDARDPARAMETER).getClass());
    }

    @Test
    void getBasicCredentialsParameter() {
        Assertions.assertEquals(BasicCredentialsParameterModel.class,
                ParameterModelFactory.getParameter(SDOSConstants.BASICCREDENTIALSPARAMETER).getClass());
    }

    @Test
    void getTokenCredentialsParameter() {
        Assertions.assertEquals(TokenCredentialsParameterModel.class,
                ParameterModelFactory.getParameter(SDOSConstants.TOKENCREDENTIALSPARAMETER).getClass());
    }

    @Test
    void getSparqlQueryParameter() {
        Assertions.assertEquals(SparqlQueryParameterModel.class,
                ParameterModelFactory.getParameter(SDOSConstants.SPARQLQUERYPARAMETER).getClass());
    }

    @Test
    void getModelNotMatch() {
        Assertions.assertEquals(null,
                ParameterModelFactory.getParameter("tesl"));
    }

}
