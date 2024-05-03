package com.scania.sdos.triplestores;

import com.scania.sdip.exceptions.IncidentException;
import com.scania.sdip.exceptions.SdipErrorCode;
import com.scania.sdos.model.TripleStoreType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class maps the triplestore type parameter to the corresponding TripleStoreType class
 */
public class TripleStoreFactory {

  private static final Logger LOGGER = LogManager.getLogger(TripleStoreFactory.class);

  public static TripleStore getTripleStore(TripleStoreType tripleStoreType, String sparqlEndpoint) {
    if (tripleStoreType.equals(TripleStoreType.NEPTUNE)) {
      return new Neptune(sparqlEndpoint);

    } else if (tripleStoreType.equals(TripleStoreType.GRAPHDB)) {
      return new GraphDB(sparqlEndpoint);

    } else if (tripleStoreType.equals(TripleStoreType.BLAZEGRAPH)) {
      return new Blazegraph(sparqlEndpoint);

    } else if (tripleStoreType.equals(TripleStoreType.GENERIC)) {
      return new Generic(sparqlEndpoint);

    }
    throw new IncidentException(SdipErrorCode.INVALID_TRIPLESTORE_ID, LOGGER, tripleStoreType,
        TripleStoreType.BLAZEGRAPH + ", " + TripleStoreType.GRAPHDB + ", " + TripleStoreType.NEPTUNE
            + ", " + TripleStoreType.GENERIC);
  }

}

