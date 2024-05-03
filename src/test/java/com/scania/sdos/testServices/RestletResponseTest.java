package com.scania.sdos.testServices;
import com.scania.sdos.services.EmptyBodyException;
import com.scania.sdos.services.RestletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.restlet.Response;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.data.Status;
import org.springframework.http.HttpStatus;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
public class RestletResponseTest {
    @Mock
    private ClientResource resource;

    @Mock
    private Response response;
    @Mock
    private Representation representation;
    @Mock
    private Status status;

    @Test
    void test_body() throws IOException, EmptyBodyException {
        RestletResponse response1= new RestletResponse(resource);
        doReturn(response).when(resource).getResponse();
        doReturn(representation).when(response).getEntity();
        doReturn("Test").when(representation).getText();

        assertEquals("Test", response1.body());
    }

    @Test
    void test_status() throws TimeoutException {
        RestletResponse response1= new RestletResponse(resource);
        doReturn(status).when(resource).getStatus();
        doReturn(200).when(status).getCode();

        assertEquals(HttpStatus.class,
                response1.status().getClass());
    }

    @Test
    void test_statusException(){
        RestletResponse response1= new RestletResponse(resource);
        doReturn(status).when(resource).getStatus();
        doReturn(1000).when(status).getCode();

        assertThrows(TimeoutException.class, () -> {
            response1.status();
        });
    }
}
