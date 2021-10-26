package pt.tecnico.bicloin.hub;

import io.grpc.Context;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.HashMap;
import java.util.List;
import static io.grpc.Status.Code.DEADLINE_EXCEEDED;
import static io.grpc.Status.Code.UNAVAILABLE;
import java.io.*;  
import java.nio.charset.StandardCharsets;
import pt.ulisboa.tecnico.sdis.zk.*;
import pt.tecnico.rec.grpc.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.concurrent.TimeUnit ;
import pt.ulisboa.tecnico.sdis.zk.*;
import pt.tecnico.bicloin.hub.grpc.*;
import pt.tecnico.bicloin.hub.grpc.Hub.*;
import io.grpc.StatusRuntimeException;

public class HubFrontend {

	Collection<ZKRecord> records;
	int recNumber, readQuoron, writeQuoron;
	String zooHost, zooPort;
	ZKNaming zkNaming;
	
	int readCounter = 0, writeCounter = 0;
	
	HubFrontend(Collection<ZKRecord> _records, String _zooHost, String _zooPort) {
		records = _records;
		recNumber = records.size();
		readQuoron = (recNumber / 3) + 1;
		writeQuoron = (recNumber - readQuoron) + 1;
		zooHost = _zooHost;
		zooPort = _zooPort;
		zkNaming = new ZKNaming(zooHost, zooPort);
	} 
	
	/* Função para inicializar os registos nos recs */
	public void initializingWrites(String register, String data){
		
		ManagedChannel channel;
		RecordServiceGrpc.RecordServiceBlockingStub initializingStub;
		
		for (ZKRecord record: records) {
			String target = record.getURI();
			channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
			initializingStub = RecordServiceGrpc.newBlockingStub(channel);
			try {
				WriteRequest request = WriteRequest.newBuilder().setRegister(register).setValue(data).setSequenceNumber("0").build();
				String confirmation = initializingStub.write(request).getConfirmation();
			}
			catch (StatusRuntimeException e) {
				System.out.println("Error while trying to initialize rec: " + e.getMessage());
				System.out.println("Could not initialize register: " + register + ", with data: " + data);
				continue;
			}
			channel.shutdown();
		 }
	}
	
	/* Função de escrita genérica no rec */
	public String read(String register) {
		
		try {
			records = zkNaming.listRecords("/grpc/bicloin/rec");
		}
		catch (ZKNamingException e) {
			System.out.println("Could not connect to Zookeeper!");
			return "ERROR";
		}
		
		ArrayList<ManagedChannel> channels = new ArrayList<ManagedChannel>();
	
		ReadObserver<ReadResponse> readObserver = new ReadObserver<ReadResponse>(readQuoron);
		
		Context ctx = Context.current().fork();
		
		System.out.println("//--------------- READING REGISTER: " + register + " ---------------\\");
		System.out.println("Read number: " + ++readCounter);
		
		synchronized(readObserver) {
		
		ctx.run(() -> {

		for (ZKRecord record: records) {
			String target = record.getURI();
			System.out.println("Reading from: " + target);
			ManagedChannel channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
			RecordServiceGrpc.RecordServiceStub stub = RecordServiceGrpc.newStub(channel);
			channels.add(channel);
			
			ReadRequest request = ReadRequest.newBuilder().setRegister(register).build();
			stub.read(request, readObserver);
		 }
		
		});
		
		while (readObserver.rc.counter < readQuoron) {try {readObserver.wait(100);} catch (InterruptedException e) {continue;}}
		
		}
		
		ReadResponse rtrn = readObserver.getMaxValue(readQuoron);
		String response = rtrn.getValue();
		String sequenceNumber = rtrn.getSequenceNumber();
		
		System.out.println("Final read value: " + response + ", with sequence number: " + sequenceNumber);
		
		readObserver.clean();
		clearChannels(channels);
		
		System.out.println("//------------------------- END OF READ -------------------------\\");
		System.out.println("");
		
		return response;
	}
	
	/* Função de leitura genérica no rec */
	public String write(String register, String data) {
		
		try {
			records = zkNaming.listRecords("/grpc/bicloin/rec");
		}
		catch (ZKNamingException e) {
			System.out.println("Could not connect to Zookeeper!");
			return "ERROR";
		}
		
		ArrayList<ManagedChannel> channels = new ArrayList<ManagedChannel>();
		
		WriteObserver<WriteResponse> writeObserver = new WriteObserver<WriteResponse>(writeQuoron);
		ReadObserver<ReadResponse> readTagObserver = new ReadObserver<ReadResponse>(readQuoron);
		
		System.out.println("//--------------- WRITING REGISTER: " + register + " ---------------\\");
		System.out.println("Write number: " + ++writeCounter + ", Data to write: " + data);
		
		Context ctx = Context.current().fork();
		
		synchronized(readTagObserver) {
		
		ctx.run(() -> {
		
		for (ZKRecord record: records) {
			String target = record.getURI();
			ManagedChannel channel1 = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
			RecordServiceGrpc.RecordServiceStub stub1 = RecordServiceGrpc.newStub(channel1);
			channels.add(channel1);
			
			ReadRequest request = ReadRequest.newBuilder().setRegister(register).build();
			stub1.read(request, readTagObserver);
		 }
		
		});
		
		while (readTagObserver.rc.counter < readQuoron) {try {readTagObserver.wait(100);} catch (InterruptedException e) {continue;}};
		
		}
		
		int sequenceNumber = Integer.parseInt(readTagObserver.getMaxValue(readQuoron).getSequenceNumber());
		
		readTagObserver.clean();
		clearChannels(channels);
		
		Context ctx1 = Context.current().fork();
		
		synchronized(writeObserver) {
		
		ctx1.run(() -> {
		
		for (ZKRecord record: records) {
			String target1 = record.getURI();
			ManagedChannel channel2 = ManagedChannelBuilder.forTarget(target1).usePlaintext().build();
			RecordServiceGrpc.RecordServiceStub stub2 = RecordServiceGrpc.newStub(channel2);
			channels.add(channel2);
			
			WriteRequest request = WriteRequest.newBuilder().setRegister(register).setValue(data).setSequenceNumber(String.valueOf(sequenceNumber + 1)).build();
			stub2.write(request, writeObserver);
		 }
		
		});
		
		while (writeObserver.wc.counter < writeQuoron) {try {writeObserver.wait(100);} catch (InterruptedException e) {continue;}};
		
		}
		
		writeObserver.clean();
		clearChannels(channels);
		
		System.out.println("Written with sequence number: " + (sequenceNumber + 1));
		System.out.println("//------------------------- END OF WRITE -------------------------\\");
		System.out.println("");
		
		return "OK";
	}
	
	/*Função para fechar os canais de comunicação de cada chamada assíncrona */
	public synchronized void clearChannels(ArrayList<ManagedChannel> channels){
		for (ManagedChannel e: channels) {
			e.shutdown();
		}
		channels.clear();
	}
	

}
