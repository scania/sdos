package com.scania.sdos.orchestration;

import com.predic8.soamodel.WrongGrammarException;
import com.predic8.wsdl.Binding;
import com.predic8.wsdl.Definitions;
import com.predic8.wsdl.Port;
import com.predic8.wsdl.Service;
import com.predic8.wsdl.WSDLParser;
import com.predic8.wsdl.WSDLVersion2NotSupportedException;
import com.predic8.wstool.creator.RequestCreator;
import com.predic8.wstool.creator.SOARequestCreator;
import com.scania.sdip.exceptions.IncidentException;
import com.scania.sdip.exceptions.SdipErrorCode;
import com.scania.sdip.exceptions.SdipErrorParameter;
import groovy.xml.MarkupBuilder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import javax.xml.namespace.QName;

//import jakarta.xml.soap.
import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.MimeHeaders;
import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPMessage;
import jakarta.xml.soap.SOAPEnvelope;
import jakarta.xml.soap.SOAPHeader;
import jakarta.xml.soap.SOAPElement;
import org.apache.http.entity.StringEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class is the soapCLient
 */
public class SoapClient {

  private static final Logger LOGGER = LogManager.getLogger(SoapClient.class);

  private WSDLParser parser;
  private Definitions wsdl;
  private String portType;
  private String bindingName;
  private String urlLocation;
  private RestTemplateClient restTemplateClient;
  private String wsdllocation;


  public WSDLParser getWsdlParser() {
    return new WSDLParser();
  }

  public SoapClient() {
    //SoapClient constructor
  }

  //TODO need to find better wayinlcude these when running empty constructor
  public void init(String wsdlFile, String bindingName) {
    this.wsdllocation = wsdlFile;
    this.parser = getWsdlParser();
    this.setRestTemplateClient(new RestTemplateClient());
    this.bindingName = bindingName;
    try {
      this.wsdl = parser.parse(wsdlFile);
    } catch (WrongGrammarException e) {
      throw new IncidentException(SdipErrorCode.SOAP_RESPONSE_WSDL_NOK, LOGGER, e.getMessage(),
          this.wsdllocation);
    } catch (WSDLVersion2NotSupportedException e) {
      throw new IncidentException(SdipErrorCode.SOAP_RESPONSE_WSDL_NOK, LOGGER, e.getMessage(),
          this.wsdllocation);
    } catch (RuntimeException e) {
      throw new IncidentException(SdipErrorCode.SOAP_RESPONSE_WSDL_NOK, LOGGER, e.getMessage(),
          this.wsdllocation);
    }
    this.portType = findPortType();
    this.urlLocation = findUrlLocation();
  }

  public void setRestTemplateClient(RestTemplateClient restTemplateClient) {
    this.restTemplateClient = restTemplateClient;
  }

  private String findUrlLocation() {
    for (Service service : this.wsdl.getServices()) {
      for (Port port : service.getPorts()) {
        if (this.bindingName.equals(port.getBinding().getName())) {
          return port.getAddress().getLocation();
        }
      }
    }
    throw new IncidentException(SdipErrorCode.SOAP_RESPONSE_WSDL_NOK, LOGGER,
        "wsdl file did not contain a valid url for the given bindingName: " + this.bindingName,
        this.wsdllocation);
  }

  private String findPortType() {
    for (Binding bnd : wsdl.getBindings()) {
      if (bnd.getName().equals(bindingName)) {
        return bnd.getPortType().getName();
      }
    }
    throw new IncidentException(SdipErrorCode.SOAP_RESPONSE_WSDL_NOK, LOGGER,
        "wsdl file did not contain a port type for the given bindingName: " + this.bindingName,
        this.wsdllocation);
  }

