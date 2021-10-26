package pt.tecnico.bicloin.hub;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.HashMap;
import java.util.List;
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
import static io.grpc.Status.Code.DEADLINE_EXCEEDED;
import static io.grpc.Status.Code.UNAVAILABLE;

public class HubBackend {
	
	HashMap<String, User> users = new HashMap<String, User>();
	HashMap<String, Station> stations = new HashMap<String, Station>();
	
	String path;
	String zooHost;
	String zooPort;
	
	Collection<ZKRecord> records;
	
	HubFrontend frontend;
	
	/* Inicialização das ligações hub - rec, leitura dos ficheiros de input */
	
	HubBackend(String usersFilename, String stationsFilename, Boolean initRec, String _path, String _zooHost, String _zooPort) throws FileNotFoundException, IOException, ZKNamingException{
		
		path = _path; zooHost = _zooHost; zooPort = _zooPort;
		initializeConnection();

		try {
			User userTemp;
			Station stationTemp;
			String row;
			BufferedReader sc = new BufferedReader(new InputStreamReader(new FileInputStream(usersFilename),"UTF8"));
			while ((row = sc.readLine()) != null)  //returns a boolean value  
			{  
				String[] data = row.split(",");
				if (data.length != 3) {
					System.out.println("ERRO: Invalid format for user information");
					continue;
				}
				if ((data[0].length() < 3) || (data[0].length() > 10) || (data[1].length() > 30)) {
					System.out.println("ERRO: Invalid user information for user: " + data[0] + " , " + data[1]);
					continue;
				}
				userTemp = new User(data[0],data[1],data[2]);
				if (initRec) initUser(data[0]);
				users.put(data[0],userTemp);
			}   
			sc.close();
			
			sc = new BufferedReader(new FileReader(stationsFilename));
			while ((row = sc.readLine()) != null)  //returns a boolean value  
			{  
				String[] data = row.split(",");
				if (data.length != 7) {
					System.out.println("ERRO: Invalid format for station information");
					continue;
				}
				if ((data[1].length() != 4) || (Integer.parseInt(data[4]) < 0) || (Integer.parseInt(data[6]) < 0) || (Integer.parseInt(data[5]) < 0) || (Integer.parseInt(data[4]) < Integer.parseInt(data[5]))) {
					System.out.println("ERRO: Invalid station information for station: " + data[1]);
					continue;
				}
				stationTemp = new Station(data[0],data[1],data[2], data[3],Integer.parseInt(data[4]),Integer.parseInt(data[6]));
				if (initRec) initStation(data[1], data[5]);
				stations.put(data[1],stationTemp);
			}   
			sc.close();
			
		} catch(FileNotFoundException e) {
			System.out.println("One of the files does not exist: " + usersFilename + ", " + stationsFilename);
			System.exit(0);
		}
		
	}
	
	/* Inicialização da ligação com o rec */
	public void initializeConnection() throws ZKNamingException{
		 ZKNaming zkNaming = new ZKNaming(zooHost, zooPort);
		 records = zkNaming.listRecords("/grpc/bicloin/rec");
		 frontend =  new HubFrontend(records, zooHost, zooPort);
	}

	/* Inicialização dos utilizadores no rec através de mensagens com a informação destes */
	public void initUser(String userid) {
		frontend.initializingWrites("balance_" + userid,"0");
		frontend.initializingWrites("userbike_" + userid,"false");
	}
	
	/* Inicialização das estações no rec através de mensagens com a informação destas */
	public void initStation(String station, String bikeNumber) {
		frontend.initializingWrites("bikenumber_" + station,bikeNumber);
		frontend.initializingWrites("stationdev_" + station,"0");
		frontend.initializingWrites("stationlev_" + station,"0");
	}
	
	/* Função de escrita genérica no rec */
	public String read(String register) {
		return frontend.read(register);
	}
	
	/* Função de leitura genérica no rec */
	public String write(String register, String data) {
		return frontend.write(register, data);
	}
	
	
	public synchronized User getUser(String userid) {
		return users.get(userid);
	}
	
