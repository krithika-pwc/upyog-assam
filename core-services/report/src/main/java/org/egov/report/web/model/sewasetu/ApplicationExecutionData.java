package org.egov.report.web.model.sewasetu;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationExecutionData {

    @JsonProperty("task_name")
    private String taskName;

    @JsonProperty("official_name")
    private String officialName;

    @JsonProperty("designation")
    private String designation;

    @JsonProperty("office_location")
    private String officeLocation;

    @JsonProperty("received_time")
    private String receivedTime;

    @JsonProperty("execution_time")
    private String executionTime;

    @JsonProperty("action_taken")
    private String actionTaken;

    @JsonProperty("remarks")
    private String remarks;
}
