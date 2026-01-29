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
public class AttributeDetails {

    @JsonProperty("user_type")
    private String userType;

    @JsonProperty("applied_user_type")
    private String appliedUserType;

    @JsonProperty("applicant_name")
    private String applicantName;

    @JsonProperty("applicant_gender")
    private String applicantGender;

    @JsonProperty("caste")
    private String caste;

    @JsonProperty("date_of_birth")
    private String dateOfBirth;

    @JsonProperty("e-mail")
    private String email;
}

