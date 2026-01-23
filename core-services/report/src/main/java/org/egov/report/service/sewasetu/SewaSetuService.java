package org.egov.report.service.sewasetu;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.report.repository.sewasetu.SewaSetuRepository;
import org.egov.report.web.model.sewasetu.SewaSetuData;
import org.egov.report.web.model.sewasetu.SewaSetuResponse;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    /**
     * Fetch application details for a specific application reference number
     * 
     * @param applRefNo Application reference number
     * @param requestInfo Request information
     * @param tenantId Tenant ID
     * @return SewaSetuResponse with application details
     */
    public SewaSetuResponse getApplicationDetails(String applRefNo, RequestInfo requestInfo, String tenantId) {
        try {
            List<Map<String, Object>> workflowHistory = sewaSetuRepository.fetchWorkflowHistory(applRefNo, tenantId);
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
}
