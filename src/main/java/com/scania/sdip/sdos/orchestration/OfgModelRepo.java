package com.scania.sdip.sdos.orchestration;

import com.scania.sdip.exceptions.IncidentException;
import com.scania.sdip.exceptions.SdipErrorCode;
import com.scania.sdip.exceptions.SdipErrorParameter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

/**
 * This class holds the graph as an in-memory Repository for the duration of the
 * call for the endpoint.
 */
public class OfgModelRepo {

  private static final Logger LOGGER = LogManager.getLogger(OfgModelRepo.class);
  private Repository repository;
  public OfgModelRepo() {
      //Default constructor
  }

  public Repository getRepository() {
    return repository;
  }

  public void addModel(Model model) {
    repository = getSailRepository();
    try (RepositoryConnection conn = repository.getConnection()) {
      // add the model
      conn.add(model);
    } catch (Exception e) {
      // before throwing exception, make sure the database is properly shut down.
      repository.shutDown();
      throw new IncidentException(e, SdipErrorCode.UNKNOWN_REASON_ERROR, LOGGER, e.getMessage(),
              SdipErrorParameter.SUPPORTMAIL);
    }
  }

  public SailRepository getSailRepository() {
    return new SailRepository(new MemoryStore());
  }

  public void removeModel() {
    if (repository != null) {
      repository.shutDown();
      repository = null;
    }
  }
}
