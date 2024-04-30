package com.scania.sdip.sdos.utils;

/**
 * The SDOSConstants class contains constants used by SDOS.
 */
public class SDOSConstants {

  public static final String SDOS = "SDOS";
  public static final String TIMESTAMP_PATTERN = "uuuu-MM-dd-HH:mm:ss:SS";
  public static final String RESULTGRAPH_PATTERN = "uuuuMMddHHmmssSS";
  public static final String RESULTGRAPH = "resultgraph";
  public static final String UPDATE = "/update";
  public static final String VAR_GRAPH = "VAR_GRAPH";
  public static final String VAR_SUBJECT_IRI = "VAR_SUBJECT_IRI";

  public static final String FLOW_GRAPH_TYPE = "GRAPH_TYPE";

  public static final String VAR_CREATOR = "VAR_CREATOR";

  public static final String VAR_CONTRIBUTOR = "VAR_CONTRIBUTOR";
  public static final String VAR_INFORMATION_RESPONSIBLE = "VAR_INFORMATION_RESPONSIBLE";
  public static final String VAR_TIMESTAMP = "VAR_TIMESTAMP";
  public static final String VAR_STATE = "VAR_STATE";
  public static final String VAR_DESCRIPTION = "VAR_DESCRIPTION";
  public static final String OUTPUTPARAMETERS = "outputParameters";
  public static final String INPUTPARAMETERS = "inputParameter";
  public static final String CONFIGURATION_HASH_PATH = "/configurationhash";
  public static final String SERVICESTATUS_PATH = "/servicestatus";
  public static final String CONFIGURE_PATH = "/configuration/";
  public static final String ERRORCODE_PREFIX = "sdipErrorCodes";
  public static final String CONFIGURATION = "CONFIGURATION";
  public static final String SERVICESTATUS = "status";
  public static final String CONFIGURATIONSTATUS = "configurationStatus";
  public static final String SERVICETYPE = "serviceType";
  public static final String SERVICEID = "serviceId";
  public static final String SUCCESSFUL = "SUCCESSFUL";
  public static final String UNCONFIGURED = "UNCONFIGURED";
  public static final String CONFIGURATION_HASH = "configurationHash";
  public static final String BEARER = "Bearer";
  public static final String EMPTY_STRING = "";


  public static final String ACCEPT = org.springframework.http.HttpHeaders.ACCEPT;


  public static final String NAMEDGRAPH = "NAMEDGRAPH";//TODO TEMP
  public static final String ENDPOINT = "endpoint";
  public static final String VALUE = "value";
  public static final String HTTPBODY = "httpBody";
  public static final String HTTPHEADER = "httpHeader";
  public static final String HTTPQUERYPARAM = "httpQueryParameter";
  public static final String SOAPOPERATION = "soapOperation";
  public static final String WSDLFILE = "wsdlFile";
  public static final String BINDINGS = "bindings";
  public static final String RESULTS = "results";
  public static final String VARIABLE = "VARIABLE";
  public static final String SCRIPT = "script";
  public static final String NEXT_ACTION = "nextaction";
  public static final String NEXT_ACTION_TYPE = "nextactiontype";
  public static final String OUTPUT_PARAMETER = "outputparameter";
  public static final String OUTPUT_PARAMETER_TYPE = "outputparametertype";
  public static final String INPUT_PARAMETER = "inputparameter";
  public static final String INPUT_PARAMETER_TYPE = "inputparametertype";
  public static final String SCRIPT_TYPE = "scripttype";
  public static final String BINDING_NAME = "bindingName";
  public static final String TASK_ID = "taskid";
  public static final String RESULTMETADATA = "resultMetaData";
  public static final String SUBJECT_IRI = "subjectIri";
  public static final String HAS_AUTH_METHOD = "hasAuthenticationMethod";
  public static final String PARAM_NAME = "paramName";
  public static final String CONTEXT = "context";
  public static final String HAS_CONTEXT = "hasContext";
  public static final String HTTP_CONNECTOR = "HTTPConnector";
  public static final String SOAP_CONNECTOR = "SOAPConnector";
  public static final String HAS_CONNECTOR = "hasConnector";

