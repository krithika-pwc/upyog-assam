/*
 * UPYOG  SmartCity eGovernance suite aims to improve the internal efficiency,transparency,
 * accountability and the service delivery of the government  organizations.
 *
 *  Copyright (C) <2019>  eGovernments Foundation
 *
 *  The updated version of eGov suite of products as by eGovernments Foundation
 *  is available at http://www.egovernments.org
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see http://www.gnu.org/licenses/ or
 *  http://www.gnu.org/licenses/gpl.html .
 *
 *  In addition to the terms of the GPL license to be adhered to in using this
 *  program, the following additional terms are to be complied with:
 *
 *      1) All versions of this program, verbatim or modified must carry this
 *         Legal Notice.
 *      Further, all user interfaces, including but not limited to citizen facing interfaces,
 *         Urban Local Bodies interfaces, dashboards, mobile applications, of the program and any
 *         derived works should carry eGovernments Foundation logo on the top right corner.
 *
 *      For the logo, please refer http://egovernments.org/html/logo/egov_logo.png.
 *      For any further queries on attribution, including queries on brand guidelines,
 *         please contact contact@egovernments.org
 *
 *      2) Any misrepresentation of the origin of the material is prohibited. It
 *         is required that all modified versions of this material be marked in
 *         reasonable ways as different from the original version.
 *
 *      3) This license does not grant any rights to any user of the program
 *         with regards to rights under trademark law for use of the trade names
 *         or trademarks of eGovernments Foundation.
 *
 *  In case of any queries, you can reach eGovernments Foundation at contact@egovernments.org.
 */

package org.egov.edcr.feature;

