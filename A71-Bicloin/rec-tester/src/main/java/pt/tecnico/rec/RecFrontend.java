package pt.tecnico.rec;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.tecnico.rec.grpc.*;

public class RecFrontend implements AutoCloseable {
	
	private final ManagedChannel channel;
	private final RecordServiceGrpc.RecordServiceBlockingStub stub;
	
	public RecFrontend(String target) {
		// Channel is the abstraction to connect to a service endpoint.
		// Let us use plaintext communication because we do not have certificates.
		this.channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();

		// Create a blocking stub.
		stub = RecordServiceGrpc.newBlockingStub(channel);
	}

	public CtrlPingResponse ping(CtrlPingRequest request) {
		return stub.ping(request);
	}
	
	public ReadResponse read(ReadRequest request) {
		return stub.read(request);
	}
	
	public WriteResponse write(WriteRequest request) {
		return stub.write(request);
	}


	@Override
	public final void close() {
		channel.shutdown();
	}

}
