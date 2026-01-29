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
public class SewaSetuResponse {

    @JsonProperty("sucess")
    private Boolean success;

    @JsonProperty("data")
    private List<SewaSetuData> data;
}
