package com.scania.sdos.testOrchestration.testOFactory;
import com.scania.sdos.orchestration.ParameterMemory;
import com.scania.sdos.orchestration.factory.HelpModelFactory;
import com.scania.sdos.orchestration.model.HelpModel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.reset;

@ExtendWith(MockitoExtension.class)
public class HelpModelFactoryTest {

    @Spy
    private ParameterMemory parameterMemory;

    @BeforeEach
    void setup() {
        reset(parameterMemory);
    }

    @Test
    void getHelpModel() {
        Assertions.assertEquals(HelpModel.class,
                HelpModelFactory.getInstance().getClass());
    }
}
