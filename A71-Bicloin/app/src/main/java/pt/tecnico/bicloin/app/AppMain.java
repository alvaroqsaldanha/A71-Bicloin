package pt.tecnico.bicloin.app;

import io.grpc.ManagedChannel;
import pt.ulisboa.tecnico.sdis.zk.*;
import io.grpc.ManagedChannelBuilder;
import pt.tecnico.bicloin.hub.grpc.*;
import io.grpc.StatusRuntimeException;
import pt.tecnico.bicloin.hub.grpc.Hub.CtrlPingRequest;
import java.util.Scanner;
import static io.grpc.Status.Code.INVALID_ARGUMENT;
import static io.grpc.Status.Code.DEADLINE_EXCEEDED;
import static io.grpc.Status.Code.UNAVAILABLE;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.lang.String;
import java.util.*;

public class AppMain {
	
	private static String userid;
	
	private static String phonenumber;
	
	public static void main(String[] args) {
		System.out.println(AppMain.class.getSimpleName());
		
		// receive and print arguments
		System.out.printf("Received %d arguments%n", args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.printf("arg[%d] = %s%n", i, args[i]);
		}
		
		final String zooHost = args[0];
		final String zooPort = args[1];
		userid = args[2];
		phonenumber = args[3];
		String latitude = args[4];
		String longitude = args[5];
		
		if ((userid.length() < 3) || (userid.length() > 10)) {
			System.out.println("ERRO: Invalid username");
			return;
		}
			
		
		try {
			
			App app = new App(latitude,longitude,zooHost,zooPort);
			
			Boolean isConnected = app.initializeConnection();
			if (!isConnected) {
				System.out.println("ERRO: There are no hubs to connect to. Exiting...");
				System.exit(0);
			}
			
			Scanner scanner  = new Scanner(System.in);
			
			System.out.print(">");
			String input = scanner.nextLine();
			
			while(!input.equals("stop")) {
				 
				String[] data = input.split(" ");
				
				try {
					if ((input.equals("")) || (data[0].charAt(0) == '#')) {
						System.out.print(">");
						input = scanner.nextLine();
						continue;
					}
				}
				catch(StringIndexOutOfBoundsException | ArrayIndexOutOfBoundsException e) {
					System.out.println("ERRO: Invalid Input! First char is space :(");
					System.out.print(">");
					input = scanner.nextLine();
					continue;
				}

				
				try {
			     switch(data[0]) {
			     case "ping":
			    	 app.ping();
			    	 break;
			     case "balance":
			    	 app.balance(userid);
			    	 break;
			     case "top-up":
			    	 app.topUp(data[1], userid, phonenumber);
			    	 break;	
			     case "at":
			    	 app.getLocation(userid);
			    	 break;
			     case "tag":
			    	 app.tag(data[1],data[2],data[3]);
			    	 break;
			     case "move":
			    	 if (data.length == 2) app.move(data[1],userid);
			    	 else if (data.length == 3) app.moveCoors(data[1],data[2],userid);
			    	 break;
			     case "scan":
			    	 app.scan(data[1]);
			    	 break;
			     case "info":
			    	 app.stationInfo(data[1]);
			    	 break;
			     case "bike-up":
			    	 app.bikeUp(userid, data[1]);
			    	 break;
			     case "bike-down":
			    	 app.bikeDown(userid, data[1]);
			    	 break;
			     case "help":
			    	 app.help();
			    	 break;
			     case "sys_status":
			    	 app.sys_status();
			    	 break;
			     case "zzz":
			    	 TimeUnit.MILLISECONDS.sleep(Integer.parseInt(data[1]));
			    	 break;
			     default:
			    	 System.out.println("ERRO: Invalid Input! :(");
			    	 break;
			     }
				}
				catch(ArrayIndexOutOfBoundsException e) {
					System.out.println("ERRO: Invalid Input :(, you might be forgetting something...");
				}
				catch (StatusRuntimeException e) {
					if (e.getStatus().getCode() == INVALID_ARGUMENT) {
						System.out.println("Caught exception with description: " + e.getStatus().getDescription());
					}
					else if((e.getStatus().getCode() == DEADLINE_EXCEEDED) || (e.getStatus().getCode() == UNAVAILABLE)) {
						System.out.println("ERRO: Lost connection to hub! Finding another one to connect to...");
						isConnected = app.initializeConnection();
						if (!isConnected) {
							System.out.println("ERRO: There are no hubs to connect to. Exiting...");
							app.closeConnection();
							System.exit(0);
						}
						
					}
					;;
				}
				catch (NumberFormatException e) {
					System.out.println("ERRO: Invalid Input :(, you might be forgetting something...");
				}
				catch (NullPointerException e) {
					System.out.println("ERRO: Invalid Input :(, you might be forgetting something...");
				}
			     
			    System.out.print(">");
			    try {
				    input = scanner.nextLine();
			    }
			    catch (NoSuchElementException e) {
			    	app.closeConnection();
			    	System.exit(0);
			    }

			}

			
		}
		catch(ZKNamingException e) {
			System.out.println("ERRO: Something went wrong with Zookeeper! :(");
		}
		catch(InterruptedException e) {
			System.out.println("ERRO: Something went wrong with sleep! :(");
		}
		return;	
	}

}
