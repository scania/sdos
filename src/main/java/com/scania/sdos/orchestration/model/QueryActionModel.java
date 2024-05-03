package com.scania.sdos.orchestration.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonSyntaxException;
import com.scania.sdip.exceptions.IncidentException;
import com.scania.sdip.exceptions.SdipErrorCode;
import com.scania.sdip.exceptions.SdipErrorParameter;
import com.scania.sdos.model.ServiceArguments;
import com.scania.sdos.orchestration.Rdf4jClient;
import com.scania.sdos.orchestration.interfaces.IParameterMemory;
import com.scania.sdos.orchestration.interfaces.IParameterModel;
import com.scania.sdos.utils.SDOSConstants;
import com.scania.sdos.utils.Utility;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.rdf4j.query.parser.ParsedGraphQuery;
import org.eclipse.rdf4j.query.parser.ParsedQuery;
import org.eclipse.rdf4j.query.parser.ParsedTupleQuery;

public class QueryActionModel extends ActionModel {

    private Rdf4jClient rdf4jClient;
    private boolean enableReasoner;

    public void setEnableReasoner(boolean enableReasoner) {
        this.enableReasoner = enableReasoner;
    }

    public boolean isEnableReasoner() {
        return enableReasoner;
    }

    public void setRdf4jClient(Rdf4jClient rdf4jClient) {
        this.rdf4jClient = rdf4jClient;
    }

    public QueryActionModel() {
        this.rdf4jClient = new Rdf4jClient();
    }

