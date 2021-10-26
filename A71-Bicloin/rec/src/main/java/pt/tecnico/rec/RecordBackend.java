package pt.tecnico.rec;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

public class RecordBackend {
	
	String recInstance;
	
	HashMap<String, Integer> userBalances = new HashMap<String, Integer>();
	HashMap<String, Integer> stationDev = new HashMap<String, Integer>();
	HashMap<String, Integer> availableBikes = new HashMap<String, Integer>();
	HashMap<String, Integer> stationLev = new HashMap<String, Integer>();
	HashMap<String, Boolean> userHasBike = new HashMap<String, Boolean>();
	HashMap<String, Integer> sequenceNumbers = new HashMap<String, Integer>();
	
	String userBalancesFile;
	String stationDevFile;
	String stationLevFile;
	String availableBikesFile;
	String userHasBikeFile;
	String sequenceNumbersFile;
	
	RecordBackend(String instance){
		recInstance = instance;
		userBalancesFile = "userBalancesFile" + recInstance;
		stationDevFile = "stationDevFile" + recInstance;
		stationLevFile = "stationLevFile" + recInstance;
		availableBikesFile = "availableBikesFile" + recInstance;
		userHasBikeFile = "userHasBikeFile" + recInstance;
		sequenceNumbersFile = "sequenceNumbersFile" + recInstance;
		getHashMaps();
		if (userBalances == null) userBalances = new HashMap<String, Integer>();
		if (stationDev == null) stationDev = new HashMap<String, Integer>();
		if (stationLev == null) stationLev = new HashMap<String, Integer>();
		if (availableBikes == null) availableBikes = new HashMap<String, Integer>();
		if (userHasBike == null) userHasBike = new HashMap<String, Boolean>();
		if (sequenceNumbers == null) sequenceNumbers = new HashMap<String, Integer>();
	}
	
	//------------------------------------WRITES---------------------------------------------//
	
	public synchronized Boolean writeUserBalance(String register, String data) {
		userBalances.put(register,Integer.parseInt(data));
		serializeObject(userBalancesFile,userBalances);
		return true;
	}
	
	public synchronized Boolean writeBikeNumber(String register, String data) {
		availableBikes.put(register,Integer.parseInt(data));
		serializeObject(availableBikesFile,availableBikes);
		return true;
	}
	
	public synchronized Boolean writeStationDev(String register, String data) {
		stationDev.put(register,Integer.parseInt(data));
		serializeObject(stationDevFile,stationDev);
		return true;
	}
	
	public synchronized Boolean writeStationLev(String register, String data) {
		stationLev.put(register,Integer.parseInt(data));
		serializeObject(stationLevFile,stationLev);
		return true;
	}
	
	public synchronized Boolean writeUserBike(String register, String data) {
		userHasBike.put(register,Boolean.valueOf(data));
		serializeObject(userHasBikeFile,userHasBike);
		return true;
	}
	
	public synchronized Boolean writeSequenceNumber(String register, int data) {
		sequenceNumbers.put(register,data);
		serializeObject(sequenceNumbersFile,sequenceNumbers);
		return true;
	}
	
	//------------------------------------READS---------------------------------------------//
	
	public synchronized String readUserBalance(String register) {
		return String.valueOf(userBalances.get(register));
	}
	
	public synchronized String readBikeNumber(String register) {
		return String.valueOf(availableBikes.get(register));
	}
	
	public synchronized String readStationDev(String register) {
		return String.valueOf(stationDev.get(register));
	}
	
	public synchronized String readStationLev(String register) {
		return String.valueOf(stationLev.get(register));
	}
	
	public synchronized String readUserBike(String register) {
		if (userHasBike.get(register))
			return "true";
		return "false";
	}
	
	public synchronized String readSequenceNumber(String register) {
		if (sequenceNumbers.get(register) != null)
			return String.valueOf(sequenceNumbers.get(register));
		else
			return "0";
	}
	
	//-----------------------------------SERIALIZABLE DATA----------------------------------------------//
	
	public void getHashMaps() {
		userBalances = (HashMap<String,Integer>)getSerializedObject(userBalancesFile);
		stationDev = (HashMap<String,Integer>)getSerializedObject(stationDevFile);
		stationLev = (HashMap<String,Integer>)getSerializedObject(stationLevFile);
		availableBikes = (HashMap<String,Integer>)getSerializedObject(availableBikesFile);
		userHasBike = (HashMap<String,Boolean>)getSerializedObject(userHasBikeFile);
		sequenceNumbers = (HashMap<String,Integer>)getSerializedObject(sequenceNumbersFile);
	}
	
	public HashMap<String, ?> getSerializedObject(String filename){
        try {
            FileInputStream fis =  new FileInputStream(filename);
            ObjectInputStream is = new ObjectInputStream(fis);
            HashMap<String, ?> arm = (HashMap<String, ?>) is.readObject();
            is.close();
            fis.close();
            return arm;         
        }
        catch (FileNotFoundException ex) {
            return null;
        }
        catch (IOException ex) {
            ex.printStackTrace();
            return null;
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
            return null;
        }

	}
	
	public void serializeObject(String filename, HashMap<String,?> arm) {
        try {
            FileOutputStream fos =  new FileOutputStream(filename);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(arm);
            os.close();
            fos.close();      
        }
        catch (FileNotFoundException ex) {
        	ex.printStackTrace();
        }
        catch (IOException ex) {
            ex.printStackTrace();
        } 
	}

}
