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


public class PingIT extends BaseIT {
	
	@BeforeEach
	public void setUp() {
	}
	
	@Test
	public void pingOKTest() {
		CtrlPingRequest request = CtrlPingRequest.newBuilder().setInput("friend").build();
		CtrlPingResponse response = frontend.ping(request);
		assertEquals("Hello friend!", response.getOutput());
	}
	
	@Test
	public void emptyPingTest() {
		CtrlPingRequest request = CtrlPingRequest.newBuilder().setInput("").build();
		assertEquals(INVALID_ARGUMENT,assertThrows(StatusRuntimeException.class, () -> frontend.ping(request)).getStatus().getCode());
	}
	
	@AfterEach
	public void clear() {
	}

}