    private static final Logger LOGGER = LogManager.getLogger(QueryActionModel.class);
    public static final String SPARQL =
            SDOSConstants.PREFIX_RDF
                    + SDOSConstants.PREFIX_RDFS
                    + "PREFIX : <" + SDOSConstants.ORCHESTRATION_PREFIX + "> \n"
                    + "SELECT ?" + SDOSConstants.LABEL
                    + " ?" + SDOSConstants.INPUT_PARAMETER
                    + " ?" + SDOSConstants.OUTPUT_PARAMETER
                    + " ?" + SDOSConstants.INPUT_PARAMETER_TYPE
                    + " ?" + SDOSConstants.OUTPUT_PARAMETER_TYPE
                    + " ?" + SDOSConstants.NEXT_ACTION
                    + " ?" + SDOSConstants.NEXT_ACTION_TYPE
                    + " ?" + SDOSConstants.ENABLEREASONER
                    + "\n "
                    + "WHERE \n"
                    + "{  \n"
                    + "BIND(<" + SDOSConstants.VARIABLE + "> AS ?subject)\n"
                    + "    ?subject  rdf:type :" + SDOSConstants.QUERY_ACTION + " ;\n"
                    + "        rdfs:label ?" + SDOSConstants.LABEL + ";\n"
                    + "        :enableReasoner ?" + SDOSConstants.ENABLEREASONER + ".\n"
                    + "    ?subject :inputParameter  ?" + SDOSConstants.INPUT_PARAMETER + " . \n"
                    + SDOSConstants.OPENING + SDOSConstants.INPUT_PARAMETER + " rdf:type :Parameter .\n"
                    + "            BIND(:Parameter AS ?" + SDOSConstants.INPUT_PARAMETER_TYPE + ")}\n"
                    + SDOSConstants.UNION + SDOSConstants.INPUT_PARAMETER + " rdf:type :HTTPParameter .\n"
                    + "            BIND(:HTTPParameter AS ?" + SDOSConstants.INPUT_PARAMETER_TYPE + ")}\n"
                    + SDOSConstants.UNION + SDOSConstants.INPUT_PARAMETER + " rdf:type :StandardParameter .\n"
                    + "            BIND(:StandardParameter AS ?" + SDOSConstants.INPUT_PARAMETER_TYPE + ")}\n"
                    + SDOSConstants.UNION + SDOSConstants.INPUT_PARAMETER + " rdf:type :SparqlQueryParameter .\n"
                    + "            BIND(:SparqlQueryParameter AS ?" + SDOSConstants.INPUT_PARAMETER_TYPE + ")}\n"
                    + "    OPTIONAL{?subject :outputParameter  ?" + SDOSConstants.OUTPUT_PARAMETER + " .\n"
                    + "        {?" + SDOSConstants.OUTPUT_PARAMETER + " rdf:type :Parameter .\n"
                    + "            BIND(:Parameter AS ?" + SDOSConstants.OUTPUT_PARAMETER_TYPE + ")}\n"
                    + SDOSConstants.UNION + SDOSConstants.OUTPUT_PARAMETER + " rdf:type :StandardParameter .\n"
                    + "            BIND(:StandardParameter AS ?" + SDOSConstants.OUTPUT_PARAMETER_TYPE + ")}\n"
                    + SDOSConstants.UNION + SDOSConstants.OUTPUT_PARAMETER + " rdf:type :HTTPParameter .\n"
                    + "            BIND(:HTTPParameter AS ?" + SDOSConstants.OUTPUT_PARAMETER_TYPE + ")}\n"
                    + "    }\n"
                    + "    OPTIONAL{?subject :hasNextAction ?" + SDOSConstants.NEXT_ACTION + " .\n"
                    + "        {?" + SDOSConstants.NEXT_ACTION + " rdf:type :Action .} \n"
                    + SDOSConstants.UNION + SDOSConstants.NEXT_ACTION + " rdf:type :HTTPAction .\n"
                    + "            BIND(:HTTPAction AS ?" + SDOSConstants.NEXT_ACTION_TYPE + SDOSConstants.CLOSING
                    + SDOSConstants.UNION + SDOSConstants.NEXT_ACTION + " rdf:type :SOAPAction .\n"
                    + "            BIND(:SOAPAction AS ?" + SDOSConstants.NEXT_ACTION_TYPE + SDOSConstants.CLOSING
                    + SDOSConstants.UNION + SDOSConstants.NEXT_ACTION + " rdf:type :ScriptAction .\n"
                    + "            BIND(:ScriptAction AS ?" + SDOSConstants.NEXT_ACTION_TYPE + SDOSConstants.CLOSING
                    + SDOSConstants.UNION + SDOSConstants.NEXT_ACTION + " rdf:type :ResultAction . \n"
                    + "            BIND(:ResultAction AS ?" + SDOSConstants.NEXT_ACTION_TYPE + SDOSConstants.CLOSING
                    + SDOSConstants.UNION + SDOSConstants.NEXT_ACTION + " rdf:type :VirtualGraphAction .\n"
                    + "            BIND(:VirtualGraphAction AS ?" + SDOSConstants.NEXT_ACTION_TYPE + ")}\n"
                    + "        UNION { ?" + SDOSConstants.NEXT_ACTION + " rdf:type :SparqlConvertAction . \n"
                    + "            BIND(:SparqlConvertAction AS ?" + SDOSConstants.NEXT_ACTION_TYPE + SDOSConstants.CLOSING
                    + "        UNION { ?" + SDOSConstants.NEXT_ACTION + " rdf:type :QueryAction . \n"
                    + "            BIND(:QueryAction AS ?" + SDOSConstants.NEXT_ACTION_TYPE + SDOSConstants.CLOSING
                    + "    }\n"
                    + "}\n";

