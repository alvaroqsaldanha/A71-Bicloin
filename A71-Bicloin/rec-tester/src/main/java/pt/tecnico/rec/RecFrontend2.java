package pt.tecnico.rec;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.tecnico.rec.grpc.*;


public class RecFrontend2 implements AutoCloseable {
	
	private ManagedChannel channel;
	private RecordServiceGrpc.RecordServiceStub stub;
	
	public RecordObserver<CtrlPingResponse> ro = new RecordObserver<CtrlPingResponse>();
	
	public RecFrontend2() {

	}
	
	public void setConnections(String target) {
		channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
		stub = RecordServiceGrpc.newStub(channel);
	}

	public void ping(CtrlPingRequest request) {
		stub.ping(request, ro);
	}

	@Override
	public final void close() {
		channel.shutdown();
	}

}
