package pt.tecnico.bicloin.hub;

import pt.tecnico.bicloin.hub.grpc.*;
import pt.tecnico.bicloin.hub.grpc.Hub.*;
import pt.ulisboa.tecnico.sdis.zk.*;
import io.grpc.StatusRuntimeException;

public class HubTester {
	
	public static void main(String[] args) throws ZKNamingException {
		System.out.println(HubTester.class.getSimpleName());
		
		// receive and print arguments
		System.out.printf("Received %d arguments%n", args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.printf("arg[%d] = %s%n", i, args[i]);
		}
		
		final String host = args[0];
		final String port = args[1];
		
		ZKNaming zkNaming = new ZKNaming(host,port);
		ZKRecord record = zkNaming.lookup("/grpc/bicloin/hub/1");
		String target = record.getURI();
		
		HubFrontend frontend = new HubFrontend(target);

		CtrlPingRequest request = CtrlPingRequest.newBuilder().setInput("friend").build();
		CtrlPingResponse response = frontend.ping(request);
		System.out.println(response.getOutput());
		
		frontend.close();
		
		return;
	
	}
}
