package com.scania.sdip.sdos.services;

import com.github.jsonldjava.shaded.com.google.common.base.Objects;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import org.restlet.resource.ClientResource;
import org.springframework.http.HttpStatus;


/**
 * The RestletResponse class represents an HTTP response and is an implementation of {@link
 * IResponse}.
 */
public class RestletResponse implements IResponse {


  private final ClientResource resource;

  /**
   * Constructs a restlet HTTP response with a resource of type {@link ClientResource}.
   *
   * @param resource a resource of type {@link ClientResource}
   */
  public RestletResponse(ClientResource resource) {
    this.resource = resource;
  }

  @Override
  public String body() throws EmptyBodyException {
    try {
      return resource.getResponse().getEntity().getText();
    } catch (IOException | NullPointerException e) {
      throw new EmptyBodyException("No body in the response");
    }
  }

  @Override
  public HttpStatus status() throws TimeoutException {
    if (Objects.equal(1000, resource.getStatus().getCode())) {
      throw new TimeoutException("Connection timed out, no status code");
    }
    return HttpStatus.valueOf(resource.getStatus().getCode());
  }
}
