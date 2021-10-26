package pt.tecnico.bicloin.hub;

import static io.grpc.Status.Code.INVALID_ARGUMENT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.tecnico.bicloin.hub.HubFrontend;
import pt.tecnico.bicloin.hub.grpc.*;
import pt.tecnico.bicloin.hub.grpc.Hub.*;
import io.grpc.StatusRuntimeException;


public class InfoStationIT extends BaseIT {
	
	@BeforeEach
	public void setUp() {
	}
	
	@Test
	public void infoStationOKTest() {
		InfoStationRequest request = InfoStationRequest.newBuilder().setStation("ista").build();
		InfoStationResponse response = frontend.infoStation(request);
		assertEquals("IST Alameda, lat 38.7369, -9.1366long, 20 docas, 3 BIC prémio, 19 bicicletas,0 levantamentos, 0 devoluções, https://www.google.com/maps/place/38.7369,-9.1366", response.getStatistics());
	}
	
	@Test
	public void InvalidStationTest() {
		InfoStationRequest request = InfoStationRequest.newBuilder().setStation("asdasdasd").build();
		assertEquals(INVALID_ARGUMENT,assertThrows(StatusRuntimeException.class, () -> frontend.infoStation(request)).getStatus().getCode());
	}
	
	@AfterEach
	public void clear() {
	}

}