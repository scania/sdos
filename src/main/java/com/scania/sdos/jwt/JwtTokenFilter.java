package com.scania.sdos.jwt;

import com.scania.sdip.exceptions.IncidentException;
import com.scania.sdip.exceptions.SdipErrorCode;
import com.scania.sdos.orchestration.ParameterMemory;
import com.scania.sdos.utils.SDOSConstants;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenFilter {
    private static final Logger LOGGER = LogManager.getLogger(JwtTokenFilter.class);

    private JwtTokenProvider jwtTokenProvider;

    /*
     * This method validate jwt bearer token .
     */
    @Autowired
    public JwtTokenFilter(JwtTokenProvider jwtTokenProvider){
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public void setJwtTokenProvider(JwtTokenProvider jwtTokenProvider) { this.jwtTokenProvider = jwtTokenProvider; }

    public JwtTokenFilter(){ }
    public Boolean validateJwtToken(String bearerToken) {
        Boolean status = false;
        if (StringUtils.startsWith(bearerToken, SDOSConstants.BEARER+" ")) {
            String jwtToken = bearerToken.substring(SDOSConstants.BEARER.length()+1);
            status = jwtTokenProvider.validateToken(jwtToken);
        } else {
            LOGGER.error("JWT Token does not begin with Bearer String");
            throw new IncidentException(SdipErrorCode.SIGNATURE_JWT_TOKEN_ERROR, LOGGER , "JWT Token does not begin with Bearer String");
        }
        return status;
    }

    /*
     * This method process jwt bearer token for valid token.
     */
    public void jwtProcess(String bearerToken, Boolean status, ParameterMemory parameterMemory) {
            if(status) {

                //Save bearerToken in parameter memory
                JwtTokenUtil.saveJwtToken(bearerToken, parameterMemory);

                String jwtToken = bearerToken.substring(SDOSConstants.BEARER.length()+1);
                //Fetch ClientId
                String appId= jwtTokenProvider.getAppIdFromToken(jwtToken);
                if(!appId.isEmpty()) {
                       fetchOBOToken(jwtToken, appId, parameterMemory);
                }
            }
    }

    /*
     * This method fetch OBO token and save OBO token in memory .
     */
    public void fetchOBOToken(String jwtToken, String appId,  ParameterMemory parameterMemory){
        String obo_response = jwtTokenProvider.callOboService(jwtToken, appId);
        String obo_token = jwtTokenProvider.getOBOToken(obo_response);
        if (jwtTokenProvider.rolesExistsInOboToken(obo_token)) {
            JwtTokenUtil.saveOboToken(obo_token, parameterMemory);
        }
    }
}
