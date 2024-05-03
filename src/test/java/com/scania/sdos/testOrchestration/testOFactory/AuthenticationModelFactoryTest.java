package com.scania.sdos.testOrchestration.testOFactory;

import com.scania.sdos.orchestration.factory.AuthenticationModelFactory;
import com.scania.sdos.orchestration.model.*;
import com.scania.sdos.orchestration.model.HttpBasicAuthModel;
import com.scania.sdos.orchestration.model.HttpBearerTokenAuthModel;
import com.scania.sdos.orchestration.model.SoapBasicAuthModel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class AuthenticationModelFactoryTest {

    @BeforeEach
    void setup() {
    }

    @Test
    void getSoapBasicAuthModel(){
        Assertions.assertEquals(SoapBasicAuthModel.class,
                AuthenticationModelFactory.getAuthModel("SOAPBasicAuthenticationMethod").getClass());
    }

    @Test
    void getHttpBasicAuthModel(){
        assertEquals(HttpBasicAuthModel.class,
                AuthenticationModelFactory.getAuthModel("HTTPBasicAuthenticationMethod").getClass());
    }

    @Test
    void getHttpBearerTokenAuthModel(){
        assertEquals(HttpBearerTokenAuthModel.class,
                AuthenticationModelFactory.getAuthModel("HTTPBearerTokenAuthenticationMethod").getClass());
    }

    @Test
    void getNoModel(){
        assertEquals(null,
                AuthenticationModelFactory.getAuthModel("connector"));
    }
}
