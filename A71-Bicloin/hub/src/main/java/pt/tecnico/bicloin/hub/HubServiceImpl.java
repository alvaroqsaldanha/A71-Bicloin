package pt.tecnico.bicloin.hub;

import pt.tecnico.bicloin.hub.grpc.*;
import pt.tecnico.bicloin.hub.grpc.Hub.*;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.sdis.zk.*;
import java.io.*;  
import static io.grpc.Status.INVALID_ARGUMENT;

public class HubServiceImpl extends HubServiceGrpc.HubServiceImplBase {
	
	HubBackend backend; 
	
	HubServiceImpl(String usersFilename, String stationsFilename, Boolean initRec, String _path, String zooHost, String zooPort) throws FileNotFoundException, IOException, ZKNamingException {
		super();
		backend = new HubBackend(usersFilename,stationsFilename, initRec, _path, zooHost, zooPort);
	}
	
	// CADA MÉTODO DESTA CLASSE CORRESPONDE À IMPLEMENTAÇÃO DE UM PROCEDIMENTO REMOTO
	
	@Override
	public void ping(CtrlPingRequest request, StreamObserver<CtrlPingResponse> responseObserver) {
		String input = request.getInput();
		if (input == null || input.isBlank()) {
			responseObserver.onError(INVALID_ARGUMENT.withDescription("ERRO: Input cannot be empty!").asRuntimeException());
			return;
		}
		
		String output = "Hello " + input + "!";
		CtrlPingResponse response = CtrlPingResponse.newBuilder().setOutput(output).build();
		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}
	
	@Override
	public void balance(BalanceRequest request, StreamObserver<BalanceResponse> responseObserver) {
		String userid = request.getUsername();
		User u = backend.getUser(userid);
		if (u == null) {
			responseObserver.onError(INVALID_ARGUMENT.withDescription("ERRO: User does not exist!").asRuntimeException());
			return;
		}
		
		int balance;
		
		synchronized(u) {

		balance = Integer.parseInt(backend.read("balance_" + userid));
		
		}
		
		BalanceResponse response = BalanceResponse.newBuilder().setBalance(balance).build();
		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}
	
	@Override
	public void topUp(TopUpRequest request, StreamObserver<TopUpResponse> responseObserver) {
		String userid = request.getUsername();
		int amount = request.getAmount();
		String phonenumber = request.getPhonenumber();
		User u = backend.getUser(userid);
		if (u == null) {
			responseObserver.onError(INVALID_ARGUMENT.withDescription("ERRO: User does not exist!").asRuntimeException());
			return;
		}

		if (!u.getPhoneNumber().equals(phonenumber)) {
			responseObserver.onError(INVALID_ARGUMENT.withDescription("ERRO: Phonenumbers don't match!").asRuntimeException());
			return;
		}
		
		if ((amount > 20) || (amount < 1)) {
			responseObserver.onError(INVALID_ARGUMENT.withDescription("ERRO: Invalid amount of EUR!").asRuntimeException());
			return;
		}
		
		int balance;
		
		synchronized(u) {
			
		balance = Integer.parseInt(backend.read("balance_" + userid));
		int newBalance = balance + amount * 10;
		String confirmation = backend.write("balance_" + userid, String.valueOf(newBalance));
		balance = Integer.parseInt(backend.read("balance_" + userid));
		
		}
		
		TopUpResponse response = TopUpResponse.newBuilder().setBalance(balance).build();
		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}
	
	@Override
	public void infoStation(InfoStationRequest request, StreamObserver<InfoStationResponse> responseObserver) {
		String station = request.getStation();
		Station s = backend.getStation(station);
		if (s == null) {
			responseObserver.onError(INVALID_ARGUMENT.withDescription("ERRO: Station does not exist!").asRuntimeException());
			return;
		}
		
		String bikeNumber, levNumber, devNumber;
		
		synchronized(s) {
		
		bikeNumber = backend.read("bikenumber_" + station);
		levNumber = backend.read("stationlev_" + station);
		devNumber = backend.read("stationdev_" + station);
		
		}

		String statistics = s.getName() + ", lat " + s.getLatitude() + ", " + s.getLongitude() + "long, " + s.getDockNumber() + " docas, " + s.getAward() + " BIC prémio, "
				            + bikeNumber + " bicicletas," + levNumber + " levantamentos, " + devNumber + " devoluções, https://www.google.com/maps/place/" + s.getLatitude() + ","
				            + s.getLongitude();
		
		InfoStationResponse response = InfoStationResponse.newBuilder().setStatistics(statistics).build();
		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}
	
	@Override
	public void locateStation(LocateStationRequest request, StreamObserver<LocateStationResponse> responseObserver) {
		String latitude = request.getLatitude();
		String longitude = request.getLongitude();
		int numberOfStations = request.getNumberOfStations();
		
		if (numberOfStations < 0) {
			responseObserver.onError(INVALID_ARGUMENT.withDescription("ERRO: Invalid amount of stations!").asRuntimeException());
			return;
		}

		String stations = backend.getClosestStations(latitude,longitude,numberOfStations);
		
		LocateStationResponse response = LocateStationResponse.newBuilder().setStations(stations).build();
		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}
	
