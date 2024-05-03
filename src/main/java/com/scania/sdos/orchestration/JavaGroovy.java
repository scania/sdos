package com.scania.sdos.orchestration;

import com.scania.sdip.exceptions.IncidentException;
import com.scania.sdip.exceptions.SdipErrorCode;
import com.scania.sdip.exceptions.SdipErrorParameter;
import com.scania.sdos.utils.Utility;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.MissingPropertyException;
import groovy.lang.Script;
import java.util.HashMap;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Class to run groovy scripts
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class JavaGroovy {

  private final GroovyShell shell = null;

  private static final Logger LOGGER = LogManager.getLogger(Utility.class);

  public HashMap runGroovyShell(HashMap<String, String> bindings, String groovyScript) {

    try {
      Binding binding = new Binding();
      bindings.forEach((k, v) ->
      {
        binding.setVariable(k, v);
      });
      GroovyShell shell = getGroovyShell(binding);
      Script scrpt = shell.parse(groovyScript);
      HashMap result = (HashMap) scrpt.run();
      return result;
    } catch (MultipleCompilationErrorsException e) {
      throw new IncidentException(e, SdipErrorCode.SCRIPT_INVALID, LOGGER, e.getMessage());
    } catch (MissingPropertyException e) {
      throw new IncidentException(e, SdipErrorCode.SCRIPT_PROPERTY_MISSING, LOGGER, e.getMessage());
    } catch (ClassCastException e) {
      throw new IncidentException(e, SdipErrorCode.SCRIPT_OUTPUT_ERROR, LOGGER, e.getMessage());
    } catch (Exception e) {
      throw new IncidentException(e, SdipErrorCode.UNKNOWN_REASON_ERROR, LOGGER, e.getMessage(),
              SdipErrorParameter.SUPPORTMAIL);
    }
  }

  //LEGACY FUNCTION ONLY USED FOR AUTHSCRIPT WHICH IS STILL BLACK BOX
  public List runGroovyShellList(HashMap<String, String> bindings, String groovyScript) {

    try {
      Binding binding = new Binding();
      bindings.forEach((k, v) ->
      {
        binding.setVariable(k, v);
      });
      GroovyShell shell = getGroovyShell(binding);
      Script scrpt = shell.parse(groovyScript);
      List result = (List) scrpt.run();
      return result;
    } catch (MultipleCompilationErrorsException e) {
      throw new IncidentException(SdipErrorCode.SCRIPT_INVALID, LOGGER, e.getMessage());
    } catch (MissingPropertyException e) {
      throw new IncidentException(SdipErrorCode.SCRIPT_PROPERTY_MISSING, LOGGER, e.getMessage());
    } catch (ClassCastException e) {
      throw new IncidentException(SdipErrorCode.SCRIPT_OUTPUT_ERROR, LOGGER, e.getMessage());
    } catch (Exception e) {
      throw new IncidentException(SdipErrorCode.UNKNOWN_REASON_ERROR, LOGGER, e.getMessage(),
              SdipErrorParameter.SUPPORTMAIL);
    }
  }

  public GroovyShell getGroovyShell(Binding binding) {
    return new GroovyShell(binding);
  }
}
