package org.egov.report.service.sewasetu;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.report.repository.sewasetu.SewaSetuRepository;
import org.egov.report.service.sewasetu.SewaSetuTransformer;
import org.egov.report.web.model.sewasetu.ApplicationExecutionData;
import org.egov.report.web.model.sewasetu.ApplicationInitiatedData;
import org.egov.report.web.model.sewasetu.AttributeDetails;
import org.egov.report.web.model.sewasetu.SewaSetuData;
import org.egov.report.web.model.sewasetu.SewaSetuResponse;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SewaSetuService {

    @Autowired
    private SewaSetuRepository sewaSetuRepository;

    @Autowired
    private SewaSetuTransformer sewaSetuTransformer;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Fetch application details for a specific application reference number
     *
     * @param applRefNo   Application reference number
     * @param requestInfo Request information
     * @return SewaSetuResponse with application details
     */
    public SewaSetuResponse getApplicationDetails(String applRefNo, RequestInfo requestInfo) {
        try {
            List<Map<String, Object>> workflowHistory = sewaSetuRepository.fetchWorkflowHistory(applRefNo, requestInfo);
            SewaSetuData sewaSetuData = sewaSetuTransformer.transformToSewaSetuData(applRefNo, workflowHistory);

            List<SewaSetuData> dataList = new ArrayList<>();
            dataList.add(sewaSetuData);

            return SewaSetuResponse.builder()
                    .success(true)
                    .data(dataList)
                    .build();

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error in getApplicationDetails", e);
            throw new CustomException("ERROR_FETCHING_APPLICATION_DETAILS",
                    "Error fetching application details: " + e.getMessage());
        }
    }

    /**
     * Fetch application details by submission date
     *
     * @param submissionDate Submission date in DD-MM-YYYY format (e.g. 24-12-2025)
     * @param requestInfo    Request information
     * @return SewaSetuResponse with application details for all matching applications
     */
    public SewaSetuResponse getApplicationDetailsBySubmissionDate(String submissionDate, RequestInfo requestInfo) {
        try {
            // Step 1: Fetch application numbers based on submission date (DD-MM-YYYY)
            List<String> applicationNumbers = sewaSetuRepository.fetchApplicationNumbersBySubmissionDate(submissionDate);

            if (applicationNumbers == null || applicationNumbers.isEmpty()) {
                return SewaSetuResponse.builder()
                        .success(true)
                        .data(new ArrayList<>())
                        .build();
            }

            // Step 2: Fetch initiated data for all applications (with decryption when requestInfo has userInfo)
            List<Map<String, Object>> initiatedDataList = sewaSetuRepository.fetchInitiatedData(applicationNumbers, requestInfo);

            // Step 3: Fetch attribute data for all applications (with decryption when requestInfo has userInfo)
            List<Map<String, Object>> attributeDataList = sewaSetuRepository.fetchAttributeData(applicationNumbers, requestInfo);

            // Step 4: Fetch execution data (workflow history) for all applications
            Map<String, List<Map<String, Object>>> workflowHistoryMap =
                    sewaSetuRepository.fetchWorkflowHistoryForApplications(applicationNumbers, requestInfo);

            // Step 5: Create maps for easy lookup
            Map<String, Map<String, Object>> initiatedDataMap = initiatedDataList.stream()
                    .collect(Collectors.toMap(
                            data -> getStringValue(data.get("application_no")),
                            data -> data,
                            (existing, replacement) -> existing
                    ));

            Map<String, Map<String, Object>> attributeDataMap = attributeDataList.stream()
                    .collect(Collectors.toMap(
                            data -> getStringValue(data.get("application_no")),
                            data -> data,
                            (existing, replacement) -> existing
                    ));

            // Step 6: Transform and combine data for each application
            List<SewaSetuData> sewaSetuDataList = new ArrayList<>();

            for (String applRefNo : applicationNumbers) {
                try {
                    // Get initiated data
                    Map<String, Object> initiatedData = initiatedDataMap.get(applRefNo);
                    ApplicationInitiatedData applicationInitiatedData = null;
                    if (initiatedData != null) {
                        applicationInitiatedData = sewaSetuTransformer.transformInitiatedDataFromMap(initiatedData);
                    } else {
                        // Create minimal initiated data if not found
                        applicationInitiatedData = new ApplicationInitiatedData();
                        applicationInitiatedData.setApplRefNo(applRefNo);
                    }

                    // Get attribute data
                    Map<String, Object> attributeData = attributeDataMap.get(applRefNo);
                    AttributeDetails attributeDetails = null;
                    if (attributeData != null) {
                        attributeDetails = sewaSetuTransformer.transformAttributeDataFromMap(attributeData);
                    }

                    // Set attribute details in initiated data
                    if (applicationInitiatedData != null && attributeDetails != null) {
                        applicationInitiatedData.setAttributeDetails(attributeDetails);
                    }

                    // Get execution data (workflow history)
                    List<Map<String, Object>> workflowHistory = workflowHistoryMap.getOrDefault(applRefNo, new ArrayList<>());
                    List<ApplicationExecutionData> applicationExecutionDataList =
                            sewaSetuTransformer.transformExecutionData(workflowHistory);

                    // Build SewaSetuData
                    SewaSetuData sewaSetuData = SewaSetuData.builder()
                            .applicationInitiatedData(applicationInitiatedData)
                            .applicationExecutionData(applicationExecutionDataList)
                            .build();

                    sewaSetuDataList.add(sewaSetuData);

                } catch (Exception e) {
                    log.error("Error processing application: {}", applRefNo, e);
                    // Continue with next application instead of failing completely
                }
            }

            return SewaSetuResponse.builder()
                    .success(true)
                    .data(sewaSetuDataList)
                    .build();

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error in getApplicationDetailsBySubmissionDate", e);
            throw new CustomException("ERROR_FETCHING_APPLICATION_DETAILS_BY_DATE",
                    "Error fetching application details by submission date: " + e.getMessage());
        }
    }

    private String getStringValue(Object value) {
        if (value == null) {
            return "";
        }
        return value.toString();
    }

    public ObjectNode getDaywiseUpdateData() {
        String resultJson = sewaSetuRepository.getDaywiseUpdateData();

        try {
            return (ObjectNode) objectMapper.readTree(resultJson);
        } catch (IOException e) {
            log.error("Error parsing daywise update result from DB", e);
            throw new CustomException("JSON_PARSE_ERROR", "Failed to parse result from database");
        }
    }

    public ObjectNode getApplicationSummaryCount() {
        Map<String, Object> result = sewaSetuRepository.getApplicationSummaryCount();
        return objectMapper.convertValue(result, ObjectNode.class);
    }
}
