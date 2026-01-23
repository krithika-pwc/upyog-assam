package org.egov.report.service.sewasetu;

import lombok.extern.slf4j.Slf4j;
import org.egov.report.web.model.sewasetu.ExecutionData;
import org.egov.report.web.model.sewasetu.InitiatedData;
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
        InitiatedData initiatedData = transformInitiatedData(applRefNo);
        List<ExecutionData> executionDataList = transformExecutionData(workflowHistory);
        
        return SewaSetuData.builder()
                .initiatedData(initiatedData)
                .executionData(executionDataList)
                .build();
    }

    private InitiatedData transformInitiatedData(String applRefNo) {
        InitiatedData initiatedData = new InitiatedData();
        initiatedData.setApplRefNo(applRefNo);
        return initiatedData;
    }

    private List<ExecutionData> transformExecutionData(List<Map<String, Object>> workflowHistory) {
        List<ExecutionData> executionDataList = new ArrayList<>();
        
        if (workflowHistory == null || workflowHistory.isEmpty()) {
            return executionDataList;
        }
        
        for (Map<String, Object> workflow : workflowHistory) {
            ExecutionData executionData = new ExecutionData();
            
            if (workflow.get("wf_state") != null) {
                executionData.setTaskName(workflow.get("wf_state").toString());
            } else if (workflow.get("action") != null) {
                executionData.setTaskName(workflow.get("action").toString());
            }
            
            if (workflow.get("official_name") != null) {
                executionData.setOfficialName(workflow.get("official_name").toString());
            } else if (workflow.get("assigner_name") != null) {
                executionData.setOfficialName(workflow.get("assigner_name").toString());
            }
            
            if (workflow.get("designation") != null) {
                executionData.setDesignation(workflow.get("designation").toString());
            } else if (workflow.get("assigner_designation") != null) {
                executionData.setDesignation(workflow.get("assigner_designation").toString());
            }
            
            if (workflow.get("office_location") != null) {
                executionData.setOfficeLocation(workflow.get("office_location").toString());
            } else if (workflow.get("tenant_id") != null) {
                executionData.setOfficeLocation(workflow.get("tenant_id").toString());
            }
            
            if (workflow.get("action_time") != null) {
                executionData.setReceivedTime(workflow.get("action_time").toString());
            } else if (workflow.get("created_time") != null) {
                Long createdTime = getLongValue(workflow.get("created_time"));
                if (createdTime != null) {
                    executionData.setReceivedTime(formatDate(createdTime));
                }
            }
            
            if (workflow.get("execution_time") != null) {
                executionData.setExecutionTime(workflow.get("execution_time").toString());
            } else if (workflow.get("last_modified_time") != null) {
                Long modifiedTime = getLongValue(workflow.get("last_modified_time"));
                if (modifiedTime != null) {
                    executionData.setExecutionTime(formatDate(modifiedTime));
                }
            } else {
                executionData.setExecutionTime(executionData.getReceivedTime());
            }
            
            if (workflow.get("action") != null) {
                executionData.setActionTaken(workflow.get("action").toString());
            } else {
                executionData.setActionTaken("");
            }
            
            if (workflow.get("remarks") != null) {
                executionData.setRemarks(workflow.get("remarks").toString());
            } else if (workflow.get("comment") != null) {
                executionData.setRemarks(workflow.get("comment").toString());
            } else {
                executionData.setRemarks("");
            }
            
            executionDataList.add(executionData);
        }
        
        return executionDataList;
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