import static org.egov.edcr.constants.CommonFeatureConstants.*;
import static org.egov.edcr.constants.CommonKeyConstants.BLOCK;
import static org.egov.edcr.constants.DxfFileConstants.A;
import static org.egov.edcr.constants.DxfFileConstants.H;
import static org.egov.edcr.constants.DxfFileConstants.D;
import static org.egov.edcr.constants.DxfFileConstants.C;
import static org.egov.edcr.constants.DxfFileConstants.E;
import static org.egov.edcr.constants.DxfFileConstants.D_M;
import static org.egov.edcr.constants.DxfFileConstants.E_NS;
import static org.egov.edcr.constants.DxfFileConstants.E_PS;
import static org.egov.edcr.constants.DxfFileConstants.B2;
import static org.egov.edcr.constants.DxfFileConstants.E_CLG;
import static org.egov.edcr.constants.DxfFileConstants.A_AF;
import static org.egov.edcr.constants.DxfFileConstants.A_PO;
import static org.egov.edcr.constants.DxfFileConstants.A_R;
import static org.egov.edcr.constants.DxfFileConstants.B;
import static org.egov.edcr.constants.DxfFileConstants.D;
import static org.egov.edcr.constants.DxfFileConstants.D_AW;
import static org.egov.edcr.constants.DxfFileConstants.F;
import static org.egov.edcr.constants.DxfFileConstants.G;
import static org.egov.edcr.constants.DxfFileConstants.G_SI;
import static org.egov.edcr.constants.DxfFileConstants.G_LI;
import static org.egov.edcr.constants.DxfFileConstants.G_PHI;
import static org.egov.edcr.constants.DxfFileConstants.I;
import static org.egov.edcr.constants.EdcrReportConstants.*;
import static org.egov.edcr.utility.DcrConstants.FRONT_YARD_DESC;
import static org.egov.edcr.utility.DcrConstants.OBJECTNOTDEFINED;
import static org.egov.edcr.utility.DcrConstants.REAR_YARD_DESC;
import static org.egov.edcr.utility.DcrConstants.YES;
import static org.egov.edcr.constants.EdcrReportConstants.ERR_NARROW_ROAD_RULE; 

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.egov.common.constants.MdmsFeatureConstants;
import org.egov.common.entity.edcr.Block;
import org.egov.common.entity.edcr.Building;
import org.egov.common.entity.edcr.FeatureEnum;
import org.egov.common.entity.edcr.MdmsFeatureRule;
import org.egov.common.entity.edcr.Occupancy;
import org.egov.common.entity.edcr.OccupancyTypeHelper;
import org.egov.common.entity.edcr.Plan;
import org.egov.common.entity.edcr.Plot;
import org.egov.common.entity.edcr.RearSetBackRequirement;
import org.egov.common.entity.edcr.Result;
import org.egov.common.entity.edcr.ScrutinyDetail;
import org.egov.common.entity.edcr.SetBack;
import org.egov.edcr.constants.DxfFileConstants;
import org.egov.edcr.constants.EdcrRulesMdmsConstants;
import org.egov.edcr.service.FetchEdcrRulesMdms;
import org.egov.edcr.service.MDMSCacheManager;
import org.egov.infra.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RearYardService_Assam extends RearYardService {
	private static final Logger LOG = LogManager.getLogger(RearYardService_Assam.class);

	private class RearYardResult {
		String rule;
		String subRule;
		String blockName;
		Integer level;
		BigDecimal actualMeanDistance = BigDecimal.ZERO;
		BigDecimal actualMinDistance = BigDecimal.ZERO;
		String occupancy;
		BigDecimal expectedminimumDistance = BigDecimal.ZERO;
		BigDecimal expectedmeanDistance = BigDecimal.ZERO;
		boolean status = false;
	}

	@Autowired
	FetchEdcrRulesMdms fetchEdcrRulesMdms;
	
	 @Autowired
	 MDMSCacheManager cache;
	 
	public void processRearYard(final Plan pl) {
		HashMap<String, String> errors = new HashMap<>();
		final Plot plot = pl.getPlot();
		if (plot == null)
			return;

		validateRearYard(pl);
		

		if (plot != null && !pl.getBlocks().isEmpty()) {
			for (Block block : pl.getBlocks()) { // for each block

				scrutinyDetail = new ScrutinyDetail();
				scrutinyDetail.setKey(BLOCK + block.getName() + UNDERSCORE + REAR_SETBACK_SUFFIX);
				scrutinyDetail.addColumnHeading(1, RULE_NO);
				scrutinyDetail.addColumnHeading(2, LEVEL);
				scrutinyDetail.addColumnHeading(3, OCCUPANCY);
				scrutinyDetail.addColumnHeading(4, FIELDVERIFIED);
				scrutinyDetail.addColumnHeading(5, PERMISSIBLE);
				scrutinyDetail.addColumnHeading(6, PROVIDED);
				scrutinyDetail.addColumnHeading(7, STATUS);
				scrutinyDetail.setHeading(REAR_YARD_DESC);
				RearYardResult rearYardResult = new RearYardResult();

				for (SetBack setback : block.getSetBacks()) {
					BigDecimal min;
					BigDecimal mean;
					final Occupancy occupancy = block.getBuilding().getTotalArea().get(0);
					if (setback.getRearYard() != null
							&& setback.getRearYard().getMean().compareTo(BigDecimal.ZERO) > 0) {
						min = setback.getRearYard().getMinimumDistance();
						mean = setback.getRearYard().getMean();

						// if height defined at rear yard level, then use elase use buidling height.
						BigDecimal buildingHeight = setback.getRearYard().getHeight() != null
								&& setback.getRearYard().getHeight().compareTo(BigDecimal.ZERO) > 0
										? setback.getRearYard().getHeight()
										: block.getBuilding().getBuildingHeight();

						if (buildingHeight != null && (min.doubleValue() > 0 || mean.doubleValue() > 0)) {
							checkRearYard(pl, block.getBuilding(), block, setback.getLevel(), plot,
									REAR_YARD_DESC, min, mean, occupancy.getTypeHelper(), rearYardResult,
									buildingHeight, errors);
							addRearYardResult(pl, errors, rearYardResult);
			
									/*
									 * if (buildingHeight.compareTo(BigDecimal.valueOf(10)) <= 0 &&
									 * block.getBuilding() .getFloorsAboveGround().compareTo(BigDecimal.valueOf(3))
									 * <= 0) { checkRearYardUptoTenMts(pl, block.getBuilding(), block,
									 * setback.getLevel(), plot, REAR_YARD_DESC, min, mean,
									 * occupancy.getTypeHelper(), rearYardResult, buildingHeight);
									 * 
									 * } else if (buildingHeight.compareTo(BigDecimal.valueOf(12)) <= 0 &&
									 * block.getBuilding().getFloorsAboveGround() .compareTo(BigDecimal.valueOf(4))
									 * <= 0) { checkRearYardUptoToTweleveMts(setback, block.getBuilding(), pl,
									 * block, setback.getLevel(), plot, REAR_YARD_DESC, min, mean,
									 * occupancy.getTypeHelper(), rearYardResult, errors);
									 * 
									 * } else if (buildingHeight.compareTo(BigDecimal.valueOf(16)) <= 0) {
									 * checkRearYardUptoToSixteenMts(setback, block.getBuilding(), pl, block,
									 * setback.getLevel(), plot, REAR_YARD_DESC, min, mean,
									 * occupancy.getTypeHelper(), rearYardResult, errors);
									 * 
									 * } else if (buildingHeight.compareTo(BigDecimal.valueOf(16)) > 0) {
									 * checkRearYardAboveSixteenMts(setback, block.getBuilding(), pl, block,
									 * setback.getLevel(), plot, REAR_YARD_DESC, min, mean,
									 * occupancy.getTypeHelper(), rearYardResult, buildingHeight);
									 * 
									 * }
									 */
								} /*
									 * else if (G.equalsIgnoreCase(occupancy.getTypeHelper().getType().getCode())) {
									 * checkRearYardForIndustrial(setback, block.getBuilding(), pl, block,
									 * setback.getLevel(), plot, REAR_YARD_DESC, min, mean,
									 * occupancy.getTypeHelper(), rearYardResult); } else {
									 * checkRearYardOtherOccupancies(setback, block.getBuilding(), pl, block,
									 * setback.getLevel(), plot, REAR_YARD_DESC, min, mean,
									 * occupancy.getTypeHelper(), rearYardResult, buildingHeight); }
									 */

							}}

				}
			}
		

	}
	
	private void addRearYardResult(final Plan pl, HashMap<String, String> errors, RearYardResult rearYardResult) {
		if (rearYardResult != null) {
			Map<String, String> details = new HashMap<>();
			details.put(RULE_NO, rearYardResult.subRule);
			details.put(LEVEL, rearYardResult.level != null ? rearYardResult.level.toString() : "");
			details.put(OCCUPANCY, rearYardResult.occupancy);
			if (rearYardResult.expectedmeanDistance != null
					&& rearYardResult.expectedmeanDistance.compareTo(BigDecimal.valueOf(0)) == 0) {
				details.put(FIELDVERIFIED, MINIMUMLABEL);
				details.put(PERMISSIBLE, rearYardResult.expectedminimumDistance.toString());
				details.put(PROVIDED, rearYardResult.actualMinDistance.toString());

			} else {
				details.put(FIELDVERIFIED, MINIMUMLABEL);
				details.put(PERMISSIBLE, rearYardResult.expectedmeanDistance.toString());
				details.put(PROVIDED, rearYardResult.actualMeanDistance.toString());
			}
			if (rearYardResult.status) {
				details.put(STATUS, Result.Accepted.getResultVal());

			} else {
				details.put(STATUS, Result.Not_Accepted.getResultVal());
			}
			scrutinyDetail.getDetail().add(details);
			pl.getReportOutput().getScrutinyDetails().add(scrutinyDetail);
		}

	}

//	private Boolean processRearYard1(Plan pl, Block block, Integer level, final BigDecimal min, final BigDecimal mean,
//			final OccupancyTypeHelper mostRestrictiveOccupancy, RearYardResult rearYardResult, String subRule,
//			String rule, BigDecimal minVal, BigDecimal meanVal, BigDecimal depthOfPlot, Boolean valid,
//			String occupancyName) {
//
//	
//		BigDecimal plotArea = pl.getPlot().getArea();
//
//	
//		List<Object> rules = cache.getFeatureRules(pl, FeatureEnum.REAR_SET_BACK.getValue(), false);
//        Optional<RearSetBackRequirement> matchedRule = rules.stream()
//            .filter(RearSetBackRequirement.class::isInstance)
//            .map(RearSetBackRequirement.class::cast)
//            .filter(ruleFeature -> plotArea.compareTo(ruleFeature.getFromPlotArea()) >= 0 && plotArea.compareTo(ruleFeature.getToPlotArea()) < 0)
//            .findFirst();
//      
//		if (matchedRule.isPresent()) {
//			RearSetBackRequirement mdmsRule = matchedRule.get();
//			meanVal = mdmsRule.getPermissible();
//		} else {
//			meanVal = BigDecimal.ZERO;
//		}
//
//		valid = validateMinimumAndMeanValue(min, mean, minVal, meanVal);
//
//		/*
//		 * valid = validateMinimumAndMeanValue(min, mean, minVal, meanVal); /* if (-1 ==
//		 * level) { subRule = SUB_RULE_24_12; rule = BSMT_REAR_YARD_DESC; subRuleDesc =
//		 * SUB_RULE_24_12_DESCRIPTION; }
//		 */
//		compareRearYardResult(block.getName(), min, mean, mostRestrictiveOccupancy, rearYardResult, valid, subRule,
//				rule, minVal, meanVal, level);
//		return valid;
//	}
	
	private Boolean processRearYardResidential(Plan pl, Block block, Integer level, final BigDecimal min, final BigDecimal mean,
	        final OccupancyTypeHelper mostRestrictiveOccupancy, RearYardResult rearYardResult, String subRule,
	        String rule, BigDecimal minVal, BigDecimal meanVal, BigDecimal buildingHeight, Boolean valid,
	        String occupancyName) {

	    subRule = SUB_RULE_SIDE_YARD;
	    
	    List<Object> rules = cache.getFeatureRules(pl, FeatureEnum.REAR_SET_BACK.getValue(), false);

	    Optional<RearSetBackRequirement> matchedRule = rules.stream()
	        .filter(RearSetBackRequirement.class::isInstance)
	        .map(RearSetBackRequirement.class::cast)
	        .filter(ruleFeature ->
	                ruleFeature.getFromBuildingHeight() != null && ruleFeature.getToBuildingHeight() != null
	                && buildingHeight.compareTo(ruleFeature.getFromBuildingHeight()) >= 0
	                && buildingHeight.compareTo(ruleFeature.getToBuildingHeight()) < 0
	                && Boolean.TRUE.equals(ruleFeature.getActive()))
	        .findFirst();

	    if (matchedRule.isPresent()) {
	        RearSetBackRequirement mdmsRule = matchedRule.get();
	        meanVal = mdmsRule.getPermissible();
	        minVal = meanVal; 
	    } else {
	        meanVal = BigDecimal.ZERO;
	        minVal = BigDecimal.ZERO;
	    }

	    valid = validateMinimumAndMeanValue(min, mean, minVal, meanVal);

	    compareRearYardResult(block.getName(), min, mean, mostRestrictiveOccupancy, rearYardResult, valid, subRule,
	            rule, minVal, meanVal, level);
	    return valid;
	}
	
	private Boolean processRearYardHospitalAndNursingHomes(Plan pl, Block block, Integer level, final BigDecimal min, final BigDecimal mean,
	        final OccupancyTypeHelper mostRestrictiveOccupancy, RearYardResult rearYardResult, String subRule,
	        String rule, BigDecimal minVal, BigDecimal meanVal, BigDecimal buildingHeight, Boolean valid,
	        String occupancyName) {

	    subRule = SUB_RULE_SIDE_YARD;
	    
	    List<Object> rules = cache.getFeatureRules(pl, FeatureEnum.REAR_SET_BACK.getValue(), false);
	    
	    BigDecimal depthOfPlot = pl.getPlanInformation().getDepthOfPlot();

	    Optional<RearSetBackRequirement> matchedRule = rules.stream()
	        .filter(RearSetBackRequirement.class::isInstance)
	        .map(RearSetBackRequirement.class::cast)
	        .filter(ruleFeature ->
	                ruleFeature.getFromPlotDepth() != null && ruleFeature.getToPlotDepth() != null
	                && depthOfPlot.compareTo(ruleFeature.getFromPlotDepth()) >= 0
	                && depthOfPlot.compareTo(ruleFeature.getToPlotDepth()) < 0
	                && Boolean.TRUE.equals(ruleFeature.getActive()))
	        .findFirst();

	    if (matchedRule.isPresent()) {
	        RearSetBackRequirement mdmsRule = matchedRule.get();
	        meanVal = mdmsRule.getPermissible();
	        minVal = meanVal; // If there's a separate `minimum` field, use it here.
	    } else {
	        meanVal = BigDecimal.ZERO;
	        minVal = BigDecimal.ZERO;
	    }

	    valid = validateMinimumAndMeanValue(min, mean, minVal, meanVal);

	    compareRearYardResult(block.getName(), min, mean, mostRestrictiveOccupancy, rearYardResult, valid, subRule,
	            rule, minVal, meanVal, level);
	    return valid;
	}

	

	private Boolean processRearYardCommercial(Plan pl, Block block, Integer level, final BigDecimal min, final BigDecimal mean,
	        final OccupancyTypeHelper mostRestrictiveOccupancy, RearYardResult rearYardResult, String subRule,
	        String rule, BigDecimal minVal, BigDecimal meanVal, BigDecimal buildingHeight, Boolean valid,
	        String occupancyName) {

	    subRule = SUB_RULE_SIDE_YARD;

	    BigDecimal depthOfPlot = pl.getPlanInformation().getDepthOfPlot();
	    BigDecimal plotArea = pl.getPlanInformation().getPlotArea();

	    // Step 1: Check applicability of this rule
	  

	        // Step 2: Get rules from MDMS
	        List<Object> rules = cache.getFeatureRules(pl, FeatureEnum.REAR_SET_BACK.getValue(), false);

	        Optional<RearSetBackRequirement> matchedRule = rules.stream()
	            .filter(RearSetBackRequirement.class::isInstance)
	            .map(RearSetBackRequirement.class::cast)
	            .filter(ruleFeature ->
	                ruleFeature.getFromPlotDepth() != null && ruleFeature.getToPlotDepth() != null
	                && depthOfPlot.compareTo(ruleFeature.getFromPlotDepth()) >= 0
	                && depthOfPlot.compareTo(ruleFeature.getToPlotDepth()) < 0
	                && Boolean.TRUE.equals(ruleFeature.getActive()))
	            .findFirst();

	        if (matchedRule.isPresent()) {
	            RearSetBackRequirement mdmsRule = matchedRule.get();
	            meanVal = mdmsRule.getPermissible();
	            minVal = meanVal; // If MDMS has separate min, replace here.
	        } else {
	            meanVal = BigDecimal.ZERO;
	            minVal = BigDecimal.ZERO;
	        }
	  

	    valid = validateMinimumAndMeanValue(min, mean, minVal, meanVal);

	    compareRearYardResult(block.getName(), min, mean, mostRestrictiveOccupancy, rearYardResult, valid, subRule,
	            rule, minVal, meanVal, level);

	    return valid;
	}
	
	private Boolean processRearYardIndustrial(
	        Plan pl,
	        Block block,
	        Integer level,
	        final BigDecimal min,
	        final BigDecimal mean,
	        final OccupancyTypeHelper mostRestrictiveOccupancy,
	        RearYardResult rearYardResult,
	        String subRule,
	        String rule,
	        BigDecimal minVal,
	        BigDecimal meanVal,
	        BigDecimal buildingHeight,
	        Boolean valid,
	        String occupancyName) {

	    subRule = SUB_RULE_SIDE_YARD;

	    // Fetch REAR_SET_BACK rules from MDMS
	    List<Object> rules = cache.getFeatureRules(pl, FeatureEnum.REAR_SET_BACK.getValue(), false);

	    Optional<RearSetBackRequirement> matchedRule = rules.stream()
	            .filter(RearSetBackRequirement.class::isInstance)
	            .map(RearSetBackRequirement.class::cast)
	            .filter(ruleFeature -> Boolean.TRUE.equals(ruleFeature.getActive()))
	            .findFirst();

	    if (matchedRule.isPresent()) {
	        RearSetBackRequirement mdmsRule = matchedRule.get();

	        String subtypeCode = mostRestrictiveOccupancy.getSubtype() != null
	                ? mostRestrictiveOccupancy.getSubtype().getCode()
	                : null;

	        if (G_SI.equalsIgnoreCase(subtypeCode)) {
	            meanVal = mdmsRule.getPermissibleLight();
	        } else if (G_LI.equalsIgnoreCase(subtypeCode)) {
	            meanVal = mdmsRule.getPermissibleMedium();
	        } else if (G_PHI.equalsIgnoreCase(subtypeCode)) {
	            meanVal = mdmsRule.getPermissibleFlattered();
	        } else {
	            meanVal = mdmsRule.getPermissible();
	        }

	        minVal = meanVal;
	    } else {
	        meanVal = BigDecimal.ZERO;
	        minVal = BigDecimal.ZERO;
	    }

	    valid = validateMinimumAndMeanValue(min, mean, minVal, meanVal);

	    compareRearYardResult(
	            block.getName(),
	            min,
	            mean,
	            mostRestrictiveOccupancy,
	            rearYardResult,
	            valid,
	            subRule,
	            rule,
	            minVal,
	            meanVal,
	            level
	    );

	    return valid;
	}


	private Boolean processRearYardPlaceOfworship(Plan pl, Block block, Integer level, final BigDecimal min, final BigDecimal mean,
	        final OccupancyTypeHelper mostRestrictiveOccupancy, RearYardResult rearYardResult, String subRule,
	        String rule, BigDecimal minVal, BigDecimal meanVal, BigDecimal buildingHeight, Boolean valid,
	        String occupancyName) {

	    subRule = SUB_RULE_SIDE_YARD;
	    
	    List<Object> rules = cache.getFeatureRules(pl, FeatureEnum.REAR_SET_BACK.getValue(), false);

	    Optional<RearSetBackRequirement> matchedRule = rules.stream()
	        .filter(RearSetBackRequirement.class::isInstance)
	        .map(RearSetBackRequirement.class::cast)
	        .filter(ruleFeature ->
	                ruleFeature.getFromPlotDepth() != null && ruleFeature.getToPlotDepth() != null
	                && buildingHeight.compareTo(ruleFeature.getFromPlotDepth()) >= 0
	                && buildingHeight.compareTo(ruleFeature.getToPlotDepth()) < 0
	                && Boolean.TRUE.equals(ruleFeature.getActive()))
	        .findFirst();

	    if (matchedRule.isPresent()) {
	        RearSetBackRequirement mdmsRule = matchedRule.get();
	        meanVal = mdmsRule.getPermissible();
	        minVal = meanVal; // If there's a separate `minimum` field, use it here.
	    } else {
	        meanVal = BigDecimal.ZERO;
	        minVal = BigDecimal.ZERO;
	    }

	    valid = validateMinimumAndMeanValue(min, mean, minVal, meanVal);

	    compareRearYardResult(block.getName(), min, mean, mostRestrictiveOccupancy, rearYardResult, valid, subRule,
	            rule, minVal, meanVal, level);
	    return valid;
	}
	
	private Boolean processRearYardAssembly(Plan pl, Block block, Integer level, final BigDecimal min, final BigDecimal mean,
	        final OccupancyTypeHelper mostRestrictiveOccupancy, RearYardResult rearYardResult, String subRule,
	        String rule, BigDecimal minVal, BigDecimal meanVal, BigDecimal buildingHeight, Boolean valid,
	        String occupancyName) {

	    subRule = SUB_RULE_SIDE_YARD;
	    
	    List<Object> rules = cache.getFeatureRules(pl, FeatureEnum.REAR_SET_BACK.getValue(), false);
	   

	    Optional<RearSetBackRequirement> matchedRule = rules.stream()
	        .filter(RearSetBackRequirement.class::isInstance)
	        .map(RearSetBackRequirement.class::cast)
	        .filter(ruleFeature ->
	                ruleFeature.getFromPlotDepth() != null && ruleFeature.getToPlotDepth() != null
	                && buildingHeight.compareTo(ruleFeature.getFromPlotDepth()) >= 0
	                && buildingHeight.compareTo(ruleFeature.getToPlotDepth()) < 0
	                && Boolean.TRUE.equals(ruleFeature.getActive()))
	        .findFirst();

	    if (matchedRule.isPresent()) {
	        RearSetBackRequirement mdmsRule = matchedRule.get();
	        meanVal = mdmsRule.getPermissible();
	        minVal = meanVal; // If there's a separate `minimum` field, use it here.
	    } else {
	        meanVal = BigDecimal.ZERO;
	        minVal = BigDecimal.ZERO;
	    }

	    valid = validateMinimumAndMeanValue(min, mean, minVal, meanVal);

	    compareRearYardResult(block.getName(), min, mean, mostRestrictiveOccupancy, rearYardResult, valid, subRule,
	            rule, minVal, meanVal, level);
	    return valid;
	}
	
	private Boolean processRearYardMultiplex(Plan pl, Block block, Integer level, final BigDecimal min, final BigDecimal mean,
	        final OccupancyTypeHelper mostRestrictiveOccupancy, RearYardResult rearYardResult, String subRule,
	        String rule, BigDecimal minVal, BigDecimal meanVal, BigDecimal buildingHeight, Boolean valid,
	        String occupancyName) {

	    subRule = SUB_RULE_SIDE_YARD;
	    
	    List<Object> rules = cache.getFeatureRules(pl, FeatureEnum.REAR_SET_BACK.getValue(), false);
	    
	    BigDecimal depthOfPlot = pl.getPlanInformation().getDepthOfPlot();

	    Optional<RearSetBackRequirement> matchedRule = rules.stream()
	        .filter(RearSetBackRequirement.class::isInstance)
	        .map(RearSetBackRequirement.class::cast)
	        .filter(ruleFeature ->
	                ruleFeature.getFromPlotDepth() != null && ruleFeature.getToPlotDepth() != null
	                && depthOfPlot.compareTo(ruleFeature.getFromPlotDepth()) >= 0
	                && depthOfPlot.compareTo(ruleFeature.getToPlotDepth()) < 0
	                && Boolean.TRUE.equals(ruleFeature.getActive()))
	        .findFirst();

	    if (matchedRule.isPresent()) {
	        RearSetBackRequirement mdmsRule = matchedRule.get();
	        meanVal = mdmsRule.getPermissible();
	        minVal = meanVal; // If there's a separate `minimum` field, use it here.
	    } else {
	        meanVal = BigDecimal.ZERO;
	        minVal = BigDecimal.ZERO;
	    }

	    valid = validateMinimumAndMeanValue(min, mean, minVal, meanVal);

	    compareRearYardResult(block.getName(), min, mean, mostRestrictiveOccupancy, rearYardResult, valid, subRule,
	            rule, minVal, meanVal, level);
	    return valid;
	}


	private Boolean processRearYardSchool(
	        Plan pl,
	        Block block,
	        Integer level,
	        final BigDecimal min,
	        final BigDecimal mean,
	        final OccupancyTypeHelper mostRestrictiveOccupancy,
	        RearYardResult rearYardResult,
	        String subRule,
	        String rule,
	        BigDecimal minVal,
	        BigDecimal meanVal,
	        BigDecimal buildingHeight,
	        Boolean valid,
	        String occupancyName) {

	    subRule = "Rear setback for School buildings";

	    // Fetch REAR_SET_BACK rules from MDMS
	    List<Object> rules = cache.getFeatureRules(pl, FeatureEnum.REAR_SET_BACK.getValue(), false);

	    Optional<RearSetBackRequirement> matchedRule = rules.stream()
	            .filter(RearSetBackRequirement.class::isInstance)
	            .map(RearSetBackRequirement.class::cast)
	            .filter(ruleFeature -> Boolean.TRUE.equals(ruleFeature.getActive()))
	            .findFirst();

	    if (matchedRule.isPresent()) {
	        RearSetBackRequirement mdmsRule = matchedRule.get();

	        String subtypeCode = mostRestrictiveOccupancy.getSubtype() != null
	                ? mostRestrictiveOccupancy.getSubtype().getCode()
	                : null;

	        if (E_NS.equalsIgnoreCase(subtypeCode)) {
	            meanVal = mdmsRule.getPermissibleNursery();
	        } else if (E_PS.equalsIgnoreCase(subtypeCode)) {
	            meanVal = mdmsRule.getPermissiblePrimary();
	        } else if (B2.equalsIgnoreCase(subtypeCode)) {
	            meanVal = mdmsRule.getPermissibleHighSchool();
	        }  else if (E_CLG.equalsIgnoreCase(subtypeCode)) {
	            meanVal = mdmsRule.getPermissibleCollege();
	        }

	        minVal = meanVal;
	    } else {
	        meanVal = BigDecimal.ZERO;
	        minVal = BigDecimal.ZERO;
	    }

	    valid = validateMinimumAndMeanValue(min, mean, minVal, meanVal);

	    compareRearYardResult(
	            block.getName(),
	            min,
	            mean,
	            mostRestrictiveOccupancy,
	            rearYardResult,
	            valid,
	            subRule,
	            rule,
	            minVal,
	            meanVal,
	            level
	    );

	    return valid;
	}


	// Added by Bimal 18-March-2924 to check Rear yard based on plot are not on height
	private Boolean checkRearYardResidentialCommon(Plan pl, Building building, String blockName, Integer level,
			Plot plot, String frontYardFieldName, BigDecimal min, BigDecimal mean,
			OccupancyTypeHelper mostRestrictiveOccupancy, RearYardResult rearYardResult,
			HashMap<String, String> errors) {
		Boolean valid = false;
		String subRule = RULE_4_4_4_I;
		String rule = FRONT_YARD_DESC;
		BigDecimal meanVal = BigDecimal.ZERO;
		BigDecimal depthOfPlot = pl.getPlanInformation().getDepthOfPlot();
		BigDecimal plotArea = pl.getPlanInformation().getPlotArea();

		// Process only for A_R, A_AF, and A_ occupancy types
		if (mostRestrictiveOccupancy.getSubtype() != null
				&& (A_R.equalsIgnoreCase(mostRestrictiveOccupancy.getSubtype().getCode())
						|| A_AF.equalsIgnoreCase(mostRestrictiveOccupancy.getSubtype().getCode())
						|| A_PO.equalsIgnoreCase(mostRestrictiveOccupancy.getSubtype().getCode()))) {

			valid = processRearYardResidential(blockName, level, min, mean, mostRestrictiveOccupancy, rearYardResult,
					valid, subRule, rule, meanVal, depthOfPlot, errors, pl, plotArea);

		}

		return valid;
	}

	// Added by Bimal 18-March-2924 to check Rear yard based on plot are not on height
	private Boolean processRearYardResidential(String blockName, Integer level, BigDecimal min, BigDecimal mean,
			OccupancyTypeHelper mostRestrictiveOccupancy, RearYardResult frontYardResult, Boolean valid, String subRule,
			String rule, BigDecimal meanVal, BigDecimal depthOfPlot, HashMap<String, String> errors, Plan pl,
			BigDecimal plotArea) {

		LOG.info("Processing RearYardResult:");

		BigDecimal minVal = BigDecimal.ZERO;

		// Set minVal based on plot area
		if (plotArea.compareTo(MIN_PLOT_AREA) <= 0) {
			// Plot area is less than zero
			errors.put(PLOT_AREA_ERROR, PLOT_AREA_CANNOT_BE_LESS_THAN + MIN_PLOT_AREA);
		} else if (plotArea.compareTo(PLOT_AREA_100_SQM) <= 0) {
			minVal = MIN_VAL_100_SQM;
		} else if (plotArea.compareTo(PLOT_AREA_150_SQM) <= 0) {
			minVal = MIN_VAL_150_SQM;
		} else if (plotArea.compareTo(PLOT_AREA_200_SQM) <= 0) {
			minVal = MIN_VAL_200_SQM;
		} else if (plotArea.compareTo(PLOT_AREA_300_SQM) <= 0) {
			minVal = MIN_VAL_300_PlUS_SQM;
		} else if (plotArea.compareTo(PLOT_AREA_500_SQM) <= 0) {
			minVal = MIN_VAL_300_PlUS_SQM;
		} else if (plotArea.compareTo(PLOT_AREA_1000_SQM) <= 0) {
			minVal = MIN_VAL_300_PlUS_SQM;
		}

		// Validate minimum and mean value
		valid = validateMinimumAndMeanValue(min, mean, minVal, mean, plotArea);

//		    // Add error if plot area is less than or equal to 10
//		    if (plotArea.compareTo(MIN_PLOT_AREA) <= 0) {
//		        errors.put("uptoSixteenHeightUptoTenDepthFrontYard",
//		                "No construction shall be permitted if depth of plot is less than 10 and building height less than 16 having floors upto G+4.");
//		        pl.addErrors(errors);
//		    }
		if (!valid) {
			LOG.info("Rear Yard Service: min value validity False: " + minVal+"/"+min);
			errors.put(MIN_AND_MEAN_VALUE_VALIDATION,
					MIN_VALUE_LESS_THAN_REQUIRED_MIN + minVal+ SLASH +min);

		} else {
			LOG.info("Rear Yard Service: min value validity True: " + minVal+"/"+min);
		}
		pl.addErrors(errors);
		compareRearYardResult(blockName, min, mean, mostRestrictiveOccupancy, frontYardResult, valid, subRule, rule,
				minVal, meanVal, level);

		return valid;
	}
	
	// Added by Bimal 18-March-2924 to check Rear yard based on plotarea not on height
	private Boolean validateMinimumAndMeanValue(final BigDecimal min, final BigDecimal mean, final BigDecimal minval,
	        final BigDecimal meanval, final BigDecimal plotArea) {
	    Boolean valid = false;
	    
	    if (plotArea.compareTo(new BigDecimal("200")) <= 0) {
	        // For plot areas less than or equal to 200 sqm, min can be less than, equal to, or greater than minval
	    	if ((min.compareTo(minval) <= 0 || min.compareTo(minval) >= 0) && (mean.compareTo(meanval) <= 0 || mean.compareTo(meanval) >= 0)) {
	            valid = true;
	        }
	    } else {
	        // For plot areas greater than 200 sqm, min should be at least minval
	        if (min.compareTo(minval) >= 0 && mean.compareTo(meanval) >= 0) {
	            valid = true;
	        }
	    }
	    
	    return valid;
	}

	private Boolean checkRearYardUptoTenMts(final Plan pl, Building building, Block block, Integer level,
			final Plot plot, final String rearYardFieldName, final BigDecimal min, final BigDecimal mean,
			final OccupancyTypeHelper mostRestrictiveOccupancy, RearYardResult rearYardResult,
			BigDecimal buildingHeight) {
		String subRule = RULE_4_4_4_I;
		String rule = REAR_YARD_DESC;
		Boolean valid = false;
		BigDecimal minVal = BigDecimal.valueOf(0);
		BigDecimal meanVal = BigDecimal.valueOf(0);
		BigDecimal depthOfPlot = pl.getPlanInformation().getDepthOfPlot();

		if (mostRestrictiveOccupancy.getSubtype() != null
				&& (A_R.equalsIgnoreCase(mostRestrictiveOccupancy.getSubtype().getCode())
						|| A_AF.equalsIgnoreCase(mostRestrictiveOccupancy.getSubtype().getCode())
						|| A_PO.equalsIgnoreCase(mostRestrictiveOccupancy.getSubtype().getCode()))) {
			if (pl.getPlanInformation() != null && pl.getPlanInformation().getRoadWidth() != null
					&& StringUtils.isNotBlank(pl.getPlanInformation().getLandUseZone())
					&& DxfFileConstants.COMMERCIAL.equalsIgnoreCase(pl.getPlanInformation().getLandUseZone())
					&& pl.getPlanInformation().getRoadWidth().compareTo(ROAD_WIDTH_TWELVE_POINTTWO) < 0) {
				valid = commercialUptoSixteenMts(block, level, min, mean, mostRestrictiveOccupancy, rearYardResult,
						DxfFileConstants.RULE_28, rule, minVal, meanVal, depthOfPlot, valid);
			} else {
				valid = residentialUptoTenMts(block, level, min, mean, mostRestrictiveOccupancy, rearYardResult,
						subRule, rule, minVal, meanVal, depthOfPlot, valid);
			}

		} else if (F.equalsIgnoreCase(mostRestrictiveOccupancy.getType().getCode())) {
			valid = commercialUptoSixteenMts(block, level, min, mean, mostRestrictiveOccupancy, rearYardResult, subRule,
					rule, minVal, meanVal, depthOfPlot, valid);
		}

		return valid;
	}

	private Boolean residentialUptoTenMts(Block block, Integer level, final BigDecimal min, final BigDecimal mean,
			final OccupancyTypeHelper mostRestrictiveOccupancy, RearYardResult rearYardResult, String subRule,
			String rule, BigDecimal minVal, BigDecimal meanVal, BigDecimal depthOfPlot, Boolean valid) {

		if (depthOfPlot.compareTo(BigDecimal.valueOf(10)) <= 0) {
			minVal = REARYARDMINIMUM_DISTANCE_0_9;
		} else if (depthOfPlot.compareTo(BigDecimal.valueOf(10)) > 0
				&& depthOfPlot.compareTo(BigDecimal.valueOf(15)) <= 0) {
			minVal = REARYARDMINIMUM_DISTANCE_1_2;
		} else if (depthOfPlot.compareTo(BigDecimal.valueOf(15)) > 0
				&& depthOfPlot.compareTo(BigDecimal.valueOf(21)) <= 0) {
			minVal = REARYARDMINIMUM_DISTANCE_1_5;
		} else if (depthOfPlot.compareTo(BigDecimal.valueOf(21)) > 0
				&& depthOfPlot.compareTo(BigDecimal.valueOf(27)) <= 0) {
			minVal = REARYARDMINIMUM_DISTANCE_1_8;
		} else if (depthOfPlot.compareTo(BigDecimal.valueOf(27)) > 0
				&& depthOfPlot.compareTo(BigDecimal.valueOf(33)) <= 0) {
			minVal = REARYARDMINIMUM_DISTANCE_2_5;
		} else if (depthOfPlot.compareTo(BigDecimal.valueOf(33)) > 0
				&& depthOfPlot.compareTo(BigDecimal.valueOf(39)) <= 0) {
			minVal = REARYARDMINIMUM_DISTANCE_3;
		} else if (depthOfPlot.compareTo(BigDecimal.valueOf(39)) > 0
				&& depthOfPlot.compareTo(BigDecimal.valueOf(45)) <= 0) {
			minVal = REARYARDMINIMUM_DISTANCE_4;
		} else if (depthOfPlot.compareTo(BigDecimal.valueOf(45)) > 0) {
			minVal = REARYARDMINIMUM_DISTANCE_4;
		}

		valid = validateMinimumAndMeanValue(min, mean, minVal, meanVal);
		/*
		 * if (-1 == level) { subRule = SUB_RULE_24_12; rule = BSMT_REAR_YARD_DESC;
		 * subRuleDesc = SUB_RULE_24_12_DESCRIPTION; }
		 */
		compareRearYardResult(block.getName(), min, mean, mostRestrictiveOccupancy, rearYardResult, valid, subRule,
				rule, minVal, meanVal, level);
		return valid;
	}

	private Boolean checkRearYardBasement(Plan plan, Building building, String blockName, Integer level, Plot plot,
			String rearYardFieldName, BigDecimal min, BigDecimal mean, OccupancyTypeHelper mostRestrictiveOccupancy,
			RearYardResult rearYardResult) {
		Boolean valid = false;
		String subRule = RULE_47;
		String rule = REAR_YARD_DESC;
		BigDecimal minVal = BigDecimal.ZERO;
		BigDecimal meanVal = BigDecimal.ZERO;
		if ((mostRestrictiveOccupancy.getSubtype() != null
				&& A_R.equalsIgnoreCase(mostRestrictiveOccupancy.getSubtype().getCode())
				|| A_AF.equalsIgnoreCase(mostRestrictiveOccupancy.getSubtype().getCode())
				|| A_PO.equalsIgnoreCase(mostRestrictiveOccupancy.getSubtype().getCode()))
				|| F.equalsIgnoreCase(mostRestrictiveOccupancy.getType().getCode())) {
			if (plot.getArea().compareTo(BigDecimal.valueOf(PLOTAREA_300)) <= 0) {
				minVal = REARYARDMINIMUM_DISTANCE_3;
				valid = validateMinimumAndMeanValue(min, mean, minVal, meanVal);
			}

			rule = BSMT_REAR_YARD_DESC;

			compareRearYardResult(blockName, min, mean, mostRestrictiveOccupancy, rearYardResult, valid, subRule, rule,
					minVal, meanVal, level);
		}
		return valid;
	}

	private Boolean checkRearYardUptoToTweleveMts(SetBack setback, Building building, final Plan pl, Block block,
			Integer level, final Plot plot, final String rearYardFieldName, final BigDecimal min, final BigDecimal mean,
			final OccupancyTypeHelper mostRestrictiveOccupancy, RearYardResult rearYardResult,
			HashMap<String, String> errors) {
		String subRule = RULE_4_4_4_I;
		String rule = REAR_YARD_DESC;
		Boolean valid = false;
		BigDecimal minVal = BigDecimal.valueOf(0);
		BigDecimal meanVal = BigDecimal.valueOf(0);
		BigDecimal depthOfPlot = pl.getPlanInformation().getDepthOfPlot();

		if (mostRestrictiveOccupancy.getSubtype() != null
				&& A_R.equalsIgnoreCase(mostRestrictiveOccupancy.getSubtype().getCode())
				|| A_AF.equalsIgnoreCase(mostRestrictiveOccupancy.getSubtype().getCode())
				|| A_PO.equalsIgnoreCase(mostRestrictiveOccupancy.getSubtype().getCode())) {
			if (pl.getPlanInformation() != null && pl.getPlanInformation().getRoadWidth() != null
					&& StringUtils.isNotBlank(pl.getPlanInformation().getLandUseZone())
					&& DxfFileConstants.COMMERCIAL.equalsIgnoreCase(pl.getPlanInformation().getLandUseZone())
					&& pl.getPlanInformation().getRoadWidth().compareTo(ROAD_WIDTH_TWELVE_POINTTWO) < 0) {
				commercialUptoSixteenMts(block, level, min, mean, mostRestrictiveOccupancy, rearYardResult,
						DxfFileConstants.RULE_28, rule, minVal, meanVal, depthOfPlot, valid);
			} else {
				valid = residentialUptoTwelveMts(block, level, min, mean, mostRestrictiveOccupancy, rearYardResult,
						subRule, rule, minVal, meanVal, depthOfPlot, valid, errors, pl);
			}

		} else if (F.equalsIgnoreCase(mostRestrictiveOccupancy.getType().getCode())) {
			valid = commercialUptoSixteenMts(block, level, min, mean, mostRestrictiveOccupancy, rearYardResult, subRule,
					rule, minVal, meanVal, depthOfPlot, valid);
		}

		return valid;
	}

	private Boolean checkRearYardForIndustrial(SetBack setback, Building building, final Plan pl, Block block,
			Integer level, final Plot plot, final String rearYardFieldName, final BigDecimal min, final BigDecimal mean,
			final OccupancyTypeHelper mostRestrictiveOccupancy, RearYardResult rearYardResult) {
		String subRule = RULE_4_4_4_I;
		String rule = REAR_YARD_DESC;
		Boolean valid = false;
		BigDecimal minVal = BigDecimal.valueOf(0);
		BigDecimal meanVal = BigDecimal.valueOf(0);
		BigDecimal widthOfPlot = pl.getPlanInformation().getWidthOfPlot();

		valid = processRearYardForIndustrial(block, level, min, mean, mostRestrictiveOccupancy, rearYardResult, subRule,
				rule, minVal, meanVal, pl.getPlot().getArea(), widthOfPlot, valid);

		return valid;
	}

	private Boolean residentialUptoTwelveMts(Block block, Integer level, final BigDecimal min, final BigDecimal mean,
			final OccupancyTypeHelper mostRestrictiveOccupancy, RearYardResult rearYardResult, String subRule,
			String rule, BigDecimal minVal, BigDecimal meanVal, BigDecimal depthOfPlot, Boolean valid,
			HashMap<String, String> errors, Plan pl) {

		if (depthOfPlot.compareTo(BigDecimal.valueOf(10)) <= 0) {
			errors.put(TWELVE_HEIGHT_TEN_DEPTH_REAR_YARD, NO_CONST_PERMIT_DEPTH_LESS_10_BUILDING_HEIGHT_12);
			pl.addErrors(errors);
		} else if (depthOfPlot.compareTo(BigDecimal.valueOf(10)) > 0
				&& depthOfPlot.compareTo(BigDecimal.valueOf(15)) <= 0) {
			minVal = REARYARDMINIMUM_DISTANCE_1_8;
		} else if (depthOfPlot.compareTo(BigDecimal.valueOf(15)) > 0
				&& depthOfPlot.compareTo(BigDecimal.valueOf(21)) <= 0) {
			minVal = REARYARDMINIMUM_DISTANCE_2;
		} else if (depthOfPlot.compareTo(BigDecimal.valueOf(21)) > 0
				&& depthOfPlot.compareTo(BigDecimal.valueOf(27)) <= 0) {
			minVal = REARYARDMINIMUM_DISTANCE_2_5;
		} else if (depthOfPlot.compareTo(BigDecimal.valueOf(27)) > 0
				&& depthOfPlot.compareTo(BigDecimal.valueOf(33)) <= 0) {
			minVal = REARYARDMINIMUM_DISTANCE_3;
		} else if (depthOfPlot.compareTo(BigDecimal.valueOf(33)) > 0
				&& depthOfPlot.compareTo(BigDecimal.valueOf(39)) <= 0) {
			minVal = REARYARDMINIMUM_DISTANCE_4;
		} else if (depthOfPlot.compareTo(BigDecimal.valueOf(39)) > 0
				&& depthOfPlot.compareTo(BigDecimal.valueOf(45)) <= 0) {
			minVal = REARYARDMINIMUM_DISTANCE_4;
		} else if (depthOfPlot.compareTo(BigDecimal.valueOf(45)) > 0) {
			minVal = REARYARDMINIMUM_DISTANCE_4;
		}

		valid = validateMinimumAndMeanValue(min, mean, minVal, meanVal);
		/*
		 * if (-1 == level) { subRule = SUB_RULE_24_12; rule = BSMT_REAR_YARD_DESC;
		 * subRuleDesc = SUB_RULE_24_12_DESCRIPTION; }
		 */

		compareRearYardResult(block.getName(), min, mean, mostRestrictiveOccupancy, rearYardResult, valid, subRule,
				rule, minVal, meanVal, level);
		return valid;
	}

	private Boolean processRearYardForIndustrial(Block block, Integer level, final BigDecimal min,
			final BigDecimal mean, final OccupancyTypeHelper mostRestrictiveOccupancy, RearYardResult rearYardResult,
			String subRule, String rule, BigDecimal minVal, BigDecimal meanVal, BigDecimal plotArea,
			BigDecimal widthOfPlot, Boolean valid) {

		if (plotArea.compareTo(BigDecimal.valueOf(550)) < 0) {
			if (widthOfPlot.compareTo(BigDecimal.valueOf(10)) <= 0) {
				minVal = REARYARDMINIMUM_DISTANCE_3;
			} else if (widthOfPlot.compareTo(BigDecimal.valueOf(12)) <= 0) {
				minVal = REARYARDMINIMUM_DISTANCE_3;
			} else if (widthOfPlot.compareTo(BigDecimal.valueOf(15)) <= 0) {
				minVal = REARYARDMINIMUM_DISTANCE_3;
			} else if (widthOfPlot.compareTo(BigDecimal.valueOf(18)) <= 0) {
				minVal = REARYARDMINIMUM_DISTANCE_4;
			} else if (widthOfPlot.compareTo(BigDecimal.valueOf(18)) > 0) {
				minVal = REARYARDMINIMUM_DISTANCE_4_5;
			}
		} else if (plotArea.compareTo(BigDecimal.valueOf(550)) > 0
				&& plotArea.compareTo(BigDecimal.valueOf(1000)) <= 0) {
			minVal = REARYARDMINIMUM_DISTANCE_4_5;

		} else if (plotArea.compareTo(BigDecimal.valueOf(1000)) > 0
				&& plotArea.compareTo(BigDecimal.valueOf(5000)) <= 0) {
			minVal = REARYARDMINIMUM_DISTANCE_6;

		} else if (plotArea.compareTo(BigDecimal.valueOf(5000)) > 0
				&& plotArea.compareTo(BigDecimal.valueOf(30000)) <= 0) {
			minVal = REARYARDMINIMUM_DISTANCE_9;

		} else if (plotArea.compareTo(BigDecimal.valueOf(30000)) > 0) {
			minVal = REARYARDMINIMUM_DISTANCE_12;

		}

		valid = validateMinimumAndMeanValue(min, mean, minVal, meanVal);
		/*
		 * if (-1 == level) { subRule = SUB_RULE_24_12; rule = BSMT_REAR_YARD_DESC;
		 * subRuleDesc = SUB_RULE_24_12_DESCRIPTION; }
		 */

		compareRearYardResult(block.getName(), min, mean, mostRestrictiveOccupancy, rearYardResult, valid, subRule,
				rule, minVal, meanVal, level);
		return valid;
	}

	private Boolean checkRearYardUptoToSixteenMts(SetBack setback, Building building, final Plan pl, Block block,
			Integer level, final Plot plot, final String rearYardFieldName, final BigDecimal min, final BigDecimal mean,
			final OccupancyTypeHelper mostRestrictiveOccupancy, RearYardResult rearYardResult,
			HashMap<String, String> errors) {
		String subRule = RULE_4_4_4_I;
		String rule = REAR_YARD_DESC;
		Boolean valid = false;
		BigDecimal minVal = BigDecimal.valueOf(0);
		BigDecimal meanVal = BigDecimal.valueOf(0);
		BigDecimal depthOfPlot = pl.getPlanInformation().getDepthOfPlot();

		if (A_R.equalsIgnoreCase(mostRestrictiveOccupancy.getSubtype().getCode())
				|| A_AF.equalsIgnoreCase(mostRestrictiveOccupancy.getSubtype().getCode())
				|| A_PO.equalsIgnoreCase(mostRestrictiveOccupancy.getSubtype().getCode())
						&& block.getBuilding().getFloorsAboveGround().compareTo(BigDecimal.valueOf(5)) <= 0) {
			if (pl.getPlanInformation() != null && pl.getPlanInformation().getRoadWidth() != null
					&& StringUtils.isNotBlank(pl.getPlanInformation().getLandUseZone())
					&& DxfFileConstants.COMMERCIAL.equalsIgnoreCase(pl.getPlanInformation().getLandUseZone())
					&& pl.getPlanInformation().getRoadWidth().compareTo(ROAD_WIDTH_TWELVE_POINTTWO) < 0) {
				valid = commercialUptoSixteenMts(block, level, min, mean, mostRestrictiveOccupancy, rearYardResult,
						DxfFileConstants.RULE_28, rule, minVal, meanVal, depthOfPlot, valid);
			} else {
				valid = residentialUptoSixteenMts(block, level, min, mean, mostRestrictiveOccupancy, rearYardResult,
						subRule, rule, minVal, meanVal, depthOfPlot, valid, errors, pl);
			}
		} else if (F.equalsIgnoreCase(mostRestrictiveOccupancy.getType().getCode())) {
			valid = commercialUptoSixteenMts(block, level, min, mean, mostRestrictiveOccupancy, rearYardResult, subRule,
					rule, minVal, meanVal, depthOfPlot, valid);
		}

		return valid;
	}

	private Boolean residentialUptoSixteenMts(Block block, Integer level, final BigDecimal min, final BigDecimal mean,
			final OccupancyTypeHelper mostRestrictiveOccupancy, RearYardResult rearYardResult, String subRule,
			String rule, BigDecimal minVal, BigDecimal meanVal, BigDecimal depthOfPlot, Boolean valid,
			HashMap<String, String> errors, Plan pl) {
		if (depthOfPlot.compareTo(BigDecimal.valueOf(10)) <= 0) {
			errors.put(SIXTEEN_HEIGHT_TEN_DEPTH_REAR_YARD, NO_CONST_PERMIT_DEPTH_10_BUILDING_HEIGHT_16);
			pl.addErrors(errors);
		} else if (depthOfPlot.compareTo(BigDecimal.valueOf(10)) > 0
				&& depthOfPlot.compareTo(BigDecimal.valueOf(15)) <= 0) {
			minVal = REARYARDMINIMUM_DISTANCE_2;
		} else if (depthOfPlot.compareTo(BigDecimal.valueOf(15)) > 0
				&& depthOfPlot.compareTo(BigDecimal.valueOf(21)) <= 0) {
			minVal = REARYARDMINIMUM_DISTANCE_3;
		} else if (depthOfPlot.compareTo(BigDecimal.valueOf(21)) > 0
				&& depthOfPlot.compareTo(BigDecimal.valueOf(27)) <= 0) {
			minVal = REARYARDMINIMUM_DISTANCE_3_6;
		} else if (depthOfPlot.compareTo(BigDecimal.valueOf(27)) > 0
				&& depthOfPlot.compareTo(BigDecimal.valueOf(33)) <= 0) {
			minVal = REARYARDMINIMUM_DISTANCE_4;
		} else if (depthOfPlot.compareTo(BigDecimal.valueOf(33)) > 0
				&& depthOfPlot.compareTo(BigDecimal.valueOf(39)) <= 0) {
			minVal = REARYARDMINIMUM_DISTANCE_4;
		} else if (depthOfPlot.compareTo(BigDecimal.valueOf(39)) > 0
				&& depthOfPlot.compareTo(BigDecimal.valueOf(45)) <= 0) {
			minVal = REARYARDMINIMUM_DISTANCE_4;
		} else if (depthOfPlot.compareTo(BigDecimal.valueOf(45)) > 0) {
			minVal = REARYARDMINIMUM_DISTANCE_4_5;
		}

		valid = validateMinimumAndMeanValue(min, mean, minVal, meanVal);
		/*
		 * if (-1 == level) { subRule = SUB_RULE_24_12; rule = BSMT_REAR_YARD_DESC;
		 * subRuleDesc = SUB_RULE_24_12_DESCRIPTION; }
		 */

		compareRearYardResult(block.getName(), min, mean, mostRestrictiveOccupancy, rearYardResult, valid, subRule,
				rule, minVal, meanVal, level);
		return valid;
	}

	private Boolean commercialUptoSixteenMts(Block block, Integer level, final BigDecimal min, final BigDecimal mean,
			final OccupancyTypeHelper mostRestrictiveOccupancy, RearYardResult rearYardResult, String subRule,
			String rule, BigDecimal minVal, BigDecimal meanVal, BigDecimal depthOfPlot, Boolean valid) {
		if (depthOfPlot.compareTo(BigDecimal.valueOf(10)) <= 0) {
			minVal = REARYARDMINIMUM_DISTANCE_2;
		} else if (depthOfPlot.compareTo(BigDecimal.valueOf(10)) > 0
				&& depthOfPlot.compareTo(BigDecimal.valueOf(15)) <= 0) {
			minVal = REARYARDMINIMUM_DISTANCE_3;
		} else if (depthOfPlot.compareTo(BigDecimal.valueOf(15)) > 0
				&& depthOfPlot.compareTo(BigDecimal.valueOf(21)) <= 0) {
			minVal = REARYARDMINIMUM_DISTANCE_4;
		} else if (depthOfPlot.compareTo(BigDecimal.valueOf(21)) > 0
				&& depthOfPlot.compareTo(BigDecimal.valueOf(27)) <= 0) {
			minVal = REARYARDMINIMUM_DISTANCE_4;
		} else if (depthOfPlot.compareTo(BigDecimal.valueOf(27)) > 0
				&& depthOfPlot.compareTo(BigDecimal.valueOf(33)) <= 0) {
			minVal = REARYARDMINIMUM_DISTANCE_4;
		} else if (depthOfPlot.compareTo(BigDecimal.valueOf(33)) > 0
				&& depthOfPlot.compareTo(BigDecimal.valueOf(39)) <= 0) {
			minVal = REARYARDMINIMUM_DISTANCE_4_5;
		} else if (depthOfPlot.compareTo(BigDecimal.valueOf(39)) > 0
				&& depthOfPlot.compareTo(BigDecimal.valueOf(45)) <= 0) {
			minVal = REARYARDMINIMUM_DISTANCE_4_5;
		} else if (depthOfPlot.compareTo(BigDecimal.valueOf(45)) > 0) {
			minVal = REARYARDMINIMUM_DISTANCE_4_5;
		}

		valid = validateMinimumAndMeanValue(min, mean, minVal, meanVal);
		/*
		 * if (-1 == level) { subRule = SUB_RULE_24_12; rule = BSMT_REAR_YARD_DESC;
		 * subRuleDesc = SUB_RULE_24_12_DESCRIPTION; }
		 */

		compareRearYardResult(block.getName(), min, mean, mostRestrictiveOccupancy, rearYardResult, valid, subRule,
				rule, minVal, meanVal, level);
		return valid;
	}

	private Boolean checkRearYardAboveSixteenMts(SetBack setback, Building building, final Plan pl, Block block,
			Integer level, final Plot plot, final String rearYardFieldName, final BigDecimal min, final BigDecimal mean,
			final OccupancyTypeHelper mostRestrictiveOccupancy, RearYardResult rearYardResult,
			BigDecimal buildingHeight) {
		Boolean valid = false;
		String subRule = RULE_36;
		String rule = REAR_YARD_DESC;
		BigDecimal minVal = BigDecimal.ZERO;
		BigDecimal meanVal = BigDecimal.ZERO;
		valid = allOccupancyForHighRise(block, level, min, mean, mostRestrictiveOccupancy, rearYardResult, subRule,
				rule, minVal, meanVal, buildingHeight);
		return valid;
	}

	private Boolean allOccupancyForHighRise(Block block, Integer level, final BigDecimal min, final BigDecimal mean,
			final OccupancyTypeHelper mostRestrictiveOccupancy, RearYardResult rearYardResult, String subRule,
			String rule, BigDecimal minVal, BigDecimal meanVal, BigDecimal blockBuildingHeight) {
		Boolean valid = false;

		if (blockBuildingHeight.compareTo(BigDecimal.valueOf(16)) > 0
				&& blockBuildingHeight.compareTo(BigDecimal.valueOf(19)) <= 0) {
			minVal = REARYARDMINIMUM_DISTANCE_4_5;
		} else if (blockBuildingHeight.compareTo(BigDecimal.valueOf(19)) > 0
				&& blockBuildingHeight.compareTo(BigDecimal.valueOf(22)) <= 0) {
			minVal = REARYARDMINIMUM_DISTANCE_4_5;
		} else if (blockBuildingHeight.compareTo(BigDecimal.valueOf(22)) > 0
				&& blockBuildingHeight.compareTo(BigDecimal.valueOf(25)) <= 0) {
			minVal = REARYARDMINIMUM_DISTANCE_5;
		} else if (blockBuildingHeight.compareTo(BigDecimal.valueOf(25)) > 0
				&& blockBuildingHeight.compareTo(BigDecimal.valueOf(28)) <= 0) {
			minVal = REARYARDMINIMUM_DISTANCE_6;
		} else if (blockBuildingHeight.compareTo(BigDecimal.valueOf(28)) > 0
				&& blockBuildingHeight.compareTo(BigDecimal.valueOf(31)) <= 0) {
			minVal = REARYARDMINIMUM_DISTANCE_7;
		} else if (blockBuildingHeight.compareTo(BigDecimal.valueOf(31)) > 0
				&& blockBuildingHeight.compareTo(BigDecimal.valueOf(36)) <= 0) {
			minVal = REARYARDMINIMUM_DISTANCE_7;
		} else if (blockBuildingHeight.compareTo(BigDecimal.valueOf(36)) > 0
				&& blockBuildingHeight.compareTo(BigDecimal.valueOf(41)) <= 0) {
			minVal = REARYARDMINIMUM_DISTANCE_8;
		} else if (blockBuildingHeight.compareTo(BigDecimal.valueOf(41)) > 0
				&& blockBuildingHeight.compareTo(BigDecimal.valueOf(46)) <= 0) {
			minVal = REARYARDMINIMUM_DISTANCE_8;
		} else if (blockBuildingHeight.compareTo(BigDecimal.valueOf(46)) > 0
				&& blockBuildingHeight.compareTo(BigDecimal.valueOf(51)) <= 0) {
			minVal = REARYARDMINIMUM_DISTANCE_9;
		} else if (blockBuildingHeight.compareTo(BigDecimal.valueOf(51)) > 0) {
			minVal = REARYARDMINIMUM_DISTANCE_9;
		}

		/*
		 * if (-1 == level) { rule = BSMT_REAR_YARD_DESC; subRuleDesc =
		 * SUB_RULE_24_12_DESCRIPTION; subRule = SUB_RULE_24_12; }
		 */

		valid = validateMinimumAndMeanValue(min, mean, minVal, meanVal);

		compareRearYardResult(block.getName(), min, mean, mostRestrictiveOccupancy, rearYardResult, valid, subRule,
				rule, minVal, meanVal, level);
		return valid;
	}
	private Boolean checkRearYard(
	        final Plan pl,
	        Building building,
	        Block block,
	        Integer level,
	        final Plot plot,
	        final String rearYardFieldName,
	        final BigDecimal min,
	        final BigDecimal mean,
	        final OccupancyTypeHelper mostRestrictiveOccupancy,
	        RearYardResult rearYardResult,
	        BigDecimal buildingHeight, HashMap<String, String> errors) {

	    String subRule = "";
	    String rule = REAR_YARD_DESC;
	    Boolean valid = false;

	    BigDecimal minVal = BigDecimal.ZERO;
	    BigDecimal meanVal = BigDecimal.ZERO;
	    BigDecimal depthOfPlot = pl.getPlanInformation().getDepthOfPlot();
	    String occupancyCode = mostRestrictiveOccupancy.getType().getCode();
	    String subOccupancyCode = mostRestrictiveOccupancy.getSubtype().getCode();
	    String occupancyName = mostRestrictiveOccupancy.getType().getName();

	    BigDecimal roadWidth = pl.getPlanInformation().getRoadWidth();
	    BigDecimal plotArea = plot.getArea();

	    // Check Narrow Road Special Rule First
	    if (roadWidth != null && roadWidth.compareTo(BigDecimal.valueOf(2.40)) == 0) {
	        LOG.info("Checking special narrow road rule for REAR yard → Block: {}, Level: {}, RoadWidth: {}", 
	                  block.getName(), level, roadWidth);

	        BigDecimal allowedFloors = BigDecimal.valueOf(2); // G + 1
	        BigDecimal actualFloors = building.getTotalFloors();

	        if (actualFloors.compareTo(allowedFloors) > 0) {
	            errors.put(ERR_NARROW_ROAD_RULE,
	                    String.format(ERR_NARROW_ROAD_RULE, actualFloors));
	            LOG.warn("Narrow road violation (Rear Yard): Allowed = {}, Actual = {}", allowedFloors, actualFloors);
	            return false; 
	        }

	        Boolean specialValid = applySpecialRuleForNarrowRoadRear(
	                pl, building, block.getName(), level, plot,
	                mostRestrictiveOccupancy, rearYardResult, 
	                buildingHeight, errors, roadWidth, plotArea);

	        if (specialValid != null) {
	            LOG.info("Special narrow road rule (Rear Yard) applied. Result = {}", specialValid);
	            return specialValid; 
	        } else {
	            LOG.info("Special narrow road rule (Rear Yard) not applicable, continuing with normal checks.");
	        }
	    }
	    
	    
	    if (A.equalsIgnoreCase(occupancyCode) || H.equalsIgnoreCase(occupancyCode)) {
	        // Residential
	        valid = processRearYardResidential(
	                pl, block, level, min, mean, mostRestrictiveOccupancy, rearYardResult,
	                subRule, rule, minVal, meanVal, buildingHeight, valid, occupancyName);

	    } else if (F.equalsIgnoreCase(occupancyCode)) {
	    	  if (buildingHeight.compareTo(BUILDING_HEIGHT) <= 0 &&
	    		        plot.getArea().compareTo(PLOT_AREA_802_SQM) <= 0) {
	        // Commercial
	        valid = processRearYardCommercial(
	                pl, block, level, min, mean, mostRestrictiveOccupancy, rearYardResult,
	                subRule, rule, minVal, meanVal, depthOfPlot, valid, occupancyName);

	    }else{
	    	 valid = processRearYardResidential(
		                pl, block, level, min, mean, mostRestrictiveOccupancy, rearYardResult,
		                subRule, rule, minVal, meanVal, depthOfPlot, valid, occupancyName);

	    	
	    }} else if (G.equalsIgnoreCase(occupancyCode)) {
	        // Industrial
	        
	        valid = processRearYardIndustrial(
	                pl, block, level, min, mean, mostRestrictiveOccupancy, rearYardResult,
	                subRule, rule, minVal, meanVal, buildingHeight, valid, occupancyName);
	    } else if (D.equalsIgnoreCase(occupancyCode) &&  D_AW.equalsIgnoreCase(occupancyCode)) {
	       
	        
	        valid = processRearYardPlaceOfworship(
	                pl, block, level, min, mean, mostRestrictiveOccupancy, rearYardResult,
	                subRule, rule, minVal, meanVal, buildingHeight, valid, occupancyName);
	    }else if (C.equalsIgnoreCase(occupancyCode)) {
	       
	        
	        valid = processRearYardHospitalAndNursingHomes(
	                pl, block, level, min, mean, mostRestrictiveOccupancy, rearYardResult,
	                subRule, rule, minVal, meanVal, buildingHeight, valid, occupancyName);
	    }else if (D.equalsIgnoreCase(occupancyCode)) {
	       
	        
	        valid = processRearYardAssembly(
	                pl, block, level, min, mean, mostRestrictiveOccupancy, rearYardResult,
	                subRule, rule, minVal, meanVal, buildingHeight, valid, occupancyName);
	    }else if (D.equalsIgnoreCase(occupancyCode) &&  D_M.equalsIgnoreCase(occupancyCode)) {
	       
	        
	        valid = processRearYardMultiplex(
	                pl, block, level, min, mean, mostRestrictiveOccupancy, rearYardResult,
	                subRule, rule, minVal, meanVal, buildingHeight, valid, occupancyName);
	    }else if (E.equalsIgnoreCase(occupancyCode) && buildingHeight.compareTo(BUILDING_HEIGHT_SCHOOL) <= 0) {
	        // For school buildings up to 15.6m height
	        valid = processRearYardSchool(
	            pl, block, level, min, mean, mostRestrictiveOccupancy, rearYardResult,
	            subRule, rule, minVal, meanVal, buildingHeight, valid, occupancyName);

	    } else if (E.equalsIgnoreCase(occupancyCode) && buildingHeight.compareTo(BUILDING_HEIGHT_SCHOOL) > 0) {
	        // For school buildings above 15.6m height
	        valid = processRearYardResidential(
	            pl, block, level, min, mean, mostRestrictiveOccupancy, rearYardResult,
	            subRule, rule, minVal, meanVal, buildingHeight, valid, occupancyName);
	    }

	    return valid;
	}

	/**
	 * Applies special rules for rear yard requirements when the building 
	 * is located on a narrow road (specifically when road width = 2.40m).
	 * <p>
	 * The rule is applicable only when the plot area falls within specific ranges:
	 * <ul>
	 *   <li><b>53.56 – 93.73 sqm:</b> Minimum rear setback of 1.80m (mean also 1.80m).</li>
	 *   <li><b>93.73 – 134 sqm:</b> Minimum rear setback of 2.00m, and side setback of 1.00m.</li>
	 * </ul>
	 * </p>
	 * <p>
	 * The method validates the provided rear yard distances against the 
	 * permissible values, updates the {@link RearYardResult}, and logs 
	 * errors if requirements are not met.
	 * </p>
	 *
	 * @param pl The {@link Plan} object containing the overall plan details.
	 * @param building The {@link Building} under consideration.
	 * @param blockName The name of the block being validated.
	 * @param level The floor/level of the block being checked.
	 * @param plot The {@link Plot} object containing plot details such as area.
	 * @param mostRestrictiveOccupancy The most restrictive {@link OccupancyTypeHelper} 
	 *                                 applicable to this block.
	 * @param rearYardResult The {@link RearYardResult} object holding measured rear yard values.
	 * @param buildingHeight The height of the building.
	 * @param errors A {@link HashMap} to collect error messages keyed by block name.
	 * @param roadWidth The width of the road adjacent to the plot (special rules apply when 2.40m).
	 * @param plotArea The total area of the plot.
	 * @return {@code true} if the rear yard meets the special setback rules, 
	 *         {@code false} otherwise.
	 */
	
	
	private Boolean applySpecialRuleForNarrowRoadRear(
	        Plan pl, Building building, String blockName, Integer level, Plot plot,
	        OccupancyTypeHelper mostRestrictiveOccupancy, RearYardResult rearYardResult,
	        BigDecimal buildingHeight, HashMap<String, String> errors,
	        BigDecimal roadWidth, BigDecimal plotArea) {

	    LOG.info("Applying special narrow road rule (Road Width = 2.40m) for Rear Yard → Block: {}, Level: {}, Plot Area: {}", 
	             blockName, level, plotArea);

	    BigDecimal minVal = BigDecimal.ZERO;  
	    BigDecimal meanVal = BigDecimal.ZERO;  
	    String subRule = "";
	    String rule = REAR_YARD_DESC;

	    if (plotArea.compareTo(BigDecimal.valueOf(53.56)) >= 0 
	            && plotArea.compareTo(BigDecimal.valueOf(93.73)) <= 0) {

	        minVal = BigDecimal.valueOf(1.80);  
	        meanVal = BigDecimal.valueOf(1.80); 
	        subRule = "Rear setback";
	        LOG.info("Rear Yard → Matched Plot Area 53.56 - 93.73 sqm → {}", subRule);

	    } else if (plotArea.compareTo(BigDecimal.valueOf(93.73)) > 0 
	            && plotArea.compareTo(BigDecimal.valueOf(134)) <= 0) {

	        minVal = BigDecimal.valueOf(2.00);  
	        meanVal = BigDecimal.valueOf(1.00); 
	        subRule = "Rear setback";
	        LOG.info("Rear Yard → Matched Plot Area 93.73 - 134 sqm → {}", subRule);
	    }

	    BigDecimal providedMin = rearYardResult.actualMinDistance;   
	    BigDecimal providedMean = rearYardResult.actualMeanDistance;

	    Boolean valid = (providedMin != null && providedMin.compareTo(minVal) >= 0);

	    compareRearYardResult(blockName, providedMin, providedMean,
	            mostRestrictiveOccupancy, rearYardResult, valid,
	            subRule, rule, minVal, meanVal, level);

	    if (!valid) {
	        errors.put(blockName + "_RearYard", 
	            "Rear setback must be at least " + minVal + " m (provided " + providedMin + " m)");
	    }

	    LOG.info("Rear Yard special rule applied → ProvidedMin: {}, RequiredMin: {}, Status: {}", 
	             providedMin, minVal, valid);

	    return valid;
	}


	private Boolean checkRearYardOtherOccupancies(SetBack setback, Building building, final Plan pl, Block block,
			Integer level, final Plot plot, final String rearYardFieldName, final BigDecimal min, final BigDecimal mean,
			final OccupancyTypeHelper mostRestrictiveOccupancy, RearYardResult rearYardResult,
			BigDecimal buildingHeight) {
		Boolean valid = false;
		String subRule = RULE_37_TWO_A;
		String rule = REAR_YARD_DESC;
		BigDecimal minVal = BigDecimal.ZERO;
		BigDecimal meanVal = BigDecimal.ZERO;
		// Educational
		if (mostRestrictiveOccupancy.getType() != null
				&& B.equalsIgnoreCase(mostRestrictiveOccupancy.getType().getCode())) {
			minVal = REARYARDMINIMUM_DISTANCE_6;
			subRule = RULE_37_TWO_A;
		} // Institutional
		if (mostRestrictiveOccupancy.getType() != null
				&& B.equalsIgnoreCase(mostRestrictiveOccupancy.getType().getCode())) {
			minVal = REARYARDMINIMUM_DISTANCE_6;
			subRule = RULE_37_TWO_B;
		} // Assembly
		if (mostRestrictiveOccupancy.getType() != null
				&& D.equalsIgnoreCase(mostRestrictiveOccupancy.getType().getCode())) {
			minVal = REARYARDMINIMUM_DISTANCE_6;
			subRule = RULE_37_TWO_C;
		} // Malls and multiplex
		if (mostRestrictiveOccupancy.getType() != null
				&& D.equalsIgnoreCase(mostRestrictiveOccupancy.getType().getCode())) {
			minVal = REARYARDMINIMUM_DISTANCE_7;
			subRule = RULE_37_TWO_D;
		} // Hazardous
		if (mostRestrictiveOccupancy.getType() != null
				&& I.equalsIgnoreCase(mostRestrictiveOccupancy.getType().getCode())) {
			minVal = REARYARDMINIMUM_DISTANCE_9;
			subRule = RULE_37_TWO_G;
		} // Affordable
		if (mostRestrictiveOccupancy.getType() != null
				&& A.equalsIgnoreCase(mostRestrictiveOccupancy.getType().getCode())) {
			minVal = REARYARDMINIMUM_DISTANCE_3;
			subRule = RULE_37_TWO_H;
		}
		// IT,ITES
		if (mostRestrictiveOccupancy.getType() != null
				&& F.equalsIgnoreCase(mostRestrictiveOccupancy.getType().getCode())) {
			// nil as per commercial
			subRule = RULE_37_TWO_I;
		}

		valid = validateMinimumAndMeanValue(min, mean, minVal, meanVal);

		compareRearYardResult(block.getName(), min, mean, mostRestrictiveOccupancy, rearYardResult, valid, subRule,
				rule, minVal, meanVal, level);
		return valid;
	}

	private Boolean validateMinimumAndMeanValue(final BigDecimal min, final BigDecimal mean, final BigDecimal minval,
			final BigDecimal meanval) {
		Boolean valid = false;
		if (min.compareTo(minval) >= 0 && mean.compareTo(meanval) >= 0)
			valid = true;
		return valid;
	}
	
	

	private void validateRearYard(final Plan pl) {
		for (Block block : pl.getBlocks()) {
			if (!block.getCompletelyExisting()) {
				Boolean rearYardDefined = false;
				for (SetBack setback : block.getSetBacks()) {
					if (setback.getRearYard() != null
							&& setback.getRearYard().getMean().compareTo(BigDecimal.valueOf(0)) > 0) {
						rearYardDefined = true;
					}
				}
				if (!rearYardDefined && !pl.getPlanInformation().getNocToAbutRearDesc().equalsIgnoreCase(YES)) {
					HashMap<String, String> errors = new HashMap<>();
					errors.put(REAR_YARD_DESC,
							prepareMessage(OBJECTNOTDEFINED, REAR_YARD_DESC + FOR_BLOCK + block.getName()));
					pl.addErrors(errors);
				}
			}

		}

	}

	private void compareRearYardResult(String blockName, BigDecimal min, BigDecimal mean,
			OccupancyTypeHelper mostRestrictiveOccupancy, RearYardResult rearYardResult, Boolean valid, String subRule,
			String rule, BigDecimal minVal, BigDecimal meanVal, Integer level) {
		String occupancyName;
		if (mostRestrictiveOccupancy.getSubtype() != null)
			occupancyName = mostRestrictiveOccupancy.getSubtype().getName();
		else
			occupancyName = mostRestrictiveOccupancy.getType().getName();
		if (minVal.compareTo(rearYardResult.expectedminimumDistance) >= 0) {
			if (minVal.compareTo(rearYardResult.expectedminimumDistance) == 0) {
				rearYardResult.rule = rearYardResult.rule != null ? rearYardResult.rule + COMMA + rule : rule;
				rearYardResult.occupancy = rearYardResult.occupancy != null
						? rearYardResult.occupancy + COMMA + occupancyName
						: occupancyName;

				if (meanVal.compareTo(rearYardResult.expectedmeanDistance) >= 0) {
					rearYardResult.expectedmeanDistance = meanVal;
					rearYardResult.actualMeanDistance = mean;
				}
			} else {
				rearYardResult.rule = rule;
				rearYardResult.occupancy = occupancyName;
				rearYardResult.expectedmeanDistance = meanVal;
				rearYardResult.actualMeanDistance = mean;

			}

			rearYardResult.subRule = subRule;
			rearYardResult.blockName = blockName;
			rearYardResult.level = level;
			rearYardResult.expectedminimumDistance = minVal;
			rearYardResult.actualMinDistance = min;
			rearYardResult.status = valid;

		}
	}
}
