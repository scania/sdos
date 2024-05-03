package com.scania.sdos.testOrchestration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.predic8.soamodel.WrongGrammarException;
import com.predic8.wsdl.AbstractAddress;
import com.predic8.wsdl.Binding;
import com.predic8.wsdl.Definitions;
import com.predic8.wsdl.Port;
import com.predic8.wsdl.PortType;
import com.predic8.wsdl.Service;
import com.predic8.wsdl.WSDLParser;
import com.predic8.wsdl.WSDLVersion2NotSupportedException;
import com.predic8.wstool.creator.SOARequestCreator;
import com.scania.sdip.exceptions.IncidentException;
import com.scania.sdip.exceptions.SdipErrorCode;
import com.scania.sdos.orchestration.JavaGroovy;
import com.scania.sdos.orchestration.RestTemplateClient;
import com.scania.sdos.orchestration.SoapClient;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.xml.namespace.QName;

import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.SOAPPart;
import jakarta.xml.soap.SOAPMessage;
import jakarta.xml.soap.SOAPEnvelope;
import jakarta.xml.soap.SOAPHeader;
import jakarta.xml.soap.SOAPElement;
import jakarta.xml.soap.SOAPException;
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
class soapClientSpyTest {

  private static MockedStatic<? extends MessageFactory> messageFactoryStaticMock;

  @Spy
  SoapClient soapClientSpy;
  @Mock
  WSDLParser wsdlParserMock;
  @Mock
  Definitions definitionsMock;
  @Mock
  RestTemplateClient httpClientMock;
  @Mock
  StringWriter stringWriterMock;
  @Mock
  SOARequestCreator soaRequestCreatorMock;
  @Mock
  MessageFactory messageFactoryMock;
  @Mock
  SOAPMessage soapMessageMock;
  @Mock
  SOAPPart soapPartMock;
  @Mock
  SOAPEnvelope soapEnvelopeMock;
  @Mock
  SOAPHeader soapHeaderMock;
  @Mock
  SOAPElement soapElementMock;
  @Mock
  ByteArrayOutputStream byteArrayOutputStreamMock;
  @Mock
  Binding bindingMock;
  @Mock
  PortType portTypeMock;
  @Mock
  Port portMock;
  @Mock
  Service serviceMock;
  @Mock
  AbstractAddress abstractAddressMock;

  @BeforeAll
  static void beforeAll() {
    messageFactoryStaticMock = mockStatic(MessageFactory.class);
  }

  @AfterAll
  static void afterAll() {
    messageFactoryStaticMock.close();
  }

  @BeforeEach
  void setUp() {
    messageFactoryStaticMock.reset();
    reset(wsdlParserMock);
  }