	public synchronized Station getStation(String station) {
		return stations.get(station);
	}
	
	
	/* Função para obter as k estações que estão mais perto dado uma latitude e uma longitude. Usada pelo comando scan na app */
	public synchronized String getClosestStations(String latitude, String longitude,int k) {
		
		List<Station> stationList = new ArrayList<Station>(stations.values());
		
		HashMap<String, Double> distances = new HashMap<String, Double>();
		
		for (Station x: stationList) {
			double distance = haversine(latitude,longitude,x.getLatitude(),x.getLongitude());
			distances.put(x.getAbv(), distance);
		}
		
	    List<Double> mapValues = new ArrayList<>(distances.values());
	    Collections.sort(mapValues);
	    
	    List<Double> subarray;
	    
	    if (k >= stationList.size())
	    	subarray = mapValues;
	    else
	    	subarray = mapValues.subList(0,k);
	    
	    List<String> _stations = new ArrayList<String>();
	    
	    for (double x: subarray) {
	    	for (String y: distances.keySet()) {
	    		if ((distances.get(y) == x) && (!_stations.contains(y))) {
	    			_stations.add(y);
	    		}
	    	}
	    }
	    
	    Station s1;
	    String rtrn = "";
	    String bikeNumber;
	    
	   for (String x : _stations) {
		   
		   s1 = getStation(x);
		   
		   synchronized(s1) {
		   
		   bikeNumber = read("bikenumber_" + x);
		   
		   }
		   
		   rtrn += x + ", lat " + s1.getLatitude() + ", " + s1.getLongitude() + "long, " + s1.getDockNumber() + " docas, " + s1.getAward() + " BIC prémio, "
		            + bikeNumber + " bicicletas, a " + String.valueOf(Math.round(distances.get(x))) + " metros\n"; 
	   }
	   return rtrn;
	}
	
	/* Função para calcular distância entre dois pontos dadas as suas latitudes e longitudes, utilizando a fórmula de haversine */
	public double haversine(String _pLatitude, String _pLongitude, String _sLatitude, String _sLongitude) {
		double pLatitude = Double.parseDouble(_pLatitude); double pLongitude = Double.parseDouble(_pLongitude);
		double sLatitude = Double.parseDouble(_sLatitude); double sLongitude = Double.parseDouble(_sLongitude);
		int earthRadius = 6371000;
		
		double a1 = (pLatitude - sLatitude) * (Math.PI/180);
		double a2 = (pLongitude - sLongitude) * (Math.PI/180);
		
		pLatitude = pLatitude * (Math.PI/180);
		sLatitude = sLatitude * (Math.PI/180);
		
		double a = Math.sin(a1 / 2) * Math.sin(a1 / 2) + Math.cos(pLatitude) * Math.cos(sLatitude) *Math.sin(a2 /2) * Math.sin(a2/2);
		double b = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
		double distance = earthRadius * b;
		
		return distance;	
	}
	
	/* Função que procura todos os recs e hubs através do zookeeper. Contacta-os e devolve à app o estado (up ou down) deles, assim como o respetivo nome*/
	public String sysStatus(){
		
		ManagedChannel channel2;
		RecordServiceGrpc.RecordServiceBlockingStub stub2;
		String rtrn = "Hub at " + path + ": up, \n";
		
		try {
			ZKNaming zkNaming = new ZKNaming(zooHost, zooPort);
			Collection<ZKRecord> records = zkNaming.listRecords("/grpc/bicloin/rec");
			for (ZKRecord record: records) {
				String target = record.getURI();
				channel2 = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
				stub2 = RecordServiceGrpc.newBlockingStub(channel2);
				try {
					pt.tecnico.rec.grpc.CtrlPingRequest request = pt.tecnico.rec.grpc.CtrlPingRequest.newBuilder().setInput("friend").build();
					System.out.println(stub2.withDeadlineAfter(5000, TimeUnit.MILLISECONDS).ping(request).getOutput());
				}
				catch (Exception e) {
					channel2.shutdown();
					rtrn += record.getPath() + ": down,\n";
					System.out.println(e.getMessage());
					continue;
	
				}
				channel2.shutdown();
				rtrn += record.getPath() + ": up,\n";
			}
		}
		catch (ZKNamingException e) {
			System.out.println("Could not connect to Zookeeper");
			return "SysStatus Unavailable";
		}
		return rtrn;
	}

}
