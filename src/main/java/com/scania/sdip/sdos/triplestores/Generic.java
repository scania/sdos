package com.scania.sdip.sdos.triplestores;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generic class that implement generic rdfstore, it is used when trying new rdfstore that has not
 * been yet supported
 */
public class Generic implements TripleStore {

  private static final Logger LOGGER = LoggerFactory.getLogger(Generic.class);
  private final String sparqlEndpoint;


  public Generic(String sparqlEndpoint) {
    this.sparqlEndpoint = sparqlEndpoint;
  }

  @Override
  public String getEndpoint() {
    return sparqlEndpoint;
  }

  @Override
  public String toString() {
    return "GENERIC{" + "sparqlEndpoint='" + sparqlEndpoint + '\'' + '}';
  }
}
