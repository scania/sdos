package com.scania.sdos.testUtils;

import com.scania.sdos.utils.SDOSConstants;

/**
 * This is class contains constants for Unit Test Cases
 */
public class UnitTestConstants {

  public static final String DUMMY_JWT_TOKEN = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsIng1dCI6Ii1LSTNROW5OUjdiUm9meG1lWm9YcWJIWkdldyIsImtpZCI6Ii1LSTNROW5OUjdiUm9meG1lWm9YcWJIWkdldyJ9.eyJhdWQiOiJhcGk6Ly8zYTFjMDNkNy01ZTI3LTQzYmQtYmM5MC0zYmZmMmY5ZTM4ZTAiLCJpc3MiOiJodHRwczovL3N0cy53aW5kb3dzLm5ldC8zYmMwNjJlNC1hYzlkLTRjMTctYjRkZC0zYWFkNjM3ZmYxYWMvIiwiaWF0IjoxNjk2ODU1MTUxLCJuYmYiOjE2OTY4NTUxNTEsImV4cCI6MTY5Njg2MDc2OSwiYWNyIjoiMSIsImFpbyI6IkFZUUFlLzhVQUFBQTRPVytsbEdVMExHNWEvRSt6bkdLNG45Y1g4SGxFTW5GTExlMjczQXYzdUlLdFpXdy9tOVVURXZ4ZUQzQ2NwWGc3azJQbExabkpaMzZYQk9VeTM3ZGZ0WEV0a0F2enFNaW5rMVJVYUtKRXlYQjZ3NmV1dnI0TFRrVkxuNDg1allEK0QyUDJ3RUIxV2lFRlJ6dlEyTTR2RWZQLy9zakVZVU5tak5CVExEbHVoWT0iLCJhbXIiOlsicHdkIiwibWZhIl0sImFwcGlkIjoiM2ExYzAzZDctNWUyNy00M2JkLWJjOTAtM2JmZjJmOWUzOGUwIiwiYXBwaWRhY3IiOiIxIiwiZmFtaWx5X25hbWUiOiJTaW5naCIsImdpdmVuX25hbWUiOiJSYXZpR3lhbiIsImlwYWRkciI6IjEzOS4xMjIuMTkxLjIyOSIsIm5hbWUiOiJTaW5naCBSYXZpR3lhbiIsIm9pZCI6ImFmZDQxNzJlLWZjMjYtNDliMC05NWIzLTRmNmVjZGU3MGFjMSIsIm9ucHJlbV9zaWQiOiJTLTEtNS0yMS0xODQ0MjM3NjE1LTYwMjE2MjM1OC03MjUzNDU1NDMtOTg4NDgwIiwicmgiOiIwLkFSRUE1R0xBTzUyc0YweTAzVHF0WTNfeHJOY0RIRG9uWHIxRHZKQTdfeS1lT09BUkFOUS4iLCJyb2xlcyI6WyJhZG1pbiJdLCJzY3AiOiJ1c2VyX2ltcGVyc29uYXRpb24iLCJzdWIiOiJNYThBUHB2Wm1XMzNPdHdNZVk0bUVwQThfQU9XV0U2eVRHWVhaRmRKVE5RIiwidGlkIjoiM2JjMDYyZTQtYWM5ZC00YzE3LWI0ZGQtM2FhZDYzN2ZmMWFjIiwidW5pcXVlX25hbWUiOiJyYXZpZ3lhbi5zaW5naEBzY2FuaWEuY29tIiwidXBuIjoicmF2aWd5YW4uc2luZ2hAc2NhbmlhLmNvbSIsInV0aSI6IjZ6VlFZRWVjNmtTUmx6SjlrOG93QUEiLCJ2ZXIiOiIxLjAifQ.AeSPgAPujprTYTezUmciTYJYukzKw4yOG0K-A_jO7D3aL-1KwqaJTWe_fVDaJves3fENJVwDAenJCIMIQXesIsusoy7V3g4l4KelihG6RCMSzf8BRkliSzCmfVdb5SKqUT_gWwQUrJz9c6acwNgEDpc12_8iR5ihxLYMqJpwSB1BEQazGSlFxshXoDT5GyksHiSyb4KRqvBUPnT9D34IqArdhX--TJfkHjrOfh6AX8CsWbBcKs4VlU1sAuETduVeD4PrmDZXi8t0TBNR1aYztrgvpYI6B9QP9HJ4qGxAJs4yDLPs1DvqHl5skn8Pm7LlQB870XQkS8F-iaVpCauLHA";
  public static final String DUMMY_JWT_TOKEN_ERROR = "Bearer eyJ0eX123AiOiJKV1QiLCJhbGciOiJSUzI1NiIsIng1dCI6Ii1LSTNROW5OUjdiUm9meG1lWm9YcWJIWkdldyIsImtpZCI6Ii1LSTNROW5OUjdiUm9meG1lWm9YcWJIWkdldyJ9.eyJhdWQiOiJhcGk6Ly8zYTFjMDNkNy01ZTI3LTQzYmQtYmM5MC0zYmZmMmY5ZTM4ZTAiLCJpc3MiOiJodHRwczovL3N0cy53aW5kb3dzLm5ldC8zYmMwNjJlNC1hYzlkLTRjMTctIjoxNjk2ODU1MTUxLCJuYmYiOjE2OTY4NTUxNTEsImV4cCI6MTY5Njg2MDc2OSwiYWNyIjoiMSIsImFpbyI6IkFZUUFlLzhVQUFBQTRPVytsbEdVMExHNWEvRSt6bkdLNG45Y1g4SGxFTW5GTExlMjczQXYzdUlLdFpXdy9tOVVURXZ4ZUQzQ2NwWGc3azJQbExabkpaMzZYQk9VeTM3ZGZ0WEV0a0F2enFNaW5rMVJVYUtKRXlYQjZ3NmV1dnI0TFRrVkxuNDg1allEK0QyUDJ3RUIxV2lFRlJ6dlEyTTR2RWZQLy9zakVZVU5tak5CVExEbHVoWT0iLCJhbXIiOlsicHdkIiwibWZhIl0sImFwcGlkIjoiM2ExYzAzZDctNWUyNy00M2JkLWJjOTAtM2JmZjJmOWUzOGUwIiwiYXBwaWRhY3IiOiIxIiwiZmFtaWx5X25hbWUiOiJTaW5naCIsImdpdmVuX25hbWUiOiJSYXZpR3lhbiIsImlwYWRkciI6XZpR3lhbiIsIm9pZCI6ImFmZDQxNzJlLWZjMjYtNDliMC05NWIzLTRmNmVjZGU3MGFjMSIsIm9ucHJlbV9zaWQiOiJTLTEtNS0yMS0xODQ0MjM3NjE1LTYwMjE2MjM1OC03MjUzNDU1NDMtOTg4NDgwIiwicmgiOiIwLkFSRUE1R0xBTzUyc0YweTAzVHF0WTNfeHJOY0RIRG9uWHIxRHZKQTdfeS1lT09BUkFOUS4iLCJyb2xlcyI6WyJhZG1pbiJdLCJzY3AiOiJ1c2VyX2ltcGVyc29uYXRpb24iLCJzdWIiOiJNYThBUHB2Wm1XMzNPdHdNZVk0bUVwQThfQU9XV0U2eVRHWVhaRmRKVE5RIiwidGlkIjoiM2JjMDYyZTQtYWM5ZC00YzE3LWI0ZGQtM2FhZDYzN2ZmMWFjIiwidW5pcXVlX25hbWUiOiJyYXZpZ3lhbi5zaW5naEBzY2FuaWEuY29tIiwidXBuIjoicmF2aWd5YW4uc2luZ2hAc2NhbmlhLmNvbSIsInV0aSI6IjZ6VlFZRWVjNmtTUmx6SjlrOG93QUEiLCJ2ZXIiOiIxLjAifQ.AeSPgAPujprTYTezUmciTYJYukzKw4yOG0K-A_jO7D3aL-1KwqaJTWe_fVDaJves3fENJVwDAenJCIMIQXesIsusoy7V3g4l4KelihG6RCMSzf8BRkliSzCmfVdb5SKqUT_gWwQUrJz9c6acwNgEDpc12_8iR5ihxLYMqJpwSB1BEQazGSlFxshXoDT5GyksHiSyb4KRqvBUPnT9D34IqArdhX--TJfkHjrOfh6AX8CsWbBcKs4VlU1sAuETduVeD4PrmDZXi8t0TBNR1aYztrgvpYI6B9QP9HJ4qGxAJs4y";
  public static final String DUMMY_OBO_TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9" +
          ".eyJpc3MiOiJGb28iLCJpYXQiOjE2ODU3MDg2NzIsImV4cCI6MTcxNzI0NDY3MiwiYXVkIjoid3d3LmJhci5jb20iLCJzdWIiOiJhQGIuY29tIn0" +
          ".vGgMppLAN6Tdb_t69gsYjrlPdHbCvP_W_ysBF5Gibuc";
  public static final String VARIABLE = "VARIABLE";
  public static final String TEST_BASEURI = "https://atest#";
  public static final String TEST_SYSTEM_A = "https://atest#SYSTEMA";
  public static final String TEST_INPUTPARAMETER_A = "https://atest#SYSTEMA";
  public static final String TEST_INPUTPARAMETERTYPE_A = "https://ahttptest#SYSTEMA";
  public static final String CONFIG_REST_ENDPOINT = "/configuration";
  public static final String API_HEADER_JWT = "X-JWT-Assertion";
  public static final String ENDPOINT = "/atest";
  public static final String PYTHONSCRIPTMODELTESTDATA = "PythonScriptModelTestData.json";
  public static final String TASKMODELTESTDATA = "TaskModelTestData.json";
  public static final String TASKMODELTESTDATA_NO_CONTEXT = "TaskModelTestData_noContext.json";

