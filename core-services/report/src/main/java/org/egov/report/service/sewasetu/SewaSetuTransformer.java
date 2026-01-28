package org.egov.report.service.sewasetu;

import lombok.extern.slf4j.Slf4j;
import org.egov.report.web.model.sewasetu.ApplicationExecutionData;
import org.egov.report.web.model.sewasetu.ApplicationInitiatedData;
import org.egov.report.web.model.sewasetu.SewaSetuData;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class SewaSetuTransformer {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    /**
     * Transform application reference number and workflow history to Sewa Setu format
     * 
     * @param applRefNo Application reference number
     * @param workflowHistory Workflow history data
     * @return SewaSetuData in required format
     */
    public SewaSetuData transformToSewaSetuData(String applRefNo, 
                                                 List<Map<String, Object>> workflowHistory) {
        ApplicationInitiatedData applicationInitiatedData = transformInitiatedData(applRefNo);
        List<ApplicationExecutionData> applicationExecutionDataList = transformExecutionData(workflowHistory);
        
        return SewaSetuData.builder()
                .applicationInitiatedData(applicationInitiatedData)
                .applicationExecutionData(applicationExecutionDataList)
                .build();
    }

    /**
     * Transform application reference number to ApplicationInitiatedData
     * */
    private ApplicationInitiatedData transformInitiatedData(String applRefNo) {
        ApplicationInitiatedData applicationInitiatedData = new ApplicationInitiatedData();
        applicationInitiatedData.setApplRefNo(applRefNo);
        return applicationInitiatedData;
    }

    /**
     * Transform workflow history to list of ApplicationExecutionData
     *
     * @param workflowHistory Workflow history data
     * @return List of ApplicationExecutionData
     */
    private List<ApplicationExecutionData> transformExecutionData(List<Map<String, Object>> workflowHistory) {
        List<ApplicationExecutionData> applicationExecutionDataList = new ArrayList<>();
        
        if (workflowHistory == null || workflowHistory.isEmpty()) {
            return applicationExecutionDataList;
        }
        
        for (Map<String, Object> workflow : workflowHistory) {
            ApplicationExecutionData applicationExecutionData = new ApplicationExecutionData();
            
            if (workflow.get("wf_state") != null) {
                applicationExecutionData.setTaskName(workflow.get("wf_state").toString());
            } else if (workflow.get("action") != null) {
                applicationExecutionData.setTaskName(workflow.get("action").toString());
            }
            
            if (workflow.get("official_name") != null) {
                applicationExecutionData.setOfficialName(workflow.get("official_name").toString());
            } else if (workflow.get("assigner_name") != null) {
                applicationExecutionData.setOfficialName(workflow.get("assigner_name").toString());
            }
            
            if (workflow.get("designation") != null) {
                applicationExecutionData.setDesignation(workflow.get("designation").toString());
            } else if (workflow.get("assigner_designation") != null) {
                applicationExecutionData.setDesignation(workflow.get("assigner_designation").toString());
            }
            
            if (workflow.get("office_location") != null) {
                applicationExecutionData.setOfficeLocation(workflow.get("office_location").toString());
            } else if (workflow.get("tenant_id") != null) {
                applicationExecutionData.setOfficeLocation(workflow.get("tenant_id").toString());
            }
            
            if (workflow.get("action_time") != null) {
                applicationExecutionData.setReceivedTime(workflow.get("action_time").toString());
            } else if (workflow.get("created_time") != null) {
                Long createdTime = getLongValue(workflow.get("created_time"));
                if (createdTime != null) {
                    applicationExecutionData.setReceivedTime(formatDate(createdTime));
                }
            }
            
            if (workflow.get("execution_time") != null) {
                applicationExecutionData.setExecutionTime(workflow.get("execution_time").toString());
            } else if (workflow.get("last_modified_time") != null) {
                Long modifiedTime = getLongValue(workflow.get("last_modified_time"));
                if (modifiedTime != null) {
                    applicationExecutionData.setExecutionTime(formatDate(modifiedTime));
                }
            } else {
                applicationExecutionData.setExecutionTime(applicationExecutionData.getReceivedTime());
            }
            
            if (workflow.get("action") != null) {
                applicationExecutionData.setActionTaken(workflow.get("action").toString());
            } else {
                applicationExecutionData.setActionTaken("");
            }
            
            if (workflow.get("remarks") != null) {
                applicationExecutionData.setRemarks(workflow.get("remarks").toString());
            } else if (workflow.get("comment") != null) {
                applicationExecutionData.setRemarks(workflow.get("comment").toString());
            } else {
                applicationExecutionData.setRemarks("");
            }
            
            applicationExecutionDataList.add(applicationExecutionData);
        }
        
        return applicationExecutionDataList;
    }

    private String formatDate(Long epochTime) {
        if (epochTime == null) {
            return "";
        }
        try {
            Date date = new Date(epochTime);
            return DATE_FORMAT.format(date);
        } catch (Exception e) {
            log.error("Error formatting date: {}", epochTime, e);
            return "";
        }
    }

    private Long getLongValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Long) {
            return (Long) value;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (Exception e) {
            log.error("Error converting to Long: {}", value, e);
            return null;
        }
    }
}
