package com.scania.sdos.testOrchestration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.reset;

import com.scania.sdos.orchestration.OfgModelRepo;
import com.scania.sdos.orchestration.ParameterMemory;
import com.scania.sdos.orchestration.interfaces.IParameterMemory;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import com.scania.sdos.testUtils.UnitTestConstants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IParameterMemoryTest {

  @Spy
  private IParameterMemory iParameterMemory;
  @Mock
  private OfgModelRepo ofgModelRepo;

  @BeforeEach
  void setUp() {
    reset(iParameterMemory);
    iParameterMemory = new ParameterMemory();
  }

  @Test
  void putParameter() {
    HashMap hashMap = new HashMap();
    hashMap.put(UnitTestConstants.USERNAME, "user1");
    iParameterMemory.putParameter(UnitTestConstants.SUBJECTURI, hashMap);

    assertEquals("user1", iParameterMemory.getValue(UnitTestConstants.SUBJECTURI).get(UnitTestConstants.USERNAME));
  }
  @Test
  void testSetGetOfgModelRepo() {
    iParameterMemory.setOfgModelRepo(ofgModelRepo);
    Assertions.assertEquals(ofgModelRepo, iParameterMemory.getOfgModelRepo());
  }
  @Test
  void putParameter_clear() {
    HashMap hashMap = new HashMap();
    hashMap.put(UnitTestConstants.USERNAME, "user1");
    iParameterMemory.putParameter(UnitTestConstants.SUBJECTURI, hashMap);

    assertEquals("user1", iParameterMemory.getValue(UnitTestConstants.SUBJECTURI).get(UnitTestConstants.USERNAME));

    iParameterMemory.clear();
    assertThrows(NullPointerException.class,
        () -> iParameterMemory.getValue(UnitTestConstants.SUBJECTURI).get(UnitTestConstants.USERNAME));

  }

  @Test
  void putParameter_ok() {
    List<String> users = new ArrayList<>();
    users.add("user1");
    users.add("user2");
    HashMap<String, List<String>> hashMap = new HashMap();
    hashMap.put(UnitTestConstants.USERNAME, users);
    hashMap.put(UnitTestConstants.PASSWORD, Collections.singletonList("pass"));
    iParameterMemory.putParameter(UnitTestConstants.SUBJECTURI, hashMap);
    assertEquals("user1", iParameterMemory.getValue(UnitTestConstants.SUBJECTURI).get(UnitTestConstants.USERNAME).get(0));
    assertEquals(users, iParameterMemory.getValue(UnitTestConstants.SUBJECTURI).get(UnitTestConstants.USERNAME));
  }

  @Test
  void putParameter_itemAlreadyExist_ok() {
    List<String> users = new ArrayList<>();
    users.add("user1");
    users.add("user2");
    HashMap<String, List<String>> hashMap = new HashMap();
    hashMap.put(UnitTestConstants.USERNAME, users);
    iParameterMemory.putParameter(UnitTestConstants.SUBJECTURI, hashMap);
    users = new ArrayList<>();
    users.add("user3");
    hashMap = new HashMap();
    hashMap.put(UnitTestConstants.USERNAME, users);
    iParameterMemory.putParameter(UnitTestConstants.SUBJECTURI, hashMap);
    assertEquals("user1", iParameterMemory.getValue(UnitTestConstants.SUBJECTURI).get(UnitTestConstants.USERNAME).get(0));
    assertTrue(iParameterMemory.getValue(UnitTestConstants.SUBJECTURI).get(UnitTestConstants.USERNAME).contains("user3"));
    assertTrue(iParameterMemory.getValue(UnitTestConstants.SUBJECTURI).get(UnitTestConstants.USERNAME).contains("user2"));
    assertTrue(iParameterMemory.getValue(UnitTestConstants.SUBJECTURI).get(UnitTestConstants.USERNAME).contains("user1"));
  }


}
