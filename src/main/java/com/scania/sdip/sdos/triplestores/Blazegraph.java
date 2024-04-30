package com.scania.sdip.sdos.triplestores;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * blazegraph class that implement blazegraph specific impl
 */
public class Blazegraph implements TripleStore {

  private static final Logger LOGGER = LoggerFactory.getLogger(Blazegraph.class);
  private final String sparqlEndpoint;

  public Blazegraph(String sparqlEndpoint) {
    this.sparqlEndpoint = sparqlEndpoint;
  }

  @Override
  public String getEndpoint() {
    return sparqlEndpoint;
  }

  @Override
  public String toString() {
    return "Blazegraph{" + "sparqlEndpoint='" + sparqlEndpoint + '\'' + '}';
  }
}
