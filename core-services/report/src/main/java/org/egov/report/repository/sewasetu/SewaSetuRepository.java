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
     * @param tenantId Tenant ID
     * @return List of workflow history records
     */
    public List<Map<String, Object>> fetchWorkflowHistory(String applRefNo, String tenantId) {
        try {
            ReportDefinitions reportDefinitions = ReportApp.getReportDefs();
            ReportDefinition reportDefinition = reportDefinitions.getReportDefinition(
                    MODULE_NAME + " " + WORKFLOW_REPORT_NAME);
            
            if (reportDefinition == null) {
                throw new CustomException("REPORT_CONFIG_NOT_FOUND", 
                        "Report configuration not found: " + WORKFLOW_REPORT_NAME);
            }
            
            ReportRequest reportRequest = buildReportRequest(applRefNo, tenantId);
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

    private ReportRequest buildReportRequest(String applRefNo, String tenantId) {
        ReportRequest reportRequest = new ReportRequest();
        reportRequest.setTenantId(tenantId);
        
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
}
