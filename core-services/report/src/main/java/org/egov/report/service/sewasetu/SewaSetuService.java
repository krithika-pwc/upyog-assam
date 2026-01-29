package org.egov.report.service.sewasetu;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.report.repository.sewasetu.SewaSetuRepository;
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
     * @param applRefNo Application reference number
     * @param requestInfo Request information
     * @return SewaSetuResponse with application details
     */
    public SewaSetuResponse getApplicationDetails(String applRefNo, RequestInfo requestInfo) {
        try {
            List<Map<String, Object>> workflowHistory = sewaSetuRepository.fetchWorkflowHistory(applRefNo);
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
