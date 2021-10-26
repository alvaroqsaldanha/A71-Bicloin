package pt.tecnico.bicloin.hub;

public class User {
	
	private String id;
	
	private String name;
	
	private String phoneNumber;
	
	User(String _id, String _name, String _phoneNumber){
		id = _id;
		name = _name;
		phoneNumber = _phoneNumber;
	}
	
	public String getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public String getPhoneNumber() {
		return phoneNumber;
	}
	
	public void setPhoneNumber(String a) {
		phoneNumber = a;
	}
	

}
