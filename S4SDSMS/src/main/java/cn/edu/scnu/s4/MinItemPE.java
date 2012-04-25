/**
 * 
 */
package cn.edu.scnu.s4;

import org.apache.log4j.Logger;
import io.s4.persist.Persister;
import io.s4.processor.AbstractPE;
import org.json.JSONObject;

/**
 * @author ChunweiXu
 *
 */
public class MinItemPE extends AbstractPE {
	MinItem totalMinItem = null;
	
	private Persister persister;
	private int persistTime;
	private String persistKey = "myapp:minItem";
	
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
	
	public void processEvent(MinItem minItem) {
		//System.out.println("Received : " + minItem);
		if (totalMinItem == null || minItem.getValue() < totalMinItem.getValue())
			totalMinItem = minItem;
	}

	@Override
	public void output() {
		//System.out.println("xxxxxxx : " + totalMinItem);
		
		try {
            JSONObject message = new JSONObject();
            
            message.put("key", totalMinItem.getKey());
            message.put("value", totalMinItem.getValue());
            
            persister.set(persistKey, message.toString()+"\n", persistTime);
        } catch (Exception e) {
            Logger.getLogger("s4").error(e);
        }
	}
}
