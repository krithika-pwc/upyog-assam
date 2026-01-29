package org.egov.report.web.model.sewasetu;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationInitiatedData {

    @JsonProperty("department_id")
    private String departmentId;

    @JsonProperty("department_name")
    private String departmentName;

    @JsonProperty("service_id")
    private String serviceId;

    @JsonProperty("service_name")
    private String serviceName;

    @JsonProperty("appl_ref_no")
    private String applRefNo;

    @JsonProperty("applied_by")
    private String appliedBy;

    @JsonProperty("user_name")
    private String userName;

    @JsonProperty("submission_location")
    private String submissionLocation;

    @JsonProperty("location_id")
    private Long locationId;

    @JsonProperty("district")
    private String district;

    @JsonProperty("district_id")
    private Long districtId;

    @JsonProperty("circle")
    private String circle;

    @JsonProperty("circle_id")
    private Long circleId;

    @JsonProperty("submission_date")
    private String submissionDate;

    @JsonProperty("submission_mode")
    private String submissionMode;

    @JsonProperty("payment_mode")
    private String paymentMode;

    @JsonProperty("reference_no")
    private String referenceNo;

    @JsonProperty("payment_date")
    private String paymentDate;

    @JsonProperty("amount")
    private String amount;

    @JsonProperty("payment_status")
    private String paymentStatus;

    @JsonProperty("service_charge")
    private String serviceCharge;

    @JsonProperty("govt_charge")
    private String govtCharge;

    @JsonProperty("convenience_fee")
    private Double convenienceFee;

    @JsonProperty("grn_no")
    private List<String> grnNo;

    @JsonProperty("cin_no")
    private List<String> cinNo;

    @JsonProperty("appl_status")
    private String applStatus;

    @JsonProperty("pfc_payment_response")
    private Object pfcPaymentResponse; // Can be a Map<String, Object> if dynamic

    @JsonProperty("attribute_details")
    private AttributeDetails attributeDetails;
}