package com.scania.sdip.sdos.orchestration;

import com.scania.sdip.exceptions.IncidentException;
import com.scania.sdip.exceptions.SdipErrorCode;
import com.scania.sdip.sdos.orchestration.model.RestTemplateAuthModel;
import com.scania.sdip.sdos.utils.SDOSConstants;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.entity.StringEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class RestTemplateClient implements IRestTemplateClient{

    private static final Logger LOGGER = LogManager.getLogger(RestTemplateClient.class);

    @Autowired
    RestTemplate restTemplate;

    private RestTemplateAuthModel restTemplateAuthModel;

    public RestTemplateClient() {
        this.setRestTemplate(new RestTemplate());
        this.setRestTemplateAuthModel(new RestTemplateAuthModel());
    }

    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void setRestTemplateAuthModel(RestTemplateAuthModel restTemplateAuthModel) {
        this.restTemplateAuthModel = restTemplateAuthModel;
    }

    @Override
    public String executeHttpGET(String endpoint, HashMap<String, String> queryParam, HashMap<String, String> headers,
                                 CredentialsProvider credentialsProvider) {

        String baseUrl = endpoint;

        if (queryParam != null && !queryParam.isEmpty()) {
            baseUrl = baseUrl + "?" + restTemplateAuthModel.generateUrl(queryParam);
        }

        HttpEntity<String> request = new HttpEntity<String>(restTemplateAuthModel.addAuthHeaders(headers));

        ResponseEntity<String> response = restTemplateExecute(baseUrl, HttpMethod.GET, request);

        final String result = response.getBody();
        int status = response.getStatusCodeValue();

        if (!(200 == status ||  201 == status)) {

            throw new IncidentException(new IllegalStateException("Response from "+baseUrl+" "+status + " " + result),
                    SdipErrorCode.HTTP_RESPONSE_NOK, LOGGER , status + " " + result, baseUrl);
        }
        return result;

    }

    public ResponseEntity<String> restTemplateExecute(String baseUrl, HttpMethod method, HttpEntity<?> request) {

        return restTemplate.exchange(baseUrl, method, request, String.class);
    }

    @Override
    public String executeHttpPost(String endpoint, HashMap<String, String> queryParam, HashMap<String, String> headers,
                                  String httpEntity, CredentialsProvider credentialsProvider,
                                  HashMap<String, String> formData) {

        String baseUrl = endpoint;

        ResponseEntity<String> response = null;

        if (queryParam != null && !queryParam.isEmpty()) {
            baseUrl = baseUrl + "?" + restTemplateAuthModel.generateUrl(queryParam);
        }

        if (httpEntity != null) {
            if (headers.containsKey(SDOSConstants.CONTENT_TYPE) && headers
                    .get(SDOSConstants.CONTENT_TYPE)
                    .contains(MediaType.APPLICATION_FORM_URLENCODED_VALUE)) {
                MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<String, String>();
                for (Map.Entry<String, String> entry : formData.entrySet())
                    multiValueMap.add(entry.getKey(),entry.getValue());
                HttpEntity<MultiValueMap<String, String>> request =
                        new HttpEntity<MultiValueMap<String, String>>(multiValueMap,restTemplateAuthModel.addAuthHeaders(headers));
                response = restTemplateExecute(baseUrl, HttpMethod.POST, request);

                } else {
                HttpEntity<String> request = new HttpEntity<String>(httpEntity,restTemplateAuthModel.addAuthHeaders(headers));
                response = restTemplateExecute(baseUrl, HttpMethod.POST, request);
            }
        }

        final String result = response.getBody();
        int status = response.getStatusCodeValue();

        if (!(200 == status ||  201 == status)) {

            throw new IncidentException(new IllegalStateException("Response from "+baseUrl+" "+status + " " + result),
                    SdipErrorCode.HTTP_RESPONSE_NOK, LOGGER , status + " " + result, baseUrl);
        }
        return result;

    }

}
