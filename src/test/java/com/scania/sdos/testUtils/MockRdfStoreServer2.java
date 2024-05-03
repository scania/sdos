package com.scania.sdos.testUtils;

import java.io.IOException;
import java.util.Random;

import org.junit.rules.ExternalResource;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;

public class MockRdfStoreServer2 extends ExternalResource {

  private static final int MIN_PORT_NUMBER_RANGE = 25001;
  private static final int MAX_PORT_NUMBER_RANGE = 26000;

  private final ClientAndServer mockServer;

  private int portNumber;



  public MockRdfStoreServer2() {
    portNumber = new Random().nextInt(MAX_PORT_NUMBER_RANGE - MIN_PORT_NUMBER_RANGE) + MIN_PORT_NUMBER_RANGE;

    mockServer = ClientAndServer.startClientAndServer(portNumber);

    mockServer.when(HttpRequest.request().withPath("/test/query").withMethod("GET"))
        .respond(HttpResponse.response().withStatusCode(200)
            .withHeader("Content-Type", "application/sparql-results+json")
            .withBody("{\n" +
                "  \"head\" : {\n" +
                "    \"vars\" : [\n" +
                "      \"task\",\n" +
                "      \"inputparameter\",\n" +
                "      \"inputparametertype\",\n" +
                "      \"nextaction\",\n" +
                "      \"nextactiontype\",\n" +
                "      \"context\"\n" +
                "    ]\n" +
                "  },\n" +
                "  \"results\" : {\n" +
                "    \"bindings\" : [\n" +
                "      {\n" +
                "        \"task\" : {\n" +
                "          \"type\" : \"uri\",\n" +
                "          \"value\" : \"https://kg.scania.com/it/iris_orchestration/Task1\"\n" +
                "        },\n" +
                "        \"nextaction\" : {\n" +
                "          \"type\" : \"uri\",\n" +
                "          \"value\" : \"https://kg.scania.com/it/iris_orchestration/ACTION_GET_POLARION_WORKITEM\"\n"
                +
                "        },\n" +
                "        \"inputparametertype\" : {\n" +
                "          \"type\" : \"uri\",\n" +
                "          \"value\" : \"https://kg.scania.com/it/iris_orchestration/StandardParameter\"\n"
                +
                "        },\n" +
                "        \"inputparameter\" : {\n" +
                "          \"type\" : \"uri\",\n" +
                "          \"value\" : \"https://kg.scania.com/it/iris_orchestration/PARAM_POLARION_WORKITEM\"\n"
                +
                "        },\n" +
                "        \"nextactiontype\" : {\n" +
                "          \"type\" : \"uri\",\n" +
                "          \"value\" : \"https://kg.scania.com/it/iris_orchestration/SOAPAction\"\n"
                +
                "        },\n" +
                "        \"context\" : {\n" +
                "          \"type\" : \"literal\",\n" +
                "          \"value\" : \"{\\n  \\\"@context\\\": {\\n    \\\"cccmi\\\": \\\"http://cccmi.scania.com/demo/wp5#\\\",\\n    \\\"owl\\\": \\\"http://www.w3.org/2002/07/owl#\\\",\\n    \\\"rdfs\\\": \\\"http://www.w3.org/2000/01/rdf-schema#\\\",\\n    \\\"rdf\\\": \\\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\\\",\\n    \\\"baseNameSpace\\\": \\\"cccmi\\\",\\n    \\\"cccmi:introCo\\\": {\\n      \\\"@type\\\": \\\"@id\\\"\\n    },\\n    \\\"cccmi:contains\\\": {\\n      \\\"@type\\\": \\\"@id\\\"\\n    },\\n    \\\"cccmi:hasAlternative\\\": {\\n      \\\"@type\\\": \\\"@id\\\"\\n    },\\n    \\\"cccmi:hasVariantCode\\\": {\\n      \\\"@type\\\": \\\"@id\\\"\\n    },\\n    \\\"cccmi:hasPart\\\":{\\n      \\\"@type\\\": \\\"@id\\\"\\n    },\\n    \\\"cccmi:revisionOf\\\" : {\\n      \\\"@type\\\" : \\\"@id\\\"\\n    },\\n    \\\"cccmi:usesPort\\\" : {\\n      \\\"@type\\\": \\\"@id\\\"\\n    },\\n    \\\"has_time_carrier\\\": {\\n      \\\"@id\\\": \\\"cccmi:hasTimeCarrier\\\",\\n      \\\"@type\\\": \\\"@id\\\"\\n    },\\n    \\\"has_scn\\\": {\\n      \\\"@id\\\": \\\"cccmi:hasScenario\\\",\\n      \\\"@type\\\": \\\"@id\\\"\\n    },\\n    \\\"has_uc\\\": {\\n      \\\"@id\\\": \\\"cccmi:hasUseCase\\\",\\n      \\\"@type\\\": \\\"@id\\\"\\n    },\\n    \\\"has_eco\\\": {\\n      \\\"@id\\\": \\\"cccmi:hasECO\\\",\\n      \\\"@type\\\": \\\"@id\\\"\\n    },\\n    \\\"has_intro_co\\\": {\\n      \\\"@id\\\": \\\"cccmi:hasIntroCo\\\",\\n      \\\"@type\\\": \\\"@id\\\"\\n    },\\n    \\\"uf\\\": {\\n      \\\"@id\\\": \\\"cccmi:UserFunction\\\",\\n      \\\"@type\\\": \\\"@id\\\"\\n    },\\n    \\\"DevelopmentUserfunction\\\": {\\n      \\\"@id\\\": \\\"cccmi:UserFunction\\\",\\n      \\\"@type\\\": \\\"@id\\\"\\n    },\\n    \\\"uc\\\": {\\n      \\\"@id\\\": \\\"cccmi:UseCase\\\",\\n      \\\"@type\\\": \\\"@id\\\"\\n    },\\n    \\\"eco\\\": {\\n      \\\"@id\\\": \\\"cccmi:Eco\\\",\\n      \\\"@type\\\": \\\"@id\\\"\\n    },\\n    \\\"scn\\\": {\\n      \\\"@id\\\": \\\"cccmi:Scenario\\\",\\n      \\\"@type\\\": \\\"@id\\\"\\n    },\\n    \\\"time_carrier\\\": {\\n      \\\"@id\\\": \\\"cccmi:TimeCarrier\\\",\\n      \\\"@type\\\": \\\"@id\\\"\\n    },\\n    \\\"intro_co\\\": {\\n      \\\"@id\\\": \\\"cccmi:IntroCo\\\",\\n      \\\"@type\\\": \\\"@id\\\"\\n    },\\n    \\\"WorkItem\\\": {\\n      \\\"@id\\\": \\\"cccmi:WorkItem\\\",\\n      \\\"@type\\\": \\\"@id\\\"\\n    },\\n    \\\"hasPart\\\":{\\n      \\\"@id\\\": \\\"cccmi:hasPart\\\",\\n      \\\"@type\\\": \\\"@id\\\"\\n    },\\n    \\\"revisionOf\\\" : {\\n      \\\"@id\\\" : \\\"cccmi:revisionOf\\\",\\n      \\\"@type\\\" : \\\"@id\\\"\\n    },\\n    \\\"usesPort\\\" : {\\n      \\\"@id\\\" : \\\"cccmi:usesPort\\\",\\n      \\\"@type\\\" : \\\"@id\\\"\\n    }\\n  }\\n}\"\n"
                +
                "        }\n" +
                "      }\n" +
                "    ]\n" +
                "  }\n" +
                "}"));
    mockServer.when(HttpRequest.request().withPath("/test/httpRequestPost").withMethod("POST"))
        .respond(HttpResponse.response().withStatusCode(200)
            .withHeader("Content-Type", "application/sparql-results+json")
            .withBody("{\n" +
                "  \"task\": {\n" +
                "    \"type\": \"uri\",\n" +
                "    \"value\": \"https://kg.scania.com/it/iris_orchestration/Task1\"\n" +
                "  },\n" +
                "  \"nextaction\": {\n" +
                "    \"type\": \"uri\",\n" +
                "    \"value\": \"https://kg.scania.com/it/iris_orchestration/ACTION_GET_POLARION_WORKITEM\"\n"
                +
                "  },\n" +
                "  \"inputparametertype\": {\n" +
                "    \"type\": \"uri\",\n" +
                "    \"value\": \"https://kg.scania.com/it/iris_orchestration/StandardParameter\"\n" +
                "  },\n" +
                "  \"inputparameter\": {\n" +
                "    \"type\": \"uri\",\n" +
                "    \"value\": \"https://kg.scania.com/it/iris_orchestration/PARAM_POLARION_WORKITEM\"\n"
                +
                "  },\n" +
                "  \"nextactiontype\": {\n" +
                "    \"type\": \"uri\",\n" +
                "    \"value\": \"https://kg.scania.com/it/iris_orchestration/SOAPAction\"\n" +
                "  }\n" +
                "}"));
    mockServer.when(HttpRequest.request().withPath("/test/httpRequestGet").withMethod("GET"))
        .respond(HttpResponse.response().withStatusCode(200)
            .withHeader("Content-Type", "application/sparql-results+json")
            .withBody("{\n" +
                "  \"task\": {\n" +
                "    \"type\": \"uri\",\n" +
                "    \"value\": \"https://kg.scania.com/it/iris_orchestration/Task1\"\n" +
                "  },\n" +
                "  \"nextaction\": {\n" +
                "    \"type\": \"uri\",\n" +
                "    \"value\": \"https://kg.scania.com/it/iris_orchestration/ACTION_GET_POLARION_WORKITEM\"\n"
                +
                "  },\n" +
                "  \"inputparametertype\": {\n" +
                "    \"type\": \"uri\",\n" +
                "    \"value\": \"https://kg.scania.com/it/iris_orchestration/StandardParameter\"\n" +
                "  },\n" +
                "  \"inputparameter\": {\n" +
                "    \"type\": \"uri\",\n" +
                "    \"value\": \"https://kg.scania.com/it/iris_orchestration/PARAM_POLARION_WORKITEM\"\n"
                +
                "  },\n" +
                "  \"nextactiontype\": {\n" +
                "    \"type\": \"uri\",\n" +
                "    \"value\": \"https://kg.scania.com/it/iris_orchestration/SOAPAction\"\n" +
                "  }\n" +
                "}"));
//        mockServer.when(HttpRequest.request().withPath("/test/query").withMethod("GET"))
//                .respond(HttpResponse.response().withStatusCode(200)
//                        .withHeader("Content-Type","application/sparql-results+json")
//                        .withBody("{\n" +
//                                "  \"head\" : {\n" +
//                                "    \"vars\" : [\n" +
//                                "      \"task\",\n" +
//                                "      \"inputparameter\",\n" +
//                                "      \"inputparametertype\",\n" +
//                                "      \"nextaction\",\n" +
//                                "      \"nextactiontype\",\n" +
//                                "      \"context\"\n" +
//                                "    ]\n" +
//                                "  },\n" +
//                                "  \"results\" : {\n" +
//                                "    \"bindings\" : [\n" +
//                                "      {\n" +
//                                "        \"task\" : {\n" +
//                                "          \"type\" : \"uri\",\n" +
//                                "          \"value\" : \"https://kg.scania.com/it/iris_orchestration/Task1\"\n" +
//                                "        },\n" +
//                                "        \"nextaction\" : {\n" +
//                                "          \"type\" : \"uri\",\n" +
//                                "          \"value\" : \"https://kg.scania.com/it/iris_orchestration/ACTION_GET_POLARION_WORKITEM\"\n" +
//                                "        },\n" +
//                                "        \"inputparametertype\" : {\n" +
//                                "          \"type\" : \"uri\",\n" +
//                                "          \"value\" : \"https://kg.scania.com/it/iris_orchestration/StandardParameter\"\n" +
//                                "        },\n" +
//                                "        \"inputparameter\" : {\n" +
//                                "          \"type\" : \"uri\",\n" +
//                                "          \"value\" : \"https://kg.scania.com/it/iris_orchestration/PARAM_POLARION_WORKITEM\"\n" +
//                                "        },\n" +
//                                "        \"nextactiontype\" : {\n" +
//                                "          \"type\" : \"uri\",\n" +
//                                "          \"value\" : \"https://kg.scania.com/it/iris_orchestration/SOAPAction\"\n" +
//                                "        },\n" +
//                                "        \"context\" : {\n" +
//                                "          \"type\" : \"literal\",\n" +
//                                "          \"value\" : \"{\\n  \\\"@context\\\": {\\n    \\\"cccmi\\\": \\\"http://cccmi.scania.com/demo/wp5#\\\",\\n    \\\"owl\\\": \\\"http://www.w3.org/2002/07/owl#\\\",\\n    \\\"rdfs\\\": \\\"http://www.w3.org/2000/01/rdf-schema#\\\",\\n    \\\"rdf\\\": \\\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\\\",\\n    \\\"baseNameSpace\\\": \\\"cccmi\\\",\\n    \\\"cccmi:introCo\\\": {\\n      \\\"@type\\\": \\\"@id\\\"\\n    },\\n    \\\"cccmi:contains\\\": {\\n      \\\"@type\\\": \\\"@id\\\"\\n    },\\n    \\\"cccmi:hasAlternative\\\": {\\n      \\\"@type\\\": \\\"@id\\\"\\n    },\\n    \\\"cccmi:hasVariantCode\\\": {\\n      \\\"@type\\\": \\\"@id\\\"\\n    },\\n    \\\"cccmi:hasPart\\\":{\\n      \\\"@type\\\": \\\"@id\\\"\\n    },\\n    \\\"cccmi:revisionOf\\\" : {\\n      \\\"@type\\\" : \\\"@id\\\"\\n    },\\n    \\\"cccmi:usesPort\\\" : {\\n      \\\"@type\\\": \\\"@id\\\"\\n    },\\n    \\\"has_time_carrier\\\": {\\n      \\\"@id\\\": \\\"cccmi:hasTimeCarrier\\\",\\n      \\\"@type\\\": \\\"@id\\\"\\n    },\\n    \\\"has_scn\\\": {\\n      \\\"@id\\\": \\\"cccmi:hasScenario\\\",\\n      \\\"@type\\\": \\\"@id\\\"\\n    },\\n    \\\"has_uc\\\": {\\n      \\\"@id\\\": \\\"cccmi:hasUseCase\\\",\\n      \\\"@type\\\": \\\"@id\\\"\\n    },\\n    \\\"has_eco\\\": {\\n      \\\"@id\\\": \\\"cccmi:hasECO\\\",\\n      \\\"@type\\\": \\\"@id\\\"\\n    },\\n    \\\"has_intro_co\\\": {\\n      \\\"@id\\\": \\\"cccmi:hasIntroCo\\\",\\n      \\\"@type\\\": \\\"@id\\\"\\n    },\\n    \\\"uf\\\": {\\n      \\\"@id\\\": \\\"cccmi:UserFunction\\\",\\n      \\\"@type\\\": \\\"@id\\\"\\n    },\\n    \\\"DevelopmentUserfunction\\\": {\\n      \\\"@id\\\": \\\"cccmi:UserFunction\\\",\\n      \\\"@type\\\": \\\"@id\\\"\\n    },\\n    \\\"uc\\\": {\\n      \\\"@id\\\": \\\"cccmi:UseCase\\\",\\n      \\\"@type\\\": \\\"@id\\\"\\n    },\\n    \\\"eco\\\": {\\n      \\\"@id\\\": \\\"cccmi:Eco\\\",\\n      \\\"@type\\\": \\\"@id\\\"\\n    },\\n    \\\"scn\\\": {\\n      \\\"@id\\\": \\\"cccmi:Scenario\\\",\\n      \\\"@type\\\": \\\"@id\\\"\\n    },\\n    \\\"time_carrier\\\": {\\n      \\\"@id\\\": \\\"cccmi:TimeCarrier\\\",\\n      \\\"@type\\\": \\\"@id\\\"\\n    },\\n    \\\"intro_co\\\": {\\n      \\\"@id\\\": \\\"cccmi:IntroCo\\\",\\n      \\\"@type\\\": \\\"@id\\\"\\n    },\\n    \\\"WorkItem\\\": {\\n      \\\"@id\\\": \\\"cccmi:WorkItem\\\",\\n      \\\"@type\\\": \\\"@id\\\"\\n    },\\n    \\\"hasPart\\\":{\\n      \\\"@id\\\": \\\"cccmi:hasPart\\\",\\n      \\\"@type\\\": \\\"@id\\\"\\n    },\\n    \\\"revisionOf\\\" : {\\n      \\\"@id\\\" : \\\"cccmi:revisionOf\\\",\\n      \\\"@type\\\" : \\\"@id\\\"\\n    },\\n    \\\"usesPort\\\" : {\\n      \\\"@id\\\" : \\\"cccmi:usesPort\\\",\\n      \\\"@type\\\" : \\\"@id\\\"\\n    }\\n  }\\n}\"\n" +
//                                "        }\n" +
//                                "      }\n" +
//                                "    ]\n" +
//                                "  }\n" +
//                                "}"));
  }

  public MockRdfStoreServer2(ClientAndServer clientAndServer) {
    mockServer = clientAndServer;
  }

  public int getPortNumber(){
    return portNumber;
  }

  @Override
  protected void before() throws IOException {
    while (!mockServer.hasStarted()) {
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  @Override
  protected void after() {
    mockServer.stop();
    while (!mockServer.hasStopped()) {
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }
}
