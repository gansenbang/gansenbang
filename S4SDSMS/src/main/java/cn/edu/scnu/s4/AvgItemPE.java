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
public class AvgItemPE extends AbstractPE {
	private double sum = 0;
	private int count = 0;
	
	private Persister persister;
	private int persistTime;
	private String persistKey = "myapp:avgItem";
	
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
	
	public void processEvent(AvgItem avgItem) {
		System.out.println("Received : " + avgItem);
		sum += avgItem.getValue();
		++count;
	}

	@Override
	public void output() {
		if (count > 0) {
			System.out.println("xxxxxxx : " + sum / count);
		
			try {
	            JSONObject message = new JSONObject();
	            
	            message.put("avg", sum / count);
	            
	            persister.set(persistKey, message.toString()+"\n", persistTime);
	        } catch (Exception e) {
	            Logger.getLogger("s4").error(e);
	        }
		}
	} 
	
}
