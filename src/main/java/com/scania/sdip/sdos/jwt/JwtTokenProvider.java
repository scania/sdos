package com.scania.sdip.sdos.jwt;

import com.google.gson.JsonArray;
import com.scania.sdip.exceptions.IncidentException;
import com.scania.sdip.exceptions.SdipErrorCode;
import com.scania.sdip.sdos.model.ServiceArguments;
import com.scania.sdip.sdos.orchestration.RestTemplateClient;
import com.scania.sdip.sdos.utils.SDOSConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.json.JSONArray;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import java.io.ByteArrayInputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.PublicKey;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.io.InputStream;
import java.util.HashMap;

@Component
public class JwtTokenProvider {
    private static final Logger LOGGER = LogManager.getLogger(JwtTokenProvider.class);
    @Value("${azure.obo.jwt.grant}")
    private  String grantType;
    @Value("${azure.discover.key.url}")
    private  String discoverKeyUrl;

    private RestTemplateClient restTemplateClient;

    private ServiceArguments serviceArguments;
    @Autowired
    public JwtTokenProvider(ServiceArguments serviceArguments) {
        this.setRestTemplateClient(new RestTemplateClient());
        this.setServiceArguments(serviceArguments);
    }
    public JwtTokenProvider() { }

    public void setServiceArguments(ServiceArguments serviceArguments) { this.serviceArguments = serviceArguments; }

    public ServiceArguments getServiceArguments() { return serviceArguments; }

    public void setRestTemplateClient(RestTemplateClient restTemplateClient) {
        this.restTemplateClient = restTemplateClient;
    }



    /*
      Validating the token signature
     */
    public Boolean validateToken(String token) {
        try {
            PublicKey publicKey = getPublicKey(token);

            Jws<Claims> claims = Jwts.parserBuilder()
                    .setSigningKey(publicKey)
                    .build()
                    .parseClaimsJws(token);
            return true;

        } catch (JwtException | IllegalArgumentException | NoSuchAlgorithmException | InvalidKeySpecException |
                 CertificateException  e) {
            LOGGER.error("Error in validating JWT token ");
            throw new IncidentException(SdipErrorCode.SIGNATURE_JWT_TOKEN_ERROR, LOGGER , e.getMessage());
        }
    }

    private PublicKey getPublicKey(String token) throws NoSuchAlgorithmException, InvalidKeySpecException, CertificateException {
        String[] parts = token.split(SDOSConstants.KEY_SPLIT);
        JSONObject payload = new JSONObject(decode(parts[0]));
        String kid = payload.get(SDOSConstants.KID).toString();
        String publicKey = getKey(kid);

        publicKey = publicKey.replaceAll(SDOSConstants.NEW_LINES_PATTERN, SDOSConstants.NOT_SPACE)
                .replace(SDOSConstants.BEGIN_PUBLIC_KEY, SDOSConstants.NOT_SPACE)
                .replace(SDOSConstants.END_PUBLIC_KEY, SDOSConstants.NOT_SPACE);

        byte[] encodedCert = publicKey.getBytes(StandardCharsets.UTF_8);
        byte[] decodedCert = Base64.getDecoder().decode(encodedCert);
        CertificateFactory certFactory = CertificateFactory.getInstance(SDOSConstants.KEY_X);
        InputStream in = new ByteArrayInputStream(decodedCert);
        X509Certificate certificate = (X509Certificate)certFactory.generateCertificate(in);
        PublicKey publickey = ((RSAPublicKey)certificate.getPublicKey());

        return publickey;

    }
    public String getUsernameFromToken(String token) {
        return getPayloadFromToken(token).get(SDOSConstants.GIVEN_NAME).toString();
    }

    public boolean rolesExistsInOboToken(String token) {
        JSONObject payload = getPayloadFromToken(token);
        if(payload.has(SDOSConstants.ROLES) && payload.get(SDOSConstants.ROLES)!=null && ((JSONArray)(payload
                .get(SDOSConstants.ROLES))).length() !=0){
            return true;
        } else {
            throw new IncidentException(SdipErrorCode.MISSING_OBO_TOKEN_ERROR,LOGGER);
        }
    }
    public String getAppIdFromToken(String token) {       ;
        return getPayloadFromToken(token).get(SDOSConstants.APP_ID).toString();
    }

    private JSONObject getPayloadFromToken(String token){
        String[] parts = token.split(SDOSConstants.KEY_SPLIT);
        return new JSONObject(decode(parts[1]));
    }

    public String getOBOToken(String res) {
        JSONObject payload = new JSONObject(res);
        return payload.get(SDOSConstants.ACCESS_TOKEN).toString();
    }

    private static String decode(String encodedString) {
        return new String(Base64.getUrlDecoder().decode(encodedString));
    }

    private String getKey(String kidKey){
        String publicKey= SDOSConstants.EMPTY_STRING;

        HashMap<String, String> headers = new HashMap<>();
        headers.put(SDOSConstants.ACCEPT_HEADER, SDOSConstants.ACCEPT_HEADER_VALUE);

        String result = restTemplateClient.executeHttpGET(discoverKeyUrl, null, headers,null);

        JSONObject payload = new JSONObject(result);
        //extracting data array from json string
        JSONArray ja_data = payload.getJSONArray(SDOSConstants.KEYS);
        int length = ja_data.length();
        for(int i=0; i<length; i++) {
            JSONObject jObj = ja_data.getJSONObject(i);
            String kid = jObj.optString(SDOSConstants.KID);
            if(kid.equals(kidKey)){
                publicKey= jObj.getJSONArray(SDOSConstants.KEY_X5).get(0).toString();
            }
        }
        return publicKey;
    }

    public String callOboService(String accessToken ,String appId){
        try {
            HashMap<String, String> headers = new HashMap<>();
            headers.put(SDOSConstants.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);

            HashMap<String, String> map = new HashMap<>();
            map.put(SDOSConstants.GRANT_TYPE, grantType);
            map.put(SDOSConstants.CLIENT_ID, appId);
            map.put(SDOSConstants.CLIENT_SECRET,serviceArguments.getSdosClientSecret());
            map.put(SDOSConstants.ASSERTION,accessToken);
            map.put(SDOSConstants.SCOPE,serviceArguments.getStardogClientScope());
            map.put(SDOSConstants.TOKEN_USE,SDOSConstants.TOKEN_USE_VALUE);
            String result = restTemplateClient.executeHttpPost(serviceArguments.getAzureTenantUrl(),null,
                    headers,SDOSConstants.EMPTY_STRING,null, map);
            return result;

        } catch (Exception exception){
            throw new IncidentException(SdipErrorCode.INVALID_AUTH_TOKEN_ERROR,LOGGER, exception.getMessage());
        }
    }

}
