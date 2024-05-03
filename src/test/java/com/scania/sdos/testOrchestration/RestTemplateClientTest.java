package com.scania.sdos.testOrchestration;

import com.scania.sdos.orchestration.RestTemplateClient;

import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URISyntaxException;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;

@ExtendWith(MockitoExtension.class)
class RestTemplateClientTest {

    private static MockedStatic<? extends EntityUtils> entityUtils;

    @Mock
    private RestTemplate restTemplateMock;

    @Mock
    private HttpEntity httpEntity;

    @InjectMocks
    private RestTemplateClient restTemplateClient;

    @BeforeAll
    static void beforeAll() {
        entityUtils = mockStatic(EntityUtils.class);
    }

    @AfterAll
    static void afterAll() {
        entityUtils.close();
    }

    @BeforeEach
    void setUp() {
        entityUtils.reset();
    }
    @Test
    void httpExecute_ok_Get200() throws URISyntaxException {

        String url = "http://anEndPoint";

        HashMap<String, String> queryParam = new HashMap<>();
        queryParam.put("aKey", "aValue");

        HashMap<String, String> headers = new HashMap<>();
        headers.put("aHeaderKey", "aHeaderValue");
        headers.put("username", "username");
        headers.put("password", "password");

        Mockito.when(restTemplateMock.exchange(ArgumentMatchers.anyString(),
                        ArgumentMatchers.any(HttpMethod.class),ArgumentMatchers.<HttpEntity<?>>any(),
                        ArgumentMatchers.<Class<String>>any()))
                .thenReturn(new ResponseEntity<String>("aResult", HttpStatus.OK));

        ResponseEntity<String> result = restTemplateClient
                .restTemplateExecute(url, HttpMethod.GET, httpEntity);

        assertEquals("aResult", result.getBody());

    }

    @Test
    void httpExecute_ok_Post201() throws URISyntaxException {

        String url = "http://anEndPoint";

        HashMap<String, String> queryParam = new HashMap<>();
        queryParam.put("aKey", "aValue");

        HashMap<String, String> headers = new HashMap<>();
        headers.put("aHeaderKey", "aHeaderValue");
        headers.put("username", "username");
        headers.put("password", "password");

        Mockito.when(restTemplateMock.exchange(ArgumentMatchers.anyString(),
                        ArgumentMatchers.any(HttpMethod.class),ArgumentMatchers.<HttpEntity<?>>any(),
                        ArgumentMatchers.<Class<String>>any()))
                .thenReturn(new ResponseEntity<String>("aResult", HttpStatus.CREATED));

        ResponseEntity<String> result = restTemplateClient
                .restTemplateExecute(url, HttpMethod.GET, httpEntity);

        assertEquals("aResult", result.getBody());

    }

    @Test
    void executeHttpGET_w_credentials_ok() {

        String url = "http://anEndPoint";

        HashMap<String, String> queryParam = new HashMap<>();
        queryParam.put("aKey", "aValue");

        HashMap<String, String> headers = new HashMap<>();
        headers.put("aHeaderKey", "aHeaderValue");
        headers.put("username", "username");
        headers.put("password", "password");

        Mockito.when(restTemplateMock.exchange(ArgumentMatchers.anyString(),
                        ArgumentMatchers.any(HttpMethod.class),ArgumentMatchers.<HttpEntity<?>>any(),
                        ArgumentMatchers.<Class<String>>any()))
                .thenReturn(new ResponseEntity<String>("Authenticated", HttpStatus.OK));

        ResponseEntity<String> result = restTemplateClient
                .restTemplateExecute(url, HttpMethod.GET, httpEntity);

        assertEquals("Authenticated", result.getBody());
    }

    @Test
    void executeHttpGET_wo_credentials_ok() {
        String url = "http://anEndPoint";

        HashMap<String, String> queryParam = new HashMap<>();
        queryParam.put("aKey", "aValue");

        HashMap<String, String> headers = new HashMap<>();
        headers.put("aHeaderKey", "aHeaderValue");

        Mockito.when(restTemplateMock.exchange(ArgumentMatchers.anyString(),
                        ArgumentMatchers.any(HttpMethod.class),ArgumentMatchers.<HttpEntity<?>>any(),
                        ArgumentMatchers.<Class<String>>any()))
                .thenReturn(new ResponseEntity<String>("aResult", HttpStatus.UNAUTHORIZED))
                .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

        ResponseEntity<String> result = restTemplateClient
                .restTemplateExecute(url, HttpMethod.GET, httpEntity);

        assertEquals(401, result.getStatusCode().value());

    }

