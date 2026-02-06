package org.egov.report.web.model.sewasetu;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.request.RequestInfo;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SewaSetuApplicationRequest {

    @JsonProperty("RequestInfo")
    @NotNull
    private RequestInfo requestInfo;

    @JsonProperty("appl_ref_no")
    private String applRefNo;

    @JsonProperty("tenantId")
    private String tenantId;

    @JsonProperty("submission_date")
    private String submissionDate;
}
