package com.scania.sdos.orchestration.model;

import com.scania.sdip.exceptions.IncidentException;
import com.scania.sdip.exceptions.SdipErrorCode;
import com.scania.sdip.exceptions.SdipErrorParameter;
import com.scania.sdos.orchestration.interfaces.IParameterMemory;
import com.scania.sdos.orchestration.interfaces.IParameterModel;
import com.scania.sdos.utils.SDOSConstants;
import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpHeaders;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class RestTemplateAuthModel  {

  private static final Logger LOGGER = LogManager.getLogger(RestTemplateAuthModel.class);

  public RestTemplateAuthModel() {
    //default constructor
  }
  private IParameterModel inputParameter;


  public IParameterModel getInputParameter() {
    return inputParameter;
  }

  public void setInputParameter(IParameterModel inputParameter) {
    this.inputParameter = inputParameter;
  }

  public HashMap addCredentials(HashMap requestHeaders, IParameterMemory iParameterMemory) {

    try {
      HashMap<String, List<String>> memoryValue = iParameterMemory
              .getValue(inputParameter.getSubjectIri());

      String base64Creds = new String(Base64.encodeBase64((memoryValue.get(SDOSConstants.USERNAME).get(0)+":" +
               memoryValue.get(SDOSConstants.PASSWORD).get(0)).getBytes()));

      requestHeaders.put("Authorization", "Basic " + base64Creds);

      return requestHeaders;

    } catch (Exception exception) {
      throw new IncidentException(SdipErrorCode.UNKNOWN_REASON_ERROR, LOGGER,
              exception.getMessage(), SdipErrorParameter.SUPPORTMAIL);
    }
  }

  public HttpHeaders addAuthHeaders(HashMap<String, String> headers) {

    HttpHeaders httpheaders = new HttpHeaders();
    for (Map.Entry<String, String> entry : headers.entrySet())
      httpheaders.add(entry.getKey(),entry.getValue());

    return httpheaders;
  }

  public String generateUrl(Map<String, String> queryParam) {
    String uri = "";
    if (!queryParam.isEmpty()) {
      for (Map.Entry<String, String> params : queryParam.entrySet()) {
        if (uri.isEmpty()) {
          uri = params.getKey() + "=" + params.getValue();
        } else {
          uri = uri + "&" + params.getKey() + "=" + params.getValue();
        }
      }
    }
    return uri;
  }

}
