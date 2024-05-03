package com.scania.sdos.services;

import java.net.URL;
import java.util.Map;
import org.springframework.http.HttpMethod;

/**
 * IRequest is a border interface for an http request.
 */
public interface IRequest {


  /**
   * Returns the HTTP verb of the request.
   *
   * @return a HTTP verb that the HTTP client should use to run this request. e.g. GET, POST, PUT,
   * DELETE or OPTIONS.
   */
  HttpMethod verb();

  /**
   * Returns the URL of the request.
   *
   * @return a URL to the endpoint that the request should be sent to, including query params.
   */
  URL url();

  /**
   * Returns the request body of the request.
   *
   * @return a request body that will automatically be converted by the http client to an
   * appropriate format.
   */
  Object body();

  /**
   * Returns the headers of the request as a key value mapping.
   *
   * @return a list of headers as a key value mapping.
   */
  Map<String, String> headers();

  /**
   * Sets the headers of the request as a key - value mapping.
   *
   * @param key   the header key of the key - value mapping.
   * @param value the reader value of the key - value mapping.
   */
  void setHeader(String key, String value);
}
