package org.egov.report.repository.sewasetu;

import lombok.extern.slf4j.Slf4j;
import org.egov.ReportApp;
import org.egov.common.contract.request.RequestInfo;
import org.egov.domain.model.ReportDefinitions;
import org.egov.report.repository.ReportRepository;
import org.egov.swagger.model.ReportDefinition;
import org.egov.swagger.model.ReportRequest;
import org.egov.swagger.model.SearchParam;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;

@Slf4j
@Repository
public class SewaSetuRepository {

    @Autowired
    private ReportRepository reportRepository;

    private static final String MODULE_NAME = "rainmaker-obps";
    private static final String WORKFLOW_REPORT_NAME = "sewasetu-workflow-history";

    /**
     * Fetch workflow history using config-based query
     * 
     * @param applRefNo Application reference number
     * @return List of workflow history records
     */
    public List<Map<String, Object>> fetchWorkflowHistory(String applRefNo) {
        try {
            ReportDefinitions reportDefinitions = ReportApp.getReportDefs();
            ReportDefinition reportDefinition = reportDefinitions.getReportDefinition(
                    MODULE_NAME + " " + WORKFLOW_REPORT_NAME);
            
            if (reportDefinition == null) {
                throw new CustomException("REPORT_CONFIG_NOT_FOUND", 
                        "Report configuration not found: " + WORKFLOW_REPORT_NAME);
            }
            
            ReportRequest reportRequest = buildReportRequest(applRefNo);
            List<Map<String, Object>> results = reportRepository.getData(
                    reportRequest, 
                    reportDefinition, 
                    null
            );
            
            return results != null ? results : new ArrayList<>();
            
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching workflow history for application: {}", applRefNo, e);
            throw new CustomException("ERROR_FETCHING_WORKFLOW_HISTORY", 
                    "Error fetching workflow history: " + e.getMessage());
        }
    }

    private ReportRequest buildReportRequest(String applRefNo) {
        ReportRequest reportRequest = new ReportRequest();
        
        SearchParam searchParam = new SearchParam();
        searchParam.setName("application_no");
        searchParam.setInput(applRefNo);
        
        List<SearchParam> searchParams = new ArrayList<>();
        searchParams.add(searchParam);
        reportRequest.setSearchParams(searchParams);
        
        RequestInfo requestInfo = new RequestInfo();
        reportRequest.setRequestInfo(requestInfo);
        
        return reportRequest;
    }

        @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public String getDaywiseUpdateData() {
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

        return namedParameterJdbcTemplate.getJdbcTemplate().queryForObject(query, String.class);
    }

    public Map<String, Object> getApplicationSummaryCount() {
        String query = "SELECT " +
                "COUNT(*) AS total_applications, " +
                "COUNT(*) FILTER (WHERE status IN ('PAYMENT_PENDING', 'EDIT_APPLICATION', 'CITIZEN_FINAL_PAYMENT', 'CITIZEN_APPROVAL', 'PENDING_FOR_SCRUTINY', 'PENDING_RTP_APPROVAL')) AS pending_at_applicant, " +
                "COUNT(*) FILTER (WHERE status IN ('PENDING_CEO', 'PENDING_GMDA_ENGINEER', 'PENDING_TOWNPLANNER', 'PENDING_CHAIRMAN_PRESIDENT_MB', 'PENDING_CHAIRMAN_DA', 'PENDING_DA_ENGINEER', 'PENDING_DD_AD_DEVELOPMENT_AUTHORITY', 'GIS_VALIDATION', 'FORWARDED_TO_TECHNICAL_ENGINEER_MB', 'FORWARDED_TO_TECHNICAL_ENGINEER_GP', 'FORWARDED_TO_DD_AD_TCP', 'FORWARDED_TO_ZONAL_OFFICER')) AS under_process_with_official, " +
                "COUNT(*) FILTER (WHERE status = 'APPLICATION_COMPLETED') AS delivered, " +
                "COUNT(*) FILTER (WHERE status = 'REJECTED') AS rejected " +
                "FROM ug_bpa_buildingplans";

        return namedParameterJdbcTemplate.getJdbcTemplate().queryForMap(query);
    }

}
