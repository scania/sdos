package com.scania.sdos.testServices;
import com.scania.sdos.services.IRequest;
import com.scania.sdos.services.IResponse;
import com.scania.sdos.services.Request;
import com.scania.sdos.services.RestletHttpClient;
import com.scania.sdos.services.RestletResponse;
import com.scania.sdos.utils.SDOSConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeoutException;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class RestletHttpClientTest {
    @Spy
    private RestletHttpClient restletHttpClient;

    @Test
    void test_executePOST() throws MalformedURLException, TimeoutException {
        IRequest request =new Request(HttpMethod.POST, new URL("http://local:8080/sdos/servicestatus"), "HelloTesting");
        request.setHeader(HttpHeaders.AUTHORIZATION,"Bearer 123456");
        request.setHeader(SDOSConstants.ACCEPT_HEADER, SDOSConstants.ACCEPT_HEADER_VALUE);
        request.setHeader("Content-Type", "application/json");
        IResponse response = restletHttpClient.execute(request);
        assertEquals(RestletResponse.class, response.getClass());
    }

    @Test
    void test_executeGET() throws MalformedURLException, TimeoutException {
        IRequest request =new Request(HttpMethod.GET, new URL("http://local:8080/sdos/servicestatus"), "HelloTesting");
        request.setHeader(HttpHeaders.AUTHORIZATION,"Bearer 123456");
        request.setHeader(SDOSConstants.ACCEPT_HEADER, SDOSConstants.ACCEPT_HEADER_VALUE);
        request.setHeader("Content-Type", "application/x-www-form-urlencoded");
        IResponse response = restletHttpClient.execute(request);
        assertEquals(RestletResponse.class, response.getClass());
    }

    @Test
    void test_executeDELETE() throws MalformedURLException, TimeoutException {
        IRequest request =new Request(HttpMethod.DELETE, new URL("http://local:8080/sdos/servicestatus"), "HelloTesting");
        request.setHeader(HttpHeaders.AUTHORIZATION,"Bearer 123456");
        request.setHeader(SDOSConstants.ACCEPT_HEADER, SDOSConstants.ACCEPT_HEADER_VALUE);
        request.setHeader("Content-Type", "application/x-www-form-urlencoded");
        //IResponse response = restletHttpClient.execute(request);
        assertThrows(UnsupportedOperationException.class, () -> {restletHttpClient.execute(request);});
    }
}