  public static final String TASKMODEL_TEST_SUBJECT_IRI = "https://kg.scania.com/it/iris_orchestration/Task1";
  public static final String UPLOADACTIONMODELTESTDATA = "UploadActionModelTestData.json";
  public static final String UPLOADACTIONMODELTESTDATA_VIRTUALGRAPHACTION = "UploadActionModelTestData_VirtualGraphAction.json";
  public static final String UPLOADACTIONMODELTESTDATA_MISSINGNEXTACTIONTYPE = "UploadActionModelTestData_MissingNextActionType.json";
  public static final String HELPMODELTESTDATA = "HelpModelTestData.json";
  public static final String SOAPACTIONMODELTESTDATA = "SoapActionModelTestData.json";
  public static final String SOAPACTIONMODELTESTDATA_VIRTUALGRAPHACTION = "SoapActionModelTestData_VirtualGraphAction.json";
  public static final String SCRIPTACTIONMODELTESTDATA = "ScriptActionModelTestData.json";
  public static final String SCRIPTACTIONMODELTESTDATA_W_CONTEXT = "ScriptActionModelTestData_wContext.json";
  public static final String SCRIPTACTIONMODELTESTDATA_VIRTUALGRAPHACTION = "ScriptActionModelTestData_VirtualGraphAction.json";
  public static final String PARAMETERACTIONMODELTESTDATA = "ParameterModelTestData.json";
  public static final String PARAMETERMODELINPUTHELP = "ParameterModelInputHelp.json";
  public static final String HTTPPARAMETERMODELINPUTHELP = "HttpParameterModelInputHelp.json";
  public static final String PARAMETERACTIONMODELTESTDATA2 = "ParameterModelTestData2.json";
  public static final String PARAMETERACTIONMODELTESTDATA3 = "ParameterModelTestData3.json";
  public static final String SOAPCONNECTORMODELTESTDATA = "SoapConnectorModelTestData.json";
  public static final String RDF4JCLIENTRESPONSE = "Rdf4jClientResponse.json";
  public static final String HTTPCLIENTPOSTRESPONSE = "HttpClientPostResponse.json";
  public static final String GROOVYSCRIPTOUTPUT = "GroovyScriptOutput.json";
  public static final String AUTHMODELTESTDATA = "AuthModelTestData.json";
  public static final String GROOVYSCRIPTMODELTESTDATA = "GroovyScriptModelTestData.json";
  public static final String GROOVYSCRIPTMODELTESTDATA2 = "GroovyScriptModelTestData2.json";
  public static final String HTTPACTIONMODELTESTDATA = "HttpActionModelTestData.json";

