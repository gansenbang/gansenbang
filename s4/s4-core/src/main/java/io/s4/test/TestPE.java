package io.s4.test;
import io.s4.processor.AbstractPE;
public class TestPE extends AbstractPE{
	private int eventcount;
	private long starttime = System.currentTimeMillis();
	
	public void processEvent(Object obj) {
		eventcount++;
		if(eventcount % 10000 == 0){
			long endtime = System.currentTimeMillis();
			System.out.println("The interval is:" + (endtime - starttime));
			starttime = endtime;
		}
		
    }
	@Override
	public void output() {
		// TODO Auto-generated method stub
		
	}
}
