package pt.tecnico.rec;

import io.grpc.stub.StreamObserver;
import pt.tecnico.rec.grpc.*;
import static io.grpc.Status.INVALID_ARGUMENT;
import java.util.HashMap;

public class RecordMainServiceImpl extends RecordServiceGrpc.RecordServiceImplBase {

	RecordBackend backend;
	
	RecordMainServiceImpl(String instance){
		backend = new RecordBackend(instance);
	}

	@Override
	public void ping(CtrlPingRequest request, StreamObserver<CtrlPingResponse> responseObserver) {
		String input = request.getInput();
		if (input == null || input.isBlank()) {
			responseObserver.onError(INVALID_ARGUMENT.withDescription("Input cannot be empty!").asRuntimeException());
			return;
		}
		String output = "Hello " + input + "!";
		CtrlPingResponse response = CtrlPingResponse.newBuilder().setOutput(output).build();
		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}
	
	@Override
	public void read(ReadRequest request, StreamObserver<ReadResponse> responseObserver) {
		String register = request.getRegister();
		
		System.out.println("//------------------------READ------------------------//");
		System.out.println("Register to read:" + register);

		if (register == null || register.isBlank()) {
			responseObserver.onError(INVALID_ARGUMENT.withDescription("Input cannot be empty!").asRuntimeException());
			return;
		}
		
		String[] data = register.split("_");
		if (data.length != 2) {
			responseObserver.onError(INVALID_ARGUMENT.withDescription("Invalid input!").asRuntimeException());
			return;
		}
		
		String result = "";
		ReadResponse response;
		
		try {
			switch (data[0]) {
			case "balance":
				result = backend.readUserBalance(register);
				break;
			case "bikenumber":
				result = backend.readBikeNumber(register);
				break;
			case "stationdev":
				result = backend.readStationDev(register);
				break;
			case "stationlev":
				result = backend.readStationLev(register);
				break;
			case "userbike":
				result = backend.readUserBike(register);
				break;
			default:
				responseObserver.onError(INVALID_ARGUMENT.withDescription("Invalid input!").asRuntimeException());
				return;
			}
			if (result.equals("null")) {
				responseObserver.onError(INVALID_ARGUMENT.withDescription("Invalid input!").asRuntimeException());
				return;
			}
			String sequenceNumber = backend.readSequenceNumber(register);
			System.out.println("read value: " + result + " with seqNumber: " + sequenceNumber);
			System.out.println("//-------------------------------------------------------//");
			System.out.println("");
			response = ReadResponse.newBuilder().setValue(result).setSequenceNumber(sequenceNumber).build();
			responseObserver.onNext(response);
			responseObserver.onCompleted();
		}
		catch(ArrayIndexOutOfBoundsException e) {
			responseObserver.onError(INVALID_ARGUMENT.withDescription("Invalid input!").asRuntimeException());
			return;
		}
		catch(NullPointerException e) {
			responseObserver.onError(INVALID_ARGUMENT.withDescription("Invalid input!").asRuntimeException());
			return;
		}
		
		return;
	}
	
	@Override
	public void write(WriteRequest request, StreamObserver<WriteResponse> responseObserver) {
		
		String register = request.getRegister();
		String value = request.getValue();
		String sequenceNumberToWrite = request.getSequenceNumber();
		
		System.out.println("//------------------------WRITE------------------------//");
		System.out.println("Register to write on:" + register);
		System.out.println("Value to write on register:" + value);
		
		if (register == null || register.isBlank() || value == null || value.isBlank() || sequenceNumberToWrite == null || sequenceNumberToWrite.isBlank()) {
			responseObserver.onError(INVALID_ARGUMENT.withDescription("Input cannot be empty!").asRuntimeException());
			return;
		}
		
		String[] data = register.split("_");
		if (data.length != 2) {
			responseObserver.onError(INVALID_ARGUMENT.withDescription("Invalid input!").asRuntimeException());
			return;
		}
		
		WriteResponse response;
		Boolean conf;
		
		try {
			
			int sequenceNumber = Integer.parseInt(backend.readSequenceNumber(register));
			int proposedSequenceNumber = Integer.parseInt(sequenceNumberToWrite);
			
			System.out.println("Proposed Sequence Number:" + proposedSequenceNumber);
			System.out.println("Actual Sequence Number:" + sequenceNumber);
			System.out.println("//-------------------------------------------------------//");
			System.out.println("");
			
			if ((proposedSequenceNumber <= sequenceNumber) && (proposedSequenceNumber != 0)) {
				response = WriteResponse.newBuilder().setConfirmation("OK").build();
				responseObserver.onNext(response);
				responseObserver.onCompleted();
				return;
			}
			
			conf = backend.writeSequenceNumber(register,proposedSequenceNumber);
			
			switch (data[0]) {
			case "balance":
				conf = backend.writeUserBalance(register, value);
				break;
			case "bikenumber":
				conf = backend.writeBikeNumber(register, value);
				break;
			case "stationdev":
				conf = backend.writeStationDev(register, value);
				break;
			case "stationlev":
				conf = backend.writeStationLev(register, value);
				break;
			case "userbike":
				if ((!value.equals("true")) && (!value.equals("false"))) {
					responseObserver.onError(INVALID_ARGUMENT.withDescription("Invalid input!").asRuntimeException());
					return;
				}
				conf = backend.writeUserBike(register, value);
				break;
			default:
				responseObserver.onError(INVALID_ARGUMENT.withDescription("NOK!").asRuntimeException());
				return;
			}
			
			response = WriteResponse.newBuilder().setConfirmation("OK").build();
			responseObserver.onNext(response);
			responseObserver.onCompleted();
		}
		catch(ArrayIndexOutOfBoundsException e) {
			responseObserver.onError(INVALID_ARGUMENT.withDescription("NOK!").asRuntimeException());
			return;
		}
		catch(NumberFormatException e) {
			responseObserver.onError(INVALID_ARGUMENT.withDescription("Invalid input!").asRuntimeException());
			return;
		}
		catch(NullPointerException e) {
			responseObserver.onError(INVALID_ARGUMENT.withDescription("Invalid input!").asRuntimeException());
			return;
		}
		
		return;
	}
	
	

}
