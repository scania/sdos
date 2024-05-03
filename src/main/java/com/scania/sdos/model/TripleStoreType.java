package com.scania.sdos.model;


/**
 * this Enum holds supported rdf stores in SDIP. Generic is a special Type for non supported rdf
 * stores
 */
public enum TripleStoreType {
  GRAPHDB, NEPTUNE, BLAZEGRAPH, GENERIC, STARDOG
}
