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
public class SewaSetuData {

    @JsonProperty("initiated_data")
    private ApplicationInitiatedData applicationInitiatedData;

    @JsonProperty("execution_data")
    private List<ApplicationExecutionData> applicationExecutionData;
}
