package com.scania.sdos.testOrchestration.testOFactory;

import com.scania.sdos.orchestration.OfgModelRepo;
import com.scania.sdos.orchestration.factory.OfgModelRepoFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class OfgModelRepoFactoryTest {


    @BeforeEach
    void setup() {
    }

    @Test
    void getModelRepo(){
        Assertions.assertEquals(OfgModelRepo.class,
                OfgModelRepoFactory.getModelRepo().getClass());
    }
}