	@Override
	public void bikeUp(BikeUpRequest request, StreamObserver<BikeUpResponse> responseObserver) {
		String latitude = request.getLatitude();
		String longitude = request.getLongitude();
		String userid = request.getName();
		String station = request.getStation();
		
		Station s = backend.getStation(station);
		if (s == null) {
			responseObserver.onError(INVALID_ARGUMENT.withDescription("ERRO: Station does not exist!").asRuntimeException());
			return;
		}
		
		User u = backend.getUser(userid);
		if (u == null) {
			responseObserver.onError(INVALID_ARGUMENT.withDescription("ERRO: User does not exist!").asRuntimeException());
			return;
		}
		
		synchronized(u) {
		
		Boolean hasBike = Boolean.valueOf(backend.read("userbike_" + userid));
		if (hasBike) {
			responseObserver.onError(INVALID_ARGUMENT.withDescription("ERRO: User already has a bike!").asRuntimeException());
			return;
		}
		
		double distance = backend.haversine(latitude,longitude,s.getLatitude(),s.getLongitude());
		
		if (distance > 200) {
			responseObserver.onError(INVALID_ARGUMENT.withDescription("ERRO: User is too far from the station!").asRuntimeException());
			return;
		}
		
		int balance = Integer.parseInt(backend.read("balance_" + userid));
		if (balance < 10) {
			responseObserver.onError(INVALID_ARGUMENT.withDescription("ERRO: User doesn't have enough BIC to pay!").asRuntimeException());
			return;
		}
		
		String confirmation;
		
		synchronized(s) {
		
		int bikeNumber = Integer.parseInt(backend.read("bikenumber_" + station));
		
		if (bikeNumber == 0) {
			responseObserver.onError(INVALID_ARGUMENT.withDescription("ERRO: No bikes at this station!").asRuntimeException());
			return;
		}
		
		confirmation = backend.write("bikenumber_" + station, String.valueOf(bikeNumber - 1));
		
		int levNumber = Integer.parseInt(backend.read("stationlev_" + station));
		
		backend.write("stationlev_" + station, String.valueOf(levNumber + 1));
		
		}
		
		confirmation = backend.write("balance_" + userid, String.valueOf(balance - 10));
		
		confirmation = backend.write("userbike_" + userid, String.valueOf("true"));
		
		}
		
		BikeUpResponse response = BikeUpResponse.newBuilder().setConfirmation("OK").build();
		responseObserver.onNext(response);
		responseObserver.onCompleted();
		
	}
	
	@Override
	public void bikeDown(BikeDownRequest request, StreamObserver<BikeDownResponse> responseObserver) {
		String latitude = request.getLatitude();
		String longitude = request.getLongitude();
		String userid = request.getName();
		String station = request.getStation();
		
		Station s = backend.getStation(station);
		if (s == null) {
			responseObserver.onError(INVALID_ARGUMENT.withDescription("ERRO: Station does not exist!").asRuntimeException());
			return;
		}
		
		User u = backend.getUser(userid);
		if (u == null) {
			responseObserver.onError(INVALID_ARGUMENT.withDescription("ERRO: User does not exist!").asRuntimeException());
			return;
		}
		
		synchronized(u) {
		
		Boolean hasBike = Boolean.valueOf(backend.read("userbike_" + userid));
		if (!hasBike) {
			responseObserver.onError(INVALID_ARGUMENT.withDescription("ERRO: User does not have a bike!").asRuntimeException());
			return;
		}
		
		double distance = backend.haversine(latitude,longitude,s.getLatitude(),s.getLongitude());
		
		if (distance > 200) {
			responseObserver.onError(INVALID_ARGUMENT.withDescription("ERRO: User is too far from the station!").asRuntimeException());
			return;
		}
		
		int balance;
		String confirmation;
		
		synchronized(s) {
		
		int bikeNumber = Integer.parseInt(backend.read("bikenumber_" + station));
		
		if (bikeNumber == s.getDockNumber()) {
			responseObserver.onError(INVALID_ARGUMENT.withDescription("ERRO: This station is full!").asRuntimeException());
			return;
		}
		
		balance = Integer.parseInt(backend.read("balance_" + userid));
		
		confirmation = backend.write("bikenumber_" + station, String.valueOf(bikeNumber + 1));
		
		int devNumber = Integer.parseInt(backend.read("stationdev_" + station));
		
		backend.write("stationdev_" + station, String.valueOf(devNumber + 1));
		
		}
		
		confirmation = backend.write("balance_" + userid, String.valueOf(balance + s.getAward()));
		
		confirmation = backend.write("userbike_" + userid, String.valueOf("false"));
		
		}
		
		BikeDownResponse response = BikeDownResponse.newBuilder().setConfirmation("OK").build();
		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}
	
	@Override
	public void sysStatus(SysStatusRequest request, StreamObserver<SysStatusResponse> responseObserver) {
		String response = backend.sysStatus();
		SysStatusResponse _response = SysStatusResponse.newBuilder().setResponse(response).build();
		responseObserver.onNext(_response);
		responseObserver.onCompleted();
	} 
	
	

}