  public static final String HTTPACTIONMODELTESTDATA2 = "HttpActionModelTestData2.json";

  public static final String HTTPACTIONMODELTESTDATA3 = "HttpActionModelTestData3.json";
  public static final String BASICHTTPACTIONMODELTESTDATA = "BasicHttpActionModelTestData.json";
  public static final String TOKENHTTPACTIONMODELTESTDATA = "TokenHttpActionModelTestData.json";
  public static final String HTTPCONNECTORMODELTESTDATA = "HttpConnectorModelTestData.json";
  public static final String HTTPCONNECTORMODELTESTDATA_AUTH = "HttpConnectorModelAuthTestData.json";
  public static final String HTTPPARAMETERMODELTESTDATA = "HttpParameterModelTestData.json";
  public static final String HTTPPARAMETERMODELTESTDATA2 = "HttpParameterModelTestData2.json";
  public static final String BASICCREDPARAMETERMODELGETDATA = "BasicCredentialsParameterGetData.json";
  public static final String HELPMODELTESTOBJECT = "HelpModelObject.json";
  public static final String VIRTUALGRAPHACTIONMODELTESTDATA = "VirtualGraphActionModelTestData.json";
  public static final String VIRTUALGRAPHACTIONMODELTESTDATA_VIRTUALGRAPHACTION = "VirtualGraphActionModelTestData_VirtualGraphAction.json";
  public static final String METADATAMODELTESTDATA = "MetaDataModelTestData.json";
  public static final String METADATAMODELEXCEPTIONDATA = "MetaDataModelTestExceptionData.json";
  public static final String SPARQLACTIONMODELTESTDATA = "SparqlConvertActionModelTestData.json";
  public static final String SUBJECTURI = "anIri";
  public static final String ALABEL = "aLabel";
  public static final String PARAM_POLARION_RAW = "https://kg.scania.com/it/iris_orchestration/PARAM_POLARION_RAW";
  public static final String PARAM_SCRIPT_OUTPUT = "https://kg.scania.com/it/iris_orchestration/PARAM_SCRIPT1_OUTPUT";
  public static final String PARAM_POLARION_WORKITEM = "https://kg.scania.com/it/iris_orchestration/PARAM_POLARION_WORKITEM";
  public static final String POLARION_RAWDATA = "polarion_rawdata";
  public static final String TOKENCREDENTIALsPARAMETERGETDATA = "TokenCredentialsParameterGetData.json";
  public static final String TEST = "http://test";
  public static final String USERNAME = "username";
  public static final String PASSWORD = "password";
  public static final String TOKEN = "token";
  public static final String SPARQLQUERY = "select * where {?s ?p ?o. }";
  public static final String QUERY = "query";
  public static final String SDIP = "SDIP_";
  public static final String EXECUTION_ID = "http://test123";

  public static final String VALUE = "{\"@context\":{\"cccmi\":\"http://cccmi.scania.com/demo/wp5#\",\"owl\":\"http://www.w3.org/2002/07/owl#\",\"rdfs\":\"http://www.w3.org/2000/01/rdf-schema#\",\"rdf\":\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\",\"baseNameSpace\":\"cccmi\",\"cccmi:introCo\":{\"@type\":\"@id\"},\"cccmi:contains\":{\"@type\":\"@id\"},\"cccmi:hasAlternative\":{\"@type\":\"@id\"},\"cccmi:hasVariantCode\":{\"@type\":\"@id\"},\"cccmi:hasPart\":{\"@type\":\"@id\"},\"cccmi:revisionOf\":{\"@type\":\"@id\"},\"cccmi:usesPort\":{\"@type\":\"@id\"},\"has_time_carrier\":{\"@id\":\"cccmi:hasTimeCarrier\",\"@type\":\"@id\"},\"has_scn\":{\"@id\":\"cccmi:hasScenario\",\"@type\":\"@id\"},\"has_uc\":{\"@id\":\"cccmi:hasUseCase\",\"@type\":\"@id\"},\"has_eco\":{\"@id\":\"cccmi:hasECO\",\"@type\":\"@id\"},\"has_intro_co\":{\"@id\":\"cccmi:hasIntroCo\",\"@type\":\"@id\"},\"uf\":{\"@id\":\"cccmi:UserFunction\",\"@type\":\"@id\"},\"DevelopmentUserfunction\":{\"@id\":\"cccmi:UserFunction\",\"@type\":\"@id\"},\"uc\":{\"@id\":\"cccmi:UseCase\",\"@type\":\"@id\"},\"eco\":{\"@id\":\"cccmi:Eco\",\"@type\":\"@id\"},\"scn\":{\"@id\":\"cccmi:Scenario\",\"@type\":\"@id\"},\"time_carrier\":{\"@id\":\"cccmi:TimeCarrier\",\"@type\":\"@id\"},\"intro_co\":{\"@id\":\"cccmi:IntroCo\",\"@type\":\"@id\"},\"WorkItem\":{\"@id\":\"cccmi:WorkItem\",\"@type\":\"@id\"},\"hasPart\":{\"@id\":\"cccmi:hasPart\",\"@type\":\"@id\"},\"revisionOf\":{\"@id\":\"cccmi:revisionOf\",\"@type\":\"@id\"},\"usesPort\":{\"@id\":\"cccmi:usesPort\",\"@type\":\"@id\"}},\"@graph\":[{\"@id\":\"cccmi:WorkItem-Test11-2\",\"@type\":\"WorkItem\",\"cccmi:unresolvable\":\"false\",\"cccmi:type\":\"WorkItem\",\"cccmi:uri\":\"/default/Test11${WorkItem}Test11-2\",\"cccmi:contains\":[\"cccmi:eco-283793\",\"cccmi:uf-18\",\"cccmi:duf-18\"]}]}";
  public static final String HEADER = "\"{\n\"Content-Type\":\"application/ld+json\",\n\"Accept\": \"*/*\",\n\"username\":\"admin\",\n\"password\":\"admin\"\n}\"";


