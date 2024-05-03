package com.scania.sdos.orchestration.model;


import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.scania.sdip.exceptions.IncidentException;
import com.scania.sdip.exceptions.SdipErrorCode;
import com.scania.sdip.exceptions.SdipErrorParameter;
import com.scania.sdos.model.ServiceArguments;
import com.scania.sdos.orchestration.Rdf4jClient;
import com.scania.sdos.orchestration.SoapClient;
import com.scania.sdos.orchestration.factory.ParameterModelFactory;
import com.scania.sdos.orchestration.factory.ScriptModelFactory;
import com.scania.sdos.orchestration.interfaces.IAuthModel;
import com.scania.sdos.orchestration.interfaces.IParameterMemory;
import com.scania.sdos.orchestration.interfaces.IParameterModel;
import com.scania.sdos.orchestration.interfaces.IScriptModel;
import com.scania.sdos.utils.SDOSConstants;
import com.scania.sdos.utils.Utility;
import java.util.HashMap;
import java.util.List;
import javax.xml.namespace.QName;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class SoapBasicAuthModel implements IAuthModel {

  private static final Logger LOGGER = LogManager.getLogger(SoapBasicAuthModel.class);


  public static final String SPARQL = SDOSConstants.PREFIX_RDF
      + SDOSConstants.PREFIX_RDFS
      + "PREFIX : <" + SDOSConstants.ORCHESTRATION_PREFIX + "> \n"
      + "SELECT ?label ?wsdlFile ?inputparameter ?inputparametertype ?soapOperation ?script ?bindingName ?scripttype\n"
      + "WHERE \n"
      + "{ \n"
      + "    BIND(<" + SDOSConstants.VARIABLE + "> AS ?subject)\n"
      + "    ?subject rdf:type :SOAPBasicAuthenticationMethod ;\n"
      + "    rdfs:label  ?label  ; \n"
      + "    :wsdlFile  ?wsdlFile  ; \n"
      + "    :soapOperation ?soapOperation ; \n"
      + "    :hasScript ?script ; \n"
      + "    :bindingName ?bindingName ;\n"
      + "    :inputParameter ?inputparameter .\n"
      + "    {?inputparameter rdf:type :Parameter  .\n"
      + "    BIND(:Parameter AS ?inputparametertype)}\n"
      + "    UNION { ?inputparameter rdf:type :StandardParameter .\n"
      + "    BIND(:StandardParameter AS ?inputparametertype)}\n"
      + "    UNION { ?inputparameter rdf:type :HTTPParameter .\n"
      + "    BIND(:HTTPParameter AS ?inputparametertype)}\n"
      + "    UNION { ?inputparameter rdf:type :BasicCredentialsParameter .\n"
      + "    BIND(:BasicCredentialsParameter AS ?inputparametertype)}\n"
      + "    {?script rdf:type :Script .}\n"
      + "    UNION { ?script rdf:type :GroovyScript .\n"
      + "    BIND(:GroovyScript AS ?scripttype)} \n"
      + "    UNION { ?script rdf:type :PythonScript .\n"
      + "    BIND(:PythonScript AS ?scripttype)} \n"
      + "}";

  public SoapBasicAuthModel() {
    this.setRdf4jClient(new Rdf4jClient());
    this.setSoapClient(new SoapClient());
  }

  private String label;
  private String subjectIri;
  private String wsdlFile;
  private String soapOperation;
  private IScriptModel script;
  private String bindingName;
  private IParameterModel inputParameter;
  private Rdf4jClient rdf4jClient;
  private SoapClient soapClient;
  private HashMap<QName, String> soapHeaders;

  public void setRdf4jClient(Rdf4jClient rdf4jClient) {
    this.rdf4jClient = rdf4jClient;
  }

  @Override
  public String getSubjectIri() {
    return subjectIri;
  }

  public void setSubjectIri(String iri) {
    subjectIri = iri;
  }

  @Override
  public void populate(String subjectIri, IParameterMemory iParameterMemory,
      ServiceArguments serviceArguments) {
    try {
      setSubjectIri(subjectIri);

      JsonArray jsonArray = rdf4jClient
          .selectSparqlOfg(getSparql(subjectIri, SPARQL), iParameterMemory.getOfgModelRepo());
      setLabel(jsonArray.get(0).getAsJsonObject().get(SDOSConstants.LABEL).getAsJsonObject()
          .get(SDOSConstants.VALUE).getAsString());
      // TODO ERROR: What if jsonArray is length == 0?? Add error-handling!!!
      setWsdlFile(jsonArray.get(0).getAsJsonObject().get(SDOSConstants.WSDLFILE).getAsJsonObject()
          .get(SDOSConstants.VALUE).getAsString());
      setSoapOperation(
          jsonArray.get(0).getAsJsonObject().get(SDOSConstants.SOAPOPERATION).getAsJsonObject()
              .get(SDOSConstants.VALUE).getAsString());

      IParameterModel iParameterModel = ParameterModelFactory.getParameter(
          jsonArray.get(0).getAsJsonObject().get(SDOSConstants.INPUT_PARAMETER_TYPE)
              .getAsJsonObject()
              .get(SDOSConstants.VALUE).getAsString());

      iParameterModel.populate(
          jsonArray.get(0).getAsJsonObject().get(SDOSConstants.INPUT_PARAMETER).getAsJsonObject()
              .get(SDOSConstants.VALUE).getAsString(), iParameterMemory, serviceArguments);
      setInputParameter(iParameterModel);

      // TODO ERROR: What if  modelfactory/populate throws?? Add error-handling!!!
      IScriptModel iScriptModel = ScriptModelFactory.getScript(
          jsonArray.get(0).getAsJsonObject().get(SDOSConstants.SCRIPT_TYPE).getAsJsonObject()
              .get(SDOSConstants.VALUE).getAsString());
      iScriptModel.populate(
          jsonArray.get(0).getAsJsonObject().get(SDOSConstants.SCRIPT).getAsJsonObject()
              .get(SDOSConstants.VALUE).getAsString(), iParameterMemory, serviceArguments);
      setScript(iScriptModel);

      setBindingName(
          jsonArray.get(0).getAsJsonObject().get(SDOSConstants.BINDING_NAME).getAsJsonObject()
              .get(SDOSConstants.VALUE).getAsString());
    } catch (IllegalStateException | IndexOutOfBoundsException exception) {
      throw new IncidentException(exception, Utility.getErrorMessage(exception,label,
          subjectIri), SdipErrorCode.INVALID_JSONARRAY_RESPONSE, LOGGER,
          exception.getMessage());
    } catch (NullPointerException exception) {
      throw new IncidentException(exception, Utility.getErrorMessage(exception,label,
          subjectIri), SdipErrorCode.FAILED_TO_PARSE_JSONARRAY, LOGGER,
          exception.getMessage());
    } catch (IncidentException exception) {
      throw exception;
    } catch (Exception exception) {
      throw new IncidentException(exception, Utility.getErrorMessage(exception,label,
          subjectIri), SdipErrorCode.UNKNOWN_PARSING_ERROR, LOGGER,
          exception.getMessage());
    }
  }

  private String getSparql(String subject, String query) {
    return query.replace(SDOSConstants.VARIABLE, subject);
  }


  public String getSoapOperation() {
    return soapOperation;
  }

  public void setSoapOperation(String soapOperation) {
    this.soapOperation = soapOperation;
  }

  public String getWsdlFile() {
    return wsdlFile;
  }

  public void setWsdlFile(String wsdlFile) {
    this.wsdlFile = wsdlFile;
  }


  public IScriptModel getScript() {
    return script;
  }

  public void setScript(IScriptModel script) {
    this.script = script;
  }

  public String getBindingName() {
    return bindingName;
  }

  public void setBindingName(String bindingName) {
    this.bindingName = bindingName;
  }


  public void run(IParameterMemory iParameterMemory) {
    try {
      soapHeaders = new HashMap<QName, String>();
      String message = null;
      soapClient.init(getWsdlFile(), getBindingName());

      HashMap<String, String> params = new HashMap<>();

      HashMap<String, List<String>> memoryValue = iParameterMemory
          .getValue(inputParameter.getSubjectIri());

      List<String> inputparams = inputParameter.getKeys();

      for (String i : inputparams) {
        if (i.equals(SDOSConstants.USERNAME)) {
          //TODO GRAPH SHOULD DECIDE userName or name or whatever username input is for current envelope
          params.put("xpath:/" + this.getSoapOperation() + "/userName", memoryValue.get(i).get(0));
        } else {
          params.put("xpath:/" + this.getSoapOperation() + "/" + i, memoryValue.get(i).get(0));
        }

      }

      String soapEnv = soapClient.createSoapRequest(this.getSoapOperation(), params, null);
      message = soapClient.sendRequestGetResponse(soapEnv, this.getSoapOperation());
      //TODO QNAME SHOUDL BE BUILT IN SCRIPT NOT IN BE
      List parsed = soapClient.getValueFromResponse(script.getScript(), message);
      Gson gson = new Gson();
      JsonArray json = gson.fromJson(gson.toJson(parsed), JsonArray.class);
      // TODO: Currently only supports one header item
      String nameSpaceUri = json.getAsJsonArray().get(0).getAsJsonObject().get("nameSpaceURI")
          .getAsString();
      String localPart = json.getAsJsonArray().get(0).getAsJsonObject().get("localPart")
          .getAsString();
      String value = json.getAsJsonArray().get(0).getAsJsonObject().get("value").getAsString();

      soapHeaders.put(new QName(nameSpaceUri, localPart), value);
    } catch (IncidentException exception) {
      throw exception;
    } catch (Exception exception) {
      throw new IncidentException(SdipErrorCode.UNKNOWN_REASON_ERROR, LOGGER,
          exception.getMessage(), SdipErrorParameter.SUPPORTMAIL);
    }
  }

  public HashMap<QName, String> getSoapHeaders() {
    return soapHeaders;
  }

  public IParameterModel getInputParameter() {
    return inputParameter;
  }

  public void setInputParameter(IParameterModel inputParameter) {
    this.inputParameter = inputParameter;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public SoapClient getSoapClient() {
    return soapClient;
  }

  public void setSoapClient(SoapClient soapClient) {
    this.soapClient = soapClient;
  }
}
