package org.egov.common.entity.edcr;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
/**
 * 
 * @author mani
 *   Gates will be MainGate,WicketGate etc
 *   Further it may be Frontgate,Reargate 
 *
 */
public class Toilet implements Serializable  {
	
	private List<Measurement> toilets;
	private BigDecimal toiletVentilation;
	
	private List<BigDecimal> toiletWindowWidth;
	
	public List<BigDecimal> getToiletWindowWidth() {
		return toiletWindowWidth;
	}
	public void setToiletWindowWidth(List<BigDecimal> toiletWindowWidth) {
		this.toiletWindowWidth = toiletWindowWidth;
	}
	public List<Measurement> getToilets() {
		return toilets;
	}
	public void setToilets(List<Measurement> toilets) {
		this.toilets = toilets;
	}
	public BigDecimal getToiletVentilation() {
		return toiletVentilation;
	}
	public void setToiletVentilation(BigDecimal toiletVentilation) {
		this.toiletVentilation = toiletVentilation;
	}
	
	  
	

}
