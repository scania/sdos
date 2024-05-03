package com.scania.sdos.triplestores;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * neptune class that implement neptune specific impl
 */
public class Neptune implements TripleStore {

  private static final Logger LOGGER = LoggerFactory.getLogger(Neptune.class);
  private final String sparqlEndpoint;


  public Neptune(String sparqlEndpoint) {
    this.sparqlEndpoint = sparqlEndpoint;
  }

  @Override
  public String getEndpoint() {
    return sparqlEndpoint;
  }

  @Override
  public String toString() {
    return "Neptune{" + "sparqlEndpoint='" + sparqlEndpoint + '\'' + '}';
  }
}
