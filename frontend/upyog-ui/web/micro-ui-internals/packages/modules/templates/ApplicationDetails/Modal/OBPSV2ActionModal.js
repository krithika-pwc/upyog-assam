import { Loader, Modal} from "@upyog/digit-ui-react-components";
import React, { useState, useEffect } from "react";
import { useQueryClient } from "react-query";
import { configBPAApproverApplication } from "../config";
import * as predefinedConfig from "../config";
import  FormComposer  from "../../../../react-components/src/hoc/FormComposer";
import { useHistory } from "react-router-dom";

const Heading = (props) => {
  return <h1 style={{marginLeft:"22px"}} className="heading-m BPAheading-m">{props.label}</h1>;
};

const Close = () => (
  <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="#FFFFFF">
    <path d="M0 0h24v24H0V0z" fill="none" />
    <path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12 19 6.41z" />
  </svg>
);

const CloseBtn = (props) => {
  return (
    <div className="icon-bg-secondary" onClick={props.onClick}>
      <Close />
    </div>
  );
};

const ActionModal = ({ t, action, tenantId, state, id, closeModal, submitAction, actionData, applicationDetails, applicationData, businessService, moduleCode,workflowDetails }) => {
  const mutation1 = Digit.Hooks.obps.useObpsAPI(
    applicationData?.landInfo?.address?.city ? applicationData?.landInfo?.address?.city : tenantId,
    false
  );
  const { data: approverData, isLoading: PTALoading } = Digit.Hooks.useEmployeeSearch(
    tenantId,
    {
      roles: workflowDetails?.data?.initialActionState?.nextActions?.filter(ele=>ele?.action==action?.action)?.[0]?.assigneeRoles?.map(role=>({code:role})),
      isActive: true,
    },
    { enabled: !action?.isTerminateState }
  );

  const queryClient = useQueryClient();
  const [config, setConfig] = useState({});
  const [defaultValues, setDefaultValues] = useState({});
  const [approvers, setApprovers] = useState([]);
  const [selectedApprover, setSelectedApprover] = useState({});
  const [file, setFile] = useState(null);
  const [uploadedFile, setUploadedFile] = useState(null);
  const [error, setError] = useState(null);
  const [selectedFinancialYear, setSelectedFinancialYear] = useState(null);
  const [showEsignModal, setShowEsignModal] = useState(false);
  const [dscTokens, setDscTokens] = useState([]);
  const [isDscLoading, setIsDscLoading] = useState(false);
  const [selectedDscToken, setSelectedDscToken] = useState(null);
  const [dscCertificates, setDscCertificates] = useState([]);
  const [isCertLoading, setIsCertLoading] = useState(false);
  const [selectedCertificate, setSelectedCertificate] = useState(null);
  const [selectedCertificateKeyId, setSelectedCertificateKeyId] = useState(null);
  const [certificateResponse, setCertificateResponse] = useState([]);

  const mobileView = Digit.Utils.browser.isMobile() ? true : false;
  const history = useHistory();
  useEffect(() => {
    setApprovers(approverData?.Employees?.map((employee) => ({ uuid: employee?.uuid, name: employee?.user?.name })));
  }, [approverData]);

  function selectFile(e) {
    setFile(e.target.files[0]);
  }

  useEffect(() => {
    (async () => {
      setError(null);
      if (file) {
        const allowedFileTypesRegex = /(.*?)(jpg|jpeg|png|image|pdf)$/i
        if (file.size >= 5242880) {
          setError(t("CS_MAXIMUM_UPLOAD_SIZE_EXCEEDED"));
        } else if (file?.type && !allowedFileTypesRegex.test(file?.type)) {
          setError(t(`NOT_SUPPORTED_FILE_TYPE`))
        } else {
          try {
            const response = await Digit.UploadServices.Filestorage("OBPS", file, Digit.ULBService.getStateId() || tenantId?.split(".")[0]);
            if (response?.data?.files?.length > 0) {
              setUploadedFile(response?.data?.files[0]?.fileStoreId);
            } else {
              setError(t("CS_FILE_UPLOAD_ERROR"));
            }
          } catch (err) {
            setError(t("CS_FILE_UPLOAD_ERROR"));
          }
        }
      }
    })();
  }, [file]);




  const getSubmitReport = (data) => {
    let inspectionOb = [];
    const formdata = JSON.parse(sessionStorage.getItem("SUBMIT_REPORT_DATA"));
    if (formdata) {
       inspectionOb.push(formdata.submitReport);
     }
    return inspectionOb;
  };

  const getChiecklistQuestions = ()=>{
    let checklistQuestion = [];
    const newData = JSON.parse(sessionStorage.getItem("SUBMIT_REPORT_DATA"));
    if(newData){
      checklistQuestion.push(newData.siteInspectionQuestions)
    }
    return checklistQuestion;
  }

  const getDocuments = (applicationData) => {
    let documentsformdata = JSON.parse(sessionStorage.getItem("OBPS_DOCUMENTS"));
    let documentList = [];
    documentsformdata && documentsformdata.map(doc => {
      if(doc?.uploadedDocuments?.[0]?.values?.length > 0) documentList = [...documentList, ...doc?.uploadedDocuments?.[0]?.values];
      if(doc?.newUploadedDocs?.length > 0) documentList = [...documentList, ...doc?.newUploadedDocs]
    });
    const submitReportFormdata = JSON.parse(sessionStorage.getItem("SUBMIT_REPORT_DATA"));
    const nocDocuments = submitReportFormdata?.nocDetails?.AAI_NOC_DETAILS?.[0]?.documents;
    if (Array.isArray(nocDocuments) && nocDocuments.length > 0) {
      documentList = [...documentList, ...nocDocuments];
    }
    return documentList;
  }

  const getPendingApprovals = () => {
    const approvals = Digit.SessionStorage.get("OBPS_APPROVAL_CHECKS");
    const newApprovals = Digit.SessionStorage.get("OBPS_NEW_APPROVALS");
    let result = approvals?.reduce((acc, approval) => approval?.checked ? acc.push(approval?.label) && acc : acc, []);
    result = result?.concat(newApprovals !== null?newApprovals.filter(ob => ob.label !== "").map(approval => approval?.label):[]);
    return result;
  }

  async function submit(data) {
    if (action?.action === "SUBMIT_REPORT") {
    const storedData = JSON.parse(sessionStorage.getItem("SUBMIT_REPORT_DATA")) || {};
    const submitReport = getSubmitReport(applicationData);
    const nocList = storedData.nocList || [];
    const nocDetails = storedData.nocDetails || {};
    const getCheckList = getChiecklistQuestions();

    if(!nocList.includes("CIVIL_AVIATION")){
      // AAI_NOC_DETAILS contains details related to Civil Aviation NOC and if not selected by the user, it is removed here from nocDetails
      delete nocDetails.AAI_NOC_DETAILS;
    }

    nocDetails.permitType = "Planning Permit";
    applicationData = {
      ...applicationData,
      nocList: nocList,
      documents: getDocuments(applicationData),
      additionalDetails: {
        ...applicationData?.additionalDetails,
        submitReportinspection_pending: submitReport,
        inspectionChecklist: getCheckList,
        nocDetails: nocDetails,
        pendingapproval: getPendingApprovals(),
         adjoiningOwners: {
            ...applicationData?.additionalDetails?.adjoiningOwners,

            north:
              applicationData?.additionalDetails?.submitReportinspection_pending?.north ??
              applicationData?.additionalDetails?.adjoiningOwners?.north,

            south:
              applicationData?.additionalDetails?.submitReportinspection_pending?.south ??
              applicationData?.additionalDetails?.adjoiningOwners?.south,

            east:
              applicationData?.additionalDetails?.submitReportinspection_pending?.east ??
              applicationData?.additionalDetails?.adjoiningOwners?.east,

            west:
              applicationData?.additionalDetails?.submitReportinspection_pending?.west ??
              applicationData?.additionalDetails?.adjoiningOwners?.west,
          }
      },
       workflow:{
        action: action?.action,
        comment: data?.comments?.length > 0 ? data?.comments : null,
        assignes: (["SEND_BACK_TO_RTP"].includes(action?.action) && applicationData?.status === "PENDING_DA_ENGINEER") ? [applicationData?.rtpDetails?.rtpUUID] : null,
        varificationDocuments: uploadedFile
        ? [
          {
            documentType: action?.action + " DOC",
            fileName: file?.name,
            fileStoreId: uploadedFile,
          },
        ]
        : null
      }
    };
    } else if (action?.action === "DSC") {
      if (applicationData?.status === "PENDING_DSC") {
      const signedPPFileStoreId = await handlePlanningPermitOrder();
                applicationData = {
        ...applicationData,
        signedPpFileStoreId: signedPPFileStoreId,
        workflow:{
          action: action?.action,
          comment: data?.comments?.length > 0 ? data?.comments : null,
          assignes: (["SEND_BACK_TO_RTP"].includes(action?.action) && applicationData?.status === "PENDING_DA_ENGINEER") ? [applicationData?.rtpDetails?.rtpUUID] : null,
          varificationDocuments: uploadedFile
          ? [
            {
              documentType: action?.action + " DOC",
              fileName: file?.name,
              fileStoreId: uploadedFile,
            },
          ]
          : null
        }
      };
      }
      if (applicationData?.status === "PENDING_FINAL_DSC") {
      const signedBPFileStoreId = await handleBuildingPermitOrder();
                applicationData = {
        ...applicationData,
        signedBpFileStoreId: signedBPFileStoreId,
        workflow:{
          action: action?.action,
          comment: data?.comments?.length > 0 ? data?.comments : null,
          assignes: (["SEND_BACK_TO_RTP"].includes(action?.action) && applicationData?.status === "PENDING_DA_ENGINEER") ? [applicationData?.rtpDetails?.rtpUUID] : null,
          varificationDocuments: uploadedFile
          ? [
            {
              documentType: action?.action + " DOC",
              fileName: file?.name,
              fileStoreId: uploadedFile,
            },
          ]
          : null
        }
      };
      }
    }
     else {
      applicationData = {
        ...applicationData,
        workflow:{
          action: action?.action,
          comment: data?.comments?.length > 0 ? data?.comments : null,
          assignes: (["SEND_BACK_TO_RTP"].includes(action?.action) && applicationData?.status === "PENDING_DA_ENGINEER") ? [applicationData?.rtpDetails?.rtpUUID] : null,
          varificationDocuments: uploadedFile
          ? [
            {
              documentType: action?.action + " DOC",
              fileName: file?.name,
              fileStoreId: uploadedFile,
            },
          ]
          : null
        }
      };
    }
    submitAction({
      BPA:applicationData
    });
  }

  // console.log("applicationData", applicationData);
const fetchDscTokens = async () => {
  try {
    setIsDscLoading(true);

    console.log("Step 1: Calling dscSearchInputToken");
    console.log("TenantId:", tenantId);

    const inputTokenRes =
      await Digit.OBPSV2Services.dscSearchInputToken({ tenantId });

    console.log("Input Token Response:", inputTokenRes);
 

    const encryptedRequest =
      inputTokenRes?.input?.encryptedRequest;
    const encryptionKeyId =
      inputTokenRes?.input?.encryptionKeyId;

    console.log("encryptedRequest", inputTokenRes?.input?.encryptedRequest);
    console.log("encryptionKeyId", inputTokenRes?.input?.encryptionKeyId);

    if (!encryptedRequest || !encryptionKeyId) {
      throw new Error("Invalid input token response");
    }

    console.log("Step 2: Calling dscSearchListToken");

    const listTokenRes =
      await Digit.OBPSV2Services.dscSearchListToken({
        encryptedRequest: encryptedRequest,
        encryptionKeyId: encryptionKeyId,
      });

    console.log("List Token Response:", listTokenRes);

    const responseData = listTokenRes?.responseData;

    if (!responseData) {
      throw new Error("Invalid list token response");
    }

    console.log("Step 3: Calling dscSearchToken");

    const finalTokenRes =
      await Digit.OBPSV2Services.dscSearchToken({
        tenantId: tenantId,
        responseData: responseData,
      });

    console.log("Final Token Response:", finalTokenRes);

    const tokens =
      finalTokenRes?.tokens?.map((token) => ({
        code: token,
        name: token,
        i18nKey: token,
      })) || [];

    console.log("Final DSC Tokens:", tokens);

    setDscTokens(tokens);

  } catch (error) {
    console.error("DSC token fetch failed", error);
  } finally {
    setIsDscLoading(false);
  }
};

  useEffect(() => {
    if (action?.action === "DSC") {
      fetchDscTokens();
    }
  }, [action]);

  useEffect(() => {
    if (!isDscLoading && dscTokens.length > 0) {
      setShowEsignModal(true);
    }
  }, [dscTokens, isDscLoading]);

    const fetchDscCertificates = async () => {
    try {
      setIsCertLoading(true);
      // setDscCertificates([]);

      const inputCertRes =
        await Digit.OBPSV2Services.dscSearchInputCertificate({ tenantId });

      const encryptedRequest =
        inputCertRes?.input?.encryptedRequest;
      const encryptionKeyId =
        inputCertRes?.input?.encryptionKeyId;

      if (!encryptedRequest || !encryptionKeyId) {
        throw new Error("Invalid certificate input token");
      }

      const listCertRes =
        await Digit.OBPSV2Services.dscSearchListCertificate({
          encryptedRequest: encryptedRequest,
          encryptionKeyId: encryptionKeyId,
        });

      const responseData = listCertRes?.responseData;
      if (!responseData) {
        throw new Error("Invalid certificate list response");
      }

      const finalCertRes =
      await Digit.OBPSV2Services.dscSearchCertificate({
        tenantId,
        responseData,
      });

    setCertificateResponse(finalCertRes?.certificates || []);

    const certificates =
      finalCertRes?.certificates?.map((cert) => ({
        code: cert.commonName,
        name: cert.commonName,
        i18nKey: cert.commonName,
      })) || [];

    setDscCertificates(certificates);

    } catch (err) {
      console.error("Certificate fetch failed", err);
    } finally {
      setIsCertLoading(false);
    }
  };
  useEffect(() => {
    if (selectedDscToken) {
      fetchDscCertificates();
    }
  }, [selectedDscToken]);

    useEffect(() => {
    setSelectedCertificate(null);
    setDscCertificates([]);
  }, [selectedDscToken]);

  useEffect(() => {
    console.log("CERTIFICATES STATE UPDATED", dscCertificates);
  }, [dscCertificates]);

  useEffect(() => {
  if (!selectedCertificate || !certificateResponse?.length) return;

  const matchedCert = certificateResponse.find(
    (cert) => cert.commonName === selectedCertificate.code
  );

  if (matchedCert?.keyId) {
    setSelectedCertificateKeyId(matchedCert.keyId);
  }
}, [selectedCertificate, certificateResponse]);


 const handlePlanningPermitOrder = async () => {
      const application = applicationData;
      let fileStoreId = application?.ppFileStoreId;
      const edcrResponse = await Digit.OBPSService.scrutinyDetails("assam", { edcrNumber: applicationData?.edcrNumber });
        let edcrDetail = edcrResponse?.edcrDetail?.[0];
        const gisResponse = await Digit.OBPSV2Services.gisSearch({
          GisSearchCriteria: {
            applicationNo: application?.applicationNo,
            tenantId: application?.tenantId,
            status: "SUCCESS"
          }
        });
      if (!fileStoreId) {
        const response = await Digit.PaymentService.generatePdf(
          application?.tenantId,
          {Bpa : [{...application, edcrDetail: [{ ...edcrDetail }], gisResponse}]},
          "bpaPlanningPermit"
        );
  
        fileStoreId = response?.filestoreIds?.[0];
        const signedFileStoreIds = await signPdfWithDSC(fileStoreId);

      return signedFileStoreIds;
        // const updatedApplication = {
        // ...application,
        // signedPpFileStoreId: fileStoreId,
        // additionalDetails: {
        // ...application.additionalDetails,
        // UPDATE_FILESTORE_ID: true
        // }
        // };

        // await Digit.OBPSV2Services.update({
        // BPA: updatedApplication
        // });
  
      };
  
      const fileStore = await Digit.PaymentService.printReciept(
        tenantId,
        { fileStoreIds: fileStoreId }
      );
  
      window.open(fileStore[fileStoreId], "_blank");
    };

  const handleBuildingPermitOrder = async () => {
          const application = applicationData;
          let fileStoreId = application?.bpFileStoreId;
          const edcrResponse = await Digit.OBPSService.scrutinyDetails("assam", { edcrNumber: applicationData?.edcrNumber });
            let edcrDetail = edcrResponse?.edcrDetail?.[0];
            const gisResponse = await Digit.OBPSV2Services.gisSearch({
              GisSearchCriteria: {
                applicationNo: application?.applicationNo,
                tenantId: application?.tenantId,
                status: "SUCCESS"
              }
            });
          if (!fileStoreId) {
            const response = await Digit.PaymentService.generatePdf(
              application?.tenantId,
              {
                Bpa: [{...application, edcrDetail: [{ ...edcrDetail }], gisResponse}] 
              },
              "bpaBuildingPermit"
            );
      
            fileStoreId = response?.filestoreIds?.[0];
            const signedFileStoreId = await signPdfWithDSC(fileStoreId);
            return signedFileStoreId;
            // fileStoreId = signedFileStoreId;

            //   const updatedApplication = {
            //     ...application,
            //     signedBpFileStoreId: fileStoreId,
            //     additionalDetails: {
            //       ...application.additionalDetails,
            //       UPDATE_FILESTORE_ID: true
            //     }
            //   };

            //   await Digit.OBPSV2Services.update({
            //     BPA: updatedApplication
            //   });
      
          }
      
          const fileStore = await Digit.PaymentService.printReciept(
            tenantId,
            { fileStoreIds: fileStoreId }
          );
      
          window.open(fileStore[fileStoreId], "_blank");
  };

    const signPdfWithDSC = async (fileStoreId) => {
    const metaRes = await Digit.OBPSV2Services.dscGetFileMetaData({
      tenantId: applicationData?.tenantId,
      fileStoreId,
    });

    const inputRes = await Digit.OBPSV2Services.dscGetPdfSignInput({
      tokenDisplayName: selectedDscToken.code,
      keyStorePassPhrase: "12345678",
      keyId: selectedCertificateKeyId,
      file: fileStoreId,
      fileName: metaRes.fileName,
      tenantId: applicationData?.tenantId,
    });

    const pkcsRes = await Digit.OBPSV2Services.dscGetPKCSBulkSign({
      encryptedRequest: inputRes.input.encryptedRequest,
      encryptionKeyId: inputRes.input.encryptionKeyId
    });

  const signRes = await Digit.OBPSV2Services.dscGetPdfSign({
      responseData: pkcsRes?.responseData,
      tempFilePath: inputRes?.input.tempFilePath,
      tenantId: applicationData?.tenantId,
      moduleName: "esign"
    });
      return signRes?.fileStoreId;
  };


  useEffect(() => {
    if (action) {
      if (action.action === "PAY") {
        let servicePath = "BPA.PLANNING_PERMIT_FEE";
        if (applicationData?.status === "CITIZEN_FINAL_PAYMENT") {
          servicePath = "BPA.BUILDING_PERMIT_FEE";
        }
        console.log(servicePath);
        return history.push(`/upyog-ui/employee/payment/collect/${servicePath}/${applicationData.applicationNo}`);
      }
      setConfig(
        configBPAApproverApplication({
         t,
      action,
      approvers,
      selectedApprover,
      setSelectedApprover,
      selectFile,
      uploadedFile,
      setUploadedFile,
      businessService,
      assigneeLabel: "WF_ASSIGNEE_NAME_LABEL",
      error,
      showEsignModal,
      dscTokens,
      selectedDscToken,
      setSelectedDscToken,
      dscCertificates,
      selectedCertificate,
      setSelectedCertificate,
        })
      );
    }
  }, [action,
  approvers,
  uploadedFile,
  error,
  showEsignModal,
  dscTokens,
  selectedDscToken,
  dscCertificates,
  selectedCertificate ]);

    if (action?.action === "DSC" && (isDscLoading || dscTokens.length === 0)) {
      return (
          <Loader />
      );
    }

    return action && config.form ? (
      <Modal
        headerBarMain={<Heading label={t(config.label.heading)} />}
        headerBarEnd={<CloseBtn onClick={closeModal} />}
        actionCancelLabel={t(config.label.cancel)}
        actionCancelOnSubmit={closeModal}
        actionSaveLabel={t(config.label.submit)}
        actionSaveOnSubmit={() => { }}
        formId="modal-action"
        isOBPSFlow={true}
        popupStyles={mobileView?{width:"720px"}:{}}
        style={!mobileView?{minHeight: "45px", height: "auto", width:"107px",paddingLeft:"0px",paddingRight:"0px"}:{minHeight: "45px", height: "auto",width:"44%"}}
        popupModuleMianStyles={mobileView?{paddingLeft:"5px"}: {}}
      >
      {(
        <FormComposer
          config={config.form}
          cardStyle={{marginLeft:"0px",marginRight:"0px", marginTop:"-25px"}}
          className="BPAemployeeCard"
          noBoxShadow
          inline
          childrenAtTheBottom
          onSubmit={submit}
          defaultValues={defaultValues}
          formId="modal-action"
        />
      )}
      </Modal>
    ) : (
      <Loader />
);
};

export default ActionModal;