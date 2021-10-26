package pt.tecnico.bicloin.hub;

import java.io.File;
import java.io.IOException;
import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import pt.ulisboa.tecnico.sdis.zk.*;

public class HubMain {
	public static void main(String[] args) throws IOException, InterruptedException, ZKNamingException {
		System.out.println(HubMain.class.getSimpleName());
		
		// receive and print arguments
		System.out.printf("Received %d arguments%n", args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.printf("arg[%d] = %s%n", i, args[i]);
		}
		
		// check arguments
		if (args.length < 7) {
			System.err.println("ERRO: Argument(s) missing!");
			return;
		}
		

		final String zooHost = args[0];
		final String zooPort = args[1];
		final String host = args[2];
		final int port = Integer.parseInt(args[3]);
		final String path = args[4];
		String userFile = args[5];
		String stationFile = args[6];
		Boolean initRec = false;
		if (args.length == 8) initRec = true;

		/* Registo do host e do port no zookeeper.
		 * Encontrar o host e o port do Rec através do Zookeeper.
		 */
		
		ZKNaming zkNaming = null;
		try {
			zkNaming = new ZKNaming(zooHost, zooPort);
			zkNaming.rebind(path, host, String.valueOf(port));
			
			userFile = ".." + File.separator + "demo" + File.separator + userFile;
			stationFile = ".." + File.separator + "demo" + File.separator + stationFile;

			final BindableService impl = new HubServiceImpl(userFile,stationFile, initRec,path,zooHost,zooPort);

			// Create a new server to listen on port
			Server server = ServerBuilder.forPort(port).addService(impl).build();
			

			// Start the server
			server.start();

			// Server threads are running in the background.
			System.out.println("Server started");

			// Do not exit the main thread. Wait until server is terminated.
			server.awaitTermination();
			
		}
		catch(ZKNamingException e){
			System.out.println("Something went wrong with Zookeeper! :(");
			System.exit(0);
		}
		finally  {

		    if (zkNaming != null) {
		        zkNaming.unbind(path,host,String.valueOf(port));
		    }
		}
	}

	
}
