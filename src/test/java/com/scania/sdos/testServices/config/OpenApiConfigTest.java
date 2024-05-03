package com.scania.sdos.testServices.config;

import com.scania.sdos.services.config.OpenApiConfig;
import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class OpenApiConfigTest {
    @Spy
    private OpenApiConfig openApiConfig;

    @Test
    void test_customOpenAPI() {
        ReflectionTestUtils.setField(openApiConfig, "applicationName", "sdos_test");
        ReflectionTestUtils.setField(openApiConfig, "swaggerDesc", "sdos_desc");
        ReflectionTestUtils.setField(openApiConfig, "name", "sdos_test");
        ReflectionTestUtils.setField(openApiConfig, "url", "http://test.com");
        ReflectionTestUtils.setField(openApiConfig, "email", "a@sdos.com");
        ReflectionTestUtils.setField(openApiConfig, "version", "2.1");
        ReflectionTestUtils.setField(openApiConfig, "service_url", "http://test.com");

        assertEquals(OpenAPI.class,openApiConfig.customOpenAPI().getClass());

        ReflectionTestUtils.setField(openApiConfig, "service_url", "LOCAL");

        assertEquals(OpenAPI.class,openApiConfig.customOpenAPI().getClass());
    }

}
