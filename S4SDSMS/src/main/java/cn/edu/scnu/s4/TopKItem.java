/**
 * 
 */
package cn.edu.scnu.s4;

/**
 * @author ChunweiXu
 *
 */
public class TopKItem implements Comparable<TopKItem> {
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
	
	public String getTopKItem() {
        return "1";
    }

    public void setTopKItem(String id) {
        // do nothing
    }
	
	@Override
	public String toString() {
		return "{key:" + key + ",value:" + value + "}";
	}

	@Override
	public int compareTo(TopKItem topKItem) {
		// TODO Auto-generated method stub
		if (topKItem.getValue() < this.value) {
            return -1;
        } else if (topKItem.getValue() > this.value) {
            return 1;
        }
        return 0;
	}
	
}
