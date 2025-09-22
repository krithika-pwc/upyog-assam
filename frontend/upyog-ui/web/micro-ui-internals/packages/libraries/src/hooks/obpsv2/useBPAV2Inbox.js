import useInbox from "../useInbox"

const useBPAV2Inbox = ({ tenantId, filters, config={} }) => {
    const { filterForm, searchForm , tableForm } = filters;
    const user = Digit.UserService.getUser();
    let { moduleName, businessService, applicationStatus, locality, assignee, applicationType } = filterForm;
    const { mobileNumber, applicationNo } = searchForm;
    const { sortBy, limit, offset, sortOrder } = tableForm;
    let applicationNumber = "";
    let _filters = {
        tenantId,
        processSearchCriteria: {
          assignee : assignee === "ASSIGNED_TO_ME"?user?.info?.uuid:"",
          moduleName: "bpa-services", 
          businessService: ["BPA_GMDA_GMC"],
          ...(applicationStatus?.length > 0 ? {status: applicationStatus} : {}),
        },
        moduleSearchCriteria: {
          ...(mobileNumber ? {mobileNumber}: {}),
          ...(!applicationNumber ? applicationNo ? {applicationNo} : {} : (applicationNumber ? {applicationNumber} : {})),
          ...(applicationNumber ? {applicationNumber} : {}),
          ...(sortOrder ? {sortOrder} : {}),
          ...(sortBy ? {sortBy} : {}),
          // ...(applicationType?.length > 0 ? {applicationType: applicationType.map((item) => item.code).join(",")} : {}),
          ...(applicationType && applicationType?.length > 0 ? {applicationType} : {}),
          ...(locality?.length > 0 ? {locality: locality.map((item) => item.code.split("_").pop()).join(",")} : {}),
        },
        limit
    }

    if (!applicationNo) {
      _filters = { ..._filters, offset}
    }
     return useInbox({
  tenantId,
  filters: _filters,
  config: {
    select: (data) => {

      return {
        statuses: data.statusMap,
        table:
          data?.items?.map((application) => ({
            applicationId:
              application?.businessObject?.applicationNo || "NA",
            // date: application?.businessObject?.auditDetails?.createdTime,
            applicantName:
              application?.businessObject?.landInfo?.owners?.[0]?.name || "NA",
            fatherOrHusbandName:
              application?.businessObject?.landInfo?.owners?.[0]
                ?.fatherOrHusbandName || "NA",
            mobileNumber:
              application?.businessObject?.landInfo?.owners?.[0]?.mobileNumber ||
              "NA",
            locality:
              application?.businessObject?.landInfo?.ownerAddresses?.[0]
                ?.locality || "NA",
            wardNo:
              application?.businessObject?.areaMapping?.ward || "NA",
            status: application?.businessObject?.status || "NA",
            nextActions : application?.ProcessInstance,
            sla: Math.round(
              application?.ProcessInstance?.businesssServiceSla /
                (24 * 60 * 60 * 1000)
            ),
            tenantId: application?.businessObject?.tenantId,
          })) || [],
        totalCount: data?.totalCount,
        
        nearingSlaCount: data?.nearingSlaCount,
      };
    },
    ...config,
  },
});




}

export default useBPAV2Inbox
