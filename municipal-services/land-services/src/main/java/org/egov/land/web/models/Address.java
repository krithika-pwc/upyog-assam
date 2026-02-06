package org.egov.land.web.models;

import javax.validation.Valid;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.SafeHtml;
import org.springframework.validation.annotation.Validated;

/**
 * Representation of an address. Individual APIs may choose to extend from this using allOf if more details need to be added in their case.
 */
@ApiModel(description = "Representation of an address. Individual APIs may choose to extend from this using allOf if more details need to be added in their case.")
@Validated
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Address {

    @ApiModelProperty(value = "Unique identifier of the address.")
    private String id;

    @ApiModelProperty(value = "Unique Identifier of the tenant to which the user primarily belongs.")
    @SafeHtml
    private String tenantId;

    @ApiModelProperty(value = "House number or door number.")
    @SafeHtml
    private String houseNo;

    @ApiModelProperty(value = "Address line 1.")
    @SafeHtml
    private String addressLine1;

    @ApiModelProperty(value = "Address line 2.")
    @SafeHtml
    private String addressLine2;

    @ApiModelProperty(value = "Additional landmark to help locate the address.")
    @SafeHtml
    private String landmark;

    @ApiModelProperty(value = "Locality boundary details.")
    private Boundary locality;

    @ApiModelProperty(value = "Code of the locality.")
    private String localityCode;

    @ApiModelProperty(value = "The district in which the property is located.")
    @SafeHtml
    private String district;

    @ApiModelProperty(value = "The region in which the property is located.")
    @SafeHtml
    private String region;

    @ApiModelProperty(value = "The state in which the property is located.")
    @SafeHtml
    private String state;

    @ApiModelProperty(value = "The country in which the property is located.")
    @SafeHtml
    private String country;

    @ApiModelProperty(value = "PIN code of the address. Indian pincodes will usually be all numbers.")
    @SafeHtml
    private String pincode;

    @ApiModelProperty(value = "Type of the address.")
    private String addressType;

    @ApiModelProperty(value = "Category of the address.")
    private String addressCategory;

    @ApiModelProperty(value = "Additional details in JSON format.")
    private Object additionalDetails;

    @ApiModelProperty(value = "Geo-location details of the address.")
    @Valid
    private GeoLocation geoLocation;

    @ApiModelProperty(value = "Audit details of the address.")
    @Valid
    private AuditDetails auditDetails;

    @ApiModelProperty(value = "This ID will hold address from owner info.")
    private String ownerInfoId;

    @ApiModelProperty(value = "Boolean flag to identify whether the address is same as site address")
    private Boolean sameAsSiteAddress;
}