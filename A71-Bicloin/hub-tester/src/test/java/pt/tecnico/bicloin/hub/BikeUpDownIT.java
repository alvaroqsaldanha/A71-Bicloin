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


public class BikeUpDownIT extends BaseIT {
	
	@BeforeEach
	public void setUp() {
	}
	
	@Test
	public void bikeUpDownOKTest() {
		TopUpRequest __request = TopUpRequest.newBuilder().setUsername("alice").setPhonenumber("+35191102030").setAmount(10).build();
		frontend.topUp(__request);
		BikeUpRequest request = BikeUpRequest.newBuilder().setStation("istt").setName("alice").setLatitude("38.7376").setLongitude("-9.3031").build();
		BikeUpResponse response = frontend.bikeUp(request);
		BikeDownRequest _request = BikeDownRequest.newBuilder().setStation("istt").setName("alice").setLatitude("38.7376").setLongitude("-9.3031").build();
		BikeDownResponse _response = frontend.bikeDown(_request);
		assertEquals("OK", response.getConfirmation());
		assertEquals("OK", _response.getConfirmation());
	}
	
	@Test
	public void bikeUpTwiceTest() {
		TopUpRequest __request = TopUpRequest.newBuilder().setUsername("carlos").setPhonenumber("+34203040").setAmount(10).build();
		frontend.topUp(__request);
		BikeUpRequest request = BikeUpRequest.newBuilder().setStation("istt").setName("carlos").setLatitude("38.7376").setLongitude("-9.3031").build();
		BikeUpResponse response = frontend.bikeUp(request);
		assertEquals("OK", response.getConfirmation());
		assertEquals(INVALID_ARGUMENT,assertThrows(StatusRuntimeException.class, () -> frontend.bikeUp(request)).getStatus().getCode());
	}
	
	@Test
	public void BikeDownWithoutBikeTest() {
		TopUpRequest __request = TopUpRequest.newBuilder().setUsername("joao").setPhonenumber("+3518882823").setAmount(10).build();
		frontend.topUp(__request);
		BikeDownRequest request = BikeDownRequest.newBuilder().setStation("istt").setName("joao").setLatitude("38.7380").setLongitude("-9.3000").build();
		assertEquals(INVALID_ARGUMENT,assertThrows(StatusRuntimeException.class, () -> frontend.bikeDown(request)).getStatus().getCode());
	}
	
	@Test
	public void BikeUpInvalidUserTest() {
		BikeUpRequest request = BikeUpRequest.newBuilder().setStation("istt").setName("sadasd").setLatitude("38.7380").setLongitude("-9.3000").build();
		assertEquals(INVALID_ARGUMENT,assertThrows(StatusRuntimeException.class, () -> frontend.bikeUp(request)).getStatus().getCode());
	}
	
	@Test
	public void BikeUpInvalidStationTest() {
		BikeUpRequest request = BikeUpRequest.newBuilder().setStation("asdasdasdas").setName("eva").setLatitude("38.7380").setLongitude("-9.3000").build();
		assertEquals(INVALID_ARGUMENT,assertThrows(StatusRuntimeException.class, () -> frontend.bikeUp(request)).getStatus().getCode());
	}
	
	@Test
	public void BikeDownInvalidUserTest() {
		BikeDownRequest request = BikeDownRequest.newBuilder().setStation("istt").setName("sadasd").setLatitude("38.7380").setLongitude("-9.3000").build();
		assertEquals(INVALID_ARGUMENT,assertThrows(StatusRuntimeException.class, () -> frontend.bikeDown(request)).getStatus().getCode());
	}
	
	@Test
	public void BikeDownInvalidStationTest() {
		BikeDownRequest request = BikeDownRequest.newBuilder().setStation("asdasdasdas").setName("eva").setLatitude("38.7380").setLongitude("-9.3000").build();
		assertEquals(INVALID_ARGUMENT,assertThrows(StatusRuntimeException.class, () -> frontend.bikeDown(request)).getStatus().getCode());
	}
	
	@Test
	public void BikeUpNoMoneyTest() {
		BikeUpRequest request = BikeUpRequest.newBuilder().setStation("istt").setName("eva").setLatitude("38.7380").setLongitude("-9.3000").build();
		assertEquals(INVALID_ARGUMENT,assertThrows(StatusRuntimeException.class, () -> frontend.bikeUp(request)).getStatus().getCode());
	}
	
	@Test
	public void BikeUpTooFarTest() {
		BikeUpRequest request = BikeUpRequest.newBuilder().setStation("istt").setName("eva").setLatitude("0").setLongitude("0").build();
		assertEquals(INVALID_ARGUMENT,assertThrows(StatusRuntimeException.class, () -> frontend.bikeUp(request)).getStatus().getCode());
	}
	
	@Test
	public void BikeDownTooFarTest() {
		TopUpRequest __request = TopUpRequest.newBuilder().setUsername("eva").setPhonenumber("+155509080706").setAmount(10).build();
		frontend.topUp(__request);
		BikeUpRequest request = BikeUpRequest.newBuilder().setStation("istt").setName("eva").setLatitude("38.7376").setLongitude("-9.3031").build();
		BikeUpResponse response = frontend.bikeUp(request);
		BikeDownRequest _request = BikeDownRequest.newBuilder().setStation("istt").setName("eva").setLatitude("0").setLongitude("0").build();
		assertEquals("OK", response.getConfirmation());
		assertEquals(INVALID_ARGUMENT,assertThrows(StatusRuntimeException.class, () -> frontend.bikeDown(_request)).getStatus().getCode());
	}
	
	@AfterEach
	public void clear() {
	}

}