package org.egov.controller;

import lombok.extern.slf4j.Slf4j;
import org.egov.report.service.ReportService;
import org.egov.report.service.sewasetu.SewaSetuService;
import org.egov.report.web.model.sewasetu.SewaSetuApplicationRequest;
import org.egov.report.web.model.sewasetu.SewaSetuResponse;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/v1/sewasetu")
public class SewaSetuController {

    @Autowired
    private SewaSetuService sewaSetuService;
    
    @Autowired
    private ReportService reportService;
    /**
     * Fetch specific application details for Sewa Setu integration
     * 
     * @param request SewaSetuApplicationRequest containing application reference number and tenant ID
     * @return SewaSetuResponse with initiated_data and execution_data
     */
    @PostMapping("/_applicationdetails")
    public ResponseEntity<SewaSetuResponse> getApplicationDetails(
            @Valid @RequestBody SewaSetuApplicationRequest request) {
        
        try {
            SewaSetuResponse response = sewaSetuService.getApplicationDetails(
                    request.getApplRefNo(), 
                    request.getRequestInfo()
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

    /**
     * Fetch application details by submission date for Sewa Setu integration
     *
     * @param request SewaSetuSubmissionDateRequest containing submission date and tenant ID
     * @return SewaSetuResponse with initiated_data, attribute_data, and execution_data for all matching applications
     */
    @PostMapping("/_datewiseapplications")
    public ResponseEntity<SewaSetuResponse> getApplicationDetailsBySubmissionDate(
            @Valid @RequestBody SewaSetuApplicationRequest request) {

        try {
            SewaSetuResponse response = sewaSetuService.getApplicationDetailsBySubmissionDate(
                    request.getSubmissionDate(),
                    request.getRequestInfo()
            );

            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (CustomException ex) {
            throw ex;
        } catch (Exception e) {
            log.error("Unexpected error in getApplicationDetailsBySubmissionDate", e);
            throw new CustomException("ERROR_FETCHING_APPLICATION_DETAILS_BY_DATE",
                    "Error fetching application details by submission date: " + e.getMessage());
        }
    }


    @PostMapping("/daywiseUpdate/_search")
    @ResponseBody
    public ResponseEntity<?> getDaywiseUpdate() {
        try {
        	ObjectNode response = sewaSetuService.getDaywiseUpdateData();
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (CustomException e) {
            log.error("Error in getting daywise update", e);
            throw e;
        } catch (Exception e) {
            log.error("Error in getting daywise update", e);
            throw new CustomException("ERROR_GETTING_DAYWISE_UPDATE", e.getMessage());
        }
    }

    @PostMapping("/applicationsummary/_count")
    @ResponseBody
    public ResponseEntity<?> getApplicationSummaryCount() {
        try {
        	ObjectNode response = sewaSetuService.getApplicationSummaryCount();
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (CustomException e) {
            log.error("Error in getting application summary count", e);
            throw e;
        } catch (Exception e) {
            log.error("Error in getting application summary count", e);
            throw new CustomException("ERROR_GETTING_APPLICATION_SUMMARY_COUNT", e.getMessage());
        }
    }
}
