/**
 * 
 */
package cn.edu.scnu.s4.test;

/**
 * @author ChunweiXu
 *
 */
public class TrafficFlow {
	private int zone;
	private int rateVersion;
    private int importSection;
    private int importStation;
    private int exportSection;
    private int exportStation;
    private int identifyingStation;
    private int carType;
    private int figure;
    private int mileage;
	
	public int getZone() {
		return zone;
	}
	
	public void setZone(int zone) {
		this.zone = zone;
	}
	
	public int getRateVersion() {
		return rateVersion;
	}
	
	public void setRateVersion(int rateVersion) {
		this.rateVersion = rateVersion;
	}
	
	public int getImportSection() {
		return importSection;
	}
	
	public void setImportSection(int importSection) {
		this.importSection = importSection;
	}
	
	public int getImportStation() {
		return importStation;
	}
	
	public void setImportStation(int importStation) {
		this.importStation = importStation;
	}
	
	public int getExportSection() {
		return exportSection;
	}
	
	public void setExportSection(int exportSection) {
		this.exportSection = exportSection;
	}
	
	public int getExportStation() {
		return exportStation;
	}
	
	public void setExportStation(int exportStation) {
		this.exportStation = exportStation;
	}
	
	public int getIdentifyingStation() {
		return identifyingStation;
	}
	
	public void setIdentifyingStation(int identifyingStation) {
		this.identifyingStation = identifyingStation;
	}
	
	public int getCarType() {
		return carType;
	}
	
	public void setCarType(int carType) {
		this.carType = carType;
	}
	
	public int getFigure() {
		return figure;
	}
	
	public void setFigure(int figure) {
		this.figure = figure;
	}
	
	public int getMileage() {
		return mileage;
	}
	
	public void setMileage(int mileage) {
		this.mileage = mileage;
	}
		
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("{zone:").append(zone)
		  .append(",rateVersion:").append(rateVersion)
		  .append(",importSection:").append(importSection)
		  .append(",importStation:").append(importStation)
		  .append(",exportSection:").append(exportSection)
		  .append(",exportStation:").append(exportStation)
		  .append(",identifyingStation:").append(identifyingStation)
		  .append(",carType:").append(carType)
		  .append(",figure:").append(figure)
		  .append(",mileage:").append(mileage)
		  .append("}");
		return sb.toString();
	}
	
}
