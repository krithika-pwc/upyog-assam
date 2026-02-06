package org.egov.report.service.sewasetu;

import lombok.extern.slf4j.Slf4j;
import org.egov.report.web.model.sewasetu.ApplicationExecutionData;
import org.egov.report.web.model.sewasetu.ApplicationInitiatedData;
import org.egov.report.web.model.sewasetu.AttributeDetails;
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

    /** Hardcoded Sewa Setu response values. */
    private static final String DEPARTMENT_ID = "810";
    private static final String DEPARTMENT_NAME = "TCP Assam";
    private static final String SERVICE_ID = "13960397";
    private static final String SERVICE_NAME = "BPA";
    private static final String PAYMENT_MODE_ON = "ON";
    private static final String SUBMISSION_MODE_ONLINE = "ON";
    private static final String SERVICE_CHARGE_ZERO = "0";
    private static final String GOVT_CHARGE_ZERO = "0";
    private static final Double CONVENIENCE_FEE_ZERO = 0.0;

    /**
     * Transform initiated data map to ApplicationInitiatedData
     * Applies hardcoded department/service and payment overrides; maps appl_status to Sewa Setu code.
     *
     * @param initiatedDataMap Map containing initiated data
     * @return ApplicationInitiatedData
     */
    public ApplicationInitiatedData transformInitiatedDataFromMap(Map<String, Object> initiatedDataMap) {
        if (initiatedDataMap == null) {
            return null;
        }

        ApplicationInitiatedData applicationInitiatedData = new ApplicationInitiatedData();

        applicationInitiatedData.setApplRefNo(getStringValue(initiatedDataMap.get("application_no")));
        // Single "name" field for decryption; used for applied_by, user_name (and applicant_name in attribute_details)
        String name = getStringValue(initiatedDataMap.get("name"));
        applicationInitiatedData.setAppliedBy(name);
        applicationInitiatedData.setUserName(name);
        applicationInitiatedData.setSubmissionLocation(getStringValue(initiatedDataMap.get("submission_location")));
        applicationInitiatedData.setLocationId(getLongValue(initiatedDataMap.get("location_id")));
        applicationInitiatedData.setDistrict(getStringValue(initiatedDataMap.get("district")));
        applicationInitiatedData.setDistrictId(getLongValue(initiatedDataMap.get("district_id")));
        applicationInitiatedData.setCircle(getStringValue(initiatedDataMap.get("circle")));
        applicationInitiatedData.setCircleId(getLongValue(initiatedDataMap.get("circle_id")));
        applicationInitiatedData.setSubmissionDate(getStringValue(initiatedDataMap.get("submission_date")));
        applicationInitiatedData.setSubmissionMode(getStringValue(initiatedDataMap.get("submission_mode")));
        applicationInitiatedData.setReferenceNo(getStringValue(initiatedDataMap.get("reference_no")));
        applicationInitiatedData.setPaymentDate(getStringValue(initiatedDataMap.get("payment_date")));
        applicationInitiatedData.setAmount(getStringValue(initiatedDataMap.get("amount")));
        applicationInitiatedData.setPaymentStatus(getStringValue(initiatedDataMap.get("payment_status")));

        applicationInitiatedData.setDepartmentId(DEPARTMENT_ID);
        applicationInitiatedData.setDepartmentName(DEPARTMENT_NAME);
        applicationInitiatedData.setServiceId(SERVICE_ID);
        applicationInitiatedData.setServiceName(SERVICE_NAME);
        applicationInitiatedData.setPaymentMode(PAYMENT_MODE_ON);
        applicationInitiatedData.setSubmissionMode(SUBMISSION_MODE_ONLINE);
        applicationInitiatedData.setServiceCharge(SERVICE_CHARGE_ZERO);
        applicationInitiatedData.setGovtCharge(GOVT_CHARGE_ZERO);
        applicationInitiatedData.setConvenienceFee(CONVENIENCE_FEE_ZERO);

        Object grnNoObj = initiatedDataMap.get("grn_no");
        if (grnNoObj != null) {
            if (grnNoObj instanceof List) {
                applicationInitiatedData.setGrnNo((List<String>) grnNoObj);
            } else if (grnNoObj instanceof String) {
                String grnNoStr = (String) grnNoObj;
                if (!grnNoStr.isEmpty()) {
                    List<String> grnNoList = new ArrayList<>();
                    for (String part : grnNoStr.split(",")) {
                        String trimmed = part.trim();
                        if (!trimmed.isEmpty()) {
                            grnNoList.add(trimmed);
                        }
                    }
                    applicationInitiatedData.setGrnNo(grnNoList);
                }
            }
        }

        Object cinNoObj = initiatedDataMap.get("cin_no");
        if (cinNoObj != null) {
            if (cinNoObj instanceof List) {
                applicationInitiatedData.setCinNo((List<String>) cinNoObj);
            } else if (cinNoObj instanceof String) {
                String cinNoStr = (String) cinNoObj;
                if (!cinNoStr.isEmpty()) {
                    List<String> cinNoList = new ArrayList<>();
                    for (String part : cinNoStr.split(",")) {
                        String trimmed = part.trim();
                        if (!trimmed.isEmpty()) {
                            cinNoList.add(trimmed);
                        }
                    }
                    applicationInitiatedData.setCinNo(cinNoList);
                }
            }
        }

        String ourApplStatus = getStringValue(initiatedDataMap.get("appl_status"));
        String sewaSetuCode = SewaSetuStatusMapper.toSewaSetuCode(ourApplStatus);
        applicationInitiatedData.setApplStatus(sewaSetuCode != null ? sewaSetuCode : ourApplStatus);

        Object pfcPaymentResponse = initiatedDataMap.get("pfc_payment_response");
        if (pfcPaymentResponse != null) {
            applicationInitiatedData.setPfcPaymentResponse(pfcPaymentResponse);
        }

        return applicationInitiatedData;
    }

    /** Hardcoded: user type for Sewa Setu response. */
    private static final String USER_TYPE_CITIZEN = "Citizen";

    /**
     * Transform attribute data map to AttributeDetails.
     * user_type and applied_user_type are set to "Citizen" for Sewa Setu.
     *
     * @param attributeDataMap Map containing attribute data
     * @return AttributeDetails
     */
    public AttributeDetails transformAttributeDataFromMap(Map<String, Object> attributeDataMap) {
        if (attributeDataMap == null) {
            return null;
        }

        AttributeDetails attributeDetails = new AttributeDetails();

        attributeDetails.setUserType(USER_TYPE_CITIZEN);
        attributeDetails.setAppliedUserType(USER_TYPE_CITIZEN);
        // Report returns column as "name" for decryption to map
        attributeDetails.setApplicantName(getStringValue(attributeDataMap.get("name")));
        attributeDetails.setApplicantGender(getStringValue(attributeDataMap.get("applicant_gender")));
        attributeDetails.setCaste("");
        attributeDetails.setDateOfBirth(getStringValue(attributeDataMap.get("date_of_birth")));
        attributeDetails.setEmail(getStringValue(attributeDataMap.get("email")));

        return attributeDetails;
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
    public List<ApplicationExecutionData> transformExecutionData(List<Map<String, Object>> workflowHistory) {
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
            
            // Report returns column as "name" for decryption to map
            if (workflow.get("name") != null && !workflow.get("name").toString().isEmpty()) {
                applicationExecutionData.setOfficialName(workflow.get("name").toString());
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

    private String getStringValue(Object value) {
        if (value == null) {
            return "";
        }
        return value.toString();
    }

    private Double getDoubleValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Double) {
            return (Double) value;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (Exception e) {
            log.error("Error converting to Double: {}", value, e);
            return null;
        }
    }
}
