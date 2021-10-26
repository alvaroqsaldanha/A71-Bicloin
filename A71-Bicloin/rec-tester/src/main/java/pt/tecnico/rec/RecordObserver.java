package pt.tecnico.rec;

import io.grpc.stub.StreamObserver;

public class RecordObserver<R> implements StreamObserver<R> {
	
	ResponseCollector rc = new ResponseCollector();
	
    @Override
    public void onNext(R r) {
        synchronized(rc) {
            rc.increment();
            rc.add(r);
        }
    }

    @Override
    public void onError(Throwable throwable) {
        System.out.println("Received error: " + throwable);
    }

    @Override
    public void onCompleted() {
        System.out.println("Request completed");
    }
    
    public Object getMaxValue() {
    	return rc.getMaxValue();
    }
}