package pt.tecnico.bicloin.hub;

import pt.tecnico.rec.grpc.*;
import java.util.ArrayList;

public class ReadCollector {
	
	public ArrayList<ReadResponse> responses = new ArrayList<ReadResponse>();
	
	public volatile int counter = 0;
	
	ReadCollector(){

	}
	
	public synchronized void increment() {
		counter++;
	}
	
	public synchronized void add(Object r) {
		ReadResponse resp = (pt.tecnico.rec.grpc.ReadResponse) r;
		responses.add(resp);
	}
	
	public synchronized ReadResponse getMaxValue(int quoron) {
		int maxTag = -1;
		ReadResponse maxValue = null;
		for (int i = 0; i < quoron; i++) {
			if (Integer.parseInt(responses.get(i).getSequenceNumber()) > maxTag) {
				maxTag = Integer.parseInt(responses.get(i).getSequenceNumber());
				maxValue = responses.get(i);
			}
		}
		return maxValue;
	}
	
	public void clean() {
		counter = 0;
		responses.clear();
	}

}