  public static final String HeaderTest = "\"{\\" + "n"
          + "\\\"Content-Type\":\"application/ld+json\",\n\"Accept\": \"*/*\",\n\"username\":\"admin\",\n\"password\":\"admin\"\n}\"";
  public static final String INSERT_SPARQL = "PREFIX dc: <http://purl.org/dc/elements/1.1/>\n" +
      "INSERT DATA\n" +
      "{ \n" +
      "  <http://example/book1> dc:title \"A new book\" ;\n" +
      "                         dc:creator \"A.N.Other\" .\n" +
      "}";
  public static final String DELETE_SPARQL = "PREFIX dc: <http://purl.org/dc/elements/1.1/>\n" +
      "\n" +
      "DELETE DATA\n" +
      "{\n" +
      "  <http://example/book2> dc:title \"David Copperfield\" ;\n" +
      "                         dc:creator \"Edmund Wells\" .\n" +
      "}";
  public static final String DELETE_WHERE_SPARQL = "PREFIX foaf:  <http://xmlns.com/foaf/0.1/>\n" +
      "\n" +
      "DELETE WHERE { ?person foaf:givenName 'Fred';\n" +
      "                       ?property      ?value }";
  public static final String DELETE_WHERE_SPARQL2 = "PREFIX foaf:  <http://xmlns.com/foaf/0.1/>\n" +
      "\n" +
      "DELETE WHERE {\n" +
      "  GRAPH <http://example.com/names> {\n" +
      "    ?person foaf:givenName 'Fred' ;\n" +
      "            ?property1 ?value1\n" +
      "  }\n" +
      "  GRAPH <http://example.com/addresses> {\n" +
      "    ?person ?property2 ?value2\n" +
      "  }\n" +
      "}";
  public static final String INSERT_DELETE_SPARQL =
      "PREFIX dc:  <http://purl.org/dc/elements/1.1/>\n" +
          "PREFIX dcmitype: <http://purl.org/dc/dcmitype/>\n" +
          "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
          "\n" +
          "INSERT\n" +
          "  { GRAPH <http://example/bookStore2> { ?book ?p ?v } }\n" +
          "WHERE\n" +
          "  { GRAPH  <http://example/bookStore>\n" +
          "     { ?book dc:date ?date . \n" +
          "       FILTER ( ?date < \"2000-01-01T00:00:00-02:00\"^^xsd:dateTime )\n" +
          "       ?book ?p ?v\n" +
          "     }\n" +
          "  } ;\n" +
          "\n" +
          "WITH <http://example/bookStore>\n" +
          "DELETE\n" +
          " { ?book ?p ?v }\n" +
          "WHERE\n" +
          " { ?book dc:date ?date ;\n" +
          "         dc:type dcmitype:PhysicalObject .\n" +
          "   FILTER ( ?date < \"2000-01-01T00:00:00-02:00\"^^xsd:dateTime ) \n" +
          "   ?book ?p ?v\n" +
          " } ";
  public static final String DELETE_INFO_SPARQL =
      "PREFIX dc:  <http://purl.org/dc/elements/1.1/>\n" +
          "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
          "\n" +
          "DELETE\n" +
          " { ?book ?p ?v }\n" +
          "WHERE\n" +
          " { ?book dc:date ?date .\n" +
          "   FILTER ( ?date > \"1970-01-01T00:00:00-02:00\"^^xsd:dateTime )\n" +
          "   ?book ?p ?v\n" +
          " }";
  public static final String TEST_CONNECTORTYPE_HTTP = TEST_BASEURI + "HTTPConnector";
  public static final String TEST_ACTIONTYPE_SCRIPTACTION = TEST_BASEURI + "ScriptAction";
  public static final String TEST_BODY_A = "body";
  public static final String TEST_HEADER_A = "header";
  public static final String TEST_GRAPH_A = "http://graph/";
  public static final String TEST_CONNECTOR_A = TEST_BASEURI + "daconnecta";
  public static final String TEST_ACTION_A = TEST_BASEURI + "daactiona";
  public static final String TEST_PARAMETER = "parameter";

  public static final String TEST_TASK_A = TEST_BASEURI + "dataska";
  public static final String TEST_CONTEXT_URI = TEST_BASEURI + "daContext";
  public static final String TEST_CONTEXT = "{\n" +
      "\"@context\": {\n" +
      "\"@vocab\": \"http://champ.scania.com/scaniapart#\",\n" +
      "\"man\": \"http://champ.scania.com/manpart/\",\n" +
      "\"navistar\": \"http://champ.scania.com/navistarpart/\",\n" +
      "\"xsd\": \"http://www.w3.org/2001/XMLSchema#\",\n" +
      "\"owl\" : \"http://www.w3.org/2002/07/owl#\",   \n" +
      "\"owl:sameAs\": {\n" +
      "\"@type\": \"@id\"\n" +
      "}\n" +
      "}\n" +
      "}";


  public static final String TEST_CONTEXT_2 = "{\"@context\": {\"@vocab\": \"http://champ.scania.com/scaniapart#\", \"man\": \"http://champ.scania.com/manpart/\", \"navistar\": \"http://champ.scania.com/navistarpart/\",\"xsd\": \"http://www.w3.org/2001/XMLSchema#\",\"owl\" : \"http://www.w3.org/2002/07/owl#\",\"owl:sameAs\": {\"@type\": \"@id\" } }}";

