package pt.tecnico.bicloin.hub;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.ulisboa.tecnico.sdis.zk.*;
import pt.tecnico.bicloin.hub.grpc.*;
import pt.tecnico.bicloin.hub.grpc.Hub.*;

public class HubFrontend implements AutoCloseable {
	
	private final ManagedChannel channel;
	private final HubServiceGrpc.HubServiceBlockingStub stub;
	
	public HubFrontend(String target) {
		// Channel is the abstraction to connect to a service endpoint.
		// Let us use plaintext communication because we do not have certificates.
		this.channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();

		// Create a blocking stub.
		stub = HubServiceGrpc.newBlockingStub(channel);
	}

	public CtrlPingResponse ping(CtrlPingRequest request) {
		return stub.ping(request);
	}
	
	public BalanceResponse balance(BalanceRequest request) {
		return stub.balance(request);
	}
	
	public TopUpResponse topUp(TopUpRequest request) {
		return stub.topUp(request);
	}
	
	public InfoStationResponse infoStation(InfoStationRequest request) {
		return stub.infoStation(request);
	}
	
	public LocateStationResponse scan(LocateStationRequest request) {
		return stub.locateStation(request);
	}
	
	public BikeUpResponse bikeUp(BikeUpRequest request) {
		return stub.bikeUp(request);
	}
	
	public BikeDownResponse bikeDown(BikeDownRequest request) {
		return stub.bikeDown(request);
	}
	
	public SysStatusResponse sysStatus(SysStatusRequest request) {
		return stub.sysStatus(request);
	}


	@Override
	public final void close() {
		channel.shutdown();
	}

}
