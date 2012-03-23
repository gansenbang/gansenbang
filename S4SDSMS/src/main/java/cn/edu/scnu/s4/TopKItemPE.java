/**
 * 
 */
package cn.edu.scnu.s4;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.apache.s4.persist.Persister;
import org.apache.s4.processor.AbstractPE;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author ChunweiXu
 *
 */
public class TopKItemPE extends AbstractPE {
	//private Map<String, Integer> topKMap = new ConcurrentHashMap<String, Integer>();
	private int entryCount = 10;
	/*
	private Persister persister;
	private int persistTime;
	private String persistKey = "myapp:topKItem";*/
	
	public int getEntryCount() {
        return entryCount;
    }

    public void setEntryCount(int entryCount) {
        this.entryCount = entryCount;
    }
    /*
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
    }*/
	
	public void processEvent(TopKItem topKItem) {
		System.out.println(topKItem);
		//topKMap.put(topKItem.getKey(), topKItem.getValue());
	}

	@Override
	public void output() {
		// TODO Auto-generated method stub
		List<TopKItem> list = new ArrayList<TopKItem>();
		/*
		for (String key : topKMap.keySet())
			list.add(new TopKItem(key, topKMap.get(key)));
		
		Collections.sort(list);
		System.out.println("testtest");
		
		try {
            JSONObject message = new JSONObject();
            JSONArray jsonTopK = new JSONArray();

            for (int i = 0; i < entryCount; i++) {
                if (i == list.size()) {
                    break;
                }
                TopKItem tki = list.get(i);
                JSONObject jsonEntry = new JSONObject();
                jsonEntry.put("key", tki.getKey());
                jsonEntry.put("value", tki.getValue());
                jsonTopK.put(jsonEntry);
            }
            message.put("topK", jsonTopK);
            System.out.println(message);
            //persister.set(persistKey, message.toString()+"\n", persistTime);
        } catch (Exception e) {
            Logger.getLogger("s4").error(e);
        }*/
	} 
	
}
