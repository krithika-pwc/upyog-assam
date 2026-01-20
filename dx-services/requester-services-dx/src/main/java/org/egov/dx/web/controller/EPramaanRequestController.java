package org.egov.dx.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.response.ResponseInfo;
import org.egov.dx.service.EPramaanRequestService;
import org.egov.dx.web.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.security.NoSuchAlgorithmException;


@RestController
@Slf4j
@RequestMapping("/epramaan")
@CrossOrigin
public class EPramaanRequestController {

    @Autowired
    private EPramaanRequestService ePramaanRequestService;

    @Autowired
    ResponseInfoFactory responseInfoFactory;

    @RequestMapping(value = {"/authorization/url"}, method = RequestMethod.POST)
    public ResponseEntity<AuthResponse> search(@Valid @RequestBody RequestInfo requestInfo, @RequestParam("module") String module) throws NoSuchAlgorithmException {
        AuthResponse authResponse = null;
        try {
            authResponse = ePramaanRequestService.getRedirectionURL(module);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        log.info("Auth response : {}", authResponse);
        return new ResponseEntity<>(authResponse, HttpStatus.OK);
    }

    @RequestMapping(value = "/token/citizen", method = RequestMethod.POST)
    public ResponseEntity<Object>  getTokenCitizen(@Valid @RequestBody TokenRequest tokenRequest)    {

        EPramaanTokenRes tokenRes= null;
        tokenRes = ePramaanRequestService.getToken(tokenRequest.getTokenReq());
        Object user = ePramaanRequestService.getOauthToken(tokenRequest.getRequestInfo() , tokenRes);
        
        // Add ePramaan sessionId and sub to the OAuth response for frontend to use in logout
        Object enhancedResponse = ePramaanRequestService.addEPramaanUserSessionInfo(user, tokenRes);

        log.info("Enhanced OAuth response with ePramaan session info: {}", enhancedResponse.toString());

        return new ResponseEntity<>(enhancedResponse, HttpStatus.OK);
    }

    /**
     * Generate ePramaan logout form data
     * Returns JSON object that frontend will submit as form to ePramaan logout URL
     * 
     * @param logoutRequest - Contains sessionId, sub, and tenantId
     * @return EPramaanLogoutFormData with HMAC and all required fields
     */
    @RequestMapping(value = "/getlogoutdata", method = RequestMethod.POST)
    public ResponseEntity<EPramaanLogoutFormData> getLogoutData(@Valid @RequestBody LogoutRequest logoutRequest) throws Exception {
        if (logoutRequest.getSessionId() == null || logoutRequest.getSessionId().isEmpty()) {
            throw new IllegalArgumentException("sessionId is required");
        }
        if (logoutRequest.getSub() == null || logoutRequest.getSub().isEmpty()) {
            throw new IllegalArgumentException("sub is required");
        }
        
        EPramaanLogoutFormData formData = ePramaanRequestService.generateEPramaanLogoutFormData(
            logoutRequest.getSessionId(),
            logoutRequest.getSub(),
            logoutRequest.getTenantId()
        );
        
        return new ResponseEntity<>(formData, HttpStatus.OK);
    }

}
