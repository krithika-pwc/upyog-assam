import { BPAHomeIcon, BPAIcon, CitizenHomeCard, EDCRIcon, EmployeeModuleCard, Loader, Toast } from "@upyog/digit-ui-react-components";
import React, { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useLocation } from "react-router-dom";

const OBPASCitizenHomeScreen = ({ parentRoute }) => {
  const { t } = useTranslation();
  const homeDetails = [
    {
      Icon: <BPAHomeIcon />,
      moduleName: t("ACTION_TEST_OBPAS_RTP_INBOx"),
      name: "employeeCard",
      isCitizen: true,
      kpis: [
        {
          label: t("OBPAS_PDF_TOTAL"),
          link: `/upyog-ui/citizen/obpsv2/rtp/inbox`,
        },
      ],
      links: [
        {
        
          label: t("ES_COMMON_OBPS_RTP_INBOX_LABEL"),
          link: `/upyog-ui/citizen/obpsv2/rtp/inbox`,
        },
        {
          label: t("ES_COMMON_RTP_SEARCH_APPLICATION"),
          link: `/upyog-ui/citizen/obpsv2/rtp/search/application`
        },
      ],
      className: "CitizenHomeCard",
      styles: { padding: "0px", minWidth: "90%", minHeight: "90%" },
    },
    
  ];

  const homeScreen = (
    <div className="mainContent">
      {homeDetails.map((data) => {
        return (
          <div>
            {data.name === "employeeCard" ? (
              <EmployeeModuleCard {...data} />
            ) : (
              <CitizenHomeCard header={data.title} links={data.links} Icon={() => data.Icon} styles={data?.styles} />
            )}
          </div>
        );
      })}
    </div>
  );
  sessionStorage.setItem("isPermitApplication", true);
  sessionStorage.setItem("isEDCRDisable", JSON.stringify(false));
  return homeScreen;
};

export default OBPASCitizenHomeScreen;