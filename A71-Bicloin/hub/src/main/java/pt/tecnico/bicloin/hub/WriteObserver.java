package pt.tecnico.bicloin.hub;

import io.grpc.stub.StreamObserver;
import io.grpc.StatusRuntimeException;
import static io.grpc.Status.Code.UNAVAILABLE;

public class WriteObserver<R> implements StreamObserver<R> {
	
	WriteCollector wc = new WriteCollector();
	int quo;
	
	WriteObserver(int quoron){
		quo = quoron;
	}
	
    @Override
    public void onNext(R r) {
    	synchronized(this) {
    		this.notifyAll();
    	}
    	synchronized(wc) {
    		wc.increment();
    		wc.add(r);
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
       // System.out.println("Write Request completed");
    }
    
    public void clean() {
    	wc.clean();
    }
}
