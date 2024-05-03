package com.scania.sdos.testServices;
import com.scania.sdos.services.Request;
import com.scania.sdos.utils.SDOSConstants;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.net.MalformedURLException;
import java.net.URL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

@ExtendWith(MockitoExtension.class)
public class RequestTest {

    @Test
    void test_Header() throws MalformedURLException {
        Request request =new Request(HttpMethod.POST, new URL("http://local:8080/sdos/servicestatus"), "HelloTesting");
        request.setHeader(HttpHeaders.AUTHORIZATION,"Bearer 123456");
        request.setHeader(SDOSConstants.ACCEPT_HEADER, SDOSConstants.ACCEPT_HEADER_VALUE);
        request.setHeader("Content-Type", "application/json");

        assertEquals(3, request.headers().size());
        assertEquals("HelloTesting", request.body().toString());
        assertEquals(HttpMethod.POST, request.verb());
    }
}
