package com.scania.sdos.jwt;

import com.scania.sdos.orchestration.interfaces.IParameterMemory;
import com.scania.sdos.utils.SDOSConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Base64;


public class JwtTokenUtil {

    private static final Logger LOGGER = LogManager.getLogger(JwtTokenUtil.class);

    private JwtTokenUtil(){
        //Default private constructor
    }

    public static boolean oboTokenExists(IParameterMemory iParameterMemory) {
        return iParameterMemory.getValue(SDOSConstants.OBO_TOKEN) != null;
    }

    public static String getOboToken(IParameterMemory iParameterMemory) {
        return iParameterMemory.getValue(SDOSConstants.OBO_TOKEN).get(SDOSConstants.VALUE).get(0);
    }

    public static void saveJwtToken(String token, IParameterMemory iParameterMemory){
        HashMap<String, List<String>> parameterMap = new HashMap<>();
        parameterMap.put(SDOSConstants.BEARER_TOKEN, Collections.singletonList(token));
        if (iParameterMemory.getValue(SDOSConstants.BEARER_TOKEN) == null) {
            iParameterMemory.putParameter(SDOSConstants.BEARER_TOKEN, parameterMap);
        } else {
            iParameterMemory.replaceParameter(SDOSConstants.BEARER_TOKEN, parameterMap);
        }
    }

    public static void saveOboToken(String token, IParameterMemory iParameterMemory){
        HashMap<String, List<String>> parameterMap = new HashMap<>();
        parameterMap.put(SDOSConstants.VALUE, Collections.singletonList(token));
        if (iParameterMemory.getValue(SDOSConstants.OBO_TOKEN) == null) {
            iParameterMemory.putParameter(SDOSConstants.OBO_TOKEN, parameterMap);
        } else {
            iParameterMemory.replaceParameter(SDOSConstants.OBO_TOKEN, parameterMap);
        }
    }

    /**
     * Extract the creator value from bearerToken.
     */
    public static String tokenUserId(String token) {
            LOGGER.info("call tokenUserId()");
            String jwtToken = token.substring(SDOSConstants.BEARER.length()+1);
            String[] split_string = jwtToken.split(SDOSConstants.KEY_SPLIT);
            JSONObject payload = new JSONObject(new String(Base64.getUrlDecoder().decode(split_string[1])));
            return payload.get(SDOSConstants.UNIQUE_NAME).toString();
    }
}
