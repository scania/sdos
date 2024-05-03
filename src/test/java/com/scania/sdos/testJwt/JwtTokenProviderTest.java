package com.scania.sdos.testJwt;

import com.scania.sdip.exceptions.IncidentException;
import com.scania.sdip.exceptions.SdipErrorCode;
import com.scania.sdos.model.ServiceArguments;
import com.scania.sdos.orchestration.RestTemplateClient;
import com.scania.sdos.testUtils.UnitTestConstants;
import org.json.JSONObject;
import org.junit.Assert;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.reset;
import com.scania.sdos.jwt.JwtTokenProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class JwtTokenProviderTest {
    @Spy
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private RestTemplateClient restTemplateClient;
    @Mock
    private ServiceArguments serviceArguments;
    final String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsIng1dCI6Ii1LSTNROW5OUjdiUm9meG1lWm9YcWJIWkdldyIsImtpZCI6Ii1LSTNROW5OUjdiUm9meG1lWm9YcWJIWkdldyJ9.eyJhdWQiOiJhcGk6Ly8zYTFjMDNkNy01ZTI3LTQzYmQtYmM5MC0zYmZmMmY5ZTM4ZTAiLCJpc3MiOiJodHRwczovL3N0cy53aW5kb3dzLm5ldC8zYmMwNjJlNC1hYzlkLTRjMTctYjRkZC0zYWFkNjM3ZmYxYWMvIiwiaWF0IjoxNjk2ODU1MTUxLCJuYmYiOjE2OTY4NTUxNTEsImV4cCI6MTY5Njg2MDc2OSwiYWNyIjoiMSIsImFpbyI6IkFZUUFlLzhVQUFBQTRPVytsbEdVMExHNWEvRSt6bkdLNG45Y1g4SGxFTW5GTExlMjczQXYzdUlLdFpXdy9tOVVURXZ4ZUQzQ2NwWGc3azJQbExabkpaMzZYQk9VeTM3ZGZ0WEV0a0F2enFNaW5rMVJVYUtKRXlYQjZ3NmV1dnI0TFRrVkxuNDg1allEK0QyUDJ3RUIxV2lFRlJ6dlEyTTR2RWZQLy9zakVZVU5tak5CVExEbHVoWT0iLCJhbXIiOlsicHdkIiwibWZhIl0sImFwcGlkIjoiM2ExYzAzZDctNWUyNy00M2JkLWJjOTAtM2JmZjJmOWUzOGUwIiwiYXBwaWRhY3IiOiIxIiwiZmFtaWx5X25hbWUiOiJTaW5naCIsImdpdmVuX25hbWUiOiJSYXZpR3lhbiIsImlwYWRkciI6IjEzOS4xMjIuMTkxLjIyOSIsIm5hbWUiOiJTaW5naCBSYXZpR3lhbiIsIm9pZCI6ImFmZDQxNzJlLWZjMjYtNDliMC05NWIzLTRmNmVjZGU3MGFjMSIsIm9ucHJlbV9zaWQiOiJTLTEtNS0yMS0xODQ0MjM3NjE1LTYwMjE2MjM1OC03MjUzNDU1NDMtOTg4NDgwIiwicmgiOiIwLkFSRUE1R0xBTzUyc0YweTAzVHF0WTNfeHJOY0RIRG9uWHIxRHZKQTdfeS1lT09BUkFOUS4iLCJyb2xlcyI6WyJhZG1pbiJdLCJzY3AiOiJ1c2VyX2ltcGVyc29uYXRpb24iLCJzdWIiOiJNYThBUHB2Wm1XMzNPdHdNZVk0bUVwQThfQU9XV0U2eVRHWVhaRmRKVE5RIiwidGlkIjoiM2JjMDYyZTQtYWM5ZC00YzE3LWI0ZGQtM2FhZDYzN2ZmMWFjIiwidW5pcXVlX25hbWUiOiJyYXZpZ3lhbi5zaW5naEBzY2FuaWEuY29tIiwidXBuIjoicmF2aWd5YW4uc2luZ2hAc2NhbmlhLmNvbSIsInV0aSI6IjZ6VlFZRWVjNmtTUmx6SjlrOG93QUEiLCJ2ZXIiOiIxLjAifQ.AeSPgAPujprTYTezUmciTYJYukzKw4yOG0K-A_jO7D3aL-1KwqaJTWe_fVDaJves3fENJVwDAenJCIMIQXesIsusoy7V3g4l4KelihG6RCMSzf8BRkliSzCmfVdb5SKqUT_gWwQUrJz9c6acwNgEDpc12_8iR5ihxLYMqJpwSB1BEQazGSlFxshXoDT5GyksHiSyb4KRqvBUPnT9D34IqArdhX--TJfkHjrOfh6AX8CsWbBcKs4VlU1sAuETduVeD4PrmDZXi8t0TBNR1aYztrgvpYI6B9QP9HJ4qGxAJs4yDLPs1DvqHl5skn8Pm7LlQB870XQkS8F-iaVpCauLHA";

    final String tokenWithoutRole = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6IjVCM25SeHRRN2ppOGVORGMzRnkwNUtmOTdaRSJ9.eyJhdWQiOiIzYTY5MWVkNS1iZGVhLTQzYjEtYmUzMC1kY2VlNDlmMTk2MDUiLCJpc3MiOiJodHRwczovL2xvZ2luLm1pY3Jvc29mdG9ubGluZS5jb20vM2JjMDYyZTQtYWM5ZC00YzE3LWI0ZGQtM2FhZDYzN2ZmMWFjL3YyLjAiLCJpYXQiOjE3MDYyNzMyNzAsIm5iZiI6MTcwNjI3MzI3MCwiZXhwIjoxNzA2Mjc4ODc2LCJhaW8iOiJBV1FBbS84VkFBQUFmR2c4SlgzbUxULzBwQTVLYUVzRXBZUUZlaldkS3BMOUNHK21ISUtwdUZrKzJITEMyUUVxRExXMDMvSXA0bWgyOXFzRjhOZnBuZnJxTzFWSVMrUmlzUWIyalN5dUFuTllBdnlnN2V0VWZRUTBzYmV1Vld2K3ZmWGtEWENwTkJsZiIsImF6cCI6ImNlNzQyZTQwLTdkNDYtNDljYy04NGQ0LWE4OWZkYmUxMmVjNiIsImF6cGFjciI6IjEiLCJuYW1lIjoiSmFtYnVsaW5nYW0gUmF2aXJhaiIsIm9pZCI6ImQ0NGM5NjFjLTQzNzktNGIxZC1iMTc2LWJlYmYxYTgxZDA5MiIsInByZWZlcnJlZF91c2VybmFtZSI6InJhdmlyYWouamFtYnVsaW5nYW1Ac2NhbmlhLmNvbSIsInJoIjoiMC5BUkVBNUdMQU81MnNGMHkwM1RxdFkzX3hyTlVlYVRycXZiRkR2akRjN2tueGxnVVJBTjguIiwic2NwIjoidXNlcl9pbXBlcnNvbmF0aW9uIiwic3ViIjoiVzhWc1FtMDFlYktvMjBRQ19ySnNqSHh3VzlRWDFWQXEtakVBWktMd2Q4ZyIsInRpZCI6IjNiYzA2MmU0LWFjOWQtNGMxNy1iNGRkLTNhYWQ2MzdmZjFhYyIsInV0aSI6IlFiRDVmUE1pNGtxZENRQkpxWmxNQUEiLCJ2ZXIiOiIyLjAifQ.wUlEb8CyCqGbxrBM8uSlDY5wdwR29fcI_udOPj9JWnaws7_qwrdrXBINd6I0nn7jpHyiRl3bgMR6gxNoQZrY8hoF3b1O4DDMpbAjJID76-kdQbO4Ccf19oeQU_-TzAo0EfrBfUZjPB8bnGyViNsgMz1m0_nInClNlt3MOWlabBrVqhcPqAzjeDWMXDj97RP1Bj9rrm_pBjhy_kkGebePsyIqV4AW4R2dsu_BQnfjkzVSeLezy_Rim1aMporV0EZkNYbnDSMmie8jer1LQ0jhio3_JpDZQ9ZBDVu-qOdVJDKx4Yl9kQEazeoIGJc9h7tVsJ2jHFktYewEA5K6RhnEhg";

    final String response = "{\"token_type\": \"Bearer\",\"access_token\": \"eyJ0eXAiOiJKV1QiLCJhbGci\"}";

    @BeforeAll
    static void beforeAll() {
    }

    @AfterAll
    static void afterAll() {
    }

    @BeforeEach
    void setUp() {
        reset(jwtTokenProvider);
    }

    @Test
    void jwtExpired_Test() {
        ReflectionTestUtils.setField(jwtTokenProvider, "discoverKeyUrl", "test");
        ReflectionTestUtils.setField(jwtTokenProvider, "restTemplateClient", restTemplateClient);
        ReflectionTestUtils.setField(jwtTokenProvider,"serviceArguments", serviceArguments);
        final String result= "{\"keys\":[{\"kty\":\"RSA\",\"use\":\"sig\",\"kid\":\"-KI3Q9nNR7bRofxmeZoXqbHZGew\",\"x5t\":\"-KI3Q9nNR7bRofxmeZoXqbHZGew\",\"n\":\"tJL6Wr2JUsxLyNezPQh1J6zn6wSoDAhgRYSDkaMuEHy75VikiB8wg25WuR96gdMpookdlRvh7SnRvtjQN9b5m4zJCMpSRcJ5DuXl4mcd7Cg3Zp1C5-JmMq8J7m7OS9HpUQbA1yhtCHqP7XA4UnQI28J-TnGiAa3viPLlq0663Cq6hQw7jYo5yNjdJcV5-FS-xNV7UHR4zAMRruMUHxte1IZJzbJmxjKoEjJwDTtcd6DkI3yrkmYt8GdQmu0YBHTJSZiz-M10CY3LbvLzf-tbBNKQ_gfnGGKF7MvRCmPA_YF_APynrIG7p4vPDRXhpG3_CIt317NyvGoIwiv0At83kQ\",\"e\":\"AQAB\",\"x5c\":[\"MIIDBTCCAe2gAwIBAgIQGQ6YG6NleJxJGDRAwAd/ZTANBgkqhkiG9w0BAQsFADAtMSswKQYDVQQDEyJhY2NvdW50cy5hY2Nlc3Njb250cm9sLndpbmRvd3MubmV0MB4XDTIyMTAwMjE4MDY0OVoXDTI3MTAwMjE4MDY0OVowLTErMCkGA1UEAxMiYWNjb3VudHMuYWNjZXNzY29udHJvbC53aW5kb3dzLm5ldDCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBALSS+lq9iVLMS8jXsz0IdSes5+sEqAwIYEWEg5GjLhB8u+VYpIgfMINuVrkfeoHTKaKJHZUb4e0p0b7Y0DfW+ZuMyQjKUkXCeQ7l5eJnHewoN2adQufiZjKvCe5uzkvR6VEGwNcobQh6j+1wOFJ0CNvCfk5xogGt74jy5atOutwquoUMO42KOcjY3SXFefhUvsTVe1B0eMwDEa7jFB8bXtSGSc2yZsYyqBIycA07XHeg5CN8q5JmLfBnUJrtGAR0yUmYs/jNdAmNy27y83/rWwTSkP4H5xhihezL0QpjwP2BfwD8p6yBu6eLzw0V4aRt/wiLd9ezcrxqCMIr9ALfN5ECAwEAAaMhMB8wHQYDVR0OBBYEFJcSH+6Eaqucndn9DDu7Pym7OA8rMA0GCSqGSIb3DQEBCwUAA4IBAQADKkY0PIyslgWGmRDKpp/5PqzzM9+TNDhXzk6pw8aESWoLPJo90RgTJVf8uIj3YSic89m4ftZdmGFXwHcFC91aFe3PiDgCiteDkeH8KrrpZSve1pcM4SNjxwwmIKlJdrbcaJfWRsSoGFjzbFgOecISiVaJ9ZWpb89/+BeAz1Zpmu8DSyY22dG/K6ZDx5qNFg8pehdOUYY24oMamd4J2u2lUgkCKGBZMQgBZFwk+q7H86B/byGuTDEizLjGPTY/sMms1FAX55xBydxrADAer/pKrOF1v7Dq9C1Z9QVcm5D9G4DcenyWUdMyK43NXbVQLPxLOng51KO9icp2j4U7pwHP\"]}]}";
        HttpHeaders headers = new HttpHeaders();
        ResponseEntity<String> responseEntity = new ResponseEntity<>(result, headers, HttpStatus.OK);
        doReturn(responseEntity.getBody()).when(restTemplateClient).executeHttpGET( ArgumentMatchers.anyString()
                , ArgumentMatchers.any()
                , ArgumentMatchers.any()
                , ArgumentMatchers.any());

        assertThrows(IncidentException.class, () -> {
            jwtTokenProvider.validateToken(token);
        });
    }

    @Test
    void getAppIdTest(){
        assertEquals("3a1c03d7-5e27-43bd-bc90-3bff2f9e38e0", jwtTokenProvider.getAppIdFromToken(token));
    }

    @Test
    void getUsernameTest(){
        assertEquals("RaviGyan", jwtTokenProvider.getUsernameFromToken(token));
    }
    @Test
    void callOboService_Test(){
        ReflectionTestUtils.setField(serviceArguments,"sdosClientSecret","test");
        ReflectionTestUtils.setField(serviceArguments,"stardogClientScope","api://12345/user_impersonation");
        ReflectionTestUtils.setField(serviceArguments,"tenantId","534475866425498");
        ReflectionTestUtils.setField(jwtTokenProvider, "serviceArguments", serviceArguments);
        ReflectionTestUtils.setField(jwtTokenProvider, "restTemplateClient", restTemplateClient);
        ReflectionTestUtils.setField(jwtTokenProvider, "grantType", "test");

        final String result= "{\"token_type\":\"Bearer\",\"scope\":\"api://3a691ed5-bdea-43b1-be30-dcee49f19605/user_impersonation api://3a691ed5-bdea-43b1-be30-dcee49f19605/user_login\",\"expires_in\":5277,\"ext_expires_in\":5277,\"access_token\":\"eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6Ii1LSTNROW5OUjdiUm9meG1lWm9YcWJIWkdldyJ9.eyJhdWQiOiIzYTY5MWVkNS1iZGVhLTQzYjEtYmUzMC1kY2VlNDlmMTk2MDUiLCJpc3MiOiJodHRwczovL2xvZ2luLm1pY3Jvc29mdG9ubGluZS5jb20vM2JjMDYyZTQtYWM5ZC00YzE3LWI0ZGQtM2FhZDYzN2ZmMWFjL3YyLjAiLCJpYXQiOjE2OTcwMDY2MjksIm5iZiI6MTY5NzAwNjYyOSwiZXhwIjoxNjk3MDEyMjA3LCJhaW8iOiJBV1FBbS84VUFBQUFzL2Z0VnBuK2tKQWNvOFMrS0l0cENuOXZ3RDhkcThHYTZqYTExMVpsa3FCN1d3MXpwWW1ZT1VKR3ZtME5wVkwycExoZi9XTEg3aEJvMkMza09sc0NLdmEyb0xublhOVlNMeVNyZlMrYjZFV2Y5QVhXQTFKcDZBalAzMk13Wmx3ZyIsImF6cCI6IjNhMWMwM2Q3LTVlMjctNDNiZC1iYzkwLTNiZmYyZjllMzhlMCIsImF6cGFjciI6IjEiLCJuYW1lIjoiU2luZ2ggUmF2aUd5YW4iLCJvaWQiOiJhZmQ0MTcyZS1mYzI2LTQ5YjAtOTViMy00ZjZlY2RlNzBhYzEiLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJyYXZpZ3lhbi5zaW5naEBzY2FuaWEuY29tIiwicmgiOiIwLkFSRUE1R0xBTzUyc0YweTAzVHF0WTNfeHJOVWVhVHJxdmJGRHZqRGM3a254bGdVUkFOUS4iLCJyb2xlcyI6WyJlbmRfdXNlciJdLCJzY3AiOiJ1c2VyX2ltcGVyc29uYXRpb24gdXNlcl9sb2dpbiIsInN1YiI6IjBYU1FkdFlGd1Nqc1dQY1pyQnRycW9fVU5YZFA0UThiX3B0LVZ4dUpjSlUiLCJ0aWQiOiIzYmMwNjJlNC1hYzlkLTRjMTctYjRkZC0zYWFkNjM3ZmYxYWMiLCJ1dGkiOiJQMjZYRHYtOGtVQ0lPNFY5bnYtUEFBIiwidmVyIjoiMi4wIn0.nhtiLZS8VuxmcvKpKelDd2njxi-z-jUllnWSseF3iFkKHEzwzqV-z1X93x4omfUwGnQeaqvN_rTWG6Oua49L8Sjfc-1rl8khLXIjxrlOROtAu1cAnjS-NMkoJwzNkf5JatZvqOZi9fpgoIeBhqZmVNRSgKzKpKKn7UEQ-BtkV99dMLk2QCioHJEHUUl1JJAlNy9lPSxAoMhDdsTSO4B5NeeHMFTZrfumOQanXPAq6T8y_N99xjaAo25P4pKilpX5w1mLJkedteJhkurQrt8fYJTOoLRvyTYZhqZ5-JYBT6RkGyJB79ZrAi6Lb04MApbVRZ8SFtvBnlLbej9qEJuPCg\"}";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        ResponseEntity<String> responseEntity = new ResponseEntity<>(result, headers, HttpStatus.OK);

        doReturn(responseEntity.getBody()).when( restTemplateClient).executeHttpPost( Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any());

        String response =jwtTokenProvider.callOboService(token,"3a1c03d7-5e27-43bd-bc90-3bff2f9e38e0");
        JSONObject payload = new JSONObject(response);
        assertEquals("Bearer",payload.get("token_type").toString());
    }

    @Test
    void test_constructor(){
        JwtTokenProvider jwtTokenProvider1 = new JwtTokenProvider(serviceArguments);
        Assert.assertTrue(jwtTokenProvider1.getServiceArguments() instanceof ServiceArguments);
    }

    @Test
    void test_rolesExistsInOboToken(){
        boolean role =jwtTokenProvider.rolesExistsInOboToken(token);
        Assert.assertEquals(true,role);
    }

    @Test
    void test_rolesExistsInOboToken_withoutRoles(){
        Throwable throwable = assertThrows(IncidentException.class, () -> {
            jwtTokenProvider.rolesExistsInOboToken(tokenWithoutRole);
        });

        assertEquals(SdipErrorCode.MISSING_OBO_TOKEN_ERROR.getSdipErrorCode(),
                Integer
                        .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                                .getSdipErrorCodes().get(0)
                                .replace(UnitTestConstants.SDIP, "")));
    }

    @Test
    void test_callOboService_exception(){
        Throwable throwable = assertThrows(IncidentException.class, () -> {
            jwtTokenProvider.callOboService(token,"12345");
        });

        assertEquals(SdipErrorCode.INVALID_AUTH_TOKEN_ERROR.getSdipErrorCode(),
                Integer
                        .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                                .getSdipErrorCodes().get(0)
                                .replace(UnitTestConstants.SDIP, "")));

    }

    @Test
    void test_getOboToken(){
        String oboToken = jwtTokenProvider.getOBOToken(response);
        Assert.assertEquals("eyJ0eXAiOiJKV1QiLCJhbGci",oboToken);

    }
 }
