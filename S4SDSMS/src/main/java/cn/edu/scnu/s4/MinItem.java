/**
 * 
 */
package cn.edu.scnu.s4;

/**
 * @author ChunweiXu
 *
 */
public class MinItem {
	private String key;
	private int value;
	
	public String getKey() {
		return key;
	}
	
	public void setKey(String key) {
		this.key = key;
	}
	
	public int getValue() {
		return value;
	}
	
	public void setValue(int value) {
		this.value = value;
	}
	
	public String getMinItem() {
        return "1";
    }

    public void setMinItem(String id) {
        // do nothing
    }
	
	@Override
	public String toString() {
		return "{key:" + key + ",value:" + value + "}";
	}
	
}