    @Override
    public void populate(String subjectIri, IParameterMemory iParameterMemory,
                         ServiceArguments serviceArguments) {
        try {
            setSubjectIri(subjectIri);
            JsonArray jsonArray = rdf4jClient
                .selectSparqlOfg(getSparql(subjectIri, SPARQL), iParameterMemory.getOfgModelRepo());
            setLabel(jsonArray.get(0).getAsJsonObject().get(SDOSConstants.LABEL).getAsJsonObject()
                    .get(SDOSConstants.VALUE).getAsString());
            setEnableReasoner(
                    jsonArray.get(0).getAsJsonObject().get(SDOSConstants.ENABLEREASONER)
                            .getAsJsonObject()
                            .get(SDOSConstants.VALUE).getAsBoolean());
            if (jsonArray.get(0).getAsJsonObject().has(SDOSConstants.INPUT_PARAMETER_TYPE) &&
                    jsonArray.get(0).getAsJsonObject().has(SDOSConstants.INPUT_PARAMETER)) {
                populateInputParameter(iParameterMemory, serviceArguments, jsonArray);
            } else {
                populateInputParameter(iParameterMemory, serviceArguments, null);
            }
            populateOutputParameter(iParameterMemory, serviceArguments, jsonArray);
            populateNextAction(iParameterMemory, serviceArguments, jsonArray);

        } catch (IllegalStateException exception) {
            throw new IncidentException(exception, Utility.getErrorMessage(exception, this.label,
                    this.subjectIri), SdipErrorCode.INVALID_JSONARRAY_RESPONSE, LOGGER,
                    exception.getMessage());
        } catch (NullPointerException exception) {
            throw new IncidentException(exception, Utility.getErrorMessage(exception, this.label,
                    this.subjectIri), SdipErrorCode.FAILED_TO_PARSE_JSONARRAY, LOGGER,
                    exception.getMessage());
        } catch (IncidentException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IncidentException(exception, Utility.getErrorMessage(exception, this.label,
                    this.subjectIri), SdipErrorCode.UNKNOWN_PARSING_ERROR, LOGGER,
                    exception.getMessage());
        }
    }

    @Override
    public void run(IParameterMemory iParameterMemory, JsonLdContextModel context,
        ServiceArguments serviceArguments) {
        try {
            JsonArray array;
            String query = "";
            String queryResponse = "";
            for (IParameterModel inputParameterModel : inputParameter) {
                HashMap<String, List<String>> memoryValue = iParameterMemory
                    .getValue(inputParameterModel.getSubjectIri());
                for (String key : inputParameterModel.getKeys()) {
                    if (key.equals(SDOSConstants.QUERY)) {
                        query = memoryValue.get(key).get(0);
                    }
                    ParsedQuery parsedQuery = rdf4jClient.isSPARQL(query);
                    if (parsedQuery instanceof ParsedTupleQuery) {
                        array = rdf4jClient.doSelectSparql(query,
                                serviceArguments, iParameterMemory, enableReasoner);
                        saveResponseToIParamMemory(iParameterMemory,
                                convertQueryResponseIntoString(array));
                    } else if (parsedQuery instanceof ParsedGraphQuery) {
                        queryResponse = rdf4jClient.doConstructSparql(query,
                                serviceArguments, iParameterMemory, enableReasoner);
                        saveResponseToIParamMemory(iParameterMemory,
                                Collections.singletonList(queryResponse));
                    }

                }
            }
        } catch (
            JsonSyntaxException exception) {
            throw new IncidentException(SdipErrorCode.INVALID_JSONARRAY_RESPONSE, LOGGER,
                exception.getMessage());
        } catch (IncidentException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IncidentException(exception, SdipErrorCode.UNKNOWN_REASON_ERROR, LOGGER,
                exception.getMessage(), SdipErrorParameter.SUPPORTMAIL);
        }
    }

    public void saveResponseToIParamMemory(IParameterMemory iParameterMemory,
        List<String> response) {
        HashMap<String, List<String>> outputMemoryValue = new HashMap<>();
        outputMemoryValue
            .put(outputParameter.get(0).getKeys().get(0), response);
        iParameterMemory
            .putParameter(outputParameter.get(0).getSubjectIri(), outputMemoryValue);
    }

    public List<String> convertQueryResponseIntoString(JsonArray queryResponse) {
        List<String> response = new ArrayList();
        for (int i = 0; i < queryResponse.size(); i++) {
            response.add(queryResponse.get(i).getAsJsonObject().toString());
        }
        return response;
    }

}