  public static final String TEST_CONTEXT_3 = "{\"@context\":{\"cccmi\":\"http://cccmi.scania.com/demo/wp5#\",\"owl\":\"http://www.w3.org/2002/07/owl#\",\"rdfs\":\"http://www.w3.org/2000/01/rdf-schema#\",\"rdf\":\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\",\"baseNameSpace\":\"cccmi\",\"cccmi:introCo\":{\"@type\":\"@id\"},\"cccmi:contains\":{\"@type\":\"@id\"},\"cccmi:hasAlternative\":{\"@type\":\"@id\"},\"cccmi:hasVariantCode\":{\"@type\":\"@id\"},\"cccmi:hasPart\":{\"@type\":\"@id\"},\"cccmi:revisionOf\":{\"@type\":\"@id\"},\"cccmi:usesPort\":{\"@type\":\"@id\"},\"has_time_carrier\":{\"@id\":\"cccmi:hasTimeCarrier\",\"@type\":\"@id\"},\"has_scn\":{\"@id\":\"cccmi:hasScenario\",\"@type\":\"@id\"},\"has_uc\":{\"@id\":\"cccmi:hasUseCase\",\"@type\":\"@id\"},\"has_eco\":{\"@id\":\"cccmi:hasECO\",\"@type\":\"@id\"},\"has_intro_co\":{\"@id\":\"cccmi:hasIntroCo\",\"@type\":\"@id\"},\"uf\":{\"@id\":\"cccmi:UserFunction\",\"@type\":\"@id\"},\"DevelopmentUserfunction\":{\"@id\":\"cccmi:UserFunction\",\"@type\":\"@id\"},\"uc\":{\"@id\":\"cccmi:UseCase\",\"@type\":\"@id\"},\"eco\":{\"@id\":\"cccmi:Eco\",\"@type\":\"@id\"},\"scn\":{\"@id\":\"cccmi:Scenario\",\"@type\":\"@id\"},\"time_carrier\":{\"@id\":\"cccmi:TimeCarrier\",\"@type\":\"@id\"},\"intro_co\":{\"@id\":\"cccmi:IntroCo\",\"@type\":\"@id\"},\"WorkItem\":{\"@id\":\"cccmi:WorkItem\",\"@type\":\"@id\"},\"hasPart\":{\"@id\":\"cccmi:hasPart\",\"@type\":\"@id\"},\"revisionOf\":{\"@id\":\"cccmi:revisionOf\",\"@type\":\"@id\"},\"usesPort\":{\"@id\":\"cccmi:usesPort\",\"@type\":\"@id\"}}}";

  public static final String TASK_SPARQL_RESP = "{\n" +
      "  \"head\": {\n" +
      "    \"vars\": [\n" +
      "      \"task\",\n" +
      "      \"nextaction\",\n" +
      "      \"nextactiontype\",\n" +
      "      \"taskparametertype\",\n" +
      "      \"taskparameter\",\n" +
      "      \"context\"\n" +
      "    ]\n" +
      "  },\n" +
      "  \"results\": {\n" +
      "    \"bindings\": [\n" +
      "      {\n" +
      "        \"task\": {\n" +
      "          \"type\": \"uri\",\n" +
      "          \"value\": \"" + TEST_TASK_A + "\"\n" +
      "        },\n" +
      "        \"nextaction\": {\n" +
      "          \"type\": \"uri\",\n" +
      "          \"value\": \"" + TEST_ACTION_A + "\"\n" +
      "        },\n" +
      "        \"nextactiontype\": {\n" +
      "          \"type\": \"uri\",\n" +
      "          \"value\": \"" + TEST_ACTIONTYPE_SCRIPTACTION + "\"\n" +
      "        },\n" +
      "        \"taskparametertype\": {\n" +
      "          \"type\": \"uri\",\n" +
      "          \"value\": \"" + TEST_INPUTPARAMETERTYPE_A + "\"\n" +
      "        },\n" +
      "        \"taskparameter\": {\n" +
      "          \"type\": \"uri\",\n" +
      "          \"value\": \"" + TEST_INPUTPARAMETERTYPE_A + "\"\n" +
      "        },\n" +
      "        \"context\": {\n" +
      "          \"type\": \"literal\",\n" +
      "          \"value\": " + TEST_CONTEXT +
      "}\n" +
      "      }\n" +
      "    ]\n" +
      "  }\n" +
      "}";

