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



public class SysStatusIT extends BaseIT {
	
	@BeforeEach
	public void setUp() {
	}
	
	@Test
	public void sysStatusOKTest() {
		SysStatusRequest request = SysStatusRequest.newBuilder().build();
		SysStatusResponse response = frontend.sysStatus(request);
		assertFalse(response.getResponse() == "");
	}
	
	
	@AfterEach
	public void clear() {
	}

}