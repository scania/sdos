package com.scania.sdos.testOrchestration.testOFactory;
import com.scania.sdos.orchestration.ParameterMemory;
import com.scania.sdos.orchestration.factory.ScriptModelFactory;
import com.scania.sdos.orchestration.model.GroovyScriptModel;
import com.scania.sdos.orchestration.model.PythonScriptModel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.reset;

@ExtendWith(MockitoExtension.class)
public class ScriptModelFactoryTest {
    @Spy
    private ParameterMemory parameterMemory;

    @BeforeEach
    void setup() {
        reset(parameterMemory);
    }

    @Test
    void getGroovyScript() {
        Assertions.assertEquals(GroovyScriptModel.class,
                ScriptModelFactory.getScript("GroovyScript").getClass());
    }

    @Test
    void getPythonScript() {
        Assertions.assertEquals(PythonScriptModel.class,
                ScriptModelFactory.getScript("PythonScript").getClass());
    }

    @Test
    void getModelNotMatch() {
        Assertions.assertEquals(null,
                ScriptModelFactory.getScript("Script"));
    }
}
