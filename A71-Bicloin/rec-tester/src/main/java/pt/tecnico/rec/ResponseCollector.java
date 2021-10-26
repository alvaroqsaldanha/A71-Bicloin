package pt.tecnico.rec;

import java.util.Collection;
import java.util.List;

import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import java.util.ArrayList;


public class ResponseCollector {
	
	public ArrayList<Object> responses = new ArrayList<Object>();
	
	public volatile int counter = 0;
	
	ResponseCollector(){
		
	}
	
	public synchronized void increment() {
		counter++;
	}
	
	public synchronized void add(Object r) {
		responses.add(r);
	}
	
	public synchronized Object getMaxValue() {
		return responses.get(0);
	}
}
