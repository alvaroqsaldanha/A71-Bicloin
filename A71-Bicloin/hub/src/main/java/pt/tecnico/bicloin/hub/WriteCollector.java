package pt.tecnico.bicloin.hub;

import pt.tecnico.rec.grpc.*;
import java.util.ArrayList;

public class WriteCollector {
	public ArrayList<WriteResponse> responses = new ArrayList<WriteResponse>();
	
	public volatile int counter = 0;
	
	WriteCollector(){
		
	}
	
	public synchronized void increment() {
		counter++;
	}
	
	public synchronized void add(Object r) {
		WriteResponse resp = (pt.tecnico.rec.grpc.WriteResponse) r;
		responses.add(resp);
	}
	
	public void clean() {
		counter = 0;
		responses.clear();
	}
}
