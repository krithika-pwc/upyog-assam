import React, { useMemo, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { OBPSIconSolidBg } from "@upyog/digit-ui-react-components";
import {ReportModuleCard} from "./../../components/ReportModuleCard";

const OBPSReportCard = () => {
    const user = Digit.UserService.getUser();
    const { t } = useTranslation();
    const propsForModuleCard = useMemo(()=>({
      Icon: <OBPSIconSolidBg />,
      moduleName:<div style={{ width: "200px", wordWrap: "break-word" }}>{t("ACTION_TEST_REPORTS")}</div>,
      links: [
        {
          label: t("ES_COMMON_REPORTS"),
          link: `/employee/report/rainmaker-obps/obpsApplicationReport`,
          role: "OBPS_REPORT_VIEWER",
          field: "REPORT"
        },
      ]
    }),[t]);
    const reportAccess=  user?.info?.roles?.some((role) => role.code === "OBPS_REPORT_VIEWER");
    if (!reportAccess) {
      propsForModuleCard.links = propsForModuleCard.links.filter(obj => {
        return obj.field !== 'REPORT';
      });
    }

    return reportAccess ? <ReportModuleCard {...propsForModuleCard} /> : null
  }

  export default OBPSReportCard