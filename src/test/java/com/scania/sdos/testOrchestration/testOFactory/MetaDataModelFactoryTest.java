package com.scania.sdos.testOrchestration.testOFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import com.scania.sdos.orchestration.factory.MetaDataModelFactory;
import com.scania.sdos.orchestration.model.ResultMetaDataModel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class MetaDataModelFactoryTest {

    @BeforeEach
    void setup() {
    }

    @Test
    void getModel(){
        Assertions.assertEquals(ResultMetaDataModel.class,
                MetaDataModelFactory.getModel().getClass());
    }
}
