/**
 * 
 */
package cn.edu.scnu.s4.test;

import org.apache.s4.dispatcher.Dispatcher;
import org.apache.s4.processor.AbstractPE;

import cn.edu.scnu.s4.TopKItem;
import cn.edu.scnu.s4.MaxItem;
import cn.edu.scnu.s4.MinItem;

/**
 * @author ChunweiXu
 *
 */
public class TrafficFlowPE extends AbstractPE {
	private Dispatcher dispatcher;  
	  
    public Dispatcher getDispatcher() {
        return dispatcher;  
    }
  
    public void setDispatcher(Dispatcher dispatcher) {  
        this.dispatcher = dispatcher;
    }
	
	public void processEvent(TrafficFlow trafficFlow) {
		/*
		System.out.println("The traffic-flow infomation:");
		System.out.println("    zone: " + trafficFlow.getZone());
		System.out.println("    rateVersion: " + trafficFlow.getRateVersion());
		System.out.println("    importSection: " + trafficFlow.getImportSection());
		System.out.println("    importStation: " + trafficFlow.getImportStation());
		System.out.println("    exportSection: " + trafficFlow.getExportSection());
		System.out.println("    exportStation: " + trafficFlow.getExportStation());
		System.out.println("    identifyingStation: " + trafficFlow.getIdentifyingStation());
		System.out.println("    carType: " + trafficFlow.getCarType());
		System.out.println("    figure: " + trafficFlow.getFigure());
		System.out.println("    mileage: " + trafficFlow.getMileage());*/
		//System.out.println("Received " + trafficFlow.toString());
		
		String key = trafficFlow.getZone() + "-" + trafficFlow.getIdentifyingStation() + "-" + trafficFlow.getCarType();
		int value = trafficFlow.getFigure();
		
		TopKItem topKItem = new TopKItem();
		topKItem.setKey(key);
		topKItem.setValue(value);
		dispatcher.dispatchEvent("TopKItem", topKItem);
		
		MaxItem maxItem = new MaxItem();
		maxItem.setKey(key);
		maxItem.setValue(value);
		dispatcher.dispatchEvent("MaxItem", maxItem);
		
		MinItem minItem = new MinItem();
		minItem.setKey(key);
		minItem.setValue(value);
		dispatcher.dispatchEvent("MinItem", minItem);
    }
    
    @Override
    public void output() {
        
    }
    
    @Override
    public String getId() {
        return this.getClass().getName();
    }
    
}
