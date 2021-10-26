package pt.tecnico.bicloin.app;

import java.util.HashMap;
import java.util.concurrent.TimeUnit ;
import pt.ulisboa.tecnico.sdis.zk.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.tecnico.bicloin.hub.grpc.*;
import pt.tecnico.bicloin.hub.grpc.Hub.CtrlPingRequest;
import pt.tecnico.bicloin.hub.grpc.Hub.*;
import io.grpc.StatusRuntimeException;
import io.grpc.*;
import java.util.Collection;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.channel.*;
import java.time.Instant;
import java.time.Duration;

public class App {
	
	public ManagedChannel channel;
	public HubServiceGrpc.HubServiceBlockingStub stub;
	public HashMap<String, String> tagsLatitude = new HashMap<String, String>();
	public HashMap<String, String> tagsLongitude = new HashMap<String, String>();
	
	private String latitude;
	private String longitude;
	private String zooHost;
	private String zooPort;
	ZKNaming zkNaming;
	
	
	// This class serves as the API for communication with hubs, used by the AppMain.
	App(String _lat, String _long, String _zooHost, String _zooPort){
		latitude = _lat;
		longitude = _long;
		zooHost = _zooHost;
		zooPort = _zooPort;
		zkNaming = new ZKNaming(zooHost,zooPort);
	}
	
	public boolean initializeConnection() throws ZKNamingException {
		String target;
		String path = "/grpc/bicloin/hub";
		Boolean isConnected = false;
		try {
			Collection<ZKRecord> records = zkNaming.listRecords(path);
			for (ZKRecord record: records) {
				target = record.getURI();
				System.out.println("Attempting to connect to Hub: " + record.getPath());
				try {					
					channel = NettyChannelBuilder.forTarget(target).usePlaintext().build();
					//channel = NettyChannelBuilder.forTarget(target).usePlaintext().withOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) TimeUnit.SECONDS.toMillis(5)).build();
					stub = HubServiceGrpc.newBlockingStub(channel);
					ping();
					isConnected = true;
					System.out.println("Connected to Hub: " + record.getPath());
					break;
				}
				catch(StatusRuntimeException e) {
					System.out.println("ERRO: Failed to connect to Hub: " + record.getPath() + " .Finding another one to connect to...");
					continue;
				}
			}
		}
		catch(ZKNamingException e) {
			System.out.println("ERRO: No hubs available with path: " + path);
		}
		return isConnected;
	}
	
	
	public void closeConnection() {
		channel.shutdownNow();
	}
	
	public void ping() {
		CtrlPingRequest request = CtrlPingRequest.newBuilder().setInput("friend").build();
		System.out.println(stub.withDeadlineAfter(5000, TimeUnit.MILLISECONDS).ping(request).getOutput());
	}
	
	public void balance(String userid) {
		BalanceRequest request = BalanceRequest.newBuilder().setUsername(userid).build();
		System.out.println(userid + " " + stub.withDeadlineAfter(5000, TimeUnit.MILLISECONDS).balance(request).getBalance() + " BIC");
	}
	
	public void topUp(String euros, String userid, String phonenumber) {
		TopUpRequest request = TopUpRequest.newBuilder().setUsername(userid).setPhonenumber(phonenumber).setAmount(Integer.parseInt(euros)).build();
		System.out.println(userid + " " + String.valueOf(stub.withDeadlineAfter(5000, TimeUnit.MILLISECONDS).topUp(request).getBalance()) + " BIC");
	}
	
	public void getLocation(String userid) {
		System.out.println(userid + " em https://www.google.com/maps/place/"  + latitude + "," + longitude);
	}
	
	public void tag(String _latitude, String _longitude, String name) {
		double testlat = Double.parseDouble(_latitude);
		double testlong = Double.parseDouble(_longitude);
		tagsLatitude.put(name,_latitude);
		tagsLongitude.put(name,_longitude);
	}
	
	public void move(String name, String userid) {
		if (!tagsLatitude.containsKey(name)) {
			System.out.println("ERRO: Tag doesn't exist!");
			return;
		}
		latitude = tagsLatitude.get(name);
		longitude = tagsLongitude.get(name);
		System.out.println(userid + " em https://www.google.com/maps/place/"  + latitude + "," + longitude);
	}
	
	public void moveCoors(String lat, String _longitude, String userid) {
		double testlat = Double.parseDouble(lat);
		double testlong = Double.parseDouble(_longitude);
		latitude = lat;
		longitude = _longitude;
		System.out.println(userid + " em https://www.google.com/maps/place/"  + latitude + "," + longitude);
	}
	
	public void scan(String numberOfStations) {
		LocateStationRequest request = LocateStationRequest.newBuilder().setLatitude(latitude).setLongitude(longitude).setNumberOfStations(Integer.parseInt(numberOfStations)).build();
		System.out.println(stub.withDeadlineAfter(5000, TimeUnit.MILLISECONDS).locateStation(request).getStations());
	}
	
	public void stationInfo(String station) {
		InfoStationRequest request = InfoStationRequest.newBuilder().setStation(station).build();
		System.out.println(stub.withDeadlineAfter(5000, TimeUnit.MILLISECONDS).infoStation(request).getStatistics());
	}
	
	public void bikeUp(String userid, String station) {
		BikeUpRequest request = BikeUpRequest.newBuilder().setStation(station).setName(userid).setLatitude(latitude).setLongitude(longitude).build();
		System.out.println(stub.withDeadlineAfter(5000, TimeUnit.MILLISECONDS).bikeUp(request).getConfirmation());
	}
	
	public void bikeDown(String userid, String station) {
		BikeDownRequest request = BikeDownRequest.newBuilder().setStation(station).setName(userid).setLatitude(latitude).setLongitude(longitude).build();
		System.out.println(stub.withDeadlineAfter(5000, TimeUnit.MILLISECONDS).bikeDown(request).getConfirmation());
	}
	
	public void sys_status() {
		SysStatusRequest request = SysStatusRequest.newBuilder().build();
		System.out.println(stub.withDeadlineAfter(100000, TimeUnit.MILLISECONDS).sysStatus(request).getResponse());
	}

   public void help() {
	   System.out.println("Available commands:");
	   System.out.println("ping - Get a sign of life from the hub you're connected to.");
	   System.out.println("balance - Check your balance in BIC.");
	   System.out.println("top-up - Charge your account with BICs (1 euro = 10 BICs)");
	   System.out.println("at - Get your location");
	   System.out.println("tag - Create a 'tag' for a location");
	   System.out.println("move - Move to a tagged location");
	   System.out.println("scan - Get nearest stations");
	   System.out.println("info xxxx - Get info about a station");
	   System.out.println("bike-up - Get a bike");
	   System.out.println("bike-down - Drop a bike");
	   System.out.println("sys_status - Get a sign of life from all hubs and recs");
   }
   
}