  public static final String UPLOAD_ACTION_SPARQL_RESP = "{\n" +
      "  \"head\": {\n" +
      "    \"vars\": [\n" +
      "      \"httpBody\",\n" +
      "      \"graph\",\n" +
      "      \"httpHeader\",\n" +
      "      \"system\",\n" +
      "      \"connector\",\n" +
      "      \"connectortype\",\n" +
      "      \"nextaction\",\n" +
      "      \"nextactiontype\",\n" +
      "      \"input\",\n" +
      "      \"endpoint\",\n" +
      "      \"inputparametertype\",\n" +
      "      \"inputparameter\",\n" +
      "      \"actionid\"\n" +
      "    ]\n" +
      "  },\n" +
      "  \"results\": {\n" +
      "    \"bindings\": [\n" +
      "      {\n" +
      "        \"inputparameter\": {\n" +
      "          \"type\": \"uri\",\n" +
      "          \"value\": \"" + TEST_INPUTPARAMETER_A + "\"\n" +
      "        },\n" +
      "        \"inputparametertype\": {\n" +
      "          \"type\": \"uri\",\n" +
      "          \"value\": \"" + TEST_INPUTPARAMETERTYPE_A + "\"\n" +
      "        },\n" +
      "        \"system\": {\n" +
      "          \"type\": \"uri\",\n" +
      "          \"value\": \"" + TEST_SYSTEM_A + "\"\n" +
      "        },\n" +
      "        \"connector\": {\n" +
      "          \"type\": \"uri\",\n" +
      "          \"value\": \"" + TEST_CONNECTOR_A + "\"\n" +
      "        },\n" +
      "        \"connectortype\": {\n" +
      "          \"type\": \"uri\",\n" +
      "          \"value\": \"" + TEST_CONNECTORTYPE_HTTP + "\"\n" +
      "        },\n" +
      "        \"nextaction\": {\n" +
      "          \"type\": \"uri\",\n" +
      "          \"value\": \"" + TEST_ACTION_A + "\"\n" +
      "        },\n" +
      "        \"nextactiontype\": {\n" +
      "          \"type\": \"uri\",\n" +
      "          \"value\": \"" + TEST_ACTIONTYPE_SCRIPTACTION + "\"\n" +
      "        },\n" +
      "        \"httpHeader\": {\n" +
      "          \"type\": \"literal\",\n" +
      "          \"value\":" + "HEADER" + "\n" +
      "        },\n" +
      "        \"httpBody\": {\n" +
      "          \"type\": \"literal\",\n" +
      "          \"value\": \"" + TEST_BODY_A + "\"\n" +
      "        },\n" +
      "        \"graph\": {\n" +
      "          \"type\": \"literal\",\n" +
      "          \"value\": \"" + TEST_GRAPH_A + "\"\n" +
      "        },\n" +
      "        \"endpoint\": {\n" +
      "          \"type\": \"literal\",\n" +
      "          \"value\": \"" + ENDPOINT + "\"\n" +
      "        }\n" +
      "      }\n" +
      "    ]\n" +
      "  }\n" +
      "}";
  public static final String LIBRARY_JSON_LD = "{\n"
      + "  \"@context\": {\n"
      + "    \"dc11\": \"http://purl.org/dc/elements/1.1/\",\n"
      + "    \"ex\": \"http://example.org/vocab#\",\n"
      + "    \"xsd\": \"http://www.w3.org/2001/XMLSchema#\",\n"
      + "    \"ex:contains\": {\n"
      + "      \"@type\": \"@id\"\n"
      + "    }\n"
      + "  },\n"
      + "  \"@graph\": [\n"
      + "    {\n"
      + "      \"@id\": \"http://example.org/library\",\n"
      + "      \"@type\": \"ex:Library\",\n"
      + "      \"ex:contains\": \"http://example.org/library/the-republic\"\n"
      + "    },\n"
      + "    {\n"
      + "      \"@id\": \"http://example.org/library/the-republic\",\n"
      + "      \"@type\": \"ex:Book\",\n"
      + "      \"dc11:creator\": \"Plato\",\n"
      + "      \"dc11:title\": \"The Republic\",\n"
      + "      \"ex:contains\": \"http://example.org/library/the-republic#introduction\"\n"
      + "    },\n"
      + "    {\n"
      + "      \"@id\": \"http://example.org/library/the-republic#introduction\",\n"
      + "      \"@type\": \"ex:Chapter\",\n"
      + "      \"dc11:description\": \"An introductory chapter on The Republic.\",\n"
      + "      \"dc11:title\": \"The Introduction\"\n"
      + "    }\n"
      + "  ]\n"
      + "}";

  public static String groovyRes = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
      "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n"
      +
      "<soapenv:Body>\n" +
      "<getWorkItemByIdResponse xmlns=\"http://ws.polarion.com/TrackerWebService-impl\">\n" +
      "<getWorkItemByIdReturn uri=\"subterra:data-service:objects:/default/TestXX\" unresolvable=\"false\" xsi:type=\"ns3:WorkItem\" xmlns:ns1=\"http://ws.polarion.com/types\" xmlns:ns2=\"http://ws.polarion.com/ProjectWebService-types\" xmlns:ns3=\"http://ws.polarion.com/TrackerWebService-types\">\n"
      +
      "<ns3:approvals/>\n" +
      "<ns3:assignee>\n" +
      "</ns3:assignee>\n" +
      "<ns3:attachments/>\n" +
      "<ns3:author uri=\"subterra:da";

