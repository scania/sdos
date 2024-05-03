package com.scania.sdos.testOrchestration.testOModel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

import com.scania.sdos.orchestration.ParameterMemory;
import com.scania.sdip.exceptions.IncidentException;
import com.scania.sdip.exceptions.SdipErrorCode;
import com.scania.sdos.model.ServiceArguments;
import com.scania.sdos.orchestration.Rdf4jClient;
import com.scania.sdos.orchestration.interfaces.IParameterMemory;
import com.scania.sdos.orchestration.model.ResultMetaDataModel;
import com.scania.sdos.utils.SDOSConstants;
import com.scania.sdos.utils.Utility;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;

import com.scania.sdos.orchestration.model.JsonLdContextModel;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ResultMetaDataModelTest {

  private static MockedStatic utilityMock;
  private static MockedStatic localDateTimeMock;
  @Mock
  private Logger logger;
  @Mock
  private Rdf4jClient rdf4jClient;
  @Spy
  private ResultMetaDataModel resultMetaDataModel;
  @Mock
  private ServiceArguments serviceArguments;
  @Mock
  private ParameterMemory daParams;
  @Mock
  private JsonLdContextModel context;

  @BeforeAll
  static void beforeAll() {
    utilityMock = mockStatic(Utility.class);
    localDateTimeMock = mockStatic(LocalDateTime.class);
  }


  @AfterAll
  static void afterAll() {
    utilityMock.close();
    localDateTimeMock.close();
  }

  @BeforeEach
  void setUp() {
    reset(resultMetaDataModel);
    reset(rdf4jClient);
    reset(serviceArguments);
    utilityMock.reset();
    localDateTimeMock.reset();
  }

  @Test
  void populate_ok() {
    resultMetaDataModel.setState("astate");
    when(resultMetaDataModel.getState()).thenReturn("astate");
    when(resultMetaDataModel.getSubjectIri()).thenReturn("asubjectiri");
    assertEquals("astate", resultMetaDataModel.getState());
    assertTrue(resultMetaDataModel.getSubjectIri() != null);

    //Call populate method
    LocalDateTime now = LocalDateTime.of(2023, 07, 30, 10, 0);
    localDateTimeMock.when(() -> LocalDateTime.now()).thenReturn(now);
    utilityMock.when(() ->
            Utility.getCurrentDateAndTime(now, SDOSConstants.TIMESTAMP_PATTERN))
        .thenReturn("2023-07-30-10:00:00:00");
    resultMetaDataModel.populate(any(String.class), any(IParameterMemory.class),
        any(ServiceArguments.class));
    String timestamp = resultMetaDataModel.getTimestamp();
    assertEquals("http://resultnull", resultMetaDataModel.getResultgraph());
    assertEquals("2023-07-30-10:00:00:00", resultMetaDataModel.getTimestamp());

  }

  @Test
  void run_nok() {
    resultMetaDataModel.setRdf4jClient(rdf4jClient);
    resultMetaDataModel.setResultgraph("http://agraph");
    resultMetaDataModel.setTimestamp("time");
    doReturn("subjectiri").when(resultMetaDataModel).getSubjectIri();

    doThrow(
        new IncidentException(SdipErrorCode.DOMAIN_NOT_ANSWERING, logger, "")).when(
            rdf4jClient)
        .executeUpdateSparql(anyString(), anyString(), any());
    Throwable throwable = assertThrows(IncidentException.class, () -> {
      resultMetaDataModel.run(daParams, context, serviceArguments);
    });
    assertEquals(SdipErrorCode.DOMAIN_NOT_ANSWERING.getSdipErrorCode(),
        Integer
            .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                .getSdipErrorCodes().get(0)
                .replace("SDIP_", "")));

  }

  @Test
  void run_ok() throws MalformedURLException {
    serviceArguments.setStardogBaseUrl(new URL("http://baseurl:5820"));
    serviceArguments.setResultDB("result");
    resultMetaDataModel.setRdf4jClient(rdf4jClient);
    resultMetaDataModel.setResultgraph("http://agraph");
    resultMetaDataModel.setTimestamp("time");
    doReturn("subjectiri").when(resultMetaDataModel).getSubjectIri();
    doReturn("aState").when(resultMetaDataModel).getState();

    doNothing().when(rdf4jClient).executeUpdateSparql(anyString(), anyString(), any());
    resultMetaDataModel.run(daParams, context, serviceArguments);

    verify(rdf4jClient, times(2))
            .executeUpdateSparql(anyString(), anyString(), any());
  }

}