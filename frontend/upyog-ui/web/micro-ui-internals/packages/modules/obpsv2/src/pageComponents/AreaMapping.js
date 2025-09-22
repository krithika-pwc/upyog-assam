import React, { useEffect, useState } from "react";
import { FormStep, CardLabel, Dropdown, TextInput } from "@upyog/digit-ui-react-components";

const AreaMapping = ({ t, config, onSelect, formData, searchResult }) => {

  // State for all dropdown values
  const [district, setDistrict] = useState( searchResult?.areaMapping?.district || formData?.areaMapping?.district ||  "");
  const [planningArea, setPlanningArea] = useState(formData?.areaMapping?.planningArea || searchResult?.areaMapping?.planningArea || "");
  const [ppAuthority, setPpAuthority] = useState(formData?.areaMapping?.ppAuthority || searchResult?.areaMapping?.planningPermitAuthority || "");
  const [bpAuthority, setBpAuthority] = useState(formData?.areaMapping?.bpAuthority || searchResult?.areaMapping?.buildingPermitAuthority || "");
  const [revenueVillage, setRevenueVillage] = useState(formData?.areaMapping?.revenueVillage || searchResult?.areaMapping?.revenueVillage || "");
  const [mouza, setMouza] = useState(formData?.areaMapping?.mouza || searchResult?.areaMapping?.mouza || "");
  const [ward, setWard] = useState(formData?.areaMapping?.ward || searchResult?.areaMapping?.ward || "");
  
  // State for dropdown options
  const [districts, setDistricts] = useState([]);
  const [planningAreas, setPlanningAreas] = useState([]);
  const [ppAuthorities, setPpAuthorities] = useState([]);
  const [bpAuthorities, setBpAuthorities] = useState([]);
  const [revenueVillages, setRevenueVillages] = useState([]);
  const [mouzas, setMouzas] = useState([]);

  // Fetch data from MDMS
  const { data: areaMappingData, isLoading } = Digit.Hooks.useEnabledMDMS(
    "as", 
    "BPA", 
    [
      { name: "districts" }, 
      { name: "planningAreas" }, 
      { name: "ppAuthorities" }, 
      { name: "bpAuthorities" }, 
      { name: "revenueVillages" }, 
      { name: "mouzas" }
    ],
    {
      select: (data) => {
        const formattedData = data?.BPA || {};
        return formattedData;
      },
    }
  );

  // Initialize districts from MDMS data
  useEffect(() => {
    if (areaMappingData?.districts) {
      const formattedDistricts = areaMappingData.districts.map((district) => ({
        code: district.districtCode,
        name: district.districtName,
        i18nKey: district.districtCode,
      }));
      setDistricts(formattedDistricts);
    }
  }, [areaMappingData]);

  // Update planning areas when district changes
  useEffect(() => {
    if (district && areaMappingData?.planningAreas) {
      // Filter planning areas based on selected district
      const filteredPlanningAreas = areaMappingData.planningAreas
      .filter(area => area.districtCode === district?.code)
      .map(area => ({
        code: area.planningAreaCode,
        name: area.planningAreaName,
        i18nKey: area.planningAreaCode,
      }));
      setPlanningAreas(filteredPlanningAreas);
      
      // Reset dependent fields
      setPlanningArea("");
      setPpAuthority("");
      setBpAuthority("");
      setRevenueVillage("");
      setMouza("");
      setWard("");
    }
  }, [district, areaMappingData]);

  // Update PP authorities when planning area changes
  useEffect(() => {
    if (planningArea && areaMappingData?.ppAuthorities) {
      // Filter PP authorities based on selected planning area
        const filteredPpAuthorities = areaMappingData.ppAuthorities
        .filter(authority => authority.planningAreaCode === planningArea?.code)
        .map(authority => ({
          code: authority.ppAuthorityCode,
          name: authority.ppAuthorityName,
          i18nKey: authority.ppAuthorityCode,
        }));

      setPpAuthorities(filteredPpAuthorities);
      
      // Reset dependent fields
      setPpAuthority("");
      setBpAuthority("");
      setRevenueVillage("");
      setMouza("");
      setWard("");
    }
  }, [planningArea, areaMappingData]);

  // Update BP authorities when PP authority changes
  useEffect(() => {
    if (ppAuthority && areaMappingData?.bpAuthorities) {
      // Filter BP authorities based on selected PP authority
      const filteredBpAuthorities = areaMappingData.bpAuthorities
      .filter(authority => authority.ppAuthorityCode === ppAuthority?.code)
      .map(authority => ({
        code: authority.bpAuthorityCode,
        name: authority.bpAuthorityName,
        i18nKey: authority.bpAuthorityCode,
      }));
      setBpAuthorities(filteredBpAuthorities);
      
      // Reset dependent fields
      setBpAuthority("");
      setRevenueVillage("");
      setMouza("");
      setWard("");
    }
  }, [ppAuthority, areaMappingData]);

  // Update revenue villages when BP authority changes
  useEffect(() => {
    if (bpAuthority && areaMappingData?.revenueVillages) {
      // Filter revenue villages based on selected BP authority
      const filteredRevenueVillages = areaMappingData.revenueVillages
      .filter(village => village.bpAuthorityCode === bpAuthority?.code)
      .map(village => ({
        code: village.revenueVillageCode,
        name: village.revenueVillageName,
        i18nKey: village.revenueVillageCode,
      }));
      setRevenueVillages(filteredRevenueVillages);
      
      // Reset dependent fields
      setRevenueVillage("");
      setMouza("");
      setWard("");
    }
  }, [bpAuthority, areaMappingData]);

  // Update mouzas when revenue village changes
  useEffect(() => {
    if (revenueVillage && areaMappingData?.mouzas) {
      // Filter mouzas based on selected revenue village
      const filteredMouzas = areaMappingData.mouzas
      .filter(mouza => mouza.revenueVillageCode === revenueVillage?.code)
      .map(mouza => ({
        code: mouza.mouzaCode,
        name: mouza.mouzaName,
        i18nKey: mouza.mouzaCode,
      }));
      setMouzas(filteredMouzas);
      
      // Reset dependent field
      setMouza("");
      setWard("");
    }
  }, [revenueVillage, areaMappingData]);

  // Go next
  const goNext = () => {
    let areaMappingStep = {
      district,
      planningArea,
      ppAuthority,
      bpAuthority,
      revenueVillage,
      mouza,
      ward
    };

    onSelect(config.key, { ...formData[config.key], ...areaMappingStep });
  };

  const onSkip = () => onSelect();

  return (
    <React.Fragment>
      <FormStep
        config={config}
        onSelect={goNext}
        onSkip={onSkip}
        t={t}
        isDisabled={
          !district ||
          !planningArea ||
          !ppAuthority ||
          !bpAuthority ||
          !revenueVillage ||
          !mouza ||
          !ward
        }
      >
        <div>
          {/* District */}
          <CardLabel>{`${t("DISTRICT")}`} <span className="check-page-link-button">*</span></CardLabel>
          <Dropdown
            t={t}
            option={districts}
            optionKey="i18nKey"
            id="district"
            selected={district}
            select={(value) => setDistrict(value)}
            placeholder={isLoading ? `${t("LOADING_DISTRICTS")}` : `${t("SELECT_DISTRICT")}`}
          />

          {/* Planning Area */}
          <CardLabel>{`${t("PLANNING_AREA")}`} <span className="check-page-link-button">*</span></CardLabel>
          <Dropdown
            t={t}
            option={planningAreas}
            optionKey="i18nKey"
            id="planningArea"
            selected={planningArea}
            select={(value) => setPlanningArea(value)}
            placeholder={!district ? `${t("SELECT_DISTRICT_FIRST")}` : `${t("SELECT_PLANNING_AREA")}`}
          />

          {/* PP Authority */}
          <CardLabel>{`${t("PP_AUTHORITY")}`} <span className="check-page-link-button">*</span></CardLabel>
          <Dropdown
            t={t}
            option={ppAuthorities}
            optionKey="i18nKey"
            id="ppAuthority"
            selected={ppAuthority}
            select={(value) => setPpAuthority(value)}
            placeholder={!planningArea ? `${t("SELECT_PLANNING_AREA_FIRST")}` : `${t("SELECT_PP_AUTHORITY")}`}
          />

          {/* BP Authority */}
          <CardLabel>{`${t("BP_AUTHORITY")}`} <span className="check-page-link-button">*</span></CardLabel>
          <Dropdown
            t={t}
            option={bpAuthorities}
            optionKey="i18nKey"
            id="bpAuthority"
            selected={bpAuthority}
            select={(value) => setBpAuthority(value)}
            placeholder={!ppAuthority ? `${t("SELECT_PP_AUTHORITY_FIRST")}` : `${t("SELECT_BP_AUTHORITY")}`}
          />

          {/* Revenue Village */}
          <CardLabel>{`${t("REVENUE_VILLAGE")}`} <span className="check-page-link-button">*</span></CardLabel>
          <Dropdown
            t={t}
            option={revenueVillages}
            optionKey="i18nKey"
            id="revenueVillage"
            selected={revenueVillage}
            select={(value) => setRevenueVillage(value)}
            placeholder={!bpAuthority ? `${t("SELECT_BP_AUTHORITY_FIRST")}` : `${t("SELECT_REVENUE_VILLAGE")}`}
          />

          {/* Mouza - Either dropdown or text input based on available data */}
          <CardLabel>{`${t("MOUZA")}`} <span className="check-page-link-button">*</span></CardLabel>
          {mouzas.length > 0 ? (
            <Dropdown
              t={t}
              option={mouzas}
              optionKey="i18nKey"
              id="mouza"
              selected={mouza}
              select={(value) => setMouza(value)}
              placeholder={!revenueVillage ? `${t("SELECT_REVENUE_VILLAGE_FIRST")}` : `${t("SELECT_MOUZA")}`}
            />
          ) : (
            <TextInput
              t={t}
              name="mouza"
              value={mouza}
              onChange={(e) => setMouza(e.target.value)}
              placeholder={`${t("ENTER_MOUZA_NAME")}`}
            />
          )}

          {/* Ward - Always a text input field */}
          <CardLabel>{`${t("WARD")}`} <span className="check-page-link-button">*</span></CardLabel>
          <TextInput
            t={t}
            name="ward"
            value={ward}
            onChange={(e) => setWard(e.target.value)}
            placeholder={`${t("ENTER_WARD_NUMBER")}`}
          />
        </div>
      </FormStep>
    </React.Fragment>
  );
};

export default AreaMapping;