  public static final String HTTP_ACTION = "HTTPAction";
  public static final String SOAP_ACTION = "SOAPAction";
  public static final String SCRIPT_ACTION = "ScriptAction";
  public static final String RESULT_ACTION = "ResultAction";
  public static final String QUERY_ACTION = "QueryAction";
  public static final String VIRTUAL_GRAPH_ACTION = "VirtualGraphAction";
  public static final String SPARQL_CONVERT_ACTION = "SparqlConvertAction";
  public static final String BASE_URL = "baseUrl";
  public static final String TASK = "task";
  public static final String GRAPH = "graph";
  public static final String CONNECTOR_TYPE = "connectortype";
  public static final String OFG_ENDPOINT = "ofg_endpoint";
  public static final String O_RESULT = "o_result";
  public static final String RESULT = "http://result";
  public static final String HTTPPARAMETER = "HTTPParameter";
  public static final String STANDARDPARAMETER = "StandardParameter";
  public static final String BASICCREDENTIALSPARAMETER = "BasicCredentialsParameter";
  public static final String TOKENCREDENTIALSPARAMETER = "TokenCredentialsParameter";
  public static final String SPARQLQUERYPARAMETER = "SparqlQueryParameter";
  public static final String CORE_ONTOLOGY_PREFIX = "http://kg.scania.com/core/";
  public static final String ORCHESTRATION_PREFIX = "https://kg.scania.com/it/iris_orchestration/";//TODO TEMP
  public static final String PREFIX_RDF = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n";
  public static final String PREFIX_RDFS = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n";
  public static final String PREFIX_OWL = "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n";
  public static final String ACTION_SPARQL = "sparql";
  public static final String CONSTRUCT_SPARQL = "constructSparql";
  public static final String LABEL = "label";
  public static final String INFORMATION_RESPONSIBLE = "informationResponsible";
  public static final String EXAMPLE_PARAMETERS = "{\"param1label\":{\"a\":\"\",\"b\":\"\"},\"param2label\":{\"c\":\"\"}}";
  public static final String KEY = "key";
  public static final String USERNAME = "username";
  public static final String PASSWORD = "password";
  public static final String ENCRYPTKEY = "encryption";
  public static final String HAS_AUTH_METHOD_TYPE = "hasAuthenticationMethodType";

  public static final String CONTRIBUTOR = "contributor";
  public static final String CREATOR = "creator";
  public static final String DESCRIPTION = "description";
  public static final String TITLE = "title";
  public static final String TIMESTAMP = "timestamp";
  public static final String GRAPH_TYPE = "graphType";
  public static final String PERMANENT_SERVICE = "permanentService";
  public static final String OOVERSION = "ooVersion";
  public static final String METADATA = "metadata";
  public static final String CONTENT_TYPE = "Content-Type";
  public static final String UTF_8 = "UTF-8";
  public static final String QUERY = "query";
  public static final String ENVIRONMENT_TAG = "environment_tag";
  public static final String LOCAL = "LOCAL";
  public static final String EXECUTION_REPORT = "ExecutionReport";
  public static final String ID = "Id";
  public static final String STACKTRACE_CHECK = "com.scania.sdip.sdos.orchestration";
  public static final String SYNC = "_OFG_Sync_call";
  public static final String SYNC_RESULT = "_OFG_Sync_call_result";
  public static final String AUTHORIZATION ="Authorization";
  public static final String BASIC = "Basic";
  public static final String TOKEN = "token";
  public static final String ENABLEREASONER = "enableReasoner";
  public static final String CURRENT_TASK = "_Current_Task";
  public static final String BEARER_TOKEN = "_Bearer_Token";
  public static final String OBO_TOKEN = "_Obo_Token";
  public static final String INVALID_CONTEXT = "Invalid Context Data";
  public static final String UNION = "        UNION { ?";
  public static final String CLOSING = ")} \n";
  public static final String OPENING= "        {?";
  public static final String HTTPACTIONMODEL_CLOSING2= "    }\n";
  public static final String EMPTY = "";
  public static final String NEW_LINES_PATTERN = "\\r\\n|\\r|\\n";
  public static final String BEGIN_PUBLIC_KEY = "-----BEGIN PUBLIC KEY-----";
  public static final String END_PUBLIC_KEY = "-----END PUBLIC KEY-----";
  public static final String KEYS = "keys";
  public static final String KID = "kid";
  public static final String KEY_X5 = "x5c";

  public static final String KEY_X = "X.509";
  public static final String KEY_SPLIT = "\\.";
  public static final String NOT_SPACE = "";

  public static final String GRANT_TYPE = "grant_type";
  public static final String CLIENT_ID = "client_id";
  public static final String CLIENT_SECRET = "client_secret";
  public static final String ASSERTION = "assertion";
  public static final String SCOPE = "scope";
  public static final String TOKEN_USE = "requested_token_use";
  public static final String TOKEN_USE_VALUE = "on_behalf_of";
  public static final String ACCEPT_HEADER = "Accept";
  public static final String ACCESS_TOKEN = "access_token";
  public static final String APP_ID = "appid";
  public static final String GIVEN_NAME = "given_name";
  public static final String UNIQUE_NAME = "unique_name";
  public static final String NOT_VALID = "Token is not valid ";
  public static final String ACCEPT_HEADER_VALUE = "application/json";
  public static final String CONSTRUCT_CLOSING = "        }\n";
  public static final String AZURE_TENANT_URL= "https://login.microsoftonline.com/tenantId/oauth2/v2.0/token";

  public static final String TENANT_ID = "tenantId";

  public static final String ROLES = "roles";
  public static final String GETALLTASKS_QUERY = "PREFIX : <" + SDOSConstants.ORCHESTRATION_PREFIX + "> \n"
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
                    + HTTPACTIONMODEL_CLOSING2
                    + "}}\n";
                    
  private SDOSConstants() {  }
}
