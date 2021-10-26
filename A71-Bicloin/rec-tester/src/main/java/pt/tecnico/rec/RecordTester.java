package pt.tecnico.rec;

import pt.tecnico.rec.grpc.*;
import pt.ulisboa.tecnico.sdis.zk.*;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;


public class RecordTester {
	
	public static void main(String[] args) throws ZKNamingException {
		System.out.println(RecordTester.class.getSimpleName());
		
		// receive and print arguments
		System.out.printf("Received %d arguments%n", args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.printf("arg[%d] = %s%n", i, args[i]);
		}
		
		final String host = args[0];
		final String port = args[1];
		final String path = args[2];
		
		RecFrontend2 frontend = new RecFrontend2();
		
		ZKNaming zkNaming = new ZKNaming(host, port);
		Collection<ZKRecord> records = zkNaming.listRecords("/grpc/bicloin/rec");
		for (ZKRecord record: records) {
				String target = record.getURI();
				frontend.setConnections(target);
				CtrlPingRequest request = CtrlPingRequest.newBuilder().setInput("friend").build();
				frontend.ping(request);
				frontend.close();
		}
		
		System.out.println("Shutting down");
		
		
		return;
							
	}
	
}
