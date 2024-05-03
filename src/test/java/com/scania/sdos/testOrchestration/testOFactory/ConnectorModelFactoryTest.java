package com.scania.sdos.testOrchestration.testOFactory;

import com.scania.sdos.orchestration.factory.ConnectorModelFactory;
import com.scania.sdos.orchestration.model.HttpConnectorModel;
import com.scania.sdos.orchestration.model.SoapConnectorModel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class ConnectorModelFactoryTest {

    @BeforeEach
    void setup() {
    }

    @Test
    void getSoapConnectorModel(){
        Assertions.assertEquals(SoapConnectorModel.class,
                ConnectorModelFactory.getConnector("SOAPConnector").getClass());
    }

    @Test
    void getHttpConnectorModel(){
        assertEquals(HttpConnectorModel.class,
                ConnectorModelFactory.getConnector("HTTPConnector").getClass());
    }

    @Test
    void getNoModel(){
        assertEquals(null,
                ConnectorModelFactory.getConnector("connector"));
    }
}
