package org.egov.dx.service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.AESDecrypter;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.oauth2.sdk.ResponseType;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.State;
import com.nimbusds.oauth2.sdk.pkce.CodeChallengeMethod;
import com.nimbusds.oauth2.sdk.pkce.CodeVerifier;
import com.nimbusds.openid.connect.sdk.AuthenticationRequest;
import com.nimbusds.openid.connect.sdk.Nonce;
import com.nimbusds.openid.connect.sdk.OIDCScopeValue;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.dx.repository.EPramaanMapper;
import org.egov.dx.util.Configurations;
import org.egov.dx.web.models.*;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.URI;
import java.util.Base64;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.util.*;

@Service
@Slf4j
public class EPramaanRequestService {


    private static final User User = null;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private Configurations configurations;

    @Autowired
    private EPramaanMapper ePramaanMapper;

    private static Map<String, EPramaanData> stateCodeMap = new HashMap<>();


    public AuthResponse getRedirectionURL(String module) throws Exception
    {

        String authGrantRequestUri = configurations.getEpAuthGrantRequestUri();
        String clientId = configurations.getEpClientId();
        String redirectUri = configurations.getEpRedirectUri();
        String scopeConfig = configurations.getEpScope();
        String responseTypeConfig = configurations.getEpResponseType();
        String codeChallengeMethodConfig = configurations.getEpCodeChallengeMethod();

        validateConfig("epramaan.authGrantRequestUri", authGrantRequestUri);
        validateConfig("epramaan.clientId", clientId);
        validateConfig("epramaan.redirectUri", redirectUri);
        validateConfig("epramaan.scope", scopeConfig);
        validateConfig("epramaan.responseType", responseTypeConfig);
        validateConfig("epramaan.codeChallengeMethod", codeChallengeMethodConfig);

//        TODO : Remove this log after testing
        log.info("Preparing ePramaan authorization request. authGrantRequestUri={}, clientIdHash={}, redirectUri={}, scope={}, responseType={}, codeChallengeMethod={}",
                authGrantRequestUri, clientId, redirectUri, scopeConfig, responseTypeConfig, codeChallengeMethodConfig);

        //1. save codeVerifier, stateID, nonce in db
        State stateID = new State(UUID.randomUUID().toString());
        Nonce nonce = new Nonce();
        CodeVerifier codeVerifier = new CodeVerifier();

//        TODO : Remove this log after testing
        log.debug("Generated PKCE parameters. state={}, nonce={}, codeVerifier={}", stateID.getValue(), nonce.getValue(), codeVerifier.getValue());

        Scope scope = new Scope();
        scope.add(OIDCScopeValue.OPENID);

        AuthenticationRequest authenticationRequest =
                new AuthenticationRequest.Builder(URI.create(authGrantRequestUri), new ClientID(clientId))
                        .scope(scope)
                        .state(stateID)
                        .redirectionURI(URI.create(redirectUri))
                        .endpointURI(URI.create(authGrantRequestUri))
                        .codeChallenge(codeVerifier, CodeChallengeMethod.parse(codeChallengeMethodConfig))
                        .nonce(nonce)
                        .responseType(new ResponseType(responseTypeConfig)).build();

        String inputValue = clientId + configurations.getEpAesKey() + stateID + nonce + redirectUri
                + scopeConfig + authenticationRequest.getCodeChallenge();
        String apiHmac = hashHMACHex(inputValue, configurations.getEpAesKey());

//        TODO : Remove this log after testing
        log.debug("Computed apiHmac for state {}: {}", stateID.getValue(), apiHmac);
        String finalUrl = authenticationRequest.toURI().toString() + "&apiHmac=" + apiHmac;
        //return finalUrl;
        UriComponents uriComponents = UriComponentsBuilder
                .fromHttpUrl(finalUrl)
                .build();

        EPramaanData ePramaanData = EPramaanData.builder()
                .codeVerifier(codeVerifier.getValue())
                .nonce(nonce.getValue())
                .state(stateID.getValue())
                .build();

        AuthResponse authResponse = AuthResponse.builder()
                .redirectURL(uriComponents.toUri().toString())
                .epramaanData(ePramaanData).build();

        stateCodeMap.put(stateID.getValue(), ePramaanData);
        log.info("Generated ePramaan redirect URL for state={} and module={}", stateID.getValue(), module);
        log.debug("ePramaan redirect URL: {}", authResponse.getRedirectURL());

        return authResponse;
    }

    private void validateConfig(String propertyName, String value) {
        if (value == null || value.trim().isEmpty()) {
            log.error("Missing or empty configuration value for {}", propertyName);
            throw new IllegalArgumentException("Configuration property " + propertyName + " must not be null or empty");
        }
    }