  public static String GROOVYSCRIPT = "import com.google.gson.Gson\n" +
      "import groovy.json.JsonSlurper\n" +
      "\n" +
      "def jsonSlurper = new JsonSlurper();\n" +
      "xmlSlurper = new XmlSlurper();\n" +
      "data_raw = xmlSlurper.parseText(polarion_rawdata)\n" +
      "data_ctx = jsonSlurper.parseText(context)\n" +
      "\n" +
      "//ClassLoader classLoader = getClass().getClassLoader();\n" +
      "//URL templateuri = classLoader.getResource('apiResources/Polarion_Response_test11_2.xml');\n"
      +
      "//def _data = new File(templateuri.toURI())\n" +
      "//data_raw = xmlSlurper.parse(_data)\n" +
      "//\n" +
      "//URL templateuri1 = classLoader.getResource('cccmi_context.json');\n" +
      "//def _data2 = new File(templateuri1.toURI())\n" +
      "//data_ctx = jsonSlurper.parse(_data2)\n" +
      "baseNamePrefix = \"cccmi\"\n" +
      "\n" +
      "\n" +
      "mapGraph = [:]\n" +
      "listElem = []\n" +
      "id = \"\"\n" +
      "result = [:]\n" +
      "test = []\n" +
      "\n" +
      "typeMap = [\n" +
      "        \"http://rdf.scania.com/ns/scania_oas#relatedEco\"                            : \"eco\",\n"
      +
      "        \"http://rdf.scania.com/ns/scania_oas#relatedUserFunction\"                   : \"uf\",\n"
      +
      "        \"http://rdf.scania.com/ns/scania_fam#relatedDevelopmentUserFunctionRevision\": \"duf\"\n"
      +
      "]\n" +
      "\n" +
      "def colonAdder(prefix) {\n" +
      "    return prefix + \":\"\n" +
      "}\n" +
      "\n" +
      "def createResult(String subjectIri, HashMap<String, String> keyValue) {\n" +
      "    resultValue = [:]\n" +
      "    keyValue.each { key, value ->\n" +
      "        resultValue.put(key, value)\n" +
      "    }\n" +
      "    result.put(subjectIri, resultValue)\n" +
      "}\n" +
      "\n" +
      "def workItemMapperfunc(Object obj, String parentId, String prefix) {\n" +
      "    mapTemp = [:]\n" +
      "    id = \"\"\n" +
      "    mapTemp[\"@id\"] = prefix + (obj.'@xsi:type'.text() - ~/.*:/) + \"-\" + obj.id\n" +
      "    mapTemp[\"@type\"] = (obj.'@xsi:type'.text() - ~/.*:/)\n" +
      "    obj.attributes().each { key, value ->\n" +
      "        key = key - ~/\\{.*\\}/\n" +
      "        mapTemp[prefix + key] = (value - ~/.*:/)\n" +
      "    }\n" +
      "    listElem.add(mapTemp)\n" +
      "    return listElem.size()\n" +
      "}\n" +
      "\n" +
      "def linkedOslcResourceMapperfunc(Object obj, Object parentId, String prefix) {\n" +
      "    mapTemp = [:]\n" +
      "    typeShort = typeMap[obj.role.id]\n" +
      "    typeShortShort = obj.role.id.text().substring(obj.role.id.text().indexOf('#') + 1, obj.role.id.text().length());\n"
      +
      "    parent = listElem[parentId - 1]\n" +
      "    if (!(parent.find { it.key == prefix + \"contains\" }?.value)) {\n" +
      "        parent[prefix + \"contains\"] = []\n" +
      "    }\n" +
      "    parent[prefix + \"contains\"].add(colonAdder(baseNamePrefix) + typeShort + \"-\" + (obj.uri.text() - ~/.*\\/[a-zA-Z]*/))\n"
      +
      "\n" +
      "}\n" +
      "\n" +
      "def mainfunc() {\n" +
      "    Gson gson = new Gson();\n" +
      "    workItem = data_raw.'**'.find { node -> node.name() == 'getWorkItemByIdReturn' }\n" +
      "\n" +
      "    workItemId = workItemMapperfunc(workItem, null, colonAdder(baseNamePrefix))\n" +
      "    def linkedOslcResourceList = workItem.'**'.findAll { node -> node.name() == 'LinkedOslcResource' }\n"
      +
      "    linkedOslcResourceList.each {\n" +
      "        linkedOslcResourceMapperfunc(it, workItemId, colonAdder(baseNamePrefix))\n" +
      "    }\n" +
      "    mapGraph[\"@graph\"] = listElem\n" +
      "    mapGraph = data_ctx + mapGraph\n" +
      "    resultmap = [:]\n" +
      "    resultString = gson.toJson(mapGraph, Map.class)\n" +
      "    resultmap.put(\"httpBody\", resultString)\n" +
      "    createResult(\"https://kg.scania.com/it/iris_orchestration/PARAM_SCRIPT1_OUTPUT\", resultmap)\n"
      +
      "\n" +
      "\n" +
      "}\n" +
      "\n" +
      "mainfunc()\n" +
      "\n" +
      "return result";


  public static String GROOVYSCRIPT2 = "import groovy.json.JsonOutput\n" +
      "import groovy.xml.*\n" +
      "\n" +
      "def xmlSlurper = new XmlSlurper();\n" +
      "\n" +
      "data = xmlSlurper.parseText(InternalSOAPSessionIdEnvelope)\n" +
      "\n" +
      "listElem = []\n" +
      "\n" +
      "def mainfunc() {\n" +
      "    mapTemp = [:]\n" +
      "    def base = data.'**'.find { node -> node.name() == 'sessionID' }\n" +
      "    mapTemp.put(\"nameSpaceURI\", \"http://ws.polarion.com/session\")\n" +
      "    mapTemp.put(\"localPart\", \"sessionID\")\n" +
      "    mapTemp.put(\"value\", base.text())\n" +
      "    listElem.add(mapTemp)\n" +
      "}\n" +
      "\n" +
      "mainfunc()\n" +
      "println(JsonOutput.prettyPrint(JsonOutput.toJson(listElem)))\n" +
      "return listElem";

