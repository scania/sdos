package com.scania.sdip.sdos.utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.scania.sdip.exceptions.IncidentException;
import com.scania.sdip.exceptions.SdipErrorCode;
import com.scania.sdip.sdos.orchestration.Rdf4jClient;
import com.scania.sdip.sdos.orchestration.interfaces.IParameterMemory;
import com.scania.sdip.sdos.orchestration.interfaces.IParameterModel;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.scania.sdip.sdos.orchestration.model.JsonLdContextModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.rdf4j.model.Model;

import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.UnsupportedRDFormatException;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.BasicWriterSettings;
import org.eclipse.rdf4j.rio.helpers.JSONLDMode;
import org.eclipse.rdf4j.rio.helpers.JSONLDSettings;


/**
 * The Utility class provides various utility methods used by the service.
 */
public class Utility {

  private static final Logger LOGGER = LogManager.getLogger(Utility.class);
  private static final Gson gson = new Gson();
  private static final HashMap map = new HashMap();
  private static final String EMPTY = "";
  private static final JsonArray jsonArray = new JsonArray();
  private static final JsonLdContextModel jsonLdContextModel = new JsonLdContextModel();

  private Utility() {
  }

  public static boolean regexMatchCheck(String str, String regex) {
    Matcher matcher = Pattern.compile(regex).matcher(str);
    return matcher.find();
  }

  public static String getRegexMatch(String str, String regex) {
    Matcher m2 = Pattern.compile(regex).matcher(str.trim());
    if (m2.find()) {
      return m2.group();
    }
    return null;
  }

  public static String getRegexMatchByGroup(String str, String regex, Integer groupNumber) {
    Matcher m2 = Pattern.compile(regex).matcher(str.trim());
    if (m2.find()) {
      return m2.group(groupNumber);
    }
    return null;
  }

  /**
   * Get File from resource as a inputStream first method works in IDE but not in jar, and the
   * second one is inverse
   *
   * @param relativePath
   * @return inputstream
   */
  public static InputStream getFileFromResourcesAsInputstream(String relativePath) {
    InputStream inputStream = ClassLoader
        .getSystemResourceAsStream(relativePath.replaceFirst("/", ""));
    if (inputStream == null) {
      inputStream = Utility.class.getClassLoader()
          .getResourceAsStream(relativePath);
    }
    return inputStream;
  }

  public static Integer getActionIteration(HashMap<String, List<String>> memoryValue,
      IParameterModel iParameterModel) {
    Integer iterations = 0;
    for (String key : iParameterModel.getKeys()) {
      if (iterations < memoryValue.get(key).size()) {
        iterations = memoryValue.get(key).size();
      }
    }
    return iterations;
  }

  public static String getCurrentDateAndTime(LocalDateTime time) {
    DateTimeFormatter dateAndTimeFormat = DateTimeFormatter.ofPattern(
        SDOSConstants.RESULTGRAPH_PATTERN);
    return dateAndTimeFormat.format(time);
  }

  public static String getCurrentDateAndTime(LocalDateTime time, String pattern) {
    DateTimeFormatter dateAndTimeFormat = DateTimeFormatter.ofPattern(pattern);
    return dateAndTimeFormat.format(time);
  }

  public static String getUUID4String() {
    return UUID.randomUUID().toString();
  }

  public static String getErrorMessage(Exception exception, String label, String subjectIri) {
    StackTraceElement stackTraceElement = null;
    String errorMessage = "";
    for (StackTraceElement stack : exception.getStackTrace()) {
      if (stack.getClassName().startsWith(SDOSConstants.STACKTRACE_CHECK) &&
          stackTraceElement == null) {
        stackTraceElement = stack;
        break;
      }
    }

    if (stackTraceElement != null && !stackTraceElement.equals("")) {
      if (label != null && !label.equals("")) {
        errorMessage = label + ", ";
      }
      if (subjectIri != null && !subjectIri.equals("")) {
        errorMessage = errorMessage + subjectIri;
      }
      errorMessage = errorMessage + " (class-" + stackTraceElement.getFileName().
          replace(".java", "") + ", method-" + stackTraceElement
          .getMethodName() + ", line-" + stackTraceElement.getLineNumber() + "), ";
    }
    return errorMessage;
  }

  public static String modelToJsonLd(Model model) {
    return getModelAsString(RDFFormat.JSONLD, model);
  }

  public static String modelToRdfXml(Model model) {
    return getModelAsString(RDFFormat.RDFXML, model);
  }

  public static String modelToNTriples(Model model) {
    return getModelAsString(RDFFormat.NTRIPLES, model);
  }

  public static String modelToTurtle(Model model) {
    return getModelAsString(RDFFormat.TURTLE, model);
  }

  public static String modelToN3(Model model) {
    return getModelAsString(RDFFormat.N3, model);
  }

  public static String modelToNQuads(Model model) {
    return getModelAsString(RDFFormat.NQUADS, model);
  }

  private static String getModelAsString(RDFFormat rdfFormat, Model model) {
    StringWriter stringWriter = new StringWriter();
    RDFWriter rdfWriter = Rio.createWriter(rdfFormat, stringWriter);
    if (rdfFormat == RDFFormat.JSONLD) {
      rdfWriter.getWriterConfig().set(JSONLDSettings.JSONLD_MODE, JSONLDMode.COMPACT);
      rdfWriter.getWriterConfig().set(JSONLDSettings.OPTIMIZE, true);
      rdfWriter.getWriterConfig().set(BasicWriterSettings.PRETTY_PRINT, true);
    }
    Rio.write(model, rdfWriter);
    return stringWriter.toString();
  }

  public static HashMap getStaticHashmap() {
    return map;
  }

  public static String getStaticString() {
    return EMPTY;
  }

  public static JsonArray getStaticJsonArray() {
    return jsonArray;
  }

  /**
   * Validate the MetaData Graph Type.
   */
  public static boolean checkGraphType(String graphType) {
    boolean typeExist=false;
    for (GraphType type : GraphType.values()) {
      if(type.name().equalsIgnoreCase(graphType)){
        typeExist=true;
      }
    }
    return typeExist;
  }

  /**
   * Return the JsonLdContextModel instance.
   */
  public static JsonLdContextModel getStaticJsonLdContextModel() {
    return jsonLdContextModel;
  }
  public static Model  getJsonldData(String value){
    Model result;
    try {
      InputStream ofgJsonLdStream = new ByteArrayInputStream(
              value.getBytes(SDOSConstants.UTF_8));
      result = Rio.parse(ofgJsonLdStream, RDFFormat.JSONLD);
    }catch (RDFParseException | UnsupportedRDFormatException exception) {
      throw new IncidentException(exception, SdipErrorCode.RDF_STORE_RESPONSE_NOT_JSON_LD, LOGGER,
              exception.getMessage());
    } catch (IOException exception) {
      throw new IncidentException(exception, SdipErrorCode.IO_ERROR, LOGGER,
              exception.getMessage());
    }
    return result;
  }

}
