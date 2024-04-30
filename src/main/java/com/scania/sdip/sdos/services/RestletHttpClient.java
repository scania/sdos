package com.scania.sdip.sdos.services;

import static com.scania.sdip.sdos.utils.SDOSConstants.ACCEPT;
import static com.scania.sdip.sdos.utils.SDOSConstants.BEARER;

import com.scania.sdip.sdos.utils.SDOSConstants;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

/**
 * The RestletHttpClient class is an implementation of IHttpClient that is using Restlet to run
 * requests.
 */
public class RestletHttpClient implements IHttpClient {

  private static final Logger logger = LoggerFactory.getLogger(RestletHttpClient.class);


  @Override
  public IResponse execute(IRequest request) {
    ClientResource clientResource = new ClientResource(request.url().toString());
    readHeaders(clientResource, request);

    if (HttpMethod.GET.equals(request.verb())) {

      return tryRun(clientResource, clientResource::get, request);
    } else if (HttpMethod.POST.equals(request.verb())) {
      String contentType = request.headers().get("Content-Type");
      if (contentType != null) {
        Representation representation;
        if (contentType.startsWith("application/json")) {
          representation = new JsonRepresentation(request.body());
        } else if (contentType.startsWith("application/x-www-form-urlencoded")) {
          Form form = new Form((String) request.body());
          representation = form.getWebRepresentation();
        } else {
          throw new UnsupportedOperationException(
              "The media type " + contentType + " is not a supported media type for POST");
        }
        clientResource.getRequest().setEntity(representation);
        return tryRun(clientResource, () -> clientResource.post(representation), request);
      } else {
        return tryRun(clientResource, () -> clientResource.post(request.body()), request);
      }
    } else if (HttpMethod.DELETE.equals(request.verb())) {
      if (request.body() != null) {
        throw new UnsupportedOperationException("DELETE does not support body yet, implement it");
      }
      return tryRun(clientResource, () -> clientResource.delete(), request);
    } else {
      throw new UnsupportedOperationException(
          "Class has no capability to handle a " + request.verb().toString()
              + " request, it need to be extended to handle it.");
    }
  }

  private void readHeaders(ClientResource clientResource,IRequest request){
    for (String header : request.headers().keySet()) {
      if (header.contentEquals(HttpHeaders.AUTHORIZATION)) {
        String rawValue = request.headers().get(header);
        authorizationHeaderValue( rawValue, clientResource);
      } else if (header.contentEquals(ACCEPT)) {
        clientResource.accept(MediaType.valueOf(request.headers().get(ACCEPT)));
      } else {
        clientResource.getRequest().getHeaders().add(header, request.headers().get(header));
      }
    }
  }
  private void authorizationHeaderValue(String rawValue,ClientResource clientResource){
    ChallengeResponse challengeResponse ;
    if (rawValue.startsWith(SDOSConstants.BASIC)) {
      challengeResponse = new ChallengeResponse(ChallengeScheme.HTTP_BASIC);
      challengeResponse.setRawValue(rawValue.substring(6));
    } else if (rawValue.startsWith(BEARER)) {
      challengeResponse = new ChallengeResponse(new ChallengeScheme(BEARER, BEARER));
      challengeResponse.setRawValue(rawValue.substring(7));
    } else {
      throw new UnsupportedOperationException(
              "The authentication method " + rawValue + " is not a valid authentication method");
    }
    clientResource.setChallengeResponse(challengeResponse);
  }
  private RestletResponse tryRun(ClientResource clientResource,
      ExceptionThrowingRunnable<ResourceException> consumer, IRequest request) {
    try {
      consumer.run();
      return new RestletResponse(clientResource);
    } catch (ResourceException e) {
      logger.error(
          e.getMessage() + ", when sending: " + request.body() + " to: " + request.verb() + " "
              + request.url(), e);
      return new RestletResponse(clientResource);
    }
  }

  private interface ExceptionThrowingRunnable<T extends Exception> {

    void run() throws T;
  }
}
