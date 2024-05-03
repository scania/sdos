package com.scania.sdos.testOrchestration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.scania.sdip.exceptions.Incident;
import com.scania.sdip.exceptions.IncidentException;
import com.scania.sdip.exceptions.SdipErrorCode;
import com.scania.sdip.exceptions.SdipErrorParameter;
import com.scania.sdos.orchestration.HttpClient;
import java.io.IOException;
import java.util.HashMap;

import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class HttpClientTest {

  private static MockedStatic<? extends HttpClients> httpClients;
  private static MockedStatic<? extends EntityUtils> entityUtils;
  @Mock
  private HttpClients httpClientsMock;

  @Mock
  private StringBuilder daSb;

  @Mock
  private HttpPost httpPostMock;
  @Mock
  private HttpGet httpGetMock;

  @BeforeAll
  static void beforeAll() {
    httpClients = mockStatic(HttpClients.class);
    entityUtils = mockStatic(EntityUtils.class);
  }

  @AfterAll
  static void afterAll() {
    httpClients.close();
    entityUtils.close();
  }

  @BeforeEach
  void setUp() {
//    reset(connectorModel);
    httpClients.reset();
    entityUtils.reset();
  }

  @Test
  void httpExecute_ok_Get200() {
    try {
      HttpClient httpClient = Mockito.spy(HttpClient.class);
      CloseableHttpClient closeableHttpClientMock = mock(CloseableHttpClient.class);
      CloseableHttpResponse closeableHttpResponseMock = mock(CloseableHttpResponse.class);
      HttpEntity httpEntityMock = mock(HttpEntity.class);
      CredentialsProvider credentialsProviderMock = mock(CredentialsProvider.class);
      HttpClientBuilder httpClientBuilderMock = mock(HttpClientBuilder.class);
      StatusLine statusLineMock = mock(StatusLine.class);

      httpClients.when(() ->
          HttpClients.custom()).thenReturn(httpClientBuilderMock);
      doReturn(httpClientBuilderMock).when(httpClientBuilderMock)
          .setDefaultCredentialsProvider(credentialsProviderMock);
      doReturn(closeableHttpClientMock).when(httpClientBuilderMock).build();

      doReturn(closeableHttpResponseMock).when(closeableHttpClientMock).execute(any());
      doReturn(statusLineMock).when(closeableHttpResponseMock).getStatusLine();
      doReturn(200).when(statusLineMock).getStatusCode();
      doReturn(httpEntityMock).when(closeableHttpResponseMock).getEntity();
      entityUtils.when(() ->
          EntityUtils.toString(httpEntityMock)).thenReturn("aBody");

      String result = httpClient.httpExecute(httpGetMock, credentialsProviderMock);

      assertEquals("aBody", result);

      httpClients.verify(() -> HttpClients.custom(), times(1));
      verify(httpClientBuilderMock, times(1))
          .setDefaultCredentialsProvider(credentialsProviderMock);
      verify(httpClientBuilderMock, times(1)).build();
      verify(closeableHttpClientMock, times(1)).execute(httpGetMock);
      verify(closeableHttpResponseMock, times(1)).getStatusLine();
      verify(statusLineMock, times(1)).getStatusCode();
      verify(closeableHttpResponseMock, times(1)).getEntity();
      entityUtils.verify(() -> EntityUtils.toString(httpEntityMock), times(1));

    } catch (IOException e) {
      fail();
    }
  }

  @Test
  void httpExecute_ok_Post201() {
    try {
      HttpClient httpClient = Mockito.spy(HttpClient.class);
      CloseableHttpClient closeableHttpClientMock = mock(CloseableHttpClient.class);
      CloseableHttpResponse closeableHttpResponseMock = mock(CloseableHttpResponse.class);
      HttpEntity httpEntityMock = mock(HttpEntity.class);
      CredentialsProvider credentialsProviderMock = mock(CredentialsProvider.class);
      HttpClientBuilder httpClientBuilderMock = mock(HttpClientBuilder.class);
      StatusLine statusLineMock = mock(StatusLine.class);

      httpClients.when(() ->
          HttpClients.custom()).thenReturn(httpClientBuilderMock);
      doReturn(httpClientBuilderMock).when(httpClientBuilderMock)
          .setDefaultCredentialsProvider(credentialsProviderMock);
      doReturn(closeableHttpClientMock).when(httpClientBuilderMock).build();

      doReturn(closeableHttpResponseMock).when(closeableHttpClientMock).execute(any());
      doReturn(statusLineMock).when(closeableHttpResponseMock).getStatusLine();
      doReturn(201).when(statusLineMock).getStatusCode();
      doReturn(httpEntityMock).when(closeableHttpResponseMock).getEntity();
      entityUtils.when(() ->
          EntityUtils.toString(httpEntityMock)).thenReturn("aBody");

      String result = httpClient.httpExecute(httpPostMock, credentialsProviderMock);

      assertEquals("aBody", result);

      httpClients.verify(() -> HttpClients.custom(), times(1));
      verify(httpClientBuilderMock, times(1))
          .setDefaultCredentialsProvider(credentialsProviderMock);
      verify(httpClientBuilderMock, times(1)).build();
      verify(closeableHttpClientMock, times(1)).execute(httpPostMock);
      verify(closeableHttpResponseMock, times(1)).getStatusLine();
      verify(statusLineMock, times(1)).getStatusCode();
      verify(closeableHttpResponseMock, times(1)).getEntity();
      entityUtils.verify(() -> EntityUtils.toString(httpEntityMock), times(1));

    } catch (IOException e) {
      fail();
    }
  }

  @Test
  void httpExecute_nok_Get201() {
    try {
      HttpClient httpClient = Mockito.spy(HttpClient.class);
      CloseableHttpClient closeableHttpClientMock = mock(CloseableHttpClient.class);
      CloseableHttpResponse closeableHttpResponseMock = mock(CloseableHttpResponse.class);
      HttpEntity httpEntityMock = mock(HttpEntity.class);
      CredentialsProvider credentialsProviderMock = mock(CredentialsProvider.class);
      HttpClientBuilder httpClientBuilderMock = mock(HttpClientBuilder.class);
      StatusLine statusLineMock = mock(StatusLine.class);

      httpClients.when(() ->
          HttpClients.custom()).thenReturn(httpClientBuilderMock);
      doReturn(httpClientBuilderMock).when(httpClientBuilderMock)
          .setDefaultCredentialsProvider(credentialsProviderMock);
      doReturn(closeableHttpClientMock).when(httpClientBuilderMock).build();

      doReturn(closeableHttpResponseMock).when(closeableHttpClientMock).execute(any());
      doReturn(statusLineMock).when(closeableHttpResponseMock).getStatusLine();
      doReturn(201).when(statusLineMock).getStatusCode();
      doReturn(httpEntityMock).when(closeableHttpResponseMock).getEntity();
      entityUtils.when(() ->
          EntityUtils.toString(httpEntityMock)).thenReturn("anErrorBody");

      Throwable throwable = assertThrows(IncidentException.class, () -> {
        httpClient.httpExecute(httpGetMock, credentialsProviderMock);
      });
      // TODO: It REALLY should be a LOT easier to check the sdipErrorCode!!!!
      ResponseEntity<Incident> responseEntity = ((IncidentException) throwable)
              .createHttpErrorResponse();
      assertEquals(SdipErrorParameter.ERRORCODE_PREFIX + SdipErrorCode.HTTP_RESPONSE_NOK
                      .getSdipErrorCode(),
              responseEntity.getBody().getSdipErrorCodes().get(0));
      assertTrue(responseEntity.getBody().getMessages().get(0).contains("201 anErrorBody"));

    } catch (IOException e) {
      fail();
    }
  }

  @Test
  void httpExecute_nok_Get400() {
    try {
      HttpClient httpClient = Mockito.spy(HttpClient.class);
      CloseableHttpClient closeableHttpClientMock = mock(CloseableHttpClient.class);
      CloseableHttpResponse closeableHttpResponseMock = mock(CloseableHttpResponse.class);
      HttpEntity httpEntityMock = mock(HttpEntity.class);
      CredentialsProvider credentialsProviderMock = mock(CredentialsProvider.class);
      HttpClientBuilder httpClientBuilderMock = mock(HttpClientBuilder.class);
      StatusLine statusLineMock = mock(StatusLine.class);

      httpClients.when(() ->
          HttpClients.custom()).thenReturn(httpClientBuilderMock);
      doReturn(httpClientBuilderMock).when(httpClientBuilderMock)
          .setDefaultCredentialsProvider(credentialsProviderMock);
      doReturn(closeableHttpClientMock).when(httpClientBuilderMock).build();

      doReturn(closeableHttpResponseMock).when(closeableHttpClientMock).execute(any());
      doReturn(statusLineMock).when(closeableHttpResponseMock).getStatusLine();
      doReturn(400).when(statusLineMock).getStatusCode();
      doReturn(httpEntityMock).when(closeableHttpResponseMock).getEntity();
      entityUtils.when(() ->
          EntityUtils.toString(httpEntityMock)).thenReturn("anErrorBody");

      Throwable throwable = assertThrows(IncidentException.class, () -> {
        httpClient.httpExecute(httpGetMock, credentialsProviderMock);
      });
      // TODO: It REALLY should be a LOT easier to check the sdipErrorCode!!!!
      ResponseEntity<Incident> responseEntity = ((IncidentException) throwable)
              .createHttpErrorResponse();
      assertEquals(SdipErrorParameter.ERRORCODE_PREFIX + SdipErrorCode.HTTP_RESPONSE_NOK
                      .getSdipErrorCode(),
              responseEntity.getBody().getSdipErrorCodes().get(0));
      assertTrue(responseEntity.getBody().getMessages().get(0).contains("400 anErrorBody"));

    } catch (IOException e) {
      fail();
    }
  }

  @Test
  void httpExecute_executeThrowsClientProtocolException() {
    try {
      HttpClient httpClient = Mockito.spy(HttpClient.class);
      CloseableHttpClient closeableHttpClientMock = mock(CloseableHttpClient.class);
      CredentialsProvider credentialsProviderMock = mock(CredentialsProvider.class);
      HttpClientBuilder httpClientBuilderMock = mock(HttpClientBuilder.class);

      httpClients.when(() ->
          HttpClients.custom()).thenReturn(httpClientBuilderMock);
      doReturn(httpClientBuilderMock).when(httpClientBuilderMock)
          .setDefaultCredentialsProvider(credentialsProviderMock);
      doReturn(closeableHttpClientMock).when(httpClientBuilderMock).build();
      doThrow(ClientProtocolException.class).when(closeableHttpClientMock).execute(any());

      Throwable throwable = assertThrows(IncidentException.class, () -> {
        httpClient.httpExecute(httpGetMock, credentialsProviderMock);
      });
      // TODO: It REALLY should be a LOT easier to check the sdipErrorCode!!!!
      ResponseEntity<Incident> responseEntity = ((IncidentException) throwable)
              .createHttpErrorResponse();
      assertEquals(SdipErrorParameter.ERRORCODE_PREFIX + SdipErrorCode.HTTP_COMMUNICATION_ERROR
                      .getSdipErrorCode(),
              responseEntity.getBody().getSdipErrorCodes().get(0));

    } catch (IOException e) {
      fail();
    }
  }

  @Test
  void executeHttpGET_w_credentials_ok() {
    HttpClient httpClient = Mockito.spy(HttpClient.class);
    CredentialsProvider credentialsProviderMock = mock(CredentialsProvider.class);

    HashMap<String, String> queryParam = new HashMap<>();
    queryParam.put("aKey", "aValue");

    HashMap<String, String> headers = new HashMap<>();
    headers.put("aHeaderKey", "aHeaderValue");

    doReturn(httpGetMock).when(httpClient).getHttpGet(any());
    doReturn("aResult").when(httpClient).httpExecute(any(), any());

    String result = httpClient
        .executeHttpGET("anEndPoint", queryParam, headers, credentialsProviderMock);

    assertEquals("aResult", result);

    verify(httpGetMock, times(1)).addHeader("aHeaderKey", "aHeaderValue");
    verify(httpClient, times(1)).getHttpGet("anEndPoint?aKey=aValue");
    verify(httpClient, times(1))
        .httpExecute(httpGetMock, credentialsProviderMock);
  }

  @Test
  void executeHttpGET_wo_credentials_ok() {
    HttpClient httpClient = Mockito.spy(HttpClient.class);

    HashMap<String, String> queryParam = new HashMap<>();
    queryParam.put("aKey", "aValue");

    HashMap<String, String> headers = new HashMap<>();
    headers.put("aHeaderKey", "aHeaderValue");

    doReturn(httpGetMock).when(httpClient).getHttpGet(any());
    doReturn("aResult").when(httpClient).httpExecute(any(), any());

    String result = httpClient
        .executeHttpGET("anEndPoint", queryParam, headers, null);

    assertEquals("aResult", result);

    verify(httpGetMock, times(1)).addHeader("aHeaderKey", "aHeaderValue");
    verify(httpClient, times(1)).getHttpGet("anEndPoint?aKey=aValue");
    verify(httpClient, times(1))
        .httpExecute(httpGetMock, null);
  }

  @Test
  void executeHttpGET_withEmptyUsername_ok() {
    HttpClient httpClient = Mockito.spy(HttpClient.class);

    HashMap<String, String> queryParam = new HashMap<>();
    queryParam.put("aKey", "aValue");

    HashMap<String, String> headers = new HashMap<>();
    headers.put("aHeaderKey", "aHeaderValue");
    headers.put("username", "");

    doReturn(httpGetMock).when(httpClient).getHttpGet(any());
    doReturn("aResult").when(httpClient).httpExecute(any(), any());

    String result = httpClient
        .executeHttpGET("anEndPoint", queryParam, headers, null);

    assertEquals("aResult", result);

    verify(httpGetMock, times(1)).addHeader("aHeaderKey", "aHeaderValue");
    verify(httpClient, times(1)).getHttpGet("anEndPoint?aKey=aValue");
    verify(httpClient, times(1))
        .httpExecute(httpGetMock, null);
  }

  @Test
  void executeHttpPost_w_credentials_ok() {
    HttpClient httpClient = Mockito.spy(HttpClient.class);
    HttpEntity httpEntityMock = mock(HttpEntity.class);
    CredentialsProvider credentialsProviderMock = mock(CredentialsProvider.class);

    HashMap<String, String> queryParam = new HashMap<>();
    queryParam.put("aKey", "aValue");

    HashMap<String, String> headers = new HashMap<>();
    headers.put("aHeaderKey", "aHeaderValue");

    doReturn(httpPostMock).when(httpClient).getHttpPost(any());
    doReturn("aResult").when(httpClient).httpExecute(any(), any());

    String result = httpClient
        .executeHttpPost("anEndPoint", queryParam, headers, "data",
            credentialsProviderMock,new HashMap<>());

    assertEquals("aResult", result);

    verify(httpPostMock, times(1)).addHeader("aHeaderKey", "aHeaderValue");
    verify(httpClient, times(1)).getHttpPost("anEndPoint?aKey=aValue");
    verify(httpClient, times(1))
        .httpExecute(httpPostMock, credentialsProviderMock);
  }

  @Test
  void executeHttpPost_wo_credentials_ok() {
    HttpClient httpClient = Mockito.spy(HttpClient.class);
    HttpEntity httpEntityMock = mock(HttpEntity.class);

    HashMap<String, String> queryParam = new HashMap<>();
    queryParam.put("aKey", "aValue");

    HashMap<String, String> headers = new HashMap<>();
    headers.put("aHeaderKey", "aHeaderValue");

    doReturn(httpPostMock).when(httpClient).getHttpPost(any());
    doReturn("aResult").when(httpClient).httpExecute(any(), any());

    String result = httpClient
        .executeHttpPost("anEndPoint", queryParam, headers, "data", null,null);

    assertEquals("aResult", result);

    verify(httpPostMock, times(1)).addHeader("aHeaderKey", "aHeaderValue");
    verify(httpClient, times(1)).getHttpPost("anEndPoint?aKey=aValue");
    verify(httpClient, times(1))
        .httpExecute(httpPostMock, null);
  }

  @Test
  void executeHttpPost_witEmptyUsername_ok() {
    HttpClient httpClient = Mockito.spy(HttpClient.class);
    HttpEntity httpEntityMock = mock(HttpEntity.class);

    HashMap<String, String> queryParam = new HashMap<>();
    queryParam.put("aKey", "aValue");

    HashMap<String, String> headers = new HashMap<>();
    headers.put("aHeaderKey", "aHeaderValue");
    headers.put("username", "");

    doReturn(httpPostMock).when(httpClient).getHttpPost(any());
    doReturn("aResult").when(httpClient).httpExecute(any(), any());

    String result = httpClient
        .executeHttpPost("anEndPoint", queryParam, headers, "data", null,null);

    assertEquals("aResult", result);

    verify(httpPostMock, times(1)).addHeader("aHeaderKey", "aHeaderValue");
    verify(httpClient, times(1)).getHttpPost("anEndPoint?aKey=aValue");
    verify(httpClient, times(1))
        .httpExecute(httpPostMock, null);
  }
}
