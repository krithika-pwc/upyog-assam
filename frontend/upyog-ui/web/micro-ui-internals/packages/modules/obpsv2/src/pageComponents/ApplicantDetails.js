import React, { useEffect, useState } from "react";
import { FormStep, TextInput, CardLabel, MobileNumber } from "@upyog/digit-ui-react-components";
import Timeline from "../components/Timeline";

const ApplicantDetails = ({ t, config, onSelect, formData, searchResult }) => {
  const user = Digit.UserService.getUser().info;
  const flow = window.location.href.includes("editApplication") ? "editApplication" : "buildingPermit"

  // Applicant Fields
  const [applicantName, setApplicantName] = useState(formData?.applicant?.applicantName || searchResult?.landInfo?.owners?.[0]?.name || "");
  const [emailId, setEmail] = useState(formData?.applicant?.emailId || searchResult?.landInfo?.owners?.[0]?.emailId || "");
  const [mobileNumber, setMobileNumber] = useState(formData?.applicant?.mobileNumber ||  searchResult?.landInfo?.owners?.[0]?.mobileNumber || user?.mobileNumber);
  const [alternateNumber, setAltMobileNumber] = useState(formData?.applicant?.alternateNumber || searchResult?.landInfo?.owners?.[0]?.alternateNumber || "");
  const [fatherName, setFatherName] = useState(formData?.applicant?.fatherName || "");
  const [motherName, setMotherName] = useState(formData?.applicant?.motherName || "");
  const [panCardNumber, setPanCardNumber] = useState(formData?.applicant?.panCardNumber || searchResult?.landInfo?.owners?.[0]?.pan || "");
  const [aadhaarNumber, setAadhaarNumber] = useState(formData?.applicant?.aadhaarNumber || searchResult?.landInfo?.owners?.[0]?.aadhaarNumber ||"");

  // Go next
  const goNext = () => {
    let applicantStep = {
      applicantName,
      mobileNumber,
      alternateNumber,
      emailId,
      fatherName,
      motherName,
      panCardNumber,
      aadhaarNumber,
    };

    onSelect(config.key, { ...formData[config.key], ...applicantStep });
    
  };

  const onSkip = () => onSelect();

  return (
    <React.Fragment>
      <Timeline currentStep={flow==="editApplication" ? 1 : 1} flow={flow}/>
      <FormStep
        config={config}
        onSelect={goNext}
        onSkip={onSkip}
        t={t}
        isDisabled={
          !applicantName ||
          !mobileNumber ||
          !emailId ||
          !fatherName ||
          !panCardNumber ||
          !aadhaarNumber
        }
      >
        <div>
          {/* Applicant Name */}
          <CardLabel>{`${t("BPA_APPLICANT_NAME")}`} <span className="check-page-link-button">*</span></CardLabel>
          <TextInput
            t={t}
            type="text"
            name="applicantName"
            placeholder="Enter Applicant Name"
            value={applicantName}
            onChange={(e) => setApplicantName(e.target.value.replace(/[^a-zA-Z\s]/g, ""))}
            ValidationRequired={true}
            {...{ pattern: "^[a-zA-Z ]+$", title: t("BPA_NAME_ERROR_MESSAGE") }}
          />

          {/* Mobile Number */}
          <CardLabel>{`${t("BPA_MOBILE_NUMBER")}`} <span className="check-page-link-button">*</span></CardLabel>
          <MobileNumber
            value={mobileNumber}
            name="mobileNumber"
            placeholder="Enter Mobile Number"
            onChange={(value) => setMobileNumber(value)}
            {...{ pattern: "[6-9]{1}[0-9]{9}", title: t("CORE_COMMON_APPLICANT_MOBILE_NUMBER_INVALID") }}
          />

          {/* Alternate Mobile Number */}
          <CardLabel>{`${t("BPA_ALT_MOBILE_NUMBER")}`}</CardLabel>
          <MobileNumber
            value={alternateNumber}
            name="alternateNumber"
            placeholder="Enter Alternate Mobile Number"
            onChange={(value) => setAltMobileNumber(value)}
            {...{ required: false, pattern: "[6-9]{1}[0-9]{9}", title: t("CORE_COMMON_APPLICANT_MOBILE_NUMBER_INVALID") }}
          />

          {/* Email */}
          <CardLabel>{`${t("BPA_EMAIL_ID")}`} <span className="check-page-link-button">*</span></CardLabel>
          <TextInput
            t={t}
            type="email"
            name="emailId"
            value={emailId}
            placeholder="Enter Email Id"
            onChange={(e) => setEmail(e.target.value)}
            ValidationRequired={true}
            {...{ pattern: "[A-Za-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,4}$", title: t("BPA_EMAIL_ERROR_MESSAGE") }}
          />

          {/* Father's Name */}
          <CardLabel>{`${t("BPA_FATHER_NAME")}`} <span className="check-page-link-button">*</span></CardLabel>
          <TextInput
            t={t}
            type="text"
            name="fatherName"
            value={fatherName}
            placeholder="Enter Father’s Name"
            onChange={(e) => setFatherName(e.target.value.replace(/[^a-zA-Z\s]/g, ""))}
            ValidationRequired={true}
          />

          {/* Mother's Name */}
          <CardLabel>{`${t("BPA_MOTHER_NAME")}`}</CardLabel>
          <TextInput
            t={t}
            type="text"
            name="motherName"
            value={motherName}
            placeholder="Enter Mother’s Name"
            onChange={(e) => setMotherName(e.target.value.replace(/[^a-zA-Z\s]/g, ""))}
          />

          {/* PAN Card Number */}
          <CardLabel>{`${t("BPA_PAN_NUMBER")}`} <span className="check-page-link-button">*</span></CardLabel>
          <TextInput
            t={t}
            type="text"
            name="panCardNumber"
            value={panCardNumber}
            placeholder="Enter PAN Card Number"
            onChange={(e) => setPanCardNumber(e.target.value.toUpperCase())}
            ValidationRequired={true}
            minLength={10}
            maxLength={10}
            {...{ pattern: "[A-Z]{5}[0-9]{4}[A-Z]{1}", title: t("BPA_PAN_ERROR_MESSAGE") }}
          />

          {/* Aadhaar Number */}
          <CardLabel>{`${t("BPA_AADHAAR_NUMBER")}`} <span className="check-page-link-button">*</span></CardLabel>
          <TextInput
            t={t}
            type="text"
            name="aadhaarNumber"
            value={aadhaarNumber}
            placeholder="Enter Aadhaar Card Number"
            onChange={(e) => setAadhaarNumber(e.target.value.replace(/[^0-9]/g, ""))}
            ValidationRequired={true}
            minLength={12}
            maxLength={12}
            {...{ pattern: "[0-9]{12}", title: t("BPA_AADHAAR_ERROR_MESSAGE") }}
          />
        </div>
      </FormStep>
    </React.Fragment>
  );
};

export default ApplicantDetails;