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


public class TopUpIT extends BaseIT {
	
	@BeforeEach
	public void setUp() {
	}
	
	@Test
	public void topUpOKTest() {
		TopUpRequest request = TopUpRequest.newBuilder().setUsername("diana").setPhonenumber("+34010203").setAmount(10).build();
		TopUpResponse response = frontend.topUp(request);
		assertEquals(100, response.getBalance());
	}
	
	@Test
	public void InvalidPhoneNumberTest() {
		TopUpRequest request = TopUpRequest.newBuilder().setUsername("alice").setPhonenumber("+3519111211").setAmount(10).build();
		assertEquals(INVALID_ARGUMENT,assertThrows(StatusRuntimeException.class, () -> frontend.topUp(request)).getStatus().getCode());
	}
	
	@Test
	public void InvalidAmountTest() {
		TopUpRequest request = TopUpRequest.newBuilder().setUsername("alice").setPhonenumber("+35191102030").setAmount(30).build();
		assertEquals(INVALID_ARGUMENT,assertThrows(StatusRuntimeException.class, () -> frontend.topUp(request)).getStatus().getCode());
	}
	
	@Test
	public void InvalidUserTest() {
		TopUpRequest request = TopUpRequest.newBuilder().setUsername("test").setPhonenumber("+35191102030").setAmount(10).build();
		assertEquals(INVALID_ARGUMENT,assertThrows(StatusRuntimeException.class, () -> frontend.topUp(request)).getStatus().getCode());
	}
	
	@AfterEach
	public void clear() {
	}

}