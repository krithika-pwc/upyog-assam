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
    @NotBlank(message = "Application reference number is required")
    private String applRefNo;

    @JsonProperty("tenantId")
    @NotBlank(message = "Tenant ID is required")
    private String tenantId;
}
