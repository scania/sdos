package com.scania.sdos.testOrchestration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.scania.sdip.exceptions.IncidentException;
import com.scania.sdip.exceptions.SdipErrorCode;
import com.scania.sdos.orchestration.OfgModelRepo;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.repository.sail.SailRepositoryConnection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OfgModelRepoTest {

  @Mock
  private SailRepository aRepo;
  @Mock
  private SailRepositoryConnection aConn;

  @Spy
  private OfgModelRepo ofgModelRepo;

  @BeforeEach
  void setUp() {
    reset(aRepo);
    reset(aConn);
  }

  @Test
  void addModel_getRepository_ok() {
    doReturn(aRepo).when(ofgModelRepo).getSailRepository();
    doReturn(aConn).when(aRepo).getConnection();
    doNothing().when(aConn).add(any(Model.class));
    Model aModel = mock(Model.class);

    ofgModelRepo.addModel(aModel);
    verify(aRepo, times(1)).getConnection();
    verify(aRepo, times(0)).shutDown();
    verify(aConn, times(1)).add(aModel);

    Repository result = ofgModelRepo.getRepository();
    assertEquals(aRepo, result);
  }

  @Test
  void addModel_unknownError() {
    doReturn(aRepo).when(ofgModelRepo).getSailRepository();
    doReturn(aConn).when(aRepo).getConnection();
    doThrow(new RepositoryException("anError")).when(aConn).add(any(Model.class));
    Model aModel = mock(Model.class);

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      ofgModelRepo.addModel(aModel);
    });
    assertEquals(SdipErrorCode.UNKNOWN_REASON_ERROR.getSdipErrorCode(),
            Integer
                    .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                            .getSdipErrorCodes().get(0)
                            .replace("SDIP_", "")));

    verify(aRepo, times(1)).getConnection();
    verify(aRepo, times(1)).shutDown();

  }

  @Test
  void getRepository_returnsNull() {
    Repository result = ofgModelRepo.getRepository();
    assertNull(result);
  }


  @Test
  void removeModel_ok() {
    doReturn(aRepo).when(ofgModelRepo).getSailRepository();
    doReturn(aConn).when(aRepo).getConnection();
    doNothing().when(aConn).add(any(Model.class));
    Model aModel = mock(Model.class);

    ofgModelRepo.addModel(aModel);
    verify(aRepo, times(1)).getConnection();
    verify(aRepo, times(0)).shutDown();
    verify(aConn, times(1)).add(aModel);

    Repository result = ofgModelRepo.getRepository();
    assertEquals(aRepo, result);

    ofgModelRepo.removeModel();
    verify(aRepo, times(1)).shutDown();

    result = ofgModelRepo.getRepository();
    assertNull(result);

  }

  @Test
  void removeModel_noDb() {

    ofgModelRepo.removeModel();
    verify(aRepo, times(0)).shutDown();

    Repository result = ofgModelRepo.getRepository();
    assertNull(result);
  }

  @Test
  void getSailRepo() {
    Repository sRepo = ofgModelRepo.getSailRepository();
    assertTrue(sRepo instanceof SailRepository);
  }
}