package pt.tecnico.bicloin.hub;

import io.grpc.stub.StreamObserver;
import pt.tecnico.rec.grpc.*;
import io.grpc.StatusRuntimeException;
import static io.grpc.Status.Code.UNAVAILABLE;

public class ReadObserver<R> implements StreamObserver<R> {
	
	ReadCollector rc = new ReadCollector();
	int quo;
	
	ReadObserver(int quoron){
		quo = quoron;
	}
	
    @Override
    public void onNext(R r) {
    	synchronized(this) {
    		this.notifyAll();
    	}
    	synchronized(rc) {
    		rc.increment();
    		rc.add(r);
    	}
    }

    @Override
    public void onError(Throwable throwable) {
    	if (!((throwable.getMessage().contains("shutdown")) || (throwable.getMessage().contains("shutdown")))) {
    		StatusRuntimeException e = (StatusRuntimeException) throwable;
    		if (e.getStatus().getCode() != UNAVAILABLE)
    			System.out.println("Received error: " + throwable.getMessage());
    		else {
    			System.out.println("Requested rec server is unavailable.");
    		}
    	}
    }

    @Override
    public void onCompleted() {
      
    }
    
    public ReadResponse getMaxValue(int quoron) {
    	return rc.getMaxValue(quoron);
    }
    
    
    public void clean() {
    	rc.clean();
    }
    
}
