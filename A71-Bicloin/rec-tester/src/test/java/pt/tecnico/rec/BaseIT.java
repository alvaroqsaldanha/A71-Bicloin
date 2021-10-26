package pt.tecnico.rec;

import static io.grpc.Status.Code.INVALID_ARGUMENT;
import pt.ulisboa.tecnico.sdis.zk.*;

import java.io.IOException;
import pt.tecnico.rec.grpc.CtrlPingRequest;
import pt.tecnico.rec.grpc.CtrlPingResponse;
import pt.tecnico.rec.RecFrontend;

import java.util.Properties;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import io.grpc.BindableService;

public class BaseIT {

	private static final String TEST_PROP_FILE = "/test.properties";
	protected static Properties testProps;
	
	static RecFrontend frontend;
	
	@BeforeAll
	public static void oneTimeSetup () throws IOException, ZKNamingException {
		testProps = new Properties();
		
		try {
			testProps.load(BaseIT.class.getResourceAsStream(TEST_PROP_FILE));
			System.out.println("Test properties:");
			System.out.println(testProps);
		}catch (IOException e) {
			final String msg = String.format("Could not load properties file {}", TEST_PROP_FILE);
			System.out.println(msg);
			throw e;
		}
		
		final String zooHost = testProps.getProperty("zoo.host");
        final String zooPort = testProps.getProperty("zoo.port");
        final String path =  testProps.getProperty("path");
        
        ZKNaming zkNaming = new ZKNaming(zooHost, zooPort);
		
		ZKRecord record = zkNaming.lookup(path);
		String target = record.getURI();
        
		frontend = new RecFrontend(target);
	}
	
	@AfterAll
	public static void cleanup() {
		
	}

}