    @Test
    void executeHttpGET_withEmptyUsername_ok() {

        String url = "http://anEndPoint";

        HashMap<String, String> queryParam = new HashMap<>();
        queryParam.put("aKey", "aValue");

        HashMap<String, String> headers = new HashMap<>();
        headers.put("aHeaderKey", "aHeaderValue");
        headers.put("username", "");
        headers.put("password", "password");

        Mockito.when(restTemplateMock.exchange(ArgumentMatchers.anyString(),
                        ArgumentMatchers.any(HttpMethod.class),ArgumentMatchers.<HttpEntity<?>>any(),
                        ArgumentMatchers.<Class<String>>any()))
                .thenReturn(new ResponseEntity<String>("aResult", HttpStatus.UNAUTHORIZED))
                .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

        ResponseEntity<String> result = restTemplateClient
                .restTemplateExecute(url, HttpMethod.GET, httpEntity);

        assertEquals(401, result.getStatusCode().value());

    }

    @Test
    void executeHttpPost_w_credentials_ok() {

        String url = "http://anEndPoint";

        HashMap<String, String> queryParam = new HashMap<>();
        queryParam.put("aKey", "aValue");

        HashMap<String, String> headers = new HashMap<>();
        headers.put("aHeaderKey", "aHeaderValue");
        headers.put("username", "username");
        headers.put("password", "password");

        Mockito.when(restTemplateMock.exchange(ArgumentMatchers.anyString(),
                        ArgumentMatchers.any(HttpMethod.class),ArgumentMatchers.<HttpEntity<?>>any(),
                        ArgumentMatchers.<Class<String>>any()))
                .thenReturn(new ResponseEntity<String>("Authenticated", HttpStatus.CREATED));

        ResponseEntity<String> result = restTemplateClient
                .restTemplateExecute(url, HttpMethod.GET, httpEntity);

        assertEquals("Authenticated", result.getBody());

    }

    @Test
    void executeHttpPost_wo_credentials_ok() {

        String url = "http://anEndPoint";

        HashMap<String, String> queryParam = new HashMap<>();
        queryParam.put("aKey", "aValue");

        HashMap<String, String> headers = new HashMap<>();
        headers.put("aHeaderKey", "aHeaderValue");

        Mockito.when(restTemplateMock.exchange(ArgumentMatchers.anyString(),
                        ArgumentMatchers.any(HttpMethod.class),ArgumentMatchers.<HttpEntity<?>>any(),
                        ArgumentMatchers.<Class<String>>any()))
                .thenReturn(new ResponseEntity<String>("aResult", HttpStatus.UNAUTHORIZED))
                .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

        ResponseEntity<String> result = restTemplateClient
                .restTemplateExecute(url, HttpMethod.POST, httpEntity);

        assertEquals(401, result.getStatusCode().value());
    }

    @Test
    void executeHttpPost_witEmptyUsername_ok() {
        String url = "http://anEndPoint";

        HashMap<String, String> queryParam = new HashMap<>();
        queryParam.put("aKey", "aValue");

        HashMap<String, String> headers = new HashMap<>();
        headers.put("aHeaderKey", "aHeaderValue");
        headers.put("username", "");
        headers.put("password", "password");

        Mockito.when(restTemplateMock.exchange(ArgumentMatchers.anyString(),
                        ArgumentMatchers.any(HttpMethod.class),ArgumentMatchers.<HttpEntity<?>>any(),
                        ArgumentMatchers.<Class<String>>any()))
                .thenReturn(new ResponseEntity<String>("aResult", HttpStatus.UNAUTHORIZED))
                .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

        ResponseEntity<String> result = restTemplateClient
                .restTemplateExecute(url, HttpMethod.POST, httpEntity);

        assertEquals(401, result.getStatusCode().value());



    }

    @Test
    void executeHttpGET_withEmptyEndPoint_ok() {

        String url = "";

        HashMap<String, String> queryParam = new HashMap<>();
        queryParam.put("aKey", "aValue");

        HashMap<String, String> headers = new HashMap<>();
        headers.put("aHeaderKey", "aHeaderValue");
        headers.put("username", "username");
        headers.put("password", "password");

        Mockito.when(restTemplateMock.exchange(ArgumentMatchers.anyString(),
                        ArgumentMatchers.any(HttpMethod.class),ArgumentMatchers.<HttpEntity<?>>any(),
                        ArgumentMatchers.<Class<String>>any()))
                .thenReturn(new ResponseEntity<String>("aResult", HttpStatus.NOT_FOUND))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        ResponseEntity<String> result = restTemplateClient
                .restTemplateExecute(url, HttpMethod.GET, httpEntity);

        assertEquals(404, result.getStatusCode().value());

    }

