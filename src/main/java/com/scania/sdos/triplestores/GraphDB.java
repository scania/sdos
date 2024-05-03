package com.scania.sdos.triplestores;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * grapgdb class that implement  graphdb specific impl
 */
public class GraphDB implements TripleStore {

  private static final Logger LOGGER = LoggerFactory.getLogger(GraphDB.class);
  private final String sparqlEndpoint;

  public GraphDB(String sparqlEndpoint) {
    this.sparqlEndpoint = sparqlEndpoint;
  }

  @Override
  public String getEndpoint() {
    return sparqlEndpoint;
  }

  @Override
  public String toString() {
    return "GraphDB{" + "sparqlEndpoint='" + sparqlEndpoint + '\'' + '}';
  }
}
