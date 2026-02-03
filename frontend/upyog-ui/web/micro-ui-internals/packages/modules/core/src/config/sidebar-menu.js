import React from "react";
import { HomeIcon, LanguageIcon, LogoutIcon, AddressBookIcon, LocationIcon, LoginIcon } from "@upyog/digit-ui-react-components";
import ChangeLanguage from "../components/ChangeLanguage";

const RTPS = [
  "BPA_ARCHITECT",
  "BPA_BUILDER",
  "BPA_ENGINEER",
  "BPA_STRUCTURALENGINEER",
  "BPA_TOWNPLANNER",
  "BPA_SUPERVISOR",
  "BPA_GEO_TECH_ENGINEER",
  "BPA_CIVIL_ENGINEER",
  "BPA_UTILITY_ENGINEER",
  "BPA_LANDSCAPE_ARCHITECT",
  "BPA_GROUP_AGENCY",
  "BPA_URBAN_DESIGNER",
  "BPA_RTP"
];

const SideBarMenu = (t, closeSidebar, redirectToLoginPage, redirectToScrutinyPage ,isEmployee, storeData, tenantId) => {
  let filteredTenantData = storeData?.tenants.filter((e) => e.code === tenantId)[0]?.contactNumber || storeData?.tenants[0]?.contactNumber;
  const userInfo = Digit.UserService.getUser();
  const userRoles = userInfo?.info?.roles?.map((roleData) => roleData.code);
return [
  {
    type: "link",
    element: "HOME",
    text: t("COMMON_BOTTOM_NAVIGATION_HOME"),
    link: isEmployee ? "/upyog-ui/employee" : "/upyog-ui/citizen",
    icon: "HomeIcon",
    populators: {
      onClick: closeSidebar,
    },
  },
  {
    type: "component",
    element: "LANGUAGE",
    action: <ChangeLanguage />,
    icon: "LanguageIcon",
  },
  {
    id: "login-btn",
    element: "LOGIN",
    text: t("CORE_COMMON_LOGIN"),
    icon: "LoginIcon",
    populators: {
      onClick: redirectToLoginPage,
    },
  },
  ...(userRoles?.some(role => RTPS.includes(role)) ? [{
    text: t("Scrutiny"),
    icon: "",
    populators: {
      onClick: redirectToScrutinyPage,
    },
  }] : []),
  {
    id: "help-line",
    text: (
      <React.Fragment>
        {t("CS_COMMON_HELPLINE")}
        <div className="telephone" style={{ marginTop: "-10%" }}>
          <div className="link">
            <a href={`tel:${filteredTenantData}`}>{filteredTenantData}</a>
          </div>
        </div>
      </React.Fragment>
    ),
    element: "Helpline",
    icon: "Phone",
  },
]
};

export default SideBarMenu;