    @Test
    void executeHttpPost_withEmptyEndPoint_ok() {

        String url = "";

        HashMap<String, String> queryParam = new HashMap<>();
        queryParam.put("aKey", "aValue");

        HashMap<String, String> headers = new HashMap<>();
        headers.put("aHeaderKey", "aHeaderValue");
        headers.put("username", "username");
        headers.put("password", "password");

        Mockito.when(restTemplateMock.exchange(ArgumentMatchers.anyString(),
                        ArgumentMatchers.any(HttpMethod.class),ArgumentMatchers.<HttpEntity<?>>any(),
                        ArgumentMatchers.<Class<String>>any()))
                .thenReturn(new ResponseEntity<String>("aResult", HttpStatus.NOT_FOUND))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        ResponseEntity<String> result = restTemplateClient
                .restTemplateExecute(url, HttpMethod.POST, httpEntity);

        assertEquals(404, result.getStatusCode().value());

    }

    @Test
    void executeHttpGET_BAD_REQUEST_ok() {

        String url = "";

        HashMap<String, String> queryParam = new HashMap<>();
        queryParam.put("aKey", "aValue");

        HashMap<String, String> headers = new HashMap<>();
        headers.put("aHeaderKey", "aHeaderValue");
        headers.put("username", "username");
        headers.put("password", "password");

        Mockito.when(restTemplateMock.exchange(ArgumentMatchers.anyString(),
                        ArgumentMatchers.any(HttpMethod.class),ArgumentMatchers.<HttpEntity<?>>any(),
                        ArgumentMatchers.<Class<String>>any()))
                .thenReturn(new ResponseEntity<String>("aResult", HttpStatus.BAD_REQUEST))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        ResponseEntity<String> result = restTemplateClient
                .restTemplateExecute(url, HttpMethod.GET, httpEntity);

        assertEquals(400, result.getStatusCode().value());

    }

    @Test
    void executeHttpPost_BAD_REQUEST_ok() {

        String url = "";

        HashMap<String, String> queryParam = new HashMap<>();
        queryParam.put("aKey", "aValue");

        HashMap<String, String> headers = new HashMap<>();
        headers.put("aHeaderKey", "aHeaderValue");
        headers.put("username", "username");
        headers.put("password", "password");

        Mockito.when(restTemplateMock.exchange(ArgumentMatchers.anyString(),
                        ArgumentMatchers.any(HttpMethod.class),ArgumentMatchers.<HttpEntity<?>>any(),
                        ArgumentMatchers.<Class<String>>any()))
                .thenReturn(new ResponseEntity<String>("aResult", HttpStatus.BAD_REQUEST))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        ResponseEntity<String> result = restTemplateClient
                .restTemplateExecute(url, HttpMethod.POST, httpEntity);

        assertEquals(400, result.getStatusCode().value());

    }

    @Test
    void executeHttpGET_INTERNAL_SERVER_ERROR_ok() {

        String url = "";

        HashMap<String, String> queryParam = new HashMap<>();
        queryParam.put("aKey", "aValue");

        HashMap<String, String> headers = new HashMap<>();
        headers.put("aHeaderKey", "aHeaderValue");
        headers.put("username", "username");
        headers.put("password", "password");

        Mockito.when(restTemplateMock.exchange(ArgumentMatchers.anyString(),
                        ArgumentMatchers.any(HttpMethod.class),ArgumentMatchers.<HttpEntity<?>>any(),
                        ArgumentMatchers.<Class<String>>any()))
                .thenReturn(new ResponseEntity<String>("aResult", HttpStatus.INTERNAL_SERVER_ERROR))
                .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        ResponseEntity<String> result = restTemplateClient
                .restTemplateExecute(url, HttpMethod.GET, httpEntity);

        assertEquals(500, result.getStatusCode().value());

    }

    @Test
    void executeHttpPost_INTERNAL_SERVER_ERROR_ok() {

        String url = "";

        HashMap<String, String> queryParam = new HashMap<>();
        queryParam.put("aKey", "aValue");

        HashMap<String, String> headers = new HashMap<>();
        headers.put("aHeaderKey", "aHeaderValue");
        headers.put("username", "username");
        headers.put("password", "password");

        Mockito.when(restTemplateMock.exchange(ArgumentMatchers.anyString(),
                        ArgumentMatchers.any(HttpMethod.class),ArgumentMatchers.<HttpEntity<?>>any(),
                        ArgumentMatchers.<Class<String>>any()))
                .thenReturn(new ResponseEntity<String>("aResult", HttpStatus.INTERNAL_SERVER_ERROR))
                .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        ResponseEntity<String> result = restTemplateClient
                .restTemplateExecute(url, HttpMethod.POST, httpEntity);

        assertEquals(500, result.getStatusCode().value());

    }
}
