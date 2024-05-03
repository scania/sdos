package com.scania.sdos.orchestration.model;

import com.google.gson.JsonArray;
import com.scania.sdip.exceptions.IncidentException;
import com.scania.sdip.exceptions.SdipErrorCode;
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

public class SparqlQueryParameterModel implements IParameterModel {

    private static final Logger LOGGER = LogManager.getLogger(SparqlQueryParameterModel.class);

    public static final String SPARQL =
        SDOSConstants.PREFIX_RDF +
            SDOSConstants.PREFIX_RDFS +
            "PREFIX : <" + SDOSConstants.ORCHESTRATION_PREFIX + "> \n" +
            "SELECT" +
            " ?" + SDOSConstants.QUERY +
            " ?" + SDOSConstants.LABEL + "\n" +
            "WHERE\n" +
            "{ \n" +
            "BIND(<" + SDOSConstants.VARIABLE + "> AS ?subject)\n" +
            "?subject rdfs:label ?" + SDOSConstants.LABEL + ";\n" +
                    "         :query ?" + SDOSConstants.QUERY + ".\n" +
                    "}";



    public SparqlQueryParameterModel() {
        this.setRdf4jClient(new Rdf4jClient());
    }

    private String label;
    private String subjectIri;
    private Rdf4jClient rdf4jClient;
    private String query;

    public void setLabel(String label) { this.label = label; }

    public void setSubjectIri(String subjectIri) {  this.subjectIri = subjectIri;  }

    public Rdf4jClient getRdf4jClient() { return rdf4jClient; }

    public void setRdf4jClient(Rdf4jClient rdf4jClient) { this.rdf4jClient = rdf4jClient; }

    public String getQuery() { return query; }

    public void setQuery(String query) { this.query = query; }

    @Override
    public String getSubjectIri() { return subjectIri; }

    @Override
    public String getLabel() { return label; }

    @Override
    public List<String> getKeys() {
        List<String> params = new ArrayList<>();
        params.add(SDOSConstants.QUERY);
        return params;
    }

    @Override
    public HashMap<String, List<String>> getValue() {
        HashMap<String, List<String>> hashMap = new HashMap<>();
        if (!getQuery().isEmpty()) {
            hashMap.put(SDOSConstants.QUERY, Collections.singletonList(getQuery()));
        }
        return hashMap;
    }

    @Override
    public JsonArray createUserInputHelp() {
        return Utility.getStaticJsonArray();
    }

    /**
     * populate model
     *
     * @param subjectIri       of subject
     * @param iParameterMemory hashmap of parameters
     * @param serviceArguments
     */
    @Override
    public void populate(String subjectIri, IParameterMemory iParameterMemory,
        ServiceArguments serviceArguments) {
        doPopulate(subjectIri, iParameterMemory);
    }

    @Override
    public void populateHelp(String subjectIri, IParameterMemory iParameterMemory,
        ServiceArguments serviceArguments) {
        doPopulate(subjectIri, iParameterMemory);
    }

    private void doPopulate(String subjectIri, IParameterMemory iParameterMemory) {
        try {
            setSubjectIri(subjectIri);
            JsonArray taskJsonArray = rdf4jClient.selectSparqlOfg(getSparql(subjectIri, SPARQL), iParameterMemory.getOfgModelRepo());

            setLabel(taskJsonArray.get(0).getAsJsonObject().get(SDOSConstants.LABEL)
                .getAsJsonObject().get(SDOSConstants.VALUE).getAsString());
            setQuery(taskJsonArray.get(0).getAsJsonObject().get(SDOSConstants.QUERY)
                .getAsJsonObject().get(SDOSConstants.VALUE).getAsString());
            if (iParameterMemory.getValue(getSubjectIri())
                == null) {
                iParameterMemory.putParameter(getSubjectIri(), getValue());
            }

        } catch (IllegalStateException | IndexOutOfBoundsException exception) {
            throw new IncidentException(exception, Utility.getErrorMessage(exception, label,
                subjectIri), SdipErrorCode.INVALID_JSONARRAY_RESPONSE, LOGGER,
                exception.getMessage());
        } catch (NullPointerException exception) {
            throw new IncidentException(exception, Utility.getErrorMessage(exception, label,
                subjectIri), SdipErrorCode.FAILED_TO_PARSE_JSONARRAY, LOGGER,
                exception.getMessage());
        } catch (IncidentException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IncidentException(exception, Utility.getErrorMessage(exception, label,
                subjectIri), SdipErrorCode.UNKNOWN_PARSING_ERROR, LOGGER,
                exception.getMessage());
        }
    }

    private String getSparql(String subject, String sparql) {
        return sparql.replace(SDOSConstants.VARIABLE, subject);
    }
}
