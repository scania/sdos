package com.scania.sdip.sdos.services;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.springframework.http.HttpMethod;

/**
 * The Request class is an immutable implementation of the {@link IRequest} border interface. It is
 * consumed by an implementation of {@link IHttpClient}.
 */
public class Request implements IRequest {

  private final HttpMethod verb;
  private final URL url;
  private final Object body;
  private Map<String, String> headers = new HashMap<>();

  public Request(HttpMethod verb, URL url) {
    this(verb, url, null, null);
  }

  public Request(HttpMethod verb, URL url, Object body, Map<String, String> headers) {
    this.verb = verb;
    this.url = url;
    this.body = body;
    if (headers != null) {
      this.headers = headers;
    }
  }

  public Request(HttpMethod verb, URL url, Object body) {
    this.url = url;
    this.body = body;
    this.verb = verb;
  }

  @Override
  public HttpMethod verb() {
    return verb;
  }

  @Override
  public URL url() {
    return url;
  }

  @Override
  public Object body() {
    return body;
  }

  @Override
  public int hashCode() {
    return Objects.hash(body, url, verb);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof Request)) {
      return false;
    }
    Request other = (Request) obj;
    return Objects.equals(body, other.body) && Objects.equals(url, other.url) && (verb
        == other.verb);
  }

  @Override
  public Map<String, String> headers() {
    return new HashMap<>(headers);
  }

  @Override
  public void setHeader(String key, String value) {
    headers.put(key, value);
  }
}