  public static String POLARIONRESPONSE =
      "<?xml version=\"1.0\" encoding=\"utf-8\"?><soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><soapenv:Body><soapenv:Fault><faultcode>soapenv:Server.generalException</faultcode><faultstring>Not authorized.</faultstring><detail><ns1:stackTrace xmlns:ns1=\"http://xml.apache.org/axis/\">Not authorized.\n"
          +
          "\tat com.polarion.alm.ws.providers.DoAsUserWrapper.invoke(DoAsUserWrapper.java:37)\n" +
          "\tat org.apache.axis.strategies.InvocationStrategy.visit(InvocationStrategy.java:32)\n" +
          "\tat org.apache.axis.SimpleChain.doVisiting(SimpleChain.java:118)\n" +
          "\tat org.apache.axis.SimpleChain.invoke(SimpleChain.java:83)\n" +
          "\tat org.apache.axis.handlers.soap.SOAPService.invoke(SOAPService.java:454)\n" +
          "\tat org.apache.axis.server.AxisServer.invoke(AxisServer.java:281)\n" +
          "\tat org.apache.axis.transport.http.AxisServlet.doPost(AxisServlet.java:699)\n" +
          "\tat javax.servlet.http.HttpServlet.service(HttpServlet.java:648)\n" +
          "\tat org.apache.axis.transport.http.AxisServletBase.service(AxisServletBase.java:327)\n"
          +
          "\tat javax.servlet.http.HttpServlet.service(HttpServlet.java:729)\n" +
          "\tat org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:291)\n"
          +
          "\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:206)\n"
          +
          "\tat com.polarion.portal.tomcat.servlets.SecurityCheckFilter.doFilter(SecurityCheckFilter.java:46)\n"
          +
          "\tat org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:239)\n"
          +
          "\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:206)\n"
          +
          "\tat org.apache.catalina.core.StandardWrapperValve.invoke(StandardWrapperValve.java:212)\n"
          +
          "\tat org.apache.catalina.core.StandardContextValve.invoke(StandardContextValve.java:106)\n"
          +
          "\tat org.apache.catalina.core.StandardHostValve.invoke(StandardHostValve.java:141)\n" +
          "\tat org.apache.catalina.valves.ErrorReportValve.invoke(ErrorReportValve.java:79)\n" +
          "\tat org.apache.catalina.core.StandardEngineValve.invoke(StandardEngineValve.java:88)\n"
          +
          "\tat org.apache.catalina.authenticator.SingleSignOn.invoke(SingleSignOn.java:240)\n" +
          "\tat com.polarion.platform.session.PolarionSingleSignOn.invoke(PolarionSingleSignOn.java:182)\n"
          +
          "\tat org.apache.catalina.connector.CoyoteAdapter.service(CoyoteAdapter.java:521)\n" +
          "\tat org.apache.coyote.ajp.AbstractAjpProcessor.process(AbstractAjpProcessor.java:850)\n"
          +
          "\tat org.apache.coyote.AbstractProtocol$AbstractConnectionHandler.process(AbstractProtocol.java:674)\n"
          +
          "\tat org.apache.tomcat.util.net.NioEndpoint$SocketProcessor.doRun(NioEndpoint.java:1500)\n"
          +
          "\tat org.apache.tomcat.util.net.NioEndpoint$SocketProcessor.run(NioEndpoint.java:1456)\n"
          +
          "\tat java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1149)\n" +
          "\tat java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)\n" +
          "\tat org.apache.tomcat.util.threads.TaskThread$WrappingRunnable.run(TaskThread.java:61)\n"
          +
          "\tat java.lang.Thread.run(Thread.java:748)\n" +
          "</ns1:stackTrace><ns2:hostname xmlns:ns2=\"http://xml.apache.org/axis/\">SESOCO3206</ns2:hostname></detail></soapenv:Fault></soapenv:Body></soapenv:Envelope>";

  public static String POLARIONRESPONSE2 = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
      "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n"
      +
      "\t<soapenv:Header>\n" +
      "\t\t<ns1:sessionID soapenv:actor=\"http://schemas.xmlsoap.org/soap/actor/next\" soapenv:mustUnderstand=\"0\" xmlns:ns1=\"http://ws.polarion.com/session\">-12345</ns1:sessionID>\n"
      +
      "\t</soapenv:Header>\n" +
      "\t<soapenv:Body>\n" +
      "\t\t<logInResponse xmlns=\"http://ws.polarion.com/SessionWebService-impl\"/>\n" +
      "\t</soapenv:Body>\n" +
      "</soapenv:Envelope> ";

  public static final String JSONLDCONTEXTMODELTESTDATA = "JsonLdContextTestData.json";
  public static final String ACONTEXT_URI = "https://kg.scania.com/it/iris_orchestration/aContext";
  public static final String GRAPH_AS_MODEL = "GraphAsModel.json";
  public static final String CONSTRUCT_QUERY_RESPONSE = "GetAllTasksAndParametersConstruct.json";

  public  static final String CONSTRUCT_QUERY= "PREFIX : <" + SDOSConstants.ORCHESTRATION_PREFIX + "> \n"
          + "CONSTRUCT {\n"
          + "    ?task rdf:type :Task ;\n"
          + "          rdfs:label ?taskLabel ;\n"
          + "          :inputParameter ?inputparameter .\n"
          + "    ?inputparameter rdf:type ?inputparametertype ;\n"
          + "                    rdfs:label ?inputparamlabel ;\n"
          + "                    :paramName ?paramName ;\n"
          + "                    :endpoint ?endpoint ;\n"
          + "                    :httpBody ?httpBody ;\n"
          + "                    :httpHeader ?httpHeader ;\n"
          + "                    :httpQueryParameter ?httpQueryParameter .\n"
          + "}\n"
          + "{ graph ?g {\n"
          + "    ?task rdf:type :Task ;\n"
          + "          rdfs:label ?taskLabel.\n"
          + "    OPTIONAL{?task :inputParameter ?inputparameter .\n"
          + "        {\n"
          + "            ?inputparameter rdf:type :Parameter .\n"
          + "            BIND(:Parameter AS ?inputparametertype)\n"
          + SDOSConstants.CONSTRUCT_CLOSING
            + "        UNION {\n"
                    + "            ?inputparameter rdf:type :StandardParameter ;\n"
                    + "                            :paramName ?paramName .\n"
                    + "            BIND(:StandardParameter AS ?inputparametertype)\n"
                    + SDOSConstants.CONSTRUCT_CLOSING
            + "        UNION {\n"
                    + "            ?inputparameter rdf:type :HTTPParameter .\n"
                    + "            OPTIONAL{?inputparameter :endpoint ?endpoint . } .\n"
                    + "            OPTIONAL{?inputparameter :httpBody ?httpBody . } .\n"
                    + "            OPTIONAL{?inputparameter :httpHeader ?httpHeader . } .\n"
                    + "            OPTIONAL{?inputparameter :httpQueryParameter ?httpQueryParameter . } .\n"
                    + "            BIND(:HTTPParameter AS ?inputparametertype)\n"
                    + SDOSConstants.CONSTRUCT_CLOSING
            + "        UNION { ?inputparameter rdf:type :BasicCredentialsParameter .\n"
                    + "            BIND(:BasicCredentialsParameter AS ?inputparametertype)}\n"
                    + "        UNION { ?inputparameter rdf:type :TokenCredentialsParameter .\n"
                    + "            BIND(:TokenCredentialsParameter AS ?inputparametertype)}\n"
                    + "        ?inputparameter rdfs:label ?inputparamlabel .\n"
                    + "    }\n"
                    + "}}\n";
}
