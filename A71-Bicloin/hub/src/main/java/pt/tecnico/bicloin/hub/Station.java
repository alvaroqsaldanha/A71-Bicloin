package pt.tecnico.bicloin.hub;

public class Station {
	
	private int dockNumber;
	
	private String name;
	
	private String abv;
	
	private String latitude;
	
	private String longitude;
	
	private int award;
	
	Station(String _name, String _abv, String _latitude, String _longitude, int _dockNumber, int _award){
		name = _name;
		abv = _abv;
		dockNumber = _dockNumber;
		latitude = _latitude;
		longitude = _longitude;
		award = _award;
	}
	
	public int getDockNumber() {
		return dockNumber;
	}
	
	public String getName(){
		return name;
	}
	
	public String getAbv() {
		return abv;
	}
	
	public String getLatitude() {
		return latitude;
	}
	
	public String getLongitude() {
		return longitude;
	}
	
	public int getAward() {
		return award;
	}
}
