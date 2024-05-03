package com.scania.sdos.testOrchestration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.scania.sdos.orchestration.HttpClient;
import com.scania.sdos.orchestration.SoapClient;
import com.scania.sdos.testUtils.MockRdfStoreServer2;
import com.scania.sdos.testUtils.UnitTestHelper;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;

import com.scania.sdos.testUtils.UnitTestConstants;
import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ModelClientTest {

  @ClassRule
  public static MockRdfStoreServer2 rdfStore = new MockRdfStoreServer2();
  private final String GRAPH = "graph";

  @Test
  void test_httpClient_executeHttpPost() {

    String endpoint = "http://localhost:" + rdfStore.getPortNumber() + "/test/httpRequestPost";
    HashMap queryParam = new HashMap();
    queryParam.put(GRAPH, UnitTestConstants.TEST);
    HashMap header = new HashMap();
    header.put(UnitTestConstants.USERNAME, "user1");
    header.put(UnitTestConstants.PASSWORD, "test");
    HttpEntity entity = null;
    try {
      entity = new StringEntity("{ }");
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
      Assert.fail();
    }
    HttpClient httpClient = new HttpClient();

    String response = UnitTestHelper.readJsonFiles(UnitTestConstants.HTTPCLIENTPOSTRESPONSE);
    String daAnswer = JsonParser.parseString(response).getAsJsonObject()
        .toString()
        .replace("\\n", "").replace("\\t", "").replace(" ", "").trim();

    String httpClientPostResponse = httpClient
        .executeHttpPost(endpoint, queryParam, header, "data", null, null)
        .replace("\n", "").replace(" ", "").trim();

    assertEquals(daAnswer, httpClientPostResponse);
  }


  @Test
  void test_getValueFromResponse() {
    SoapClient soapClient = new SoapClient();
    try {
      List response = soapClient.getValueFromResponse(UnitTestConstants.GROOVYSCRIPT2, UnitTestConstants.POLARIONRESPONSE2);
      assertEquals("http://ws.polarion.com/session",
          ((HashMap) response.get(0)).get("nameSpaceURI").toString());
      assertEquals("sessionID", ((HashMap) response.get(0)).get("localPart").toString());
      assertEquals("-12345", ((HashMap) response.get(0)).get("value").toString());
    } catch (Exception e) {
      Assert.fail();
    }
  }
}
