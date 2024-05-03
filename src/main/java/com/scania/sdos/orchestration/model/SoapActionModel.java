package com.scania.sdos.orchestration.model;

import com.google.gson.JsonArray;
import com.scania.sdip.exceptions.IncidentException;
import com.scania.sdip.exceptions.SdipErrorCode;
import com.scania.sdip.exceptions.SdipErrorParameter;
import com.scania.sdos.model.ServiceArguments;
import com.scania.sdos.orchestration.Rdf4jClient;
import com.scania.sdos.orchestration.SoapClient;
import com.scania.sdos.orchestration.interfaces.IActionModel;
import com.scania.sdos.orchestration.interfaces.IParameterMemory;
import com.scania.sdos.orchestration.interfaces.IParameterModel;
import com.scania.sdos.utils.SDOSConstants;
import com.scania.sdos.utils.Utility;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SoapActionModel extends ActionModel implements IActionModel {

  private static final Logger LOGGER = LogManager.getLogger(SoapActionModel.class);

  public static final String SPARQL =
      SDOSConstants.PREFIX_RDF
          + SDOSConstants.PREFIX_RDFS
          + "PREFIX : <" + SDOSConstants.ORCHESTRATION_PREFIX + ">\n"
          + "SELECT ?soapOperation ?hasConnector ?inputparameter ?inputparametertype ?outputparameter ?outputparametertype ?nextaction ?nextactiontype ?label\n"
          + "WHERE \n"
          + "{ \n"
          + "BIND(<" + SDOSConstants.VARIABLE + "> AS ?subject)"
          + "?subject rdf:type :SOAPAction ;\n"
          + "     :soapOperation  ?soapOperation ;\n"
          + "     :inputParameter ?inputparameter ;\n"
          + "     :outputParameter ?outputparameter ;\n"
          + "     :hasConnector ?hasConnector;\n"
          + "      rdfs:label ?label ."
          + "    OPTIONAL{?subject :hasNextAction ?" + SDOSConstants.NEXT_ACTION + " .\n"
          + "        {?" + SDOSConstants.NEXT_ACTION + " rdf:type :Action .} \n"
          + SDOSConstants.UNION + SDOSConstants.NEXT_ACTION + " rdf:type :HTTPAction .\n"
          + "            BIND(:HTTPAction AS ?" + SDOSConstants.NEXT_ACTION_TYPE + SDOSConstants.CLOSING
          + SDOSConstants.UNION + SDOSConstants.NEXT_ACTION + " rdf:type :SOAPAction .\n"
          + "            BIND(:SOAPAction AS ?" + SDOSConstants.NEXT_ACTION_TYPE + SDOSConstants.CLOSING
          + SDOSConstants.UNION + SDOSConstants.NEXT_ACTION + " rdf:type :ScriptAction .\n"
          + "            BIND(:ScriptAction AS ?" + SDOSConstants.NEXT_ACTION_TYPE + SDOSConstants.CLOSING
          + SDOSConstants.UNION + SDOSConstants.NEXT_ACTION + " rdf:type :ResultAction . \n"
          + "            BIND(:ResultAction AS ?" + SDOSConstants.NEXT_ACTION_TYPE + ")} \n"
          + "        UNION { ?" + SDOSConstants.NEXT_ACTION + " rdf:type :VirtualGraphAction .\n"
          + "            BIND(:VirtualGraphAction AS ?" + SDOSConstants.NEXT_ACTION_TYPE + ")}\n"
          + "        UNION { ?" + SDOSConstants.NEXT_ACTION + " rdf:type :SparqlConvertAction . \n"
          + "            BIND(:SparqlConvertAction AS ?" + SDOSConstants.NEXT_ACTION_TYPE + ")} \n"
          + "    }\n"
          + "{?inputparameter rdf:type :Parameter .\n"
          + " BIND(:Parameter AS ?inputparametertype)}\n"
          + " UNION { ?inputparameter rdf:type :StandardParameter .\n"
          + "   BIND(:StandardParameter AS ?inputparametertype)}\n"
          + " UNION { ?inputparameter rdf:type :HTTPParameter .\n"
          + "   BIND(:HTTPParameter AS ?inputparametertype)}\n"
          + " {?outputparameter rdf:type :Parameter .\n"
          + " BIND(:Parameter AS ?outputparametertype)}\n"
          + " UNION { ?outputparameter rdf:type :StandardParameter .\n"
          + "   BIND(:StandardParameter AS ?outputparametertype)}\n"
          + " UNION { ?outputparameter rdf:type :HTTPParameter .\n"
          + "   BIND(:HTTPParameter AS ?outputparametertype)}\n"
          + "}\n";
  private String soapOperation;
  private SoapConnectorModel soapConnectorModel;
  private Rdf4jClient rdf4jClient;
  private SoapClient soapClient;

  public SoapActionModel() {
    this.setRdf4jClient(new Rdf4jClient());
    this.setSoapClient(new SoapClient());
  }

  public void setRdf4jClient(Rdf4jClient rdf4jClient) {
    this.rdf4jClient = rdf4jClient;
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

      setSoapOperation(
          jsonArray.get(0).getAsJsonObject().get(SDOSConstants.SOAPOPERATION).getAsJsonObject()
              .get(SDOSConstants.VALUE)
              .getAsString());
      setSoapConnectorModel(
          (SoapConnectorModel) createPopulateConnector(iParameterMemory, serviceArguments,
              jsonArray,
              SDOSConstants.SOAP_CONNECTOR));
      populateInputParameter(iParameterMemory, serviceArguments, jsonArray);
      populateOutputParameter(iParameterMemory, serviceArguments, jsonArray);
      populateNextAction(iParameterMemory, serviceArguments, jsonArray);

    } catch (IllegalStateException exception) {
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
          subjectIri), SdipErrorCode.UNKNOWN_REASON_ERROR, LOGGER,
          exception.getMessage(), SdipErrorParameter.SUPPORTMAIL);
    }
  }


  @Override
  public void run(IParameterMemory iParameterMemory, JsonLdContextModel context,
      ServiceArguments serviceArguments) {
    try {

      SoapClient trackerSoapClient = getSoapClient();
      soapClient
          .init(this.soapConnectorModel.getWsdlFile(), this.soapConnectorModel.getBindingName()
          );

      for (IParameterModel inputParameterModel : inputParameter) {
        HashMap<String, List<String>> memoryValue = iParameterMemory
                .getValue(inputParameterModel.getSubjectIri());

        if (memoryValue != null && !memoryValue.isEmpty()) {
          Integer iterations = Utility.getActionIteration(memoryValue, inputParameterModel);
          mapActionIteration(iterations,inputParameterModel,memoryValue,iParameterMemory, trackerSoapClient);
        }
      }
    } catch (IncidentException exception) {
      throw exception;
    } catch (Exception exception) {
      throw new IncidentException(SdipErrorCode.UNKNOWN_REASON_ERROR, LOGGER,
              exception.getMessage(), SdipErrorParameter.SUPPORTMAIL);
    }

  }

  private void mapActionIteration(Integer iterations,IParameterModel inputParameterModel,HashMap<String, List<String>> memoryValue,IParameterMemory iParameterMemory, SoapClient trackerSoapClient) {
    for (int i = 0; i < iterations; i++) {
      String message = null;
      HashMap<String, String> trackerParams = new HashMap<>();
      for (String key : inputParameterModel.getKeys()) {
        trackerParams.put("xpath:/" + this.soapOperation + "/" + key,
                memoryValue.get(key).get(i));
      }
      if (soapConnectorModel.getHasAuthenticationMethod() != null) {
        SoapBasicAuthModel authModel = (SoapBasicAuthModel) soapConnectorModel
                .getHasAuthenticationMethod();
        authModel.run(iParameterMemory);

        String trackerSoapEnv = trackerSoapClient
                .createSoapRequest(this.soapOperation, trackerParams, authModel.getSoapHeaders());
        message = trackerSoapClient.sendRequestGetResponse(trackerSoapEnv,
                this.soapOperation);
      } else {
        String trackerSoapEnv = trackerSoapClient
                .createSoapRequest(this.soapOperation, trackerParams, null);
        message = trackerSoapClient.sendRequestGetResponse(trackerSoapEnv,
                this.soapOperation);
      }

      HashMap<String, List<String>> outputMemoryValue = new HashMap<>();
      outputMemoryValue
              .put(outputParameter.get(0).getKeys().get(0), Collections.singletonList(message));
      iParameterMemory.putParameter(outputParameter.get(0).getSubjectIri(),
              outputMemoryValue);
    }
  }

    public String getSoapOperation() {
    return soapOperation;
  }

  public void setSoapOperation(String soapOperation) {
    this.soapOperation = soapOperation;
  }

  public SoapConnectorModel getSoapConnectorModel() {
    return soapConnectorModel;
  }

  public void setSoapConnectorModel(SoapConnectorModel soapConnectorModel) {
    this.soapConnectorModel = soapConnectorModel;
  }

  public SoapClient getSoapClient() {
    return soapClient;
  }

  public void setSoapClient(SoapClient soapClient) {
    this.soapClient = soapClient;
  }
}
