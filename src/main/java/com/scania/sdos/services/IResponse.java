package com.scania.sdos.services;

import java.util.concurrent.TimeoutException;
import org.springframework.http.HttpStatus;

/**
 * The IResponse is a border interface for an HTTP response.
 */
public interface IResponse {

  /**
   * Returns the body of the HTTP response.
   *
   * @return a body of an HTTP response.
   * @throws EmptyBodyException when the response doesn't contain a body.
   */
  String body() throws EmptyBodyException;

  /**
   * Returns the HTTP status code of the HTTP response.
   *
   * @return an HTTP status code(e.g. 200 OK, 400 Bad Request or something different).
   * @throws TimeoutException is thrown when the operation times out.
   */
  HttpStatus status() throws TimeoutException;
}
