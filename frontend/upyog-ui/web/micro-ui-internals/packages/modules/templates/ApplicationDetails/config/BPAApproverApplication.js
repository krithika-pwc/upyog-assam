import { Dropdown, UploadFile } from "@upyog/digit-ui-react-components";
import React from "react";

export const configBPAApproverApplication = ({
  t,
  action,
  approvers,
  selectedApprover,
  setSelectedApprover,
  selectFile,
  uploadedFile,
  setUploadedFile,
  assigneeLabel,
  businessService,
  error,
  showEsignModal,
  dscTokens = [],
  selectedDscToken,
  setSelectedDscToken,
  dscCertificates = [],
  selectedCertificate,
  setSelectedCertificate,
  isCertLoading,
}) => {
  let isRejectOrRevocate = false;
  if(action?.action == "REVOCATE" || action?.action == "REJECT" || action.action == "SKIP_PAYMENT" || action?.action == "SEND_BACK_TO_CITIZEN" || action?.action == "APPROVE") {
    isRejectOrRevocate = true;
  }

  let isCommentRequired = false;
  if(action?.action == "REVOCATE" || action?.action == "REJECT") {
    isCommentRequired = true;
  }

if (action?.action === "DSC") {
  return {
    label: {
      heading: `WF_${action?.action}_DSC`,
      submit: `WF_${businessService}_${action?.action}`,
      cancel: "BPA_CITIZEN_CANCEL_BUTTON",
    },
    form: [
      {
        body: [
          {
            label: t("WF_TOKEN"),
            type: "dropdown",
            populators: (
              <Dropdown
                option={dscTokens}
                optionKey="name"
                id="fieldInspector"
                select={setSelectedDscToken}
                selected={selectedDscToken}
                placeholder={t("WF_SELECT_TOKEN")}
              />
            ),
          },
          {
            label: t("WF_CERTIFICATE"),
            type: "dropdown",
            populators: (
              <Dropdown
                option={dscCertificates || []}
                optionKey="name"
                id="fieldInspector"
                select={setSelectedCertificate}
                selected={selectedCertificate}
                placeholder={t("WF_SELECT_CERTIFICATE")}
              />
            ),
          }
        ],
      },
    ],
  };
}


  if (window.location.href.includes("obpsv2")) {
  return {
      label: {
        heading: `WF_${action?.action}_APPLICATION`,
        submit: `WF_${businessService}_${action?.action}`,
        cancel: "BPA_CITIZEN_CANCEL_BUTTON",
      },
      form: [
        {
          body: [
            {
              label: t("WF_COMMON_COMMENTS"),
              type: "textarea",
              isMandatory: isCommentRequired,
              populators: {
                name: "comments",
              },
            },
            {
              label: t("WF_APPROVAL_UPLOAD_HEAD"),
              populators: (
                <UploadFile
                  id="workflow-doc"
                  onUpload={selectFile}
                  onDelete={() => setUploadedFile(null)}
                  message={
                    uploadedFile
                      ? `1 ${t("ES_PT_ACTION_FILEUPLOADED")}`
                      : t("CS_ACTION_NO_FILEUPLOADED")
                  }
                  accept=".pdf, .png, .jpeg, .jpg, image/*"
                  iserror={error}
                />
              ),
            },
          ],
        },
      ],
    };
  }

  return {
    label: {
      heading: `WF_${action?.action}_APPLICATION`,
      submit: `WF_${businessService}_${action?.action}`,
      cancel: "BPA_CITIZEN_CANCEL_BUTTON",
    },
    form: [
      {
        body: [
          {
            label: action.isTerminateState || isRejectOrRevocate ? null : t(assigneeLabel || `WF_ROLE_${action.assigneeRoles?.[0]}`),
            type: "dropdown",
            populators: action.isTerminateState || isRejectOrRevocate ? null : (
              <Dropdown
                option={approvers}
                autoComplete="off"
                optionKey="name"
                id="fieldInspector"
                select={setSelectedApprover}
                selected={selectedApprover}
              />
            ),
          },
          {
            label: t("WF_COMMON_COMMENTS"),
            type: "textarea",
            isMandatory: true,
            populators: {
              name: "comments",
            },
          },
          {
            label: `${t("WF_APPROVAL_UPLOAD_HEAD")}`,
            populators: (
              <UploadFile
                id={"workflow-doc"}
                onUpload={selectFile}
                onDelete={() => {
                  setUploadedFile(null);
                }}
                message={uploadedFile ? `1 ${t(`ES_PT_ACTION_FILEUPLOADED`)}` : t(`CS_ACTION_NO_FILEUPLOADED`)}
                accept= "image/*, .pdf, .png, .jpeg, .jpg"
                iserror={error}
              />
            ),
          },
        ],
      },
    ],
  };
};
