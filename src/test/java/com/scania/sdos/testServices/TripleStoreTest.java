package com.scania.sdos.testServices;

import com.scania.sdos.model.TripleStoreType;
import com.scania.sdos.triplestores.TripleStore;
import com.scania.sdos.triplestores.TripleStoreFactory;
import com.scania.sdos.utils.Utility;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

public class TripleStoreTest {

  @Test
  void tripleStoreType_generic() {

    //Act&Assert
    try {
      TripleStore tripleStore = TripleStoreFactory
          .getTripleStore(TripleStoreType.GENERIC, "http://endpoint");
      Assert.assertEquals("http://endpoint", tripleStore.getEndpoint());
      Assert.assertTrue(Utility.regexMatchCheck(tripleStore.toString().toLowerCase(), "generic"));
    } catch (Exception e) {
      Assert.fail();
    }
  }

  @Test
  void tripleStoreType_graphdb() {

    //Act&Assert
    try {
      TripleStore tripleStore = TripleStoreFactory
          .getTripleStore(TripleStoreType.GRAPHDB, "http://repositories/endpoint/statements");
      Assert.assertEquals("http://repositories/endpoint/statements", tripleStore.getEndpoint());
      Assert.assertTrue(Utility.regexMatchCheck(tripleStore.toString().toLowerCase(), "graphdb"));
    } catch (Exception e) {
      Assert.fail();
    }
  }

  @Test
  void tripleStoreType_blazegraph() {

    //Act&Assert
    try {
      TripleStore tripleStore = TripleStoreFactory
          .getTripleStore(TripleStoreType.BLAZEGRAPH, "http://endpoint");
      Assert.assertEquals("http://endpoint", tripleStore.getEndpoint());
      Assert
          .assertTrue(Utility.regexMatchCheck(tripleStore.toString().toLowerCase(), "blazegraph"));
    } catch (Exception e) {
      Assert.fail();
    }
  }

  @Test
  void tripleStoreType_neptune() {

    //Act&Assert
    try {
      TripleStore tripleStore = TripleStoreFactory
          .getTripleStore(TripleStoreType.NEPTUNE, "http://endpoint");
      Assert.assertEquals("http://endpoint", tripleStore.getEndpoint());
      Assert.assertTrue(Utility.regexMatchCheck(tripleStore.toString().toLowerCase(), "neptune"));
    } catch (Exception e) {
      Assert.fail();
    }
  }
}