  private SOAPMessage getSoapMessageFromString(String xml) {
    SOAPMessage message = null;
    try {
      MessageFactory factory = MessageFactory.newInstance();
      message = factory.createMessage(new MimeHeaders(),
          new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    } catch (SOAPException e) {
      throw new IncidentException(SdipErrorCode.SOAP_ERROR, LOGGER, e.getMessage(),
          SdipErrorParameter.SUPPORTMAIL);
    } catch (IOException e) {
      throw new IncidentException(SdipErrorCode.UNKNOWN_REASON_ERROR, LOGGER, e.getMessage(),
          SdipErrorParameter.SUPPORTMAIL);
    }
    return message;
  }

  private String addHeader(SOAPMessage message, HashMap<QName, String> headers) {
    String xmlString = "";
    try {
      SOAPEnvelope envelope = message.getSOAPPart().getEnvelope();
      SOAPHeader header = envelope.addHeader();
      headers.forEach((key, value) ->
      {
        SOAPElement headerElement = null;
        try {
          headerElement = header.addChildElement(key);
          headerElement.addTextNode(value);
        } catch (SOAPException e) {
          throw new IncidentException(SdipErrorCode.SOAP_ERROR, LOGGER, e.getMessage(),
              SdipErrorParameter.SUPPORTMAIL);
        }
      });
      message.saveChanges();
      ByteArrayOutputStream os = getByteArrayOutputStream();
      message.writeTo(os);
      xmlString = os.toString();
    } catch (SOAPException e) {
      throw new IncidentException(SdipErrorCode.SOAP_ERROR, LOGGER, e.getMessage(),
          SdipErrorParameter.SUPPORTMAIL);
    } catch (IOException e) {
      throw new IncidentException(SdipErrorCode.UNKNOWN_REASON_ERROR, LOGGER, e.getMessage(),
          SdipErrorParameter.SUPPORTMAIL);
    }
    return xmlString;
  }

  public ByteArrayOutputStream getByteArrayOutputStream() {
    return new ByteArrayOutputStream();
  }


  public String createSoapRequest(String soapOperation, HashMap<String, String> formParams,
      HashMap<QName, String> headers) {

    StringWriter writer = getStringWriter();

    SOARequestCreator creator = getSoaRequestCreator(writer);
    creator.setFormParams(formParams);

    creator.createRequest(this.portType, soapOperation, this.bindingName);

    String request = writer.toString();
    if (headers != null) {
      request = addHeader(getSoapMessageFromString(request), headers);
    }
    return request;
  }

  public StringWriter getStringWriter() {
    return new StringWriter();
  }

  public SOARequestCreator getSoaRequestCreator(StringWriter writer) {
    return new SOARequestCreator(this.wsdl, new RequestCreator(),
        new MarkupBuilder(writer));
  }

  public String sendRequestGetResponse(String soapEnv, String soapOperation) {

    HashMap<String, String> headers = new HashMap<>();
    headers.put("Content-Type", "text/xml; charset=utf-8");
    headers.put("SOAPAction", soapOperation);

    StringEntity body = null;
    try {
      body = getStringEntity(soapEnv);
    } catch (UnsupportedEncodingException e) {
      throw new IncidentException(SdipErrorCode.SOAP_ERROR, LOGGER, e.getMessage(),
          SdipErrorParameter.SUPPORTMAIL);
    }
    String response = restTemplateClient.executeHttpPost(urlLocation, null, headers, soapEnv, null,null);
    return response;
  }

  public StringEntity getStringEntity(String soapEnv) throws UnsupportedEncodingException {
    return new StringEntity(soapEnv);
  }

  public List getValueFromResponse(String groovyScript, String response) {

    JavaGroovy javaGroovy = getJavaGroovy();
    HashMap<String, String> bindings = new HashMap<>();
    bindings.put("InternalSOAPSessionIdEnvelope", response);
    List result = javaGroovy.runGroovyShellList(bindings, groovyScript);
    return result;
  }

  public JavaGroovy getJavaGroovy() {
    return new JavaGroovy();
  }

  public Definitions getWsdl() {
    return wsdl;
  }

  public void setWsdl(String wsdl) {
    this.wsdl = parser.parse(wsdl);
  }

  public String getBindingName() {
    return bindingName;
  }

  public void setBindingName(String bindingName) {
    this.bindingName = bindingName;
  }
}
