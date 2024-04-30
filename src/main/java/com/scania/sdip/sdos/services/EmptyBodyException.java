package com.scania.sdip.sdos.services;

/**
 * The EmptyBodyException is thrown whenever no body is provided in a IHttpResponse.
 */
public class EmptyBodyException extends Exception {

  private static final long serialVersionUID = 1L;

  /**
   * Constructs an EmptyBodyException with a specified message
   *
   * @param message the detail message
   */
  public EmptyBodyException(String message) {
    super(message);
  }

}
