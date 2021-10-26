package pt.tecnico.rec;

import static io.grpc.Status.Code.INVALID_ARGUMENT;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.ulisboa.tecnico.sdis.zk.*;
import pt.tecnico.rec.grpc.ReadRequest;
import pt.tecnico.rec.grpc.ReadResponse;
import pt.tecnico.rec.grpc.WriteRequest;
import pt.tecnico.rec.grpc.WriteResponse;
import pt.tecnico.rec.RecFrontend;
import io.grpc.StatusRuntimeException;


public class ReadWriteIT extends BaseIT {
	
	@BeforeEach
	public void setUp() {
	}
	
	@Test
	public void readWriteOKTest() {
		WriteRequest request = WriteRequest.newBuilder().setRegister("balance_carlos").setValue("100").setSequenceNumber("0").build();
		WriteResponse response = frontend.write(request);
		ReadRequest _request = ReadRequest.newBuilder().setRegister("balance_carlos").build();
		ReadResponse _response = frontend.read(_request);
		assertEquals("OK", response.getConfirmation());
		assertEquals("100",_response.getValue());
	}
	
	@Test
	public void readFromNonExistingRegisterTest() {
		ReadRequest request = ReadRequest.newBuilder().setRegister("balance_asdasdasd").build();
		assertEquals(INVALID_ARGUMENT,assertThrows(StatusRuntimeException.class, () -> frontend.read(request)).getStatus().getCode());
	}
	
	@Test
	public void readFromNonExistingTypeTest() {
		ReadRequest request = ReadRequest.newBuilder().setRegister("asdasdasd_asdasdasd").build();
		assertEquals(INVALID_ARGUMENT,assertThrows(StatusRuntimeException.class, () -> frontend.read(request)).getStatus().getCode());
	}
	
	@Test
	public void readFromMultipleUnderscoresTest() {
		ReadRequest request = ReadRequest.newBuilder().setRegister("balance_alice_aaaa").build();
		assertEquals(INVALID_ARGUMENT,assertThrows(StatusRuntimeException.class, () -> frontend.read(request)).getStatus().getCode());
	}
	
	@Test
	public void writeInvalidInputTest() {
		WriteRequest request = WriteRequest.newBuilder().setRegister("balance_alice").setValue("x").setSequenceNumber("0").build();
		assertEquals(INVALID_ARGUMENT,assertThrows(StatusRuntimeException.class, () -> frontend.write(request)).getStatus().getCode());
	}
	
	@Test
	public void writeInvalidInputTest2() {
		WriteRequest request = WriteRequest.newBuilder().setRegister("userbike_alice").setValue("x").setSequenceNumber("0").build();
		assertEquals(INVALID_ARGUMENT,assertThrows(StatusRuntimeException.class, () -> frontend.write(request)).getStatus().getCode());
	}
	
	@Test
	public void writeToNonExistingTypeTest() {
		WriteRequest request = WriteRequest.newBuilder().setRegister("asdasd").setValue("sdaaa").setSequenceNumber("0").build();
		assertEquals(INVALID_ARGUMENT,assertThrows(StatusRuntimeException.class, () -> frontend.write(request)).getStatus().getCode());
	}
	
	@AfterEach
	public void clear() {
	}

}