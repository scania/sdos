package com.scania.sdos.testJwt;

import com.scania.sdip.exceptions.IncidentException;
import com.scania.sdos.jwt.JwtTokenUtil;
import com.scania.sdos.orchestration.interfaces.IParameterMemory;
import com.scania.sdos.testUtils.UnitTestConstants;
import com.scania.sdos.utils.SDOSConstants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class JwtTokenUtilTest {
    @Mock
    private IParameterMemory parameterMemory;

    @Spy
    private JwtTokenUtil jwtTokenUtil;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void jwtTokenExists_doesExist() {
        when(parameterMemory.getValue(SDOSConstants.OBO_TOKEN)).thenReturn(getTestMap());
        assertEquals(true, JwtTokenUtil.oboTokenExists(parameterMemory));
    }

    @Test
    void jwtTokenExists_doesNotExist() {
        when(parameterMemory.getValue(SDOSConstants.OBO_TOKEN)).thenReturn(null);
        assertEquals(false, JwtTokenUtil.oboTokenExists(parameterMemory));
    }

    @Test
    void getJwtToken() {
        when(parameterMemory.getValue(SDOSConstants.BEARER_TOKEN)).thenReturn(getTestMap());
        String token = parameterMemory.getValue(SDOSConstants.BEARER_TOKEN).get(SDOSConstants.BEARER_TOKEN).get(0);
        Assertions.assertEquals(UnitTestConstants.DUMMY_JWT_TOKEN, token);
    }

    @Test
    void saveJwtToken_doesAlreadyExist() {
        when(parameterMemory.getValue(SDOSConstants.BEARER_TOKEN)).thenReturn(getTestMap());
        JwtTokenUtil.saveJwtToken(UnitTestConstants.DUMMY_JWT_TOKEN, parameterMemory);
        ArgumentCaptor<HashMap<String, List<String>>> captor = ArgumentCaptor.forClass(HashMap.class);
        verify(parameterMemory, times(1))
                .replaceParameter(eq(SDOSConstants.BEARER_TOKEN), captor.capture());
    }

    @Test
    void saveJwtToken_doesNotYetExist() {
        when(parameterMemory.getValue(SDOSConstants.BEARER_TOKEN)).thenReturn(null);
        JwtTokenUtil.saveJwtToken(UnitTestConstants.DUMMY_JWT_TOKEN, parameterMemory);
        ArgumentCaptor<HashMap<String, List<String>>> captor = ArgumentCaptor.forClass(HashMap.class);
        verify(parameterMemory, times(1))
                .putParameter(eq(SDOSConstants.BEARER_TOKEN), captor.capture());
        assertEquals(getTestMap(), captor.getValue());
    }

    @Test
    void saveOBOToken_doesAlreadyExist() {
        when(parameterMemory.getValue(SDOSConstants.OBO_TOKEN)).thenReturn(getOBOTestMap());
        JwtTokenUtil.saveOboToken(UnitTestConstants.DUMMY_OBO_TOKEN, parameterMemory);
        ArgumentCaptor<HashMap<String, List<String>>> captor = ArgumentCaptor.forClass(HashMap.class);
        verify(parameterMemory, times(1))
                .replaceParameter(eq(SDOSConstants.OBO_TOKEN), captor.capture());
    }

    @Test
    void saveOBOToken_doesNotYetExist() {
        when(parameterMemory.getValue(SDOSConstants.OBO_TOKEN)).thenReturn(null);
        JwtTokenUtil.saveOboToken(UnitTestConstants.DUMMY_OBO_TOKEN, parameterMemory);
        ArgumentCaptor<HashMap<String, List<String>>> captor = ArgumentCaptor.forClass(HashMap.class);
        verify(parameterMemory, times(1))
                .putParameter(eq(SDOSConstants.OBO_TOKEN), captor.capture());
        assertEquals(getOBOTestMap(), captor.getValue());
    }

    private HashMap<String, List<String>> getOBOTestMap() {
        HashMap<String, List<String>> testMap = new HashMap<>();
        testMap.put(SDOSConstants.VALUE, Collections.singletonList(UnitTestConstants.DUMMY_OBO_TOKEN));
        return testMap;
    }

    private HashMap<String, List<String>> getTestMap() {
        HashMap<String, List<String>> testMap = new HashMap<>();
        testMap.put(SDOSConstants.BEARER_TOKEN, Collections.singletonList(UnitTestConstants.DUMMY_JWT_TOKEN));
        return testMap;
    }


    @Test
    void extractToken() {
        String unique_name = jwtTokenUtil.tokenUserId(UnitTestConstants.DUMMY_JWT_TOKEN);
        assertEquals("ravigyan.singh@scania.com" , unique_name);
    }

    //@Test
    void extractToken_Error() {
        assertThrows(IncidentException.class, () -> {
            JwtTokenUtil.tokenUserId(UnitTestConstants.DUMMY_JWT_TOKEN_ERROR);
        });
    }

}