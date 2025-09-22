import React, { Fragment, useMemo } from "react"
import { Link } from "react-router-dom";
import { format } from "date-fns";
import { useTranslation } from "react-i18next";
import { Dropdown } from "@upyog/digit-ui-react-components";
import { OBPSV2Services } from "../../../../../../libraries/src/services/elements/OBPSV2";
const useInboxTableConfig = ({ parentRoute, onPageSizeChange, formState, totalCount, table, dispatch, onSortingByData}) => {
    const GetCell = (value) => <span className="cell-text styled-cell">{value}</span>;
    const GetStatusCell = (value) => value === "CS_NA" ? t(value) : value === "Active" || value>0 ? <span className="sla-cell-success">{value}</span> : <span className="sla-cell-error">{value}</span> 
    const { t } = useTranslation()
    
    const tableColumnConfig = useMemo(() => {
        return [
        {
            Header: t("BPA_APPLICATION_NUMBER_LABEL"),
            accessor: "applicationNo",
            disableSortBy: true,
            Cell: ({ row }) => {
                
            return (
                <div>
                <Link to={window.location.href.includes("/citizen") ? `${parentRoute}/bpa/${row?.original["applicationId"]}` : `${parentRoute}/inbox/bpa/${row.original["applicationId"]}`}>
                    <span className="link">{row?.original["applicationId"]||"NA"}</span>
                </Link>
                </div>
            );
            },
        },
        {
            Header: t("APPLICANT_NAME"),
            accessor: "applicantName",
            Cell: ({row}) => row?.original?.applicantName
            },
            {
            Header: t("FATHERS_NAME"),
            accessor: "fatherOrHusbandName",
            Cell: ({row}) => row?.original?.fatherOrHusbandName
            },
            {
            Header: t("MOBILE_NUMBER"),
            accessor: "mobileNumber",
            Cell: ({row}) => row?.original?.mobileNumber
            },
        {
            Header: t("ES_INBOX_LOCALITY"),
            accessor: (row) => t(row?.original?.locality),
            Cell: ({row}) => row?.original?.locality,
        },
        {
            Header: t("WARD_NUMBER"),
            accessor: "wardNo",
            Cell: ({row}) => row?.original?.wardNo
            },
        
        {
            Header: t("SLA"),
            accessor: row => GetStatusCell(row?.original?.sla),
            Cell: ({row}) => row?.original?.sla
        },
        {
            Header: t("ACTION"),
            accessor: "action",
            disableSortBy: true,
            Cell: ({ row }) => {
                const options = row?.original?.nextActions?.nextActions?.map((action) => ({
                    code: action?.action,
                    i18nKey: action?.action
                })) || [];

                
                const handleSelect = async (value) => {
                if (value.code === "APPROVE") {
                    window.location.href = `${parentRoute}/editApplication/${row?.original["applicationId"]}`;
                } else if (value.code === "View Summary") {
                    window.location.href = `${parentRoute}/application/${row?.original["applicationId"]}/${row?.original["tenantId"]}`;
                }
                else {
                    let applicationNo = row?.original?.applicationId;
                    let tenantId = row?.original?.tenantId
                    const bpaDetails = await OBPSV2Services.search({
                        tenantId,
                        filters: { applicationNo },
                        config: { staleTime: Infinity, cacheTime: Infinity }
                    });
                
                    if(bpaDetails?.bpa){
                        // bpaDetails.bpa[0].workflow.action = "APPROVE";
                        //     bpaDetails.bpa[0].workflow.assignes = null;
                        //     bpaDetails.bpa[0].workflow.comments = null;
                        const response = await OBPSV2Services.update(bpaDetails, tenantId);
                    }
                    
                }
                 };

                return (
                <Dropdown
                    t={t}
                    option={options}
                    optionKey="i18nKey"
                    id={`action-${row.original.applicationId}`}
                    selected={null} // nothing selected by default
                    select={handleSelect}
                    placeholder={t("Take Action")}
                />
                );
            },
            }

        ]
    })

    return {
        getCellProps: (cellInfo) => {
        return {
            style: {
            padding: "20px 18px",
            fontSize: "16px"
        }}},
        disableSort: false,
        autoSort:false,
        manualPagination:true,
        initSortId:"applicationDate",
        onPageSizeChange:onPageSizeChange,
        currentPage: formState.tableForm?.offset / formState.tableForm?.limit,
        onNextPage: () => dispatch({action: "mutateTableForm", data: {...formState.tableForm , offset: (parseInt(formState.tableForm?.offset) + parseInt(formState.tableForm?.limit)) }}),
        onPrevPage: () => dispatch({action: "mutateTableForm", data: {...formState.tableForm , offset: (parseInt(formState.tableForm?.offset) - parseInt(formState.tableForm?.limit)) }}),
        pageSizeLimit: formState.tableForm?.limit,
        onSort: onSortingByData,
        // sortParams: [{id: getValues("sortBy"), desc: getValues("sortOrder") === "DESC" ? true : false}],
        totalRecords: totalCount,
        onSearch: formState?.searchForm?.message,
        onLastPage: () => dispatch({action: "mutateTableForm", data: {...formState.tableForm , offset: (Math.ceil(totalCount / 10) * 10 - parseInt(formState.tableForm?.limit)) }}),
        onFirstPage: () => dispatch({action: "mutateTableForm", data: {...formState.tableForm , offset: 0 }}),
        // globalSearch: {searchForItemsInTable},
        // searchQueryForTable,
        data: table,
        columns: tableColumnConfig
    }
}

export default useInboxTableConfig