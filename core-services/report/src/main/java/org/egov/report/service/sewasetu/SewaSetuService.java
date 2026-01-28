package org.egov.report.service.sewasetu;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.report.repository.sewasetu.SewaSetuRepository;
import org.egov.report.web.model.sewasetu.SewaSetuData;
import org.egov.report.web.model.sewasetu.SewaSetuResponse;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
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

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

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
        String query =
            "SELECT json_build_object(" +
            " 'status','success'," +
            " 'submission_date',to_char(CURRENT_DATE,'DD/MM/YYYY')," +
            " 'appl_ref_no',COALESCE(json_agg(t.appl_ref_no),'[]'::json)" +
            ") AS result " +
            "FROM (" +
            " SELECT DISTINCT pi.businessid AS appl_ref_no " +
            " FROM eg_wf_processinstance_v2 pi " +
            " WHERE to_timestamp(pi.createdtime / 1000)::date = CURRENT_DATE " +
            " AND pi.businessservice IN ('BPA_DA_GP','BPA_DA_MB')" +
            " ORDER BY pi.businessid" +
            ") t";

        String resultJson = namedParameterJdbcTemplate
                .getJdbcTemplate()
                .queryForObject(query, String.class);

        try {
            return (ObjectNode) objectMapper.readTree(resultJson);
        } catch (IOException e) {
            log.error("Error parsing daywise update result from DB", e);
            throw new CustomException("JSON_PARSE_ERROR", "Failed to parse result from database");
        }
    }

    public ObjectNode getApplicationSummaryCount() {
        String query = "SELECT " +
                "COUNT(*) AS total_applications, " +
                "COUNT(*) FILTER (WHERE status IN ('PAYMENT_PENDING', 'EDIT_APPLICATION', 'CITIZEN_FINAL_PAYMENT', 'CITIZEN_APPROVAL', 'PENDING_FOR_SCRUTINY', 'PENDING_RTP_APPROVAL')) AS pending_at_applicant, " +
                "COUNT(*) FILTER (WHERE status IN ('PENDING_CEO', 'PENDING_GMDA_ENGINEER', 'PENDING_TOWNPLANNER', 'PENDING_CHAIRMAN_PRESIDENT_MB', 'PENDING_CHAIRMAN_DA', 'PENDING_DA_ENGINEER', 'PENDING_DD_AD_DEVELOPMENT_AUTHORITY', 'GIS_VALIDATION', 'FORWARDED_TO_TECHNICAL_ENGINEER_MB', 'FORWARDED_TO_TECHNICAL_ENGINEER_GP', 'FORWARDED_TO_DD_AD_TCP', 'FORWARDED_TO_ZONAL_OFFICER')) AS under_process_with_official, " +
                "COUNT(*) FILTER (WHERE status = 'APPLICATION_COMPLETED') AS delivered, " +
                "COUNT(*) FILTER (WHERE status = 'REJECTED') AS rejected " +
                "FROM ug_bpa_buildingplans";

        Map<String, Object> result = namedParameterJdbcTemplate.getJdbcTemplate().queryForMap(query);
        return objectMapper.convertValue(result, ObjectNode.class);
    }
}