    private static String hashHMACHex(String inputValue, String hMACKey) throws Exception {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(hMACKey.getBytes(StandardCharsets.US_ASCII), "HmacSHA256");
        sha256_HMAC.init(secret_key);
        return Base64.getUrlEncoder().encodeToString(sha256_HMAC.doFinal(inputValue.getBytes(StandardCharsets.US_ASCII)));
    }

    public EPramaanTokenRes getToken(TokenReq tokenReq) {

        EPramaanData ePramaanData = tokenReq.getEPramaanData();

        JSONObject data = new JSONObject();
        data.put("code", new String[] { tokenReq.getCode() });
        data.put("grant_type", new String[] {configurations.getEpGrantType() });
        data.put("scope", new String[] { configurations.getEpScope() });
        data.put("redirect_uri", new String[] {configurations.getEpTokenRequestUri()  });
        data.put("request_uri", new String[] {configurations.getEpRedirectUri() });
        data.put("code_verifier", new String[] { ePramaanData.getCodeVerifier() });
        data.put("client_id", new String[] {configurations.getEpClientId() });
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<String>(data.toString(), headers);
        log.info("Request to ePramaan Token API: " + data.toString());
        ResponseEntity<String> responseData = restTemplate.exchange(configurations.getEpTokenRequestUri(), HttpMethod.POST, entity, String.class);
        log.info("Response from ePramaan Token API: " + responseData.getBody());
        String jweToken = responseData.getBody();
        SecretKeySpec secretKeySpec = null;
        try {
            secretKeySpec = (SecretKeySpec) generateAES256Key(ePramaanData.getNonce());
        } catch (Exception e) {
            log.error("Error in generating AES key: ", e);
            throw new RuntimeException(e);
        }
        JWEObject jweObject = null;
        try {
            jweObject = JWEObject.parse(jweToken);
        } catch (ParseException e) {
            log.error("Error in parsing JWE token: ", e);
            throw new RuntimeException(e);
        }
        try {
            jweObject.decrypt(new AESDecrypter(secretKeySpec));
        } catch (JOSEException e) {
            log.error("Error in decrypting JWE token: ", e);
            throw new RuntimeException(e);
        }
        SignedJWT signedJWT = jweObject.getPayload().toSignedJWT();
        JWSVerifier jwsVerifier = null;
        try {
            jwsVerifier = new RSASSAVerifier((RSAPublicKey) getPublicKey());
        } catch (Exception e) {
            log.error("Error in getting public key: ", e);
            throw new RuntimeException(e);
        }
        boolean signatureVerified = false;
        try {
            signatureVerified = signedJWT.verify(jwsVerifier);
        } catch (JOSEException e) {
            log.error("Error in verifying JWS signature: ", e);
            throw new RuntimeException(e);
        }
        Map<String, Object> objectObjectMap = null;
        if (signatureVerified) {
            Map<String, Object> JWS = signedJWT.getPayload().toJSONObject();
            System.out.println("JWT: " + JWS);
            objectObjectMap = signedJWT.getPayload().toJSONObject();
            log.info("Token Response from ePramaan: " + objectObjectMap);
        }
        if(objectObjectMap == null) {
            throw new RuntimeException("Invalid ePramaan token");
        }
        EPramaanTokenRes ePramaanTokenRes = ePramaanMapper.mapClaimsToResponse(objectObjectMap);
        return ePramaanTokenRes;
    }

    public Key generateAES256Key(String seed) throws Exception {
        MessageDigest sha256 = null;
        sha256 = MessageDigest.getInstance("SHA-256");
        byte[] passBytes = seed.getBytes();
        byte[] passHash = sha256.digest(passBytes);
        SecretKeySpec secretKeySpec = new SecretKeySpec(passHash, "AES");
        return secretKeySpec;
    }

    /**
     * Get public key from certificate for JWT signature verification
     * Certificate value is read from application.properties
     * 
     * @return PublicKey for JWT verification
     * @throws Exception if certificate parsing fails
     */
    public PublicKey getPublicKey() throws Exception {
        String certificateValue = configurations.getEpCertificateValue();
        if (certificateValue == null || certificateValue.isEmpty()) {
            throw new IllegalArgumentException("epramaan.certificate.value is not configured");
        }

        String normalizedCertificate = certificateValue.replace("\\n", "\n");
        CertificateFactory certFac = CertificateFactory.getInstance("X.509");
        try (ByteArrayInputStream certStream = new ByteArrayInputStream(normalizedCertificate.getBytes(StandardCharsets.UTF_8))) {
            X509Certificate cer = (X509Certificate) certFac.generateCertificate(certStream);
            return cer.getPublicKey();
        }
    }

