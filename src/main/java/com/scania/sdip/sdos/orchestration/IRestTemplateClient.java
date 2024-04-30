package com.scania.sdip.sdos.orchestration;

import org.apache.http.HttpEntity;
import org.apache.http.client.CredentialsProvider;

import java.util.HashMap;

public interface IRestTemplateClient {

    String executeHttpGET(String endpoint, HashMap<String, String> queryParam,
                                 HashMap<String, String> headers,
                                 CredentialsProvider credentialsProvider);

    String executeHttpPost(String endpoint, HashMap<String, String> queryParam,
                                  HashMap<String, String> headers, String body,
                                  CredentialsProvider credentialsProvider, HashMap<String, String> formData);
}
