import React from "react";
import { Link } from "react-router-dom";
import { useTranslation } from "react-i18next";

const CitizenHomeCard = ({ header, links = [], state, Icon, Info, isInfo = false, styles }) => {
  const { t } = useTranslation();
  function replaceDigitUiWithUpyogUi(data) {
    return data.map(item => ({
      ...item,
      sidebarURL: item.sidebarURL ? item.sidebarURL.replace("digit-ui", "upyog-ui") : item.sidebarURL,
      navigationURL: item.navigationURL ? item.navigationURL.replace("digit-ui", "upyog-ui") : item.navigationURL,
      link: item.link ? item.link.replace("digit-ui", "upyog-ui") : item.link
    }));
  }
  let updatedData = replaceDigitUiWithUpyogUi(links);

  // Filter out RTP registration link based on login type, if logged in as citizen hide the link else show it
  const isRTPLogin = Digit.SessionStorage.get("isRTPLogin");

  if (!isRTPLogin) {
    updatedData = updatedData.filter(item => item.name !== "BPA_APPLY_FOR_REGISTER_AS_RTP");
  } else {
  updatedData = updatedData.filter(item => 
    item.name !== "BPA_CITIZEN_HOME_VIEW_APP_BY_CITIZEN_LABEL" && 
    item.name !== "BPA_APPLY_FOR_BUILDING_PERMIT"
    );
  };

  // Update display text for RTP link based on login status, if logged in as RTP show 'View Inbox' else show 'Register as RTP'
  updatedData = updatedData.map(item => {
    if (item.name === "BPA_APPLY_FOR_REGISTER_AS_RTP") {
      return {
        ...item,
        i18nKey: isRTPLogin ? t("BPA_RTP_VIEW_INBOX") : t("BPA_APPLY_FOR_REGISTER_AS_RTP")
      };
    }
    return item;
  });
  
//   function updateDisplayName(data, roles) {
//     return data.map(item => {
//         if (item.id === 3074) {
//             const isCitizen = roles.some(role => role.code === "CITIZEN");
//             const isArchitect = roles.some(role => role.code === "BPA_ARCHITECT");
            
//             if (isArchitect) {
//                 return {
//                     ...item,
//                     i18nKey: "View as RTP"  
//                 };
//             } else if (isCitizen) {
//                 return {
//                     ...item,
//                     i18nKey: "Register as RTP"  
//                 };
//             }
//         }
//         return item;
//     });
// }

// const roles = Digit.SessionStorage.get("User")?.info?.roles;

// const updatedData = updateDisplayName(updatedLinks, roles);



  console.log("updatedData",updatedData)
  return (
    <div className="CitizenHomeCard" style={styles ? styles : {}}>
      <div className="header">
        <h2>{header}</h2>
        <Icon />
      </div>

      <div className="links">
        {updatedData.map((e, i) => (
          <div className="linksWrapper" style={{paddingLeft:"10px"}}>
            {(e?.parentModule?.toUpperCase() == "BIRTH" ||
              e?.parentModule?.toUpperCase() == "DEATH" ||
              e?.parentModule?.toUpperCase() == "FIRENOC") ?
              <a href={e.link}>{e.i18nKey}</a> :
              <Link key={i} to={{ pathname: e.link, state: e.state }}>
                {e.i18nKey}
              </Link>
            }
          </div>
        ))}
      </div>
      <div>{isInfo ? <Info /> : null}</div>
    </div>
  );
};

export default CitizenHomeCard;