    public Object getOauthToken(RequestInfo requestinfo , EPramaanTokenRes tokenRes)
    {
        UserRequest user = new UserRequest();
        user.setMobileNumber(tokenRes.getMobileNumber());
        user.setName(tokenRes.getName());
        
        // Set generic SSO fields
        user.setSsoId(tokenRes.getEpramaanId());
        user.setSsoType("EPRAMAAN"); // Set SSO type as EPRAMAAN
        
        // Also set digilockerid for backward compatibility
        user.setDigilockerid(tokenRes.getEpramaanId());
        user.setTenantId(configurations.getStateLevelTenantId());
        user.setAccess_token(tokenRes.getSessionId());
        //user.setDob(tokenRes.getDob());

        CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest.setRequestInfo(requestinfo);
        createUserRequest.setUser(user);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Use the new generic SSO endpoint with ssoType parameter
        String url = configurations.getUserHost() + configurations.getUserSsoEndpoint() + "?ssoType=EPRAMAAN";
        log.info("Calling SSO endpoint: {}", url);
        
        // Use autowired restTemplate for user service call (typically HTTP, not HTTPS)
        Object userOauth = this.restTemplate.postForEntity(url, createUserRequest, Object.class).getBody();
        log.info("Received user object from user service: {}", userOauth.toString());
        return userOauth;
    }

    /**
     * Generate ePramaan logout form data for frontend to submit
     * As per ePramaan SLO specification, frontend submits form with "data" key containing JSON
     * 
     * @param sessionId - ePramaan session ID from JWT token
     * @param sub - Subject from JWT token (ePramaan user identifier)
     * @param tenantId - Tenant ID
     * @return EPramaanLogoutFormData containing all fields for form submission
     */
    public EPramaanLogoutFormData generateEPramaanLogoutFormData(String sessionId, String sub, String tenantId) throws Exception {
        String logoutRequestId = UUID.randomUUID().toString();
        String clientId = configurations.getEpClientId();
        String iss = configurations.getEpIss();
        String redirectUrl = configurations.getEpServiceLogoutUri();
        String customParameter = "UPYOG-Logout";
        
        log.info("ePramaan logout clientId: [{}], sessionId: [{}], iss: [{}], logoutRequestId: [{}], sub: [{}], redirectUrl: [{}]", clientId, sessionId, iss, logoutRequestId, sub, redirectUrl);
        // Generate HMAC: input = clientId + sessionId + iss + logoutRequestId + sub + redirectUrl, key = logoutRequestId
        String inputValue = clientId + sessionId + iss + logoutRequestId + sub + redirectUrl;
        String hmac = hashHMACHex(inputValue, logoutRequestId);

        return EPramaanLogoutFormData.builder()
            .sub(sub)
            .clientId(clientId)
            .redirectUrl(redirectUrl)
            .logoutRequestId(logoutRequestId)
            .hmac(hmac)
            .iss(iss)
            .customParameter(customParameter)
            .sessionId(sessionId)
            .build();
    }

    /**
     * Add ePramaan session info (sessionId and sub) to the OAuth response
     * This allows frontend to store these values for later use in logout
     * 
     * @param userOauthResponse - OAuth response from user service
     * @param tokenRes - ePramaan token response containing sessionId and sub
     * @return Modified OAuth response with sessionId and sub added
     */
    @SuppressWarnings("unchecked")
    public Object addEPramaanUserSessionInfo(Object userOauthResponse, EPramaanTokenRes tokenRes) {
        try {
            log.info("Adding ePramaan session info (sessionId and sub) to OAuth response");
            
            if (userOauthResponse instanceof Map) {
                Map<String, Object> responseMap = (Map<String, Object>) userOauthResponse;
                
                responseMap.put("sessionId", tokenRes.getSessionId());
                responseMap.put("sub", tokenRes.getSub());
                
                log.info("Successfully added - sessionId: {}, sub: {}", 
                    tokenRes.getSessionId(), tokenRes.getSub());
                
                return responseMap;
            } else {
                log.warn("OAuth response is not a Map (Type: {}), cannot add ePramaan session info", 
                    userOauthResponse.getClass().getName());
                return userOauthResponse;
            }
            
        } catch (Exception e) {
            log.error("Error adding ePramaan session info to user object: {}", e.getMessage(), e);
            log.warn("Returning original OAuth response. Frontend won't have sessionId/sub for logout.");
            return userOauthResponse;
        }
    }
}
