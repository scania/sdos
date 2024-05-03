package com.scania.sdos.testUtils;
import com.scania.sdos.orchestration.ParameterMemory;
import com.scania.sdos.utils.SDOSConstants;
import com.scania.sdos.utils.Utility;
import com.google.gson.JsonArray;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.reset;

@ExtendWith(MockitoExtension.class)
public class UtilityTest {

    @Spy
    private ParameterMemory parameterMemory;

    @BeforeEach
    void setup() {
        reset(parameterMemory);
    }

    @Test
    void getHashMap() {
        assertEquals(HashMap.class,
                Utility.getStaticHashmap().getClass());
    }

    @Test
    void extractDateTime() {
        String time = "2023-11-27T10:15:30";
        LocalDateTime localTimeObj = LocalDateTime.parse(time);

        String datetime = Utility.getCurrentDateAndTime(localTimeObj, SDOSConstants.RESULTGRAPH_PATTERN);
        assertEquals("2023112710153000" , datetime);

        datetime = Utility.getCurrentDateAndTime(localTimeObj);
        assertEquals("2023112710153000" , datetime);
    }

    @Test
    void getUUID4String() {
        String uUid = Utility.getUUID4String();
        assertEquals(String.class, uUid.getClass());
    }

    @Test
    void jsonArray() {
        assertEquals(JsonArray.class, Utility.getStaticJsonArray().getClass());
    }

    @Test
    void getRegexMatch() {
        String match = Utility.getRegexMatch("Test", "es");
        assertEquals("es", match);

        match = Utility.getRegexMatch("Test", "pq");
        assertEquals(null, match);
    }

    @Test
    void getRegexMatchByGroup() {
        String match = Utility.getRegexMatchByGroup("123 456", "(\\d+) (\\d+)", 1);
        assertEquals("123", match);

        match = Utility.getRegexMatchByGroup("123456", "(\\d+) (\\d+)", 2);
        assertEquals(null, match);
    }
    @Test
    void getFileFromResource() {
        InputStream inputStream = Utility.getFileFromResourcesAsInputstream("AuthModelTestData.json");
        assertEquals(BufferedInputStream.class, inputStream.getClass());
    }
}

