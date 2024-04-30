package com.scania.sdip.sdos.orchestration.model;

import com.google.gson.JsonArray;
import com.scania.sdip.exceptions.IncidentException;
import com.scania.sdip.exceptions.SdipErrorCode;
import com.scania.sdip.exceptions.SdipErrorParameter;
import com.scania.sdip.sdos.jwt.JwtTokenUtil;
import com.scania.sdip.sdos.model.ServiceArguments;
import com.scania.sdip.sdos.orchestration.Rdf4jClient;
import com.scania.sdip.sdos.orchestration.interfaces.IMetaDataModel;
import com.scania.sdip.sdos.orchestration.interfaces.IParameterMemory;
import com.scania.sdip.sdos.utils.SDOSConstants;
import com.scania.sdip.sdos.utils.Utility;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MetaDataModel implements IMetaDataModel {

  public static final String SPARQL =
          SDOSConstants.PREFIX_RDF +
                  SDOSConstants.PREFIX_RDFS +
                  "PREFIX : <" + SDOSConstants.ORCHESTRATION_PREFIX + "> \n"
                  + "PREFIX core: <" + SDOSConstants.CORE_ONTOLOGY_PREFIX + "> \n"
                  + "SELECT ?contributor ?description ?graphType ?title ?label ?informationResponsible\n"
                  + "WHERE \n"
                  + "{\n"
                  + "BIND(<" + SDOSConstants.VARIABLE + "> AS ?subject)"
                  + "\t?subject  rdf:type :ResultMetaData;\n"
                  + "\t        rdfs:label ?label ;\n"
                  + "\t        :description ?description;\n"
                  + "\t        core:graphType ?graphType;\n"
                  + "\t        :title ?title.\n"
                  + "\t  OPTIONAL{?subject core:informationResponsible ?informationResponsible .}\t\n"
                  + "    OPTIONAL{?subject core:contributor ?contributor .}\n"
                  + "}";
  public static final String SPARQL_INSERT =
          SDOSConstants.PREFIX_RDF +
                  SDOSConstants.PREFIX_RDFS +
                  SDOSConstants.PREFIX_OWL +
                  "PREFIX : <" + SDOSConstants.ORCHESTRATION_PREFIX + "> \n"
                  + "PREFIX core: <" + SDOSConstants.CORE_ONTOLOGY_PREFIX + "> \n"
                  + "INSERT DATA\n"
                  + "{ \n"
                  + "GRAPH <VAR_GRAPH> { \n"
                  + "  <VAR_SUBJECT_IRI> rdf:type core:Metadata ;\n"
                  + "       rdfs:label \"MetaData\" ;\n"
                  + "       core:graphType \"GRAPH_TYPE\" ;\n"
                  + "       core:creator \"VAR_CREATOR\" ;\n"
                  + "       core:contributor \"VAR_CONTRIBUTOR\" ;\n"
                  + "       core:informationResponsible \"VAR_INFORMATION_RESPONSIBLE\" ;\n"
                  + "       :description \"VAR_DESCRIPTION\" .\n"
                  + "       \n"
                  + "}}";
  private static final Logger LOGGER = LogManager.getLogger(MetaDataModel.class);
  private String contributor;
  private String creator;
  private String description;
  private String graphType;
  private String title;
  private String subjectIri;
  private Rdf4jClient rdf4jClient;
  private String label;
  private String informationResponsible;
  public MetaDataModel() {
    this.rdf4jClient = new Rdf4jClient();

  }

  public void setRdf4jClient(Rdf4jClient rdf4jClient) {
    this.rdf4jClient = rdf4jClient;
  }

  @Override
  public String getSubjectIri() {
    return subjectIri;
  }

  public void setSubjectIri(String subjectIri) {
    this.subjectIri = subjectIri;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }
  public String getInformationResponsible() {
    return informationResponsible;
  }
  public void setInformationResponsible(String informationResponsible) {
    this.informationResponsible = informationResponsible;
  }


  private String getSparql(String subject, String sparql) {
    return sparql.replace(SDOSConstants.VARIABLE, subject);
  }

  public String getContributor() {
    return contributor;
  }

  public void setContributor(String contributor) {
    this.contributor = contributor;
  }

  public String getCreator() {
    return creator;
  }

  public void setCreator(String creator) {
    this.creator = creator;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getGraphType() {
    return graphType;
  }

  public void setGraphType(String graphType) {
    this.graphType = graphType;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public void populate(String subjectIri, IParameterMemory iParameterMemory,
                       ServiceArguments serviceArguments) {
    LOGGER.info("In MetaDataModel populate()");
    try {
      setSubjectIri(subjectIri);

      JsonArray jsonArray = rdf4jClient
              .selectSparqlOfg(getSparql(subjectIri, SPARQL), iParameterMemory.getOfgModelRepo());
      processJsonArray(jsonArray);
      String token = iParameterMemory.getValue(SDOSConstants.BEARER_TOKEN).get(SDOSConstants.BEARER_TOKEN).get(0);
      setCreator(JwtTokenUtil.tokenUserId(token));

      setDescription(
              jsonArray.get(0).getAsJsonObject().get(SDOSConstants.DESCRIPTION).getAsJsonObject()
                      .get(SDOSConstants.VALUE)
                      .getAsString());
      String graphType = jsonArray.get(0).getAsJsonObject().get(SDOSConstants.GRAPH_TYPE)
              .getAsJsonObject()
              .get(SDOSConstants.VALUE)
              .getAsString();
      LOGGER.info("In MetaDataModel populate() graphType {} ", graphType);
      if (Utility.checkGraphType(graphType)) {
        setGraphType(graphType);

      } else {
        throw new IncidentException(SdipErrorCode.UNKNOWN_PARSING_ERROR, LOGGER,
                SdipErrorParameter.UNSUPPORTED_GRAPH_TYPE);
      }

      setFields(jsonArray);

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

  private void processJsonArray(JsonArray jsonArray){
    if (jsonArray.get(0).getAsJsonObject().has(SDOSConstants.CONTRIBUTOR)) {
      setContributor(
              jsonArray.get(0).getAsJsonObject().get(SDOSConstants.CONTRIBUTOR).getAsJsonObject()
                      .get(SDOSConstants.VALUE)
                      .getAsString());
    }
  }

  private void  setFields(JsonArray jsonArray){
    setTitle(jsonArray.get(0).getAsJsonObject().get(SDOSConstants.TITLE).getAsJsonObject()
            .get(SDOSConstants.VALUE)
            .getAsString());
    setLabel(jsonArray.get(0).getAsJsonObject().get(SDOSConstants.LABEL).getAsJsonObject()
            .get(SDOSConstants.VALUE)
            .getAsString());
    setInformationResponsible(jsonArray.get(0).getAsJsonObject().get(SDOSConstants.INFORMATION_RESPONSIBLE).getAsJsonObject()
            .get(SDOSConstants.VALUE)
            .getAsString());
    setDescription(jsonArray.get(0).getAsJsonObject().get(SDOSConstants.DESCRIPTION).getAsJsonObject()
            .get(SDOSConstants.VALUE)
            .getAsString());

  }
  public void run(IParameterMemory iParameterMemory, JsonLdContextModel context,
                  ServiceArguments serviceArguments
  ) {
    try {
      rdf4jClient.executeUpdateSparql(getSparql_INSERT(iParameterMemory),
              serviceArguments.getStardogResultEndpoint()
                      + SDOSConstants.UPDATE, iParameterMemory);
    } catch (IncidentException exception) {
      throw exception;
    } catch (Exception exception) {
      throw new IncidentException(exception, SdipErrorCode.UNKNOWN_REASON_ERROR, LOGGER,
              exception.getMessage(), SdipErrorParameter.SUPPORTMAIL);
    }
  }

  public String getSparql_INSERT(IParameterMemory iParameterMemory) {

    String sparqlUpdate = SPARQL_INSERT.replaceAll(SDOSConstants.VAR_GRAPH,
            iParameterMemory.getValue(SDOSConstants.EXECUTION_REPORT).get(SDOSConstants.ID).get(0));
    sparqlUpdate = sparqlUpdate.replaceAll(SDOSConstants.VAR_SUBJECT_IRI, getSubjectIri());
    sparqlUpdate = sparqlUpdate.replaceAll(SDOSConstants.FLOW_GRAPH_TYPE, getGraphType());
    sparqlUpdate = sparqlUpdate.replaceAll(SDOSConstants.VAR_CREATOR, getCreator());
    sparqlUpdate = sparqlUpdate.replaceAll(SDOSConstants.VAR_CONTRIBUTOR, getContributor());
    sparqlUpdate = sparqlUpdate.replaceAll(SDOSConstants.VAR_INFORMATION_RESPONSIBLE, getInformationResponsible());
    sparqlUpdate = sparqlUpdate.replaceAll(SDOSConstants.VAR_DESCRIPTION, getDescription());
    return sparqlUpdate;
  }

}
