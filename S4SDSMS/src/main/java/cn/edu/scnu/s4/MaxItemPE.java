/**
 * 
 */
package cn.edu.scnu.s4;

import org.apache.log4j.Logger;
import org.apache.s4.persist.Persister;
import org.apache.s4.processor.AbstractPE;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author ChunweiXu
 *
 */
public class MaxItemPE extends AbstractPE {
	MaxItem totalMaxItem = null;
	
	private Persister persister;
	private int persistTime;
	private String persistKey = "myapp:maxItem";
	
	public Persister getPersister() {
        return persister;
    }

    public void setPersister(Persister persister) {
        this.persister = persister;
    }
    
    public int getPersistTime() {
        return persistTime;
    }

    public void setPersistTime(int persistTime) {
        this.persistTime = persistTime;
    }
    
    public String getPersistKey() {
        return persistKey;
    }

    public void setPersistKey(String persistKey) {
        this.persistKey = persistKey;
    }
	
	public void processEvent(MaxItem maxItem) {
		//System.out.println("Received : " + maxItem);
		if (totalMaxItem == null || maxItem.getValue() > totalMaxItem.getValue())
			totalMaxItem = maxItem;
	}

	@Override
	public void output() {
		//System.out.println("xxxxxxx : " + totalMaxItem);
		
		try {
            JSONObject message = new JSONObject();
            
            message.put("key", totalMaxItem.getKey());
            message.put("value", totalMaxItem.getValue());
            
            persister.set(persistKey, message.toString()+"\n", persistTime);
        } catch (Exception e) {
            Logger.getLogger("s4").error(e);
        }
	}
	
}
