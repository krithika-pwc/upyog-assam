package org.egov.controller;

import lombok.extern.slf4j.Slf4j;
import org.egov.report.service.sewasetu.SewaSetuService;
import org.egov.report.web.model.sewasetu.SewaSetuApplicationRequest;
import org.egov.report.web.model.sewasetu.SewaSetuResponse;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/v1/sewasetu")
public class SewaSetuController {

    @Autowired
    private SewaSetuService sewaSetuService;

    /**
     * Fetch specific application details for Sewa Setu integration
     * 
     * @param request SewaSetuApplicationRequest containing application reference number and tenant ID
     * @return SewaSetuResponse with initiated_data and execution_data
     */
    @PostMapping("/application-details")
    public ResponseEntity<SewaSetuResponse> getApplicationDetails(
            @Valid @RequestBody SewaSetuApplicationRequest request) {
        
        try {
            SewaSetuResponse response = sewaSetuService.getApplicationDetails(
                    request.getApplRefNo(), 
                    request.getRequestInfo(),
                    request.getTenantId()
            );
            
            return new ResponseEntity<>(response, HttpStatus.OK);
            
        } catch (CustomException ex) {
            throw ex;
        } catch (Exception e) {
            log.error("Unexpected error in getApplicationDetails", e);
            throw new CustomException("ERROR_FETCHING_APPLICATION_DETAILS", 
                    "Error fetching application details: " + e.getMessage());
        }
    }
}
