package com.scania.sdos.testModel;

import com.scania.sdip.exceptions.IncidentException;
import com.scania.sdos.model.GetAllAvailableTasksRequest;
import com.scania.sdos.model.OrchestrationParameterKeyValModel;
import com.scania.sdos.model.OrchestrationParameterModel;
import com.scania.sdos.model.OrchestrationRequestModel;
import com.scania.sdos.model.ServiceArguments;
import com.scania.sdos.services.Argument;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

public class PojoGetterSetterTest {

  @Mock
  private ServiceArguments serviceArguments;

  @Mock
  private GetAllAvailableTasksRequest getAllAvailableTasksRequest;
  @Mock
  private OrchestrationParameterKeyValModel paramKeyValModel;
  @Mock
  private OrchestrationParameterModel paramModel;
  @Mock
  private OrchestrationRequestModel requestModel;

  @Test
  public void serviceArguments_testGetterSetter_() throws MalformedURLException {
    // Arrange
    serviceArguments = new ServiceArguments();
    URL url = new URL("http://dsa");
    serviceArguments.setConfigurationUrl(url);
    serviceArguments.setEnvname("environment");
    serviceArguments.setServiceId("serviceId");
    serviceArguments.setResultDB("result-db");
    serviceArguments.setOfgDB("ofg-db");
    serviceArguments.setStardogBaseUrl(url);
    serviceArguments.setThreadPoolSize("100");
    serviceArguments.setTenantId("5463549673546");
    serviceArguments.setStardogClientScope("api://12345/user_impersonation");
    serviceArguments.setSdosClientSecret("testSecret");
    //Act&Assert
    try {
      //IArgumentParser argumentParser = new ApacheCommonsCliArgumentParser();

      //IArguments arguments = argumentParser.parseArguments(
      //      new String[]{"-b", "stardogBaseUrl", "-id", "idtest", "-r", "result", "-ofg", "ofg","-esUrl",
      //            "http://localhost:1234"});

      //serviceArguments.init(arguments);
      Assert.assertEquals("environment", serviceArguments.getEnvname());
      Assert.assertEquals("100", serviceArguments.getThreadPoolSize());
      Assert.assertEquals("serviceId", serviceArguments.getServiceId());
      Assert.assertEquals("result-db", serviceArguments.getResultDB());
      Assert.assertEquals("ofg-db", serviceArguments.getOfgDB());
      Assert.assertEquals(URL.class, serviceArguments.getStardogBaseUrl().getClass());
      Assert.assertEquals(url, serviceArguments.getConfigurationUrl());
      Assert.assertEquals("http://dsa/result-db", serviceArguments.getStardogResultEndpoint());
      Assert.assertEquals("http://dsa/result-db/query",
          serviceArguments.getStardogResultQueryEndpoint());
      Assert.assertEquals("http://dsa/result-db/update",
          serviceArguments.getStardogResultUpdateEndpoint());
      Assert.assertEquals("5463549673546", serviceArguments.getTenantId());
      Assert.assertEquals("api://12345/user_impersonation",
          serviceArguments.getStardogClientScope());
      Assert.assertEquals("testSecret", serviceArguments.getSdosClientSecret());

      Assert.assertEquals("http://dsa/ofg-db", serviceArguments.getStardogOFGEndpoint());
      Assert.assertEquals("http://dsa/ofg-db/query", serviceArguments.getStardogQueryEndpoint());
      Assert.assertEquals("https://login.microsoftonline.com/5463549673546/oauth2/v2.0/token",
          serviceArguments.getAzureTenantUrl());

    } catch (IncidentException e) {
      Assert.fail();
    }
  }

  @Test
  public void orchestrationParameterKeyValModel_testGetterSetter() {
    paramKeyValModel = new OrchestrationParameterKeyValModel();
    paramKeyValModel.setKey("key");
    paramKeyValModel.setValue("value");
    Assert.assertEquals("OrchestrationParameterKeyValModel{key='key', value='value'}",
        paramKeyValModel.toString());
    Assert.assertEquals("key", paramKeyValModel.getKey());
    Assert.assertEquals("value", paramKeyValModel.getValue());
  }

  @Test
  public void OrchestrationParameterModel_testGetterSetter() {
    paramModel = new OrchestrationParameterModel();
    List<OrchestrationParameterKeyValModel> keyValuePairs = new ArrayList<OrchestrationParameterKeyValModel>();
    keyValuePairs.add(paramKeyValModel);
    paramModel.setKeyValuePairs(keyValuePairs);
    paramModel.setLabel("test");

    Assert.assertEquals("test", paramModel.getLabel());
    Assert.assertEquals(1, paramModel.getKeyValuePairs().size());
  }

  @Test
  public void orchestrationRequestModel_testGetterSetter() {
    requestModel = new OrchestrationRequestModel();
    List<OrchestrationParameterModel> parameters = new ArrayList<OrchestrationParameterModel>();
    parameters.add(paramModel);
    requestModel.setParameters(parameters);
    requestModel.setSubjectIri("http://test");

    Assert.assertEquals("http://test", requestModel.getSubjectIri());
    Assert.assertEquals(1, requestModel.getParameters().size());
  }

  @Test
  public void getAllAvailableTasksRequest_testGetterSetter() {
    getAllAvailableTasksRequest = new GetAllAvailableTasksRequest();
    getAllAvailableTasksRequest.setSparqlEndpoint("test");
    Assert.assertEquals("GetAllAvailableTasksRequest{sparqlEndpoint='test'}",
        getAllAvailableTasksRequest.toString());
    Assert.assertEquals("test", getAllAvailableTasksRequest.getSparqlEndpoint());
  }
  
  @Test
  public void test_argument() {
    Assert.assertEquals("configurationUrl", Argument.CONFIGURATION_HOST.toString());
    Assert.assertEquals("serviceId", Argument.SERVICE_ID.toString());
    Assert.assertEquals("stardogBaseUrl", Argument.STARDOG_HOST.toString());
    Assert.assertEquals("ofgDb", Argument.OFG_DB.toString());
    Assert.assertEquals("resultDb", Argument.RESULT_DB.toString());
    Assert.assertEquals("threadPoolSize", Argument.THREADPOOLSIZE.toString());
    Assert.assertEquals("stardogClientScope", Argument.STARDOG_CLIENTSCOPE.toString());
    Assert.assertEquals("sdosClientSecret", Argument.CLIENT_SECRET.toString());
    Assert.assertEquals("tenantId", Argument.TENANT_ID.toString());

  }

  @Test
  public void RequestParamsSearch_testGetterSetter_() {
//    RequestParamsSearch.
  }

}