  @Test
  void soapClientSpy_parser_expected_root_element_nok() {
    doReturn(wsdlParserMock).when(soapClientSpy).getWsdlParser();

    doThrow(WrongGrammarException.class).when(wsdlParserMock).parse(anyString());

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      soapClientSpy.init("aWsldFile", "aBindingName");
    });
    assertEquals(SdipErrorCode.SOAP_RESPONSE_WSDL_NOK.getSdipErrorCode(),
            Integer
                    .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                            .getSdipErrorCodes().get(0)
                            .replace("SDIP_", "")));
  }

  @Test
  void init_parser_expected_root_element_nok() {
    doReturn(wsdlParserMock).when(soapClientSpy).getWsdlParser();

    doThrow(WrongGrammarException.class).when(wsdlParserMock).parse(anyString());

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      soapClientSpy.init("aWsldFile", "aBindingName");
    });
    assertEquals(SdipErrorCode.SOAP_RESPONSE_WSDL_NOK.getSdipErrorCode(),
            Integer
                    .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                            .getSdipErrorCodes().get(0)
                            .replace("SDIP_", "")));
  }

  @Test
  void init_parser_not_valid_wsdl_nok() {
    doReturn(wsdlParserMock).when(soapClientSpy).getWsdlParser();

    doThrow(RuntimeException.class).when(wsdlParserMock).parse(anyString());

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      soapClientSpy.init("aWsldFile", "aBindingName");
    });
    assertEquals(SdipErrorCode.SOAP_RESPONSE_WSDL_NOK.getSdipErrorCode(),
            Integer
                    .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                            .getSdipErrorCodes().get(0)
                            .replace("SDIP_", "")));
  }

  @Test
  void init_parser_runtime_nok() {
    doReturn(wsdlParserMock).when(soapClientSpy).getWsdlParser();

    doThrow(RuntimeException.class).when(wsdlParserMock).parse(anyString());

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      soapClientSpy.init("aWsldFile", "aBindingName");
    });
    assertEquals(SdipErrorCode.SOAP_RESPONSE_WSDL_NOK.getSdipErrorCode(),
            Integer
                    .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                            .getSdipErrorCodes().get(0)
                            .replace("SDIP_", "")));
  }

  @Test
  void init_parser_wsdl_v2_notsupported_nok() {
    doReturn(wsdlParserMock).when(soapClientSpy).getWsdlParser();

    doThrow(new WSDLVersion2NotSupportedException("kalleanka")).when(wsdlParserMock)
        .parse(anyString());

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      soapClientSpy.init("aWsldFile", "aBindingName");
    });
    assertEquals(SdipErrorCode.SOAP_RESPONSE_WSDL_NOK.getSdipErrorCode(),
            Integer
                    .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                            .getSdipErrorCodes().get(0)
                            .replace("SDIP_", "")));
  }

  @Test
  void init_findPortType_notfound_nok() {
    List<Binding> bindingList = new ArrayList<>();
    bindingList.add(bindingMock);
    String sm = "notABindingName";
    doReturn(wsdlParserMock).when(soapClientSpy).getWsdlParser();
    doReturn(definitionsMock).when(wsdlParserMock).parse(anyString());
    doReturn(bindingList).when(definitionsMock).getBindings();
    doReturn(sm).when(bindingMock).getName();

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      soapClientSpy.init("aWsldFile", "aBindingName");
    });
    assertEquals(SdipErrorCode.SOAP_RESPONSE_WSDL_NOK.getSdipErrorCode(),
            Integer
                    .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                            .getSdipErrorCodes().get(0)
                            .replace("SDIP_", "")));
  }

  @Test
  void init_findPortType_urlLocation_ok() {

    List<Port> portList = new ArrayList<>();
    portList.add(portMock);

    List<Service> serviceList = new ArrayList<>();
    serviceList.add(serviceMock);

    List<Binding> bindingList = new ArrayList<>();
    bindingList.add(bindingMock);

    String bindingName = "aBindingName";
    String pType = "123";
    doReturn(wsdlParserMock).when(soapClientSpy).getWsdlParser();
    doReturn(definitionsMock).when(wsdlParserMock).parse(anyString());
    doReturn(bindingList).when(definitionsMock).getBindings();
    doReturn(portTypeMock).when(bindingMock).getPortType();
    doReturn(pType).when(portTypeMock).getName();
    doReturn(serviceList).when(definitionsMock).getServices();
    doReturn(portList).when(serviceMock).getPorts();
    doReturn(bindingMock).when(portMock).getBinding();
    doReturn(bindingName).when(bindingMock).getName();
    doReturn(abstractAddressMock).when(portMock).getAddress();
    doReturn("lcoation").when(abstractAddressMock).getLocation();

    soapClientSpy.init("aWsldFile", "aBindingName");
    verify(wsdlParserMock, times(1)).parse(anyString());
    verify(abstractAddressMock, times(1)).getLocation();
    verify(portTypeMock, times(1)).getName();

  }

  @Test
  void init_findUrlLocation_notfound_nok() {
    Binding bindingMock1 = mock(Binding.class);

    List<Port> portList = new ArrayList<>();
    portList.add(portMock);

    List<Service> serviceList = new ArrayList<>();
    serviceList.add(serviceMock);

    List<Binding> bindingList = new ArrayList<>();
    bindingList.add(bindingMock);
    bindingList.add(bindingMock1);
    String bindingName = "aBindingName";
    String pType = "123";

    doReturn(wsdlParserMock).when(soapClientSpy).getWsdlParser();
    doReturn(definitionsMock).when(wsdlParserMock).parse(anyString());
    doReturn(bindingList).when(definitionsMock).getBindings();
    doReturn(bindingName).when(bindingMock).getName();
    doReturn(portTypeMock).when(bindingMock).getPortType();
    doReturn(pType).when(portTypeMock).getName();

    doReturn(serviceList).when(definitionsMock).getServices();
    doReturn(portList).when(serviceMock).getPorts();
    doReturn(bindingMock1).when(portMock).getBinding();
    doReturn("asd").when(bindingMock1).getName();

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      soapClientSpy.init("aWsldFile", "aBindingName");
    });
    assertEquals(SdipErrorCode.SOAP_RESPONSE_WSDL_NOK.getSdipErrorCode(),
            Integer
                    .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                            .getSdipErrorCodes().get(0)
                            .replace("SDIP_", "")));
  }

  @Test
  void createSoapRequest_ok() {

    List<Port> portList = new ArrayList<>();
    portList.add(portMock);

    List<Service> serviceList = new ArrayList<>();
    serviceList.add(serviceMock);

    List<Binding> bindingList = new ArrayList<>();
    bindingList.add(bindingMock);

    String bindingName = "aBindingName";
    String pType = "123";

    String request = "<s11:Envelope xmlns:s11='http://schemas.xmlsoap.org/soap/envelope/'>\n"
        + "  <s11:Body>\n"
        + "    <tns1:logIn xmlns:tns1='http://ws.polarion.com/SessionWebService-impl'>\n"
        + "      <tns1:userName>hoppsan</tns1:userName>\n"
        + "      <tns1:password>hej</tns1:password>\n"
        + "    </tns1:logIn>\n"
        + "  </s11:Body>\n"
        + "</s11:Envelope>";

    doReturn(bindingList).when(definitionsMock).getBindings();
    doReturn(portTypeMock).when(bindingMock).getPortType();
    doReturn(pType).when(portTypeMock).getName();
    doReturn(serviceList).when(definitionsMock).getServices();
    doReturn(portList).when(serviceMock).getPorts();
    doReturn(bindingMock).when(portMock).getBinding();
    doReturn(bindingName).when(bindingMock).getName();
    doReturn(abstractAddressMock).when(portMock).getAddress();
    doReturn("lcoation").when(abstractAddressMock).getLocation();

    doReturn(wsdlParserMock).when(soapClientSpy).getWsdlParser();
    doReturn(definitionsMock).when(wsdlParserMock).parse(anyString());
    doReturn(stringWriterMock).when(soapClientSpy).getStringWriter();
    doReturn(soaRequestCreatorMock).when(soapClientSpy).getSoaRequestCreator(stringWriterMock);
    doNothing().when(soaRequestCreatorMock).setFormParams(any());
    doReturn(null).when(soaRequestCreatorMock).createRequest(any(), any(), any());
    doReturn(request).when(stringWriterMock).toString();

    soapClientSpy.init("aWsldFile", "aBindingName");
    soapClientSpy.setRestTemplateClient(httpClientMock);

    HashMap<String, String> params = new HashMap<>();
    params.put("xpath:/logIn/userName", "aUserName");
    params.put("xpath:/logIn/password", "aPassword");

    String result = soapClientSpy.createSoapRequest("logIn", params, null);
    assertTrue(result.equals(request));
    verify(soaRequestCreatorMock, times(1)).createRequest(any(), any(), any());
    verify(soapClientSpy, times(1)).getSoaRequestCreator(any());

  }

  @Test
  void createSoapRequest_withHeaders_ok() {

    List<Port> portList = new ArrayList<>();
    portList.add(portMock);

    List<Service> serviceList = new ArrayList<>();
    serviceList.add(serviceMock);

    List<Binding> bindingList = new ArrayList<>();
    bindingList.add(bindingMock);

    String bindingName = "aBindingName";
    String pType = "123";

    String request = "aRequest";

    HashMap<String, String> params = new HashMap<>();
    params.put("xpath:/getWorkItemId/id", "1");
    params.put("xpath:/getWorkItemId/name", "test");

    HashMap<QName, String> headers = new HashMap<>();
    headers.put(new QName("sessionID"), "123456789");

    doReturn(bindingList).when(definitionsMock).getBindings();
    doReturn(portTypeMock).when(bindingMock).getPortType();
    doReturn(pType).when(portTypeMock).getName();
    doReturn(serviceList).when(definitionsMock).getServices();
    doReturn(portList).when(serviceMock).getPorts();
    doReturn(bindingMock).when(portMock).getBinding();
    doReturn(bindingName).when(bindingMock).getName();
    doReturn(abstractAddressMock).when(portMock).getAddress();
    doReturn("lcoation").when(abstractAddressMock).getLocation();

    messageFactoryStaticMock.when(() -> MessageFactory.newInstance())
        .thenReturn(messageFactoryMock);
    try {
      when(messageFactoryMock.createMessage(any(), any())).thenReturn(soapMessageMock);
      when(soapPartMock.getEnvelope()).thenReturn(soapEnvelopeMock);
      when(soapEnvelopeMock.addHeader()).thenReturn(soapHeaderMock);
      when(soapHeaderMock.addChildElement(any(QName.class))).thenReturn(soapElementMock);
      doReturn(soapElementMock).when(soapElementMock).addTextNode(anyString());
      doNothing().when(soapMessageMock).saveChanges();
      doReturn(byteArrayOutputStreamMock).when(soapClientSpy).getByteArrayOutputStream();
      doReturn("ok").when(byteArrayOutputStreamMock).toString();
    } catch (IOException e) {
      e.printStackTrace();
      fail();
    } catch (SOAPException e) {
      e.printStackTrace();
      fail();
    }
    doReturn(soapPartMock).when(soapMessageMock).getSOAPPart();
    doReturn(wsdlParserMock).when(soapClientSpy).getWsdlParser();
    doReturn(definitionsMock).when(wsdlParserMock).parse(anyString());
    doReturn(stringWriterMock).when(soapClientSpy).getStringWriter();
    doReturn(soaRequestCreatorMock).when(soapClientSpy).getSoaRequestCreator(stringWriterMock);
    doNothing().when(soaRequestCreatorMock).setFormParams(any());
    doReturn(null).when(soaRequestCreatorMock).createRequest(any(), any(), any());
    doReturn(request).when(stringWriterMock).toString();

    soapClientSpy.init("aWsldFile", "aBindingName");
    soapClientSpy.setRestTemplateClient(httpClientMock);

    try {
      String result = soapClientSpy.createSoapRequest("getWorkItemId", params, headers);
      assertTrue(result.equals("ok"));
    } catch (Exception e) {
      fail();
    }
  }

  @Test
  void addHeader_noHeaders_nok() {

    List<Port> portList = new ArrayList<>();
    portList.add(portMock);

    List<Service> serviceList = new ArrayList<>();
    serviceList.add(serviceMock);

    List<Binding> bindingList = new ArrayList<>();
    bindingList.add(bindingMock);

    String bindingName = "aBindingName";
    String pType = "123";

    String request = "aRequest";
    HashMap<String, String> params = new HashMap<>();
    params.put("xpath:/getWorkItemId/id", "1");
    params.put("xpath:/getWorkItemId/name", "test");

    HashMap<QName, String> headers = new HashMap<>();
    headers.put(new QName("sessionID"), "123456789");

    doReturn(bindingList).when(definitionsMock).getBindings();
    doReturn(portTypeMock).when(bindingMock).getPortType();
    doReturn(pType).when(portTypeMock).getName();
    doReturn(serviceList).when(definitionsMock).getServices();
    doReturn(portList).when(serviceMock).getPorts();
    doReturn(bindingMock).when(portMock).getBinding();
    doReturn(bindingName).when(bindingMock).getName();
    doReturn(abstractAddressMock).when(portMock).getAddress();
    doReturn("lcoation").when(abstractAddressMock).getLocation();

    messageFactoryStaticMock.when(() -> MessageFactory.newInstance())
        .thenReturn(messageFactoryMock);
    try {
      when(messageFactoryMock.createMessage(any(), any())).thenReturn(soapMessageMock);
      when(soapPartMock.getEnvelope()).thenReturn(soapEnvelopeMock);
      when(soapEnvelopeMock.addHeader()).thenReturn(soapHeaderMock);
      when(soapHeaderMock.addChildElement(any(QName.class))).thenThrow(new SOAPException());
    } catch (IOException e) {
      e.printStackTrace();
      fail();
    } catch (SOAPException e) {
      e.printStackTrace();
      fail();
    }
    doReturn(soapPartMock).when(soapMessageMock).getSOAPPart();
    doReturn(wsdlParserMock).when(soapClientSpy).getWsdlParser();
    doReturn(definitionsMock).when(wsdlParserMock).parse(anyString());
    doReturn(stringWriterMock).when(soapClientSpy).getStringWriter();
    doReturn(soaRequestCreatorMock).when(soapClientSpy).getSoaRequestCreator(stringWriterMock);
    doNothing().when(soaRequestCreatorMock).setFormParams(any());
    doReturn(null).when(soaRequestCreatorMock).createRequest(any(), any(), any());
    doReturn(request).when(stringWriterMock).toString();

    soapClientSpy.init("aWsldFile", "aBindingName");
    soapClientSpy.setRestTemplateClient(httpClientMock);

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      soapClientSpy.createSoapRequest("getWorkItemId", params, headers);
    });
    assertEquals(SdipErrorCode.SOAP_ERROR.getSdipErrorCode(),
            Integer
                    .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                            .getSdipErrorCodes().get(0)
                            .replace("SDIP_", "")));
  }

  @Test
  void addHeader_validSoapHeader_alreadyExist_nok() {

    List<Port> portList = new ArrayList<>();
    portList.add(portMock);

    List<Service> serviceList = new ArrayList<>();
    serviceList.add(serviceMock);

    List<Binding> bindingList = new ArrayList<>();
    bindingList.add(bindingMock);

    String bindingName = "aBindingName";
    String pType = "123";

    String request = "aRequest";
    HashMap<String, String> params = new HashMap<>();
    params.put("xpath:/getWorkItemId/id", "1");
    params.put("xpath:/getWorkItemId/name", "test");

    HashMap<QName, String> headers = new HashMap<>();
    headers.put(new QName("sessionID"), "123456789");

    doReturn(bindingList).when(definitionsMock).getBindings();
    doReturn(portTypeMock).when(bindingMock).getPortType();
    doReturn(pType).when(portTypeMock).getName();
    doReturn(serviceList).when(definitionsMock).getServices();
    doReturn(portList).when(serviceMock).getPorts();
    doReturn(bindingMock).when(portMock).getBinding();
    doReturn(bindingName).when(bindingMock).getName();
    doReturn(abstractAddressMock).when(portMock).getAddress();
    doReturn("lcoation").when(abstractAddressMock).getLocation();

    messageFactoryStaticMock.when(() -> MessageFactory.newInstance())
        .thenReturn(messageFactoryMock);
    try {
      when(messageFactoryMock.createMessage(any(), any())).thenReturn(soapMessageMock);
      when(soapPartMock.getEnvelope()).thenReturn(soapEnvelopeMock);
      when(soapEnvelopeMock.addHeader()).thenThrow(new SOAPException());

    } catch (IOException e) {
      e.printStackTrace();
      fail();
    } catch (SOAPException e) {
      e.printStackTrace();
      fail();
    }
    doReturn(soapPartMock).when(soapMessageMock).getSOAPPart();
    doReturn(wsdlParserMock).when(soapClientSpy).getWsdlParser();
    doReturn(definitionsMock).when(wsdlParserMock).parse(anyString());
    doReturn(stringWriterMock).when(soapClientSpy).getStringWriter();
    doReturn(soaRequestCreatorMock).when(soapClientSpy).getSoaRequestCreator(stringWriterMock);
    doNothing().when(soaRequestCreatorMock).setFormParams(any());
    doReturn(null).when(soaRequestCreatorMock).createRequest(any(), any(), any());
    doReturn(request).when(stringWriterMock).toString();

    soapClientSpy.init("aWsldFile", "aBindingName");
    soapClientSpy.setRestTemplateClient(httpClientMock);

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      soapClientSpy.createSoapRequest("getWorkItemId", params, headers);
    });
    assertEquals(SdipErrorCode.SOAP_ERROR.getSdipErrorCode(),
            ((IncidentException) throwable).getErrorBindings().get(0).getErrorCode().getSdipErrorCode());
  }

  @Test
  void addHeader_soapError_nok() {

    List<Port> portList = new ArrayList<>();
    portList.add(portMock);

    List<Service> serviceList = new ArrayList<>();
    serviceList.add(serviceMock);

    List<Binding> bindingList = new ArrayList<>();
    bindingList.add(bindingMock);

    String bindingName = "aBindingName";
    String pType = "123";

    String request = "aRequest";
    HashMap<String, String> params = new HashMap<>();
    params.put("xpath:/getWorkItemId/id", "1");
    params.put("xpath:/getWorkItemId/name", "test");

    HashMap<QName, String> headers = new HashMap<>();
    headers.put(new QName("sessionID"), "123456789");

    doReturn(bindingList).when(definitionsMock).getBindings();
    doReturn(portTypeMock).when(bindingMock).getPortType();
    doReturn(pType).when(portTypeMock).getName();
    doReturn(serviceList).when(definitionsMock).getServices();
    doReturn(portList).when(serviceMock).getPorts();
    doReturn(bindingMock).when(portMock).getBinding();
    doReturn(bindingName).when(bindingMock).getName();
    doReturn(abstractAddressMock).when(portMock).getAddress();
    doReturn("lcoation").when(abstractAddressMock).getLocation();

    messageFactoryStaticMock.when(() -> MessageFactory.newInstance())
        .thenReturn(messageFactoryMock);
    try {
      when(messageFactoryMock.createMessage(any(), any())).thenReturn(soapMessageMock);
      when(soapPartMock.getEnvelope()).thenThrow(new SOAPException());

    } catch (IOException e) {
      e.printStackTrace();
      fail();
    } catch (SOAPException e) {
      e.printStackTrace();
      fail();
    }
    doReturn(soapPartMock).when(soapMessageMock).getSOAPPart();
    doReturn(wsdlParserMock).when(soapClientSpy).getWsdlParser();
    doReturn(definitionsMock).when(wsdlParserMock).parse(anyString());
    doReturn(stringWriterMock).when(soapClientSpy).getStringWriter();
    doReturn(soaRequestCreatorMock).when(soapClientSpy).getSoaRequestCreator(stringWriterMock);
    doNothing().when(soaRequestCreatorMock).setFormParams(any());
    doReturn(null).when(soaRequestCreatorMock).createRequest(any(), any(), any());
    doReturn(request).when(stringWriterMock).toString();

    soapClientSpy.init("aWsldFile", "aBindingName");
    soapClientSpy.setRestTemplateClient(httpClientMock);

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      soapClientSpy.createSoapRequest("getWorkItemId", params, headers);
    });
    assertEquals(SdipErrorCode.SOAP_ERROR.getSdipErrorCode(),
            Integer
                    .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                            .getSdipErrorCodes().get(0)
                            .replace("SDIP_", "")));
  }

  @Test
  void addHeader_header_novalue_nok() {

    List<Port> portList = new ArrayList<>();
    portList.add(portMock);

    List<Service> serviceList = new ArrayList<>();
    serviceList.add(serviceMock);

    List<Binding> bindingList = new ArrayList<>();
    bindingList.add(bindingMock);

    String bindingName = "aBindingName";
    String pType = "123";

    String request = "aRequest";
    HashMap<String, String> params = new HashMap<>();
    params.put("xpath:/getWorkItemId/id", "1");
    params.put("xpath:/getWorkItemId/name", "test");

    HashMap<QName, String> headers = new HashMap<>();
    headers.put(new QName("sessionID"), "123456789");

    doReturn(bindingList).when(definitionsMock).getBindings();
    doReturn(portTypeMock).when(bindingMock).getPortType();
    doReturn(pType).when(portTypeMock).getName();
    doReturn(serviceList).when(definitionsMock).getServices();
    doReturn(portList).when(serviceMock).getPorts();
    doReturn(bindingMock).when(portMock).getBinding();
    doReturn(bindingName).when(bindingMock).getName();
    doReturn(abstractAddressMock).when(portMock).getAddress();
    doReturn("lcoation").when(abstractAddressMock).getLocation();

    messageFactoryStaticMock.when(() -> MessageFactory.newInstance())
        .thenReturn(messageFactoryMock);
    try {
      when(messageFactoryMock.createMessage(any(), any())).thenReturn(soapMessageMock);
      when(soapPartMock.getEnvelope()).thenReturn(soapEnvelopeMock);
      when(soapEnvelopeMock.addHeader()).thenReturn(soapHeaderMock);
      when(soapHeaderMock.addChildElement(any(QName.class))).thenReturn(soapElementMock);
      when(soapElementMock.addTextNode(anyString())).thenThrow(new SOAPException());
    } catch (IOException e) {
      e.printStackTrace();
      fail();
    } catch (SOAPException e) {
      e.printStackTrace();
      fail();
    }
    doReturn(soapPartMock).when(soapMessageMock).getSOAPPart();
    doReturn(wsdlParserMock).when(soapClientSpy).getWsdlParser();
    doReturn(definitionsMock).when(wsdlParserMock).parse(anyString());
    doReturn(stringWriterMock).when(soapClientSpy).getStringWriter();
    doReturn(soaRequestCreatorMock).when(soapClientSpy).getSoaRequestCreator(stringWriterMock);
    doNothing().when(soaRequestCreatorMock).setFormParams(any());
    doReturn(null).when(soaRequestCreatorMock).createRequest(any(), any(), any());
    doReturn(request).when(stringWriterMock).toString();
    soapClientSpy.init("aWsldFile", "aBindingName");
    soapClientSpy.setRestTemplateClient(httpClientMock);

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      soapClientSpy.createSoapRequest("getWorkItemId", params, headers);
    });
    assertEquals(SdipErrorCode.SOAP_ERROR.getSdipErrorCode(),
            Integer
                    .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                            .getSdipErrorCodes().get(0)
                            .replace("SDIP_", "")));
  }

  @Test
  void addHeader_writeTo_nok() {

    List<Port> portList = new ArrayList<>();
    portList.add(portMock);

    List<Service> serviceList = new ArrayList<>();
    serviceList.add(serviceMock);

    List<Binding> bindingList = new ArrayList<>();
    bindingList.add(bindingMock);

    String bindingName = "aBindingName";
    String pType = "123";

    String request = "<s11:Envelope xmlns:s11='http://schemas.xmlsoap.org/soap/envelope/'>\n"
        + "  <s11:Body>\n"
        + "    <tns1:logIn xmlns:tns1='http://ws.polarion.com/SessionWebService-impl'>\n"
        + "      <tns1:userName>hoppsan</tns1:userName>\n"
        + "      <tns1:password>hej</tns1:password>\n"
        + "    </tns1:logIn>\n"
        + "  </s11:Body>\n"
        + "</s11:Envelope>";

    HashMap<String, String> params = new HashMap<>();
    params.put("xpath:/getWorkItemId/id", "1");
    params.put("xpath:/getWorkItemId/name", "test");

    HashMap<QName, String> headers = new HashMap<>();
    headers.put(new QName("sessionID"), "123456789");

    doReturn(bindingList).when(definitionsMock).getBindings();
    doReturn(portTypeMock).when(bindingMock).getPortType();
    doReturn(pType).when(portTypeMock).getName();
    doReturn(serviceList).when(definitionsMock).getServices();
    doReturn(portList).when(serviceMock).getPorts();
    doReturn(bindingMock).when(portMock).getBinding();
    doReturn(bindingName).when(bindingMock).getName();
    doReturn(abstractAddressMock).when(portMock).getAddress();
    doReturn("lcoation").when(abstractAddressMock).getLocation();

    messageFactoryStaticMock.when(() -> MessageFactory.newInstance())
        .thenReturn(messageFactoryMock);
    try {
      when(messageFactoryMock.createMessage(any(), any())).thenReturn(soapMessageMock);
      when(soapPartMock.getEnvelope()).thenReturn(soapEnvelopeMock);
      when(soapEnvelopeMock.addHeader()).thenReturn(soapHeaderMock);
      when(soapHeaderMock.addChildElement(any(QName.class))).thenReturn(soapElementMock);
      doReturn(soapElementMock).when(soapElementMock).addTextNode(anyString());
      doNothing().when(soapMessageMock).saveChanges();
      doReturn(byteArrayOutputStreamMock).when(soapClientSpy).getByteArrayOutputStream();
      doThrow(new IOException()).when(soapMessageMock).writeTo(byteArrayOutputStreamMock);
    } catch (IOException e) {
      e.printStackTrace();
      fail();
    } catch (SOAPException e) {
      e.printStackTrace();
      fail();
    }
    doReturn(soapPartMock).when(soapMessageMock).getSOAPPart();
    doReturn(wsdlParserMock).when(soapClientSpy).getWsdlParser();
    doReturn(definitionsMock).when(wsdlParserMock).parse(anyString());
    doReturn(stringWriterMock).when(soapClientSpy).getStringWriter();
    doReturn(soaRequestCreatorMock).when(soapClientSpy).getSoaRequestCreator(stringWriterMock);
    doNothing().when(soaRequestCreatorMock).setFormParams(any());
    doReturn(null).when(soaRequestCreatorMock).createRequest(any(), any(), any());
    doReturn(request).when(stringWriterMock).toString();

    soapClientSpy.init("aWsldFile", "aBindingName");
    soapClientSpy.setRestTemplateClient(httpClientMock);

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      soapClientSpy.createSoapRequest("getWorkItemId", params, headers);
    });
    assertEquals(SdipErrorCode.UNKNOWN_REASON_ERROR.getSdipErrorCode(),
            Integer
                    .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                            .getSdipErrorCodes().get(0)
                            .replace("SDIP_", "")));
  }

  @Test
  void addHeader_ok() {

    List<Port> portList = new ArrayList<>();
    portList.add(portMock);

    List<Service> serviceList = new ArrayList<>();
    serviceList.add(serviceMock);

    List<Binding> bindingList = new ArrayList<>();
    bindingList.add(bindingMock);

    String bindingName = "aBindingName";
    String pType = "123";

    String request = "<s11:Envelope xmlns:s11='http://schemas.xmlsoap.org/soap/envelope/'>\n"
        + "  <s11:Body>\n"
        + "    <tns1:logIn xmlns:tns1='http://ws.polarion.com/SessionWebService-impl'>\n"
        + "      <tns1:userName>hoppsan</tns1:userName>\n"
        + "      <tns1:password>hej</tns1:password>\n"
        + "    </tns1:logIn>\n"
        + "  </s11:Body>\n"
        + "</s11:Envelope>";
    HashMap<String, String> params = new HashMap<>();
    params.put("xpath:/getWorkItemId/id", "1");
    params.put("xpath:/getWorkItemId/name", "test");

    HashMap<QName, String> headers = new HashMap<>();
    headers.put(new QName("sessionID"), "123456789");

    doReturn(bindingList).when(definitionsMock).getBindings();
    doReturn(portTypeMock).when(bindingMock).getPortType();
    doReturn(pType).when(portTypeMock).getName();
    doReturn(serviceList).when(definitionsMock).getServices();
    doReturn(portList).when(serviceMock).getPorts();
    doReturn(bindingMock).when(portMock).getBinding();
    doReturn(bindingName).when(bindingMock).getName();
    doReturn(abstractAddressMock).when(portMock).getAddress();
    doReturn("lcoation").when(abstractAddressMock).getLocation();

    messageFactoryStaticMock.when(() -> MessageFactory.newInstance())
        .thenReturn(messageFactoryMock);
    try {
      when(messageFactoryMock.createMessage(any(), any())).thenReturn(soapMessageMock);
      when(soapPartMock.getEnvelope()).thenReturn(soapEnvelopeMock);
      when(soapEnvelopeMock.addHeader()).thenReturn(soapHeaderMock);
      when(soapHeaderMock.addChildElement(any(QName.class))).thenReturn(soapElementMock);
      doReturn(soapElementMock).when(soapElementMock).addTextNode(anyString());
      doNothing().when(soapMessageMock).saveChanges();
      doReturn(byteArrayOutputStreamMock).when(soapClientSpy).getByteArrayOutputStream();
      doReturn("ok").when(byteArrayOutputStreamMock).toString();
    } catch (IOException e) {
      e.printStackTrace();
      fail();
    } catch (SOAPException e) {
      e.printStackTrace();
      fail();
    }
    doReturn(soapPartMock).when(soapMessageMock).getSOAPPart();
    doReturn(wsdlParserMock).when(soapClientSpy).getWsdlParser();
    doReturn(definitionsMock).when(wsdlParserMock).parse(anyString());
    doReturn(stringWriterMock).when(soapClientSpy).getStringWriter();
    doReturn(soaRequestCreatorMock).when(soapClientSpy).getSoaRequestCreator(stringWriterMock);
    doNothing().when(soaRequestCreatorMock).setFormParams(any());
    doReturn(null).when(soaRequestCreatorMock).createRequest(any(), any(), any());
    doReturn(request).when(stringWriterMock).toString();

    soapClientSpy.init("aWsldFile", "aBindingName");
    soapClientSpy.setRestTemplateClient(httpClientMock);

    String result = soapClientSpy.createSoapRequest("getWorkItemId", params, headers);
    assertEquals("ok", result);
    verify(soapMessageMock, times(1)).getSOAPPart();
    try {
      verify(soapPartMock, times(1)).getEnvelope();
      verify(soapEnvelopeMock, times(1)).addHeader();
      verify(soapHeaderMock, times(1)).addChildElement(new QName("sessionID"));
      verify(soapElementMock, times(1)).addTextNode("123456789");
      verify(soapMessageMock, times(1)).saveChanges();
      verify(soapMessageMock, times(1)).writeTo(any());
    } catch (SOAPException e) {
      e.printStackTrace();
      fail();
    } catch (IOException e) {
      e.printStackTrace();
      fail();
    }


  }

  @Test
  void getSoapMessageFromString_IO_nok() {

    String request = "<s11:Envelope xmlns:s11='http://schemas.xmlsoap.org/soap/envelope/'>\n"
        + "  <s11:Body>\n"
        + "    <tns1:logIn xmlns:tns1='http://ws.polarion.com/SessionWebService-impl'>\n"
        + "      <tns1:userName>hoppsan</tns1:userName>\n"
        + "      <tns1:password>hej</tns1:password>\n"
        + "    </tns1:logIn>\n"
        + "  </s11:Body>\n"
        + "</s11:Envelope>";
    HashMap<String, String> params = new HashMap<>();
    params.put("xpath:/getWorkItemId/id", "1");
    params.put("xpath:/getWorkItemId/name", "test");

    HashMap<QName, String> headers = new HashMap<>();
    headers.put(new QName("sessionID"), "123456789");

    List<Port> portList = new ArrayList<>();
    portList.add(portMock);

    List<Service> serviceList = new ArrayList<>();
    serviceList.add(serviceMock);

    List<Binding> bindingList = new ArrayList<>();
    bindingList.add(bindingMock);

    String bindingName = "aBindingName";
    String pType = "123";

    doReturn(bindingList).when(definitionsMock).getBindings();
    doReturn(portTypeMock).when(bindingMock).getPortType();
    doReturn(pType).when(portTypeMock).getName();
    doReturn(serviceList).when(definitionsMock).getServices();
    doReturn(portList).when(serviceMock).getPorts();
    doReturn(bindingMock).when(portMock).getBinding();
    doReturn(bindingName).when(bindingMock).getName();
    doReturn(abstractAddressMock).when(portMock).getAddress();
    doReturn("lcoation").when(abstractAddressMock).getLocation();

    messageFactoryStaticMock.when(() -> MessageFactory.newInstance())
        .thenReturn(messageFactoryMock);
    try {
      doThrow(IOException.class).when(messageFactoryMock).createMessage(any(), any());
      //when(messageFactoryMock.createMessage(any(), any())).thenThrow(new IOException());
    } catch (IOException e) {
      e.printStackTrace();
      fail();
    } catch (SOAPException e) {
      e.printStackTrace();
      fail();
    }
    doReturn(wsdlParserMock).when(soapClientSpy).getWsdlParser();
    doReturn(definitionsMock).when(wsdlParserMock).parse(anyString());
    doReturn(stringWriterMock).when(soapClientSpy).getStringWriter();
    doReturn(soaRequestCreatorMock).when(soapClientSpy).getSoaRequestCreator(stringWriterMock);
    doNothing().when(soaRequestCreatorMock).setFormParams(any());
    doReturn(null).when(soaRequestCreatorMock).createRequest(any(), any(), any());
    doReturn(request).when(stringWriterMock).toString();

    soapClientSpy.init("aWsldFile", "aBindingName");
    soapClientSpy.setRestTemplateClient(httpClientMock);

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      soapClientSpy.createSoapRequest("getWorkItemId", params, headers);
    });
    assertEquals(SdipErrorCode.UNKNOWN_REASON_ERROR.getSdipErrorCode(),
            Integer
                    .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                            .getSdipErrorCodes().get(0)
                            .replace("SDIP_", "")));
  }

  @Test
  void getSoapMessageFromString_message_invalid_nok() {

    String request = "invalid message";
    HashMap<String, String> params = new HashMap<>();
    params.put("xpath:/getWorkItemId/id", "1");
    params.put("xpath:/getWorkItemId/name", "test");

    HashMap<QName, String> headers = new HashMap<>();
    headers.put(new QName("sessionID"), "123456789");

    messageFactoryStaticMock.when(() -> MessageFactory.newInstance())
        .thenReturn(messageFactoryMock);
    try {
      when(messageFactoryMock.createMessage(any(), any())).thenThrow(new SOAPException());
    } catch (IOException e) {
      e.printStackTrace();
      fail();
    } catch (SOAPException e) {
      e.printStackTrace();
      fail();
    }
    doReturn(stringWriterMock).when(soapClientSpy).getStringWriter();
    doReturn(soaRequestCreatorMock).when(soapClientSpy).getSoaRequestCreator(stringWriterMock);
    doNothing().when(soaRequestCreatorMock).setFormParams(any());
    doReturn(null).when(soaRequestCreatorMock).createRequest(any(), any(), any());
    doReturn(request).when(stringWriterMock).toString();

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      soapClientSpy.createSoapRequest("getWorkItemId", params, headers);
    });
    assertEquals(SdipErrorCode.SOAP_ERROR.getSdipErrorCode(),
            Integer
                    .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                            .getSdipErrorCodes().get(0)
                            .replace("SDIP_", "")));
  }

  @Test
  void sendRequestGetResponse_ok() {

    List<Port> portList = new ArrayList<>();
    portList.add(portMock);

    List<Service> serviceList = new ArrayList<>();
    serviceList.add(serviceMock);

    List<Binding> bindingList = new ArrayList<>();
    bindingList.add(bindingMock);

    String bindingName = "abindingName";
    String pType = "123";

    HashMap<String, String> params = new HashMap<>();
    params.put("xpath:/getWorkItemId/id", "1");
    params.put("xpath:/getWorkItemId/name", "test");

    HashMap<QName, String> headers = new HashMap<>();
    headers.put(new QName("sessionID"), "123456789");

    doReturn(bindingList).when(definitionsMock).getBindings();
    doReturn(portTypeMock).when(bindingMock).getPortType();
    doReturn(pType).when(portTypeMock).getName();
    doReturn(serviceList).when(definitionsMock).getServices();
    doReturn(portList).when(serviceMock).getPorts();
    doReturn(bindingMock).when(portMock).getBinding();
    doReturn(bindingName).when(bindingMock).getName();
    doReturn(abstractAddressMock).when(portMock).getAddress();
    doReturn("lcoation").when(abstractAddressMock).getLocation();

    messageFactoryStaticMock.when(() -> MessageFactory.newInstance())
        .thenReturn(messageFactoryMock);

    doReturn(wsdlParserMock).when(soapClientSpy).getWsdlParser();
    doReturn(definitionsMock).when(wsdlParserMock).parse(anyString());

    doReturn("re").when(httpClientMock)
        .executeHttpPost(anyString(), any(), any(), any(), any(), any());

    soapClientSpy.init("aWsdl", "abindingName");
    soapClientSpy.setRestTemplateClient(httpClientMock);

    String result = soapClientSpy.sendRequestGetResponse("lcoation", "aOperation");
    assertEquals("re", result);

  }

  @Test
  void sendRequestGetResponse_badEncoding_nok() {

    List<Port> portList = new ArrayList<>();
    portList.add(portMock);

    List<Service> serviceList = new ArrayList<>();
    serviceList.add(serviceMock);

    List<Binding> bindingList = new ArrayList<>();
    bindingList.add(bindingMock);

    String bindingName = "abindingName";
    String pType = "123";

    HashMap<String, String> params = new HashMap<>();
    params.put("xpath:/getWorkItemId/id", "1");
    params.put("xpath:/getWorkItemId/name", "test");

    HashMap<QName, String> headers = new HashMap<>();
    headers.put(new QName("sessionID"), "123456789");

    doReturn(bindingList).when(definitionsMock).getBindings();
    doReturn(portTypeMock).when(bindingMock).getPortType();
    doReturn(pType).when(portTypeMock).getName();
    doReturn(serviceList).when(definitionsMock).getServices();
    doReturn(portList).when(serviceMock).getPorts();
    doReturn(bindingMock).when(portMock).getBinding();
    doReturn(bindingName).when(bindingMock).getName();
    doReturn(abstractAddressMock).when(portMock).getAddress();
    doReturn("lcoation").when(abstractAddressMock).getLocation();

    messageFactoryStaticMock.when(() -> MessageFactory.newInstance())
        .thenReturn(messageFactoryMock);

    doReturn(wsdlParserMock).when(soapClientSpy).getWsdlParser();
    doReturn(definitionsMock).when(wsdlParserMock).parse(anyString());

    try {
      doThrow(new UnsupportedEncodingException()).when(soapClientSpy).getStringEntity(anyString());
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
      fail();
    }

    soapClientSpy.init("aWsdl", "abindingName");
    soapClientSpy.setRestTemplateClient(httpClientMock);

    Throwable throwable = assertThrows(IncidentException.class, () -> {
      soapClientSpy.sendRequestGetResponse("lcoation", "aOperation");
    });
    assertEquals(SdipErrorCode.SOAP_ERROR.getSdipErrorCode(),
            Integer
                    .parseInt(((IncidentException) throwable).createHttpErrorResponse().getBody()
                            .getSdipErrorCodes().get(0)
                            .replace("SDIP_", "")));

  }

  @Test
  void getValueFromResponse_ok() {
    JavaGroovy javaGroovyMock = mock(JavaGroovy.class);
    doReturn(javaGroovyMock).when(soapClientSpy).getJavaGroovy();
    List list = new ArrayList();

    doReturn(list).when(javaGroovyMock).runGroovyShellList(any(), anyString());
    List result = soapClientSpy.getValueFromResponse("aScript", "aResponse");
    assertEquals(list, result);
    verify(soapClientSpy, times(1)).getValueFromResponse("aScript", "aResponse");
  }

}
