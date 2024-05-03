package com.scania.sdos.testUtils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.junit.Assert;

public class UnitTestHelper {
  
  private static Gson gson = new Gson();

  public static String readJsonFiles(String fileName) throws NullPointerException {

    File file = null;
    try {
      URL res = UnitTestHelper.class.getClassLoader().getResource(fileName);
      file = Paths.get(res.toURI()).toFile();
    } catch (NullPointerException | URISyntaxException e) {
      Assert.fail("Exception in loading readJsonFiles " + e.getMessage());
    }
    String absolutePath = file.getAbsolutePath();

    StringBuilder contentBuilder = new StringBuilder();
    try (Stream<String> stream = Files.lines(Paths.get(absolutePath), StandardCharsets.UTF_8)) {
      stream.forEach(s -> contentBuilder.append(s).append("\n"));
    } catch (IOException e) {
      Assert.fail("IOException in readJsonFiles " + e.getMessage());
    }

    return contentBuilder.toString();
  }

  public static JsonArray getResponse(String fileName) {
    String response = readJsonFiles(fileName);
    return JsonParser.parseString(response).getAsJsonArray();
  }

  public static Model getLibraryModelForTest() {
    return toModel(UnitTestConstants.LIBRARY_JSON_LD, RDFFormat.JSONLD);
  }

  public static Model toModel(String rdf, RDFFormat format) {
    Model result;
    try {
      InputStream inputStream = new ByteArrayInputStream(rdf.getBytes("UTF-8"));
      result = Rio.parse(inputStream, format);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return result;
  }
}
