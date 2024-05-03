package com.scania.sdos.services;

/**
 * The IHttpClient is the border interface for an http client.
 */
public interface IHttpClient {

  /**
   * Run the request and retrieve a response from some http endpoint.
   *
   * @param request A request describing where and how some http endpoint should be called.
   * @return The response from the http endpoint.
   */
  IResponse execute(IRequest request);
}
