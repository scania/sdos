package com.scania.sdip.sdos.orchestration.model;

import com.scania.sdip.exceptions.IncidentException;
import com.scania.sdip.exceptions.SdipErrorCode;
import com.scania.sdip.exceptions.SdipErrorParameter;
import com.scania.sdip.sdos.model.ServiceArguments;
import com.scania.sdip.sdos.orchestration.Rdf4jClient;
import com.scania.sdip.sdos.orchestration.interfaces.IMetaDataModel;
import com.scania.sdip.sdos.orchestration.interfaces.IParameterMemory;
import com.scania.sdip.sdos.utils.SDOSConstants;
import com.scania.sdip.sdos.utils.StateEnum;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import com.scania.sdip.sdos.utils.Utility;
import java.time.LocalDateTime;

public class ResultMetaDataModel implements IMetaDataModel {


  private static final Logger LOGGER = LogManager.getLogger(ResultMetaDataModel.class);
  private String state;
  private String timestamp;
  private String subjectIri;
  private Rdf4jClient rdf4jClient;

  private String resultgraph;
  public static final String SPARQL_DELETE =
          SDOSConstants.PREFIX_RDF +
                  SDOSConstants.PREFIX_RDFS +
                  SDOSConstants.PREFIX_OWL +
                  "PREFIX : <" + SDOSConstants.ORCHESTRATION_PREFIX + "> \n"
                  + "PREFIX core: <" + SDOSConstants.CORE_ONTOLOGY_PREFIX + "> \n"
                  + "WITH <VAR_GRAPH> \n"
                  + "DELETE {:VAR_SUBJECT_IRI ?p ?o}\n"
                  + "WHERE {\n"
                  + " :VAR_SUBJECT_IRI  ?p ?o .\n"
                  + " :VAR_SUBJECT_IRI rdf:type :ResultMetaData\n"
                  + "  }";
  public static final String SPARQL_INSERT =
          SDOSConstants.PREFIX_RDF +
                  SDOSConstants.PREFIX_RDFS +
                  SDOSConstants.PREFIX_OWL +
                  "PREFIX : <" + SDOSConstants.ORCHESTRATION_PREFIX + "> \n"
                  + "PREFIX core: <" + SDOSConstants.CORE_ONTOLOGY_PREFIX + "> \n"
                  + "INSERT DATA\n"
                  + "{ \n"
                  + "GRAPH <VAR_GRAPH> { \n"
                  + "  :VAR_SUBJECT_IRI rdf:type :ResultMetaData ;\n"
                  + "       rdfs:label \"resultMetadata\" ;\n"
                  + "       :state \"VAR_STATE\" ;\n"
                  + "       core:timestamp \"VAR_TIMESTAMP\" .\n"
                  + "       \n"
                  + "}}";
  @Override
  public void populate(String subjectIri, IParameterMemory iParameterMemory, ServiceArguments serviceArguments) {

    setTimestamp(
            Utility.getCurrentDateAndTime(LocalDateTime.now(), SDOSConstants.TIMESTAMP_PATTERN));
    setResultgraph(
            SDOSConstants.RESULT + Utility.getCurrentDateAndTime(LocalDateTime.now(),
                    SDOSConstants.RESULTGRAPH_PATTERN));
    LOGGER.info("In ResultMetaDataModel populate timestamp {}", timestamp);
  }
  public void run(IParameterMemory iParameterMemory, JsonLdContextModel context,
                  ServiceArguments serviceArguments) {
    try {
      rdf4jClient.executeUpdateSparql(getSparql_DELETE(),
              serviceArguments.getStardogResultEndpoint()
                      + SDOSConstants.UPDATE, iParameterMemory);
      rdf4jClient.executeUpdateSparql(getSparql_INSERT(),
              serviceArguments.getStardogResultEndpoint()
                      + SDOSConstants.UPDATE, iParameterMemory);
    } catch (IncidentException exception) {
      throw exception;
    } catch (Exception exception) {
      throw new IncidentException(exception, SdipErrorCode.UNKNOWN_REASON_ERROR, LOGGER,
              exception.getMessage(), SdipErrorParameter.SUPPORTMAIL);
    }
  }

  @Autowired
  public ResultMetaDataModel() {

    this.rdf4jClient = new Rdf4jClient();
    UUID uuid = UUID.randomUUID();
    setSubjectIri(uuid.toString());
    setState(StateEnum.INCOMPLETE.toString());
  }


  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public String getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(String timestamp) {
    this.timestamp = timestamp;
  }

  public String getSubjectIri() {
    return subjectIri;
  }

  private void setSubjectIri(String subjectIri) {
    this.subjectIri = subjectIri;
  }

  public Rdf4jClient getRdf4jClient() {
    return rdf4jClient;
  }

  public void setRdf4jClient(Rdf4jClient rdf4jClient) {
    this.rdf4jClient = rdf4jClient;
  }

  private String getSparql_INSERT() {
    String sparqlUpdate = SPARQL_INSERT.replaceAll(SDOSConstants.VAR_GRAPH, getResultgraph());
    sparqlUpdate = sparqlUpdate.replaceAll(SDOSConstants.VAR_SUBJECT_IRI, getSubjectIri());
    sparqlUpdate = sparqlUpdate.replaceAll(SDOSConstants.VAR_STATE, getState());
    sparqlUpdate = sparqlUpdate.replaceAll(SDOSConstants.VAR_TIMESTAMP, getTimestamp());
    return sparqlUpdate;
  }

  private String getSparql_DELETE() {
    String sparqlUpdate = SPARQL_DELETE.replaceAll(SDOSConstants.VAR_GRAPH, getResultgraph());
    sparqlUpdate = sparqlUpdate.replaceAll(SDOSConstants.VAR_SUBJECT_IRI, getSubjectIri());
    return sparqlUpdate;
  }

  public String getResultgraph() {
    return resultgraph;
  }

  public void setResultgraph(String resultgraph) {
    this.resultgraph = resultgraph;
  }

  @Override
  public String getLabel() {
    return null;
  }
}