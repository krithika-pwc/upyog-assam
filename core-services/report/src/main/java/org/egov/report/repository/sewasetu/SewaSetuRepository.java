package org.egov.report.repository.sewasetu;

import lombok.extern.slf4j.Slf4j;
import org.egov.ReportApp;
import org.egov.common.contract.request.RequestInfo;
import org.egov.domain.model.ReportDefinitions;
import org.egov.encryption.EncryptionService;
import org.egov.report.repository.ReportRepository;
import org.egov.swagger.model.ReportDefinition;
import org.egov.swagger.model.ReportRequest;
import org.egov.swagger.model.SearchParam;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class SewaSetuRepository {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private EncryptionService encryptionService;

    private static final String MODULE_NAME = "rainmaker-sewasetu";
    private static final String WORKFLOW_REPORT_NAME = "sewasetu-workflow-history";
    private static final String APPLICATION_NUMBERS_REPORT_NAME = "sewasetu-application-numbers-by-date";
    private static final String INITIATED_DATA_REPORT_NAME = "sewasetu-initiated-data";
    private static final String ATTRIBUTE_DATA_REPORT_NAME = "sewasetu-attribute-data";

    /**
     * Fetch workflow history using config-based query. If the report has decryptionPathId
     * and requestInfo contains userInfo, official_name (and other PII) are decrypted.
     *
     * @param applRefNo   Application reference number
     * @param requestInfo Request info (required for decryption; can be null to skip decryption)
     * @return List of workflow history records
     */
    public List<Map<String, Object>> fetchWorkflowHistory(String applRefNo, RequestInfo requestInfo) {
        try {
            ReportDefinitions reportDefinitions = ReportApp.getReportDefs();
            ReportDefinition reportDefinition = reportDefinitions.getReportDefinition(
                    MODULE_NAME + " " + WORKFLOW_REPORT_NAME);

            if (reportDefinition == null) {
                throw new CustomException("REPORT_CONFIG_NOT_FOUND",
                        "Report configuration not found: " + WORKFLOW_REPORT_NAME);
            }

            ReportRequest reportRequest = buildReportRequest(applRefNo);
            if (requestInfo != null) {
                reportRequest.setRequestInfo(requestInfo);
            }
            List<Map<String, Object>> results = reportRepository.getData(
                    reportRequest,
                    reportDefinition,
                    null
            );

            if (results == null) {
                results = new ArrayList<>();
            } else if (reportDefinition.getdecryptionPathId() != null
                    && requestInfo != null
                    && requestInfo.getUserInfo() != null) {
                try {
                    results = encryptionService.decryptJson(requestInfo, results,
                            reportDefinition.getdecryptionPathId(), "Retrieve Report Data", Map.class);
                } catch (IOException e) {
                    log.error("Error decrypting workflow history for application: {}", applRefNo, e);
                    throw new CustomException("REPORT_DECRYPTION_ERROR", "Error while decrypting report data");
                }
            }

            return results;

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching workflow history for application: {}", applRefNo, e);
            throw new CustomException("ERROR_FETCHING_WORKFLOW_HISTORY",
                    "Error fetching workflow history: " + e.getMessage());
        }
    }

    /**
     * Fetch application numbers by submission date
     *
     * @param submissionDate Submission date in DD-MM-YYYY format (e.g. 24-12-2025)
     * @return List of application numbers
     */
    public List<String> fetchApplicationNumbersBySubmissionDate(String submissionDate) {
        try {
            ReportDefinitions reportDefinitions = ReportApp.getReportDefs();
            ReportDefinition reportDefinition = reportDefinitions.getReportDefinition(
                    MODULE_NAME + " " + APPLICATION_NUMBERS_REPORT_NAME);

            if (reportDefinition == null) {
                throw new CustomException("REPORT_CONFIG_NOT_FOUND",
                        "Report configuration not found: " + APPLICATION_NUMBERS_REPORT_NAME);
            }

            ReportRequest reportRequest = buildReportRequestForSubmissionDate(submissionDate);
            List<Map<String, Object>> results = reportRepository.getData(
                    reportRequest,
                    reportDefinition,
                    null
            );

            if (results == null || results.isEmpty()) {
                return new ArrayList<>();
            }

            return results.stream()
                    .map(row -> {
                        Object businessId = row.get("businessid");
                        return businessId != null ? businessId.toString() : null;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching application numbers by submission date: {}", submissionDate, e);
            throw new CustomException("ERROR_FETCHING_APPLICATION_NUMBERS",
                    "Error fetching application numbers: " + e.getMessage());
        }
    }

    /**
     * Fetch initiated data for multiple applications. If the report has decryptionPathId
     * and requestInfo contains userInfo, user_name (and other PII) are decrypted.
     *
     * @param applicationNumbers List of application numbers
     * @param requestInfo        Request info (required for decryption; can be null to skip decryption)
     * @return List of initiated data records
     */
    public List<Map<String, Object>> fetchInitiatedData(List<String> applicationNumbers, RequestInfo requestInfo) {
        try {
            if (applicationNumbers == null || applicationNumbers.isEmpty()) {
                return new ArrayList<>();
            }

            ReportDefinitions reportDefinitions = ReportApp.getReportDefs();
            ReportDefinition reportDefinition = reportDefinitions.getReportDefinition(
                    MODULE_NAME + " " + INITIATED_DATA_REPORT_NAME);

            if (reportDefinition == null) {
                throw new CustomException("REPORT_CONFIG_NOT_FOUND",
                        "Report configuration not found: " + INITIATED_DATA_REPORT_NAME);
            }

            ReportRequest reportRequest = buildReportRequestForApplicationNumbers(applicationNumbers);
            if (requestInfo != null) {
                reportRequest.setRequestInfo(requestInfo);
            }
            List<Map<String, Object>> results = reportRepository.getData(
                    reportRequest,
                    reportDefinition,
                    null
            );

            if (results == null) {
                results = new ArrayList<>();
            } else if (reportDefinition.getdecryptionPathId() != null
                    && requestInfo != null
                    && requestInfo.getUserInfo() != null) {
                try {
                    results = encryptionService.decryptJson(requestInfo, results,
                            reportDefinition.getdecryptionPathId(), "Retrieve Report Data", Map.class);
                } catch (IOException e) {
                    log.error("Error decrypting initiated data for applications: {}", applicationNumbers, e);
                    throw new CustomException("REPORT_DECRYPTION_ERROR", "Error while decrypting report data");
                }
            }

            return results;

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching initiated data for applications: {}", applicationNumbers, e);
            throw new CustomException("ERROR_FETCHING_INITIATED_DATA",
                    "Error fetching initiated data: " + e.getMessage());
        }
    }

    /**
     * Fetch attribute data for multiple applications. If the report has decryptionPathId
     * and requestInfo contains userInfo, applicant_name and email (and other PII) are decrypted.
     *
     * @param applicationNumbers List of application numbers
     * @param requestInfo       Request info (required for decryption; can be null to skip decryption)
     * @return List of attribute data records
     */
    public List<Map<String, Object>> fetchAttributeData(List<String> applicationNumbers, RequestInfo requestInfo) {
        try {
            if (applicationNumbers == null || applicationNumbers.isEmpty()) {
                return new ArrayList<>();
            }

            ReportDefinitions reportDefinitions = ReportApp.getReportDefs();
            ReportDefinition reportDefinition = reportDefinitions.getReportDefinition(
                    MODULE_NAME + " " + ATTRIBUTE_DATA_REPORT_NAME);

            if (reportDefinition == null) {
                throw new CustomException("REPORT_CONFIG_NOT_FOUND",
                        "Report configuration not found: " + ATTRIBUTE_DATA_REPORT_NAME);
            }

            ReportRequest reportRequest = buildReportRequestForApplicationNumbers(applicationNumbers);
            if (requestInfo != null) {
                reportRequest.setRequestInfo(requestInfo);
            }
            List<Map<String, Object>> results = reportRepository.getData(
                    reportRequest,
                    reportDefinition,
                    null
            );

            if (results == null) {
                results = new ArrayList<>();
            } else if (reportDefinition.getdecryptionPathId() != null
                    && requestInfo != null
                    && requestInfo.getUserInfo() != null) {
                // Remove empty decryptable fields to avoid decryption errors
                removeEmptyDecryptableFields(results, "name", "email");
                try {
                    results = encryptionService.decryptJson(requestInfo, results,
                            reportDefinition.getdecryptionPathId(), "Retrieve Report Data", Map.class);
                } catch (IOException e) {
                    log.error("Error decrypting attribute data for applications: {}", applicationNumbers, e);
                    throw new CustomException("REPORT_DECRYPTION_ERROR", "Error while decrypting report data");
                }
            }

            return results;

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching attribute data for applications: {}", applicationNumbers, e);
            throw new CustomException("ERROR_FETCHING_ATTRIBUTE_DATA",
                    "Error fetching attribute data: " + e.getMessage());
        }
    }

    /**
     * Fetch workflow history for multiple applications. Decryption is applied when
     * the workflow report has decryptionPathId and requestInfo contains userInfo.
     *
     * @param applicationNumbers List of application numbers
     * @param requestInfo       Request info (required for decryption; can be null to skip decryption)
     * @return Map of application number to workflow history
     */
    public Map<String, List<Map<String, Object>>> fetchWorkflowHistoryForApplications(
            List<String> applicationNumbers, RequestInfo requestInfo) {
        Map<String, List<Map<String, Object>>> workflowHistoryMap = new HashMap<>();

        if (applicationNumbers == null || applicationNumbers.isEmpty()) {
            return workflowHistoryMap;
        }

        for (String applRefNo : applicationNumbers) {
            try {
                List<Map<String, Object>> workflowHistory = fetchWorkflowHistory(applRefNo, requestInfo);
                workflowHistoryMap.put(applRefNo, workflowHistory);
            } catch (Exception e) {
                log.error("Error fetching workflow history for application: {}", applRefNo, e);
                workflowHistoryMap.put(applRefNo, new ArrayList<>());
            }
        }

        return workflowHistoryMap;
    }

    /**
     * Removes decryptable keys from each map when value is null or empty string,
     * so the encryption service does not attempt to decrypt them.
     */
    private void removeEmptyDecryptableFields(List<Map<String, Object>> rows, String... keys) {
        if (rows == null || keys == null) return;
        for (Map<String, Object> row : rows) {
            if (row == null) continue;
            for (String key : keys) {
                Object val = row.get(key);
                if (val == null || (val instanceof String && ((String) val).isEmpty())) {
                    row.remove(key);
                }
            }
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

    private ReportRequest buildReportRequestForSubmissionDate(String submissionDate) {
        ReportRequest reportRequest = new ReportRequest();

        SearchParam searchParam = new SearchParam();
        searchParam.setName("submission_date");
        searchParam.setInput(submissionDate);

        List<SearchParam> searchParams = new ArrayList<>();
        searchParams.add(searchParam);
        reportRequest.setSearchParams(searchParams);

        RequestInfo requestInfo = new RequestInfo();
        reportRequest.setRequestInfo(requestInfo);

        return reportRequest;
    }

    private ReportRequest buildReportRequestForApplicationNumbers(List<String> applicationNumbers) {
        ReportRequest reportRequest = new ReportRequest();

        // Convert list to comma-separated string for PostgreSQL array
        String applicationNumbersStr = String.join(",", applicationNumbers);

        SearchParam searchParam = new SearchParam();
        searchParam.setName("application_numbers");
        searchParam.setInput(applicationNumbersStr);

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
