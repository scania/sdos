package com.scania.sdos.testJwt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import com.scania.sdip.exceptions.IncidentException;
import com.scania.sdip.exceptions.SdipErrorCode;
import com.scania.sdos.jwt.JwtTokenFilter;
import com.scania.sdos.jwt.JwtTokenProvider;
import com.scania.sdos.jwt.JwtTokenUtil;
import com.scania.sdos.orchestration.ParameterMemory;
import com.scania.sdos.testUtils.UnitTestConstants;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class JwtTokenFilterTest {

    @Spy
    private JwtTokenFilter jwtTokenFilter;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private ParameterMemory parameterMemory;

    private static MockedStatic jwtTokenUtils;
    private final String bearerToken ="Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsIng1dCI6Ii1LSTNROW5OUjdiUm9meG1lWm9YcWJIWkdldyIsImtpZCI6Ii1LSTNROW5OUjdiUm9meG1lWm9YcWJIWkdldyJ9.eyJhdWQiOiJhcGk6Ly8zYTFjMDNkNy01ZTI3LTQzYmQtYmM5MC0zYmZmMmY5ZTM4ZTAiLCJpc3MiOiJodHRwczovL3N0cy53aW5kb3dzLm5ldC8zYmMwNjJlNC1hYzlkLTRjMTctYjRkZC0zYWFkNjM3ZmYxYWMvIiwiaWF0IjoxNjk2ODU1MTUxLCJuYmYiOjE2OTY4NTUxNTEsImV4cCI6MTY5Njg2MDc2OSwiYWNyIjoiMSIsImFpbyI6IkFZUUFlLzhVQUFBQTRPVytsbEdVMExHNWEvRSt6bkdLNG45Y1g4SGxFTW5GTExlMjczQXYzdUlLdFpXdy9tOVVURXZ4ZUQzQ2NwWGc3azJQbExabkpaMzZYQk9VeTM3ZGZ0WEV0a0F2enFNaW5rMVJVYUtKRXlYQjZ3NmV1dnI0TFRrVkxuNDg1allEK0QyUDJ3RUIxV2lFRlJ6dlEyTTR2RWZQLy9zakVZVU5tak5CVExEbHVoWT0iLCJhbXIiOlsicHdkIiwibWZhIl0sImFwcGlkIjoiM2ExYzAzZDctNWUyNy00M2JkLWJjOTAtM2JmZjJmOWUzOGUwIiwiYXBwaWRhY3IiOiIxIiwiZmFtaWx5X25hbWUiOiJTaW5naCIsImdpdmVuX25hbWUiOiJSYXZpR3lhbiIsImlwYWRkciI6IjEzOS4xMjIuMTkxLjIyOSIsIm5hbWUiOiJTaW5naCBSYXZpR3lhbiIsIm9pZCI6ImFmZDQxNzJlLWZjMjYtNDliMC05NWIzLTRmNmVjZGU3MGFjMSIsIm9ucHJlbV9zaWQiOiJTLTEtNS0yMS0xODQ0MjM3NjE1LTYwMjE2MjM1OC03MjUzNDU1NDMtOTg4NDgwIiwicmgiOiIwLkFSRUE1R0xBTzUyc0YweTAzVHF0WTNfeHJOY0RIRG9uWHIxRHZKQTdfeS1lT09BUkFOUS4iLCJyb2xlcyI6WyJhZG1pbiJdLCJzY3AiOiJ1c2VyX2ltcGVyc29uYXRpb24iLCJzdWIiOiJNYThBUHB2Wm1XMzNPdHdNZVk0bUVwQThfQU9XV0U2eVRHWVhaRmRKVE5RIiwidGlkIjoiM2JjMDYyZTQtYWM5ZC00YzE3LWI0ZGQtM2FhZDYzN2ZmMWFjIiwidW5pcXVlX25hbWUiOiJyYXZpZ3lhbi5zaW5naEBzY2FuaWEuY29tIiwidXBuIjoicmF2aWd5YW4uc2luZ2hAc2NhbmlhLmNvbSIsInV0aSI6IjZ6VlFZRWVjNmtTUmx6SjlrOG93QUEiLCJ2ZXIiOiIxLjAifQ.AeSPgAPujprTYTezUmciTYJYukzKw4yOG0K-A_jO7D3aL-1KwqaJTWe_fVDaJves3fENJVwDAenJCIMIQXesIsusoy7V3g4l4KelihG6RCMSzf8BRkliSzCmfVdb5SKqUT_gWwQUrJz9c6acwNgEDpc12_8iR5ihxLYMqJpwSB1BEQazGSlFxshXoDT5GyksHiSyb4KRqvBUPnT9D34IqArdhX--TJfkHjrOfh6AX8CsWbBcKs4VlU1sAuETduVeD4PrmDZXi8t0TBNR1aYztrgvpYI6B9QP9HJ4qGxAJs4yDLPs1DvqHl5skn8Pm7LlQB870XQkS8F-iaVpCauLHA";

    private final String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6IjVCM25SeHRRN2ppOGVORGMzRnkwNUtmOTdaRSJ9.eyJhdWQiOiIzYTY5MWVkNS1iZGVhLTQzYjEtYmUzMC1kY2VlNDlmMTk2MDUiLCJpc3MiOiJodHRwczovL2xvZ2luLm1pY3Jvc29mdG9ubGluZS5jb20vM2JjMDYyZTQtYWM5ZC00YzE3LWI0ZGQtM2FhZDYzN2ZmMWFjL3YyLjAiLCJpYXQiOjE3MDYyNzMyNzAsIm5iZiI6MTcwNjI3MzI3MCwiZXhwIjoxNzA2Mjc4ODc2LCJhaW8iOiJBV1FBbS84VkFBQUFmR2c4SlgzbUxULzBwQTVLYUVzRXBZUUZlaldkS3BMOUNHK21ISUtwdUZrKzJITEMyUUVxRExXMDMvSXA0bWgyOXFzRjhOZnBuZnJxTzFWSVMrUmlzUWIyalN5dUFuTllBdnlnN2V0VWZRUTBzYmV1Vld2K3ZmWGtEWENwTkJsZiIsImF6cCI6ImNlNzQyZTQwLTdkNDYtNDljYy04NGQ0LWE4OWZkYmUxMmVjNiIsImF6cGFjciI6IjEiLCJuYW1lIjoiSmFtYnVsaW5nYW0gUmF2aXJhaiIsIm9pZCI6ImQ0NGM5NjFjLTQzNzktNGIxZC1iMTc2LWJlYmYxYTgxZDA5MiIsInByZWZlcnJlZF91c2VybmFtZSI6InJhdmlyYWouamFtYnVsaW5nYW1Ac2NhbmlhLmNvbSIsInJoIjoiMC5BUkVBNUdMQU81MnNGMHkwM1RxdFkzX3hyTlVlYVRycXZiRkR2akRjN2tueGxnVVJBTjguIiwic2NwIjoidXNlcl9pbXBlcnNvbmF0aW9uIiwic3ViIjoiVzhWc1FtMDFlYktvMjBRQ19ySnNqSHh3VzlRWDFWQXEtakVBWktMd2Q4ZyIsInRpZCI6IjNiYzA2MmU0LWFjOWQtNGMxNy1iNGRkLTNhYWQ2MzdmZjFhYyIsInV0aSI6IlFiRDVmUE1pNGtxZENRQkpxWmxNQUEiLCJ2ZXIiOiIyLjAifQ.wUlEb8CyCqGbxrBM8uSlDY5wdwR29fcI_udOPj9JWnaws7_qwrdrXBINd6I0nn7jpHyiRl3bgMR6gxNoQZrY8hoF3b1O4DDMpbAjJID76-kdQbO4Ccf19oeQU_-TzAo0EfrBfUZjPB8bnGyViNsgMz1m0_nInClNlt3MOWlabBrVqhcPqAzjeDWMXDj97RP1Bj9rrm_pBjhy_kkGebePsyIqV4AW4R2dsu_BQnfjkzVSeLezy_Rim1aMporV0EZkNYbnDSMmie8jer1LQ0jhio3_JpDZQ9ZBDVu-qOdVJDKx4Yl9kQEazeoIGJc9h7tVsJ2jHFktYewEA5K6RhnEhg";
    @BeforeAll
    static void beforeAll() {
        jwtTokenUtils = mockStatic(JwtTokenUtil.class);
    }

    @AfterAll
    static void afterAll() {
        jwtTokenUtils.close();
    }

    @BeforeEach
    void setUp() {
        reset(jwtTokenProvider);
        reset(parameterMemory);
    }

    @Test
    void validateJwtTokenTest() {
        final String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsIng1dCI6Ii1LSTNROW5OUjdiUm9meG1lWm9YcWJIWkdldyIsImtpZCI6Ii1LSTNROW5OUjdiUm9meG1lWm9YcWJIWkdldyJ9.eyJhdWQiOiJhcGk6Ly8zYTFjMDNkNy01ZTI3LTQzYmQtYmM5MC0zYmZmMmY5ZTM4ZTAiLCJpc3MiOiJodHRwczovL3N0cy53aW5kb3dzLm5ldC8zYmMwNjJlNC1hYzlkLTRjMTctYjRkZC0zYWFkNjM3ZmYxYWMvIiwiaWF0IjoxNjk2ODU1MTUxLCJuYmYiOjE2OTY4NTUxNTEsImV4cCI6MTY5Njg2MDc2OSwiYWNyIjoiMSIsImFpbyI6IkFZUUFlLzhVQUFBQTRPVytsbEdVMExHNWEvRSt6bkdLNG45Y1g4SGxFTW5GTExlMjczQXYzdUlLdFpXdy9tOVVURXZ4ZUQzQ2NwWGc3azJQbExabkpaMzZYQk9VeTM3ZGZ0WEV0a0F2enFNaW5rMVJVYUtKRXlYQjZ3NmV1dnI0TFRrVkxuNDg1allEK0QyUDJ3RUIxV2lFRlJ6dlEyTTR2RWZQLy9zakVZVU5tak5CVExEbHVoWT0iLCJhbXIiOlsicHdkIiwibWZhIl0sImFwcGlkIjoiM2ExYzAzZDctNWUyNy00M2JkLWJjOTAtM2JmZjJmOWUzOGUwIiwiYXBwaWRhY3IiOiIxIiwiZmFtaWx5X25hbWUiOiJTaW5naCIsImdpdmVuX25hbWUiOiJSYXZpR3lhbiIsImlwYWRkciI6IjEzOS4xMjIuMTkxLjIyOSIsIm5hbWUiOiJTaW5naCBSYXZpR3lhbiIsIm9pZCI6ImFmZDQxNzJlLWZjMjYtNDliMC05NWIzLTRmNmVjZGU3MGFjMSIsIm9ucHJlbV9zaWQiOiJTLTEtNS0yMS0xODQ0MjM3NjE1LTYwMjE2MjM1OC03MjUzNDU1NDMtOTg4NDgwIiwicmgiOiIwLkFSRUE1R0xBTzUyc0YweTAzVHF0WTNfeHJOY0RIRG9uWHIxRHZKQTdfeS1lT09BUkFOUS4iLCJyb2xlcyI6WyJhZG1pbiJdLCJzY3AiOiJ1c2VyX2ltcGVyc29uYXRpb24iLCJzdWIiOiJNYThBUHB2Wm1XMzNPdHdNZVk0bUVwQThfQU9XV0U2eVRHWVhaRmRKVE5RIiwidGlkIjoiM2JjMDYyZTQtYWM5ZC00YzE3LWI0ZGQtM2FhZDYzN2ZmMWFjIiwidW5pcXVlX25hbWUiOiJyYXZpZ3lhbi5zaW5naEBzY2FuaWEuY29tIiwidXBuIjoicmF2aWd5YW4uc2luZ2hAc2NhbmlhLmNvbSIsInV0aSI6IjZ6VlFZRWVjNmtTUmx6SjlrOG93QUEiLCJ2ZXIiOiIxLjAifQ.AeSPgAPujprTYTezUmciTYJYukzKw4yOG0K-A_jO7D3aL-1KwqaJTWe_fVDaJves3fENJVwDAenJCIMIQXesIsusoy7V3g4l4KelihG6RCMSzf8BRkliSzCmfVdb5SKqUT_gWwQUrJz9c6acwNgEDpc12_8iR5ihxLYMqJpwSB1BEQazGSlFxshXoDT5GyksHiSyb4KRqvBUPnT9D34IqArdhX--TJfkHjrOfh6AX8CsWbBcKs4VlU1sAuETduVeD4PrmDZXi8t0TBNR1aYztrgvpYI6B9QP9HJ4qGxAJs4yDLPs1DvqHl5skn8Pm7LlQB870XQkS8F-iaVpCauLHA";

        ReflectionTestUtils.setField(jwtTokenFilter, "jwtTokenProvider", jwtTokenProvider);
        doReturn(true).when(jwtTokenProvider).validateToken(token);
        Boolean status = jwtTokenFilter.validateJwtToken(bearerToken);
        assertEquals(true,status);
    }

    @Test
    void jwtTokenNotValidTest() {
        ReflectionTestUtils.setField(jwtTokenFilter, "jwtTokenProvider", jwtTokenProvider);
        Boolean status = jwtTokenFilter.validateJwtToken(bearerToken);
        assertEquals(false,status);
    }

    @Test
    void jwtProcess_Test() {
        final String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsIng1dCI6Ii1LSTNROW5OUjdiUm9meG1lWm9YcWJIWkdldyIsImtpZCI6Ii1LSTNROW5OUjdiUm9meG1lWm9YcWJIWkdldyJ9.eyJhdWQiOiJhcGk6Ly8zYTFjMDNkNy01ZTI3LTQzYmQtYmM5MC0zYmZmMmY5ZTM4ZTAiLCJpc3MiOiJodHRwczovL3N0cy53aW5kb3dzLm5ldC8zYmMwNjJlNC1hYzlkLTRjMTctYjRkZC0zYWFkNjM3ZmYxYWMvIiwiaWF0IjoxNjk2ODU1MTUxLCJuYmYiOjE2OTY4NTUxNTEsImV4cCI6MTY5Njg2MDc2OSwiYWNyIjoiMSIsImFpbyI6IkFZUUFlLzhVQUFBQTRPVytsbEdVMExHNWEvRSt6bkdLNG45Y1g4SGxFTW5GTExlMjczQXYzdUlLdFpXdy9tOVVURXZ4ZUQzQ2NwWGc3azJQbExabkpaMzZYQk9VeTM3ZGZ0WEV0a0F2enFNaW5rMVJVYUtKRXlYQjZ3NmV1dnI0TFRrVkxuNDg1allEK0QyUDJ3RUIxV2lFRlJ6dlEyTTR2RWZQLy9zakVZVU5tak5CVExEbHVoWT0iLCJhbXIiOlsicHdkIiwibWZhIl0sImFwcGlkIjoiM2ExYzAzZDctNWUyNy00M2JkLWJjOTAtM2JmZjJmOWUzOGUwIiwiYXBwaWRhY3IiOiIxIiwiZmFtaWx5X25hbWUiOiJTaW5naCIsImdpdmVuX25hbWUiOiJSYXZpR3lhbiIsImlwYWRkciI6IjEzOS4xMjIuMTkxLjIyOSIsIm5hbWUiOiJTaW5naCBSYXZpR3lhbiIsIm9pZCI6ImFmZDQxNzJlLWZjMjYtNDliMC05NWIzLTRmNmVjZGU3MGFjMSIsIm9ucHJlbV9zaWQiOiJTLTEtNS0yMS0xODQ0MjM3NjE1LTYwMjE2MjM1OC03MjUzNDU1NDMtOTg4NDgwIiwicmgiOiIwLkFSRUE1R0xBTzUyc0YweTAzVHF0WTNfeHJOY0RIRG9uWHIxRHZKQTdfeS1lT09BUkFOUS4iLCJyb2xlcyI6WyJhZG1pbiJdLCJzY3AiOiJ1c2VyX2ltcGVyc29uYXRpb24iLCJzdWIiOiJNYThBUHB2Wm1XMzNPdHdNZVk0bUVwQThfQU9XV0U2eVRHWVhaRmRKVE5RIiwidGlkIjoiM2JjMDYyZTQtYWM5ZC00YzE3LWI0ZGQtM2FhZDYzN2ZmMWFjIiwidW5pcXVlX25hbWUiOiJyYXZpZ3lhbi5zaW5naEBzY2FuaWEuY29tIiwidXBuIjoicmF2aWd5YW4uc2luZ2hAc2NhbmlhLmNvbSIsInV0aSI6IjZ6VlFZRWVjNmtTUmx6SjlrOG93QUEiLCJ2ZXIiOiIxLjAifQ.AeSPgAPujprTYTezUmciTYJYukzKw4yOG0K-A_jO7D3aL-1KwqaJTWe_fVDaJves3fENJVwDAenJCIMIQXesIsusoy7V3g4l4KelihG6RCMSzf8BRkliSzCmfVdb5SKqUT_gWwQUrJz9c6acwNgEDpc12_8iR5ihxLYMqJpwSB1BEQazGSlFxshXoDT5GyksHiSyb4KRqvBUPnT9D34IqArdhX--TJfkHjrOfh6AX8CsWbBcKs4VlU1sAuETduVeD4PrmDZXi8t0TBNR1aYztrgvpYI6B9QP9HJ4qGxAJs4yDLPs1DvqHl5skn8Pm7LlQB870XQkS8F-iaVpCauLHA";
        ReflectionTestUtils.setField(jwtTokenFilter, "jwtTokenProvider", jwtTokenProvider);
        doReturn("12345").when(jwtTokenProvider).getAppIdFromToken(token);
        doNothing().when(jwtTokenFilter).fetchOBOToken(any(),any(),any());
        jwtTokenUtils.when(() ->
                JwtTokenUtil.saveOboToken(any(), any())).then((Answer<Void>) invocation -> null);
        jwtTokenFilter.jwtProcess(bearerToken,true, parameterMemory);
        verify(jwtTokenProvider, times(1))
                .getAppIdFromToken(token);
        verify(jwtTokenProvider, times(1))
                .getAppIdFromToken(any());
    }

    @Test
    void test_fetchOBOToken() {
        JwtTokenFilter tokenFilter = new JwtTokenFilter(jwtTokenProvider);
        jwtTokenFilter.setJwtTokenProvider(jwtTokenProvider);
        doReturn(token).when(jwtTokenProvider).callOboService(any(), any());
        doReturn(token).when(jwtTokenProvider).getOBOToken(any());
        doReturn(true).when(jwtTokenProvider).rolesExistsInOboToken(any());
        jwtTokenUtils.when(() ->
                JwtTokenUtil.saveOboToken(any(), any())).then((Answer<Void>) invocation -> null);
        jwtTokenFilter.fetchOBOToken("test","12345",parameterMemory);

        verify(jwtTokenProvider, times(1))
                .callOboService(any(), any());
        verify(jwtTokenProvider, times(1))
                .getOBOToken(any());
        verify(jwtTokenProvider, times(1))
                .rolesExistsInOboToken(any());
        jwtTokenUtils.verify(
                () -> JwtTokenUtil.saveOboToken(any(), any()), times(1));
    }

    @Test
    void test_validateJwtToken_exception() {
        Throwable throwable = assertThrows(IncidentException.class, () -> {
            jwtTokenFilter.validateJwtToken("testToken");
        });

        assertEquals(SdipErrorCode.SIGNATURE_JWT_TOKEN_ERROR.getSdipErrorCode(),
                Integer
                        .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                                .getSdipErrorCodes().get(0)
                                .replace(UnitTestConstants.SDIP, "")));
    }

}