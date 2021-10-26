package pt.tecnico.bicloin.hub;

import static io.grpc.Status.Code.INVALID_ARGUMENT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.tecnico.bicloin.hub.HubFrontend;
import pt.tecnico.bicloin.hub.grpc.*;
import pt.tecnico.bicloin.hub.grpc.Hub.*;
import io.grpc.StatusRuntimeException;


public class ScanIT extends BaseIT {
	
	@BeforeEach
	public void setUp() {
	}
	
	@Test
	public void scanOKTest() {
		LocateStationRequest request = LocateStationRequest.newBuilder().setLatitude("38.7380").setLongitude("-9.3000").setNumberOfStations(3).build();
		LocateStationResponse response = frontend.scan(request);
		assertFalse(response.getStations() == "");
	}
	
	@Test
	public void Scan0Test() {
		LocateStationRequest request = LocateStationRequest.newBuilder().setLatitude("38.7380").setLongitude("-9.3000").setNumberOfStations(0).build();
		LocateStationResponse response = frontend.scan(request);
		assertEquals("", response.getStations());
	}
	
	@Test
	public void scanMoreThanExistingTest() {
		LocateStationRequest request = LocateStationRequest.newBuilder().setLatitude("38.7380").setLongitude("-9.3000").setNumberOfStations(10).build();
		LocateStationResponse response = frontend.scan(request);
		assertFalse(response.getStations() == "");
	}
	
	@AfterEach
	public void clear() {
	}

}