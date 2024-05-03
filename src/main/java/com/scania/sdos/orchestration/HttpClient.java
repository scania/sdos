package com.scania.sdos.orchestration;

import static com.scania.sdip.exceptions.SdipErrorParameter.SUPPORTMAIL;

import com.scania.sdip.exceptions.IncidentException;
import com.scania.sdip.exceptions.SdipErrorCode;
import com.scania.sdip.exceptions.SdipErrorParameter;
import com.scania.sdos.utils.SDOSConstants;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import org.apache.http.HttpEntity;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;

public class HttpClient implements IRestTemplateClient{

  private static final Logger LOGGER = LogManager.getLogger(HttpClient.class);


  public HttpClient() {
    LOGGER.info("HttpClient constructor");
  }

  public HttpGet getHttpGet(String baseUrl) {
    return new HttpGet(baseUrl);
  }

  public HttpPost getHttpPost(String baseUrl) {
    return new HttpPost(baseUrl);
  }

  @Override
  public String executeHttpGET(String endpoint, HashMap<String, String> queryParam,
      HashMap<String, String> headers,
      CredentialsProvider credentialsProvider) {
    String baseUrl = endpoint;

    if (queryParam != null && !queryParam.isEmpty()) {
      baseUrl = baseUrl + "?" + generateUrl(queryParam);
    }
    HttpGet request = getHttpGet(baseUrl);
    if (headers != null) {
      for (Map.Entry<String, String> entry : headers.entrySet()) {
        request.addHeader(entry.getKey(), entry.getValue());
      }
    }
    String apiResponse = httpExecute(request, credentialsProvider);
    return apiResponse;
  }


  public CredentialsProvider addCredentials(HashMap<String, String> headers) {
    CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
    credentialsProvider.setCredentials(AuthScope.ANY,
        //new AuthScope("httpbin.org", 80),
        new UsernamePasswordCredentials(headers.get("username"), headers.get("password")));
    return credentialsProvider;

  }

  private String generateUrl(Map<String, String> queryParam) {
    String uri = "";
    if (!queryParam.isEmpty()) {
      for (Map.Entry<String, String> params : queryParam.entrySet()) {
        if (uri.isEmpty()) {
          uri = params.getKey() + "=" + params.getValue();
        } else {
          uri = uri + "&" + params.getKey() + "=" + params.getValue();
        }
      }
    }
    return uri;
  }

  @Override
  public String executeHttpPost(String endpoint, HashMap<String, String> queryParam,
      HashMap<String, String> headers, String body,
      CredentialsProvider credentialsProvider, HashMap<String, String> formData) {
    try {
      String baseUrl = endpoint;
      if (queryParam != null && !queryParam.isEmpty()) {
        baseUrl = baseUrl + "?" + generateUrl(queryParam);
      }

      HttpPost request = getHttpPost(baseUrl);
      if (headers != null) {
        for (Map.Entry<String, String> entry : headers.entrySet()) {
          request.addHeader(entry.getKey(), entry.getValue());
        }
      }
      if (body != null) {
        if (headers.containsKey(SDOSConstants.CONTENT_TYPE) && headers
            .get(SDOSConstants.CONTENT_TYPE)
            .contains(MediaType.APPLICATION_FORM_URLENCODED_VALUE)) {
          String test = getDataString(formData);
          request.setEntity(new StringEntity(test));
        } else {
          request.setEntity(new StringEntity(body));
        }
      }
      String apiResponse = httpExecute(request, credentialsProvider);
      return apiResponse;
    } catch (IncidentException e) {
      throw e;
    } catch (Exception e) {
      throw new IncidentException(e, SdipErrorCode.UNKNOWN_REASON_ERROR, LOGGER,
          e.getMessage(), SdipErrorParameter.SUPPORTMAIL);
    }
  }

  private String getDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
    StringBuilder result = new StringBuilder();
    boolean first = true;
    for (Map.Entry<String, String> entry : params.entrySet()) {
      if (first) {
        first = false;
      } else {
        result.append("&");
      }
      result.append(URLEncoder.encode(entry.getKey(), SDOSConstants.UTF_8));
      result.append("=");
      result.append(URLEncoder.encode(entry.getValue(), SDOSConstants.UTF_8));
    }
    return result.toString();

  }

  public String httpExecute(HttpRequestBase request, CredentialsProvider credentialsProvider) {
    String result = null;

    try (CloseableHttpClient httpClient = HttpClients.custom()
        .setDefaultCredentialsProvider(credentialsProvider).build()) {

      try (CloseableHttpResponse response = httpClient.execute(request)) {
        int status = response.getStatusLine().getStatusCode();
        HttpEntity entity = response.getEntity();

        if (entity != null) {
          result = EntityUtils.toString(entity);
        }
        if (!(200 == status ||
            (HttpPost.class == request.getClass() && 201 == status))) {

          throw new IncidentException(new IllegalStateException("Response from "+request.getURI()+" "+status + " " + result),
                  SdipErrorCode.HTTP_RESPONSE_NOK, LOGGER , status + " " + result, request.getURI());
        }

        // EmptyBodyException?
      } catch (ClientProtocolException e) {
        throw new IncidentException(e, SdipErrorCode.HTTP_COMMUNICATION_ERROR, LOGGER,
            e.getMessage(), request.getURI(), SUPPORTMAIL);
      }
    } catch (IncidentException e) {
      throw e;
    } catch (IOException e) {
      throw new IncidentException(e, SdipErrorCode.UNKNOWN_REASON_ERROR, LOGGER, e.getMessage(),
          SUPPORTMAIL);
    }
    return result;
  }

}
