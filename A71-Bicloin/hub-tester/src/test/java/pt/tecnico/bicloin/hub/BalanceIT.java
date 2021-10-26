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


public class BalanceIT extends BaseIT {
	
	@BeforeEach
	public void setUp() {
	}
	
	@Test
	public void balanceOKTest() {
		BalanceRequest request = BalanceRequest.newBuilder().setUsername("diana").build();
		BalanceResponse response = frontend.balance(request);
		assertEquals(0, response.getBalance());
	}
	
	@Test
	public void balanceFromUserThatDoesntExistTest() {
		BalanceRequest request = BalanceRequest.newBuilder().setUsername("asdasdasdasdsd").build();
		assertEquals(INVALID_ARGUMENT,assertThrows(StatusRuntimeException.class, () -> frontend.balance(request)).getStatus().getCode());
	}
	
	@AfterEach
	public void clear() {
	}

}