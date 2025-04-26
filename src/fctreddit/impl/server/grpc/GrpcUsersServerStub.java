package fctreddit.impl.server.grpc;

import java.util.List;

import io.grpc.BindableService;
import io.grpc.ServerServiceDefinition;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import fctreddit.api.User;
import fctreddit.api.java.Result;
import fctreddit.api.java.Users;
import fctreddit.impl.grpc.util.DataModelAdaptor;
import fctreddit.impl.grpc.generated_java.UsersGrpc;
import fctreddit.impl.grpc.generated_java.UsersProtoBuf.CreateUserArgs;
import fctreddit.impl.grpc.generated_java.UsersProtoBuf.CreateUserResult;
import fctreddit.impl.grpc.generated_java.UsersProtoBuf.DeleteUserArgs;
import fctreddit.impl.grpc.generated_java.UsersProtoBuf.DeleteUserResult;
import fctreddit.impl.grpc.generated_java.UsersProtoBuf.GetUserArgs;
import fctreddit.impl.grpc.generated_java.UsersProtoBuf.GetUserResult;
import fctreddit.impl.grpc.generated_java.UsersProtoBuf.GrpcUser;
import fctreddit.impl.grpc.generated_java.UsersProtoBuf.SearchUserArgs;
import fctreddit.impl.grpc.generated_java.UsersProtoBuf.UpdateUserArgs;
import fctreddit.impl.grpc.generated_java.UsersProtoBuf.UpdateUserResult;
import fctreddit.impl.server.java.JavaUsers;


public class GrpcUsersServerStub implements UsersGrpc.AsyncService, BindableService{
	private final Users impl = new JavaUsers();

	@Override
	public final ServerServiceDefinition bindService() {
	      return UsersGrpc.bindService(this);
	 }

	@Override
	public void createUser(CreateUserArgs request, StreamObserver<CreateUserResult> responseObserver) {
		try {
			User user = DataModelAdaptor.GrpcUser_to_User(request.getUser());
			Result<String> res = impl.createUser(user);
			if (!res.isOK())
				responseObserver.onError(errorCodeToStatus(res.error()));
			else
				responseObserver.onNext(CreateUserResult.newBuilder().setUserId(res.value()).build());
			responseObserver.onCompleted();
		} catch (Exception e) {
			responseObserver.onError(errorCodeToStatus(Result.ErrorCode.INTERNAL_ERROR));
		}
	}


	@Override
	public void getUser(GetUserArgs request, StreamObserver<GetUserResult> responseObserver) {
		try {
			Result<User> result = impl.getUser(request.getUserId(), request.getPassword());

			if (!result.isOK()) {
				responseObserver.onError(errorCodeToStatus(result.error()));
			} else {
				GrpcUser grpcUser = DataModelAdaptor.User_to_GrpcUser(result.value());
				responseObserver.onNext(GetUserResult.newBuilder().setUser(grpcUser).build());
			}
			responseObserver.onCompleted();
		} catch (Exception e) {
			responseObserver.onError(Status.INTERNAL.asRuntimeException());
		}
	}

	@Override
	public void updateUser(UpdateUserArgs request, StreamObserver<UpdateUserResult> responseObserver) {
		try {
			Result<User> res = impl.updateUser(request.getUserId(), request.getPassword(),
					DataModelAdaptor.GrpcUser_to_User(request.getUser()));
			if (!res.isOK())
				responseObserver.onError(errorCodeToStatus(res.error()));
			else
				responseObserver.onNext(UpdateUserResult.newBuilder()
					.setUser(DataModelAdaptor.User_to_GrpcUser(res.value()))
					.build());
			responseObserver.onCompleted();
		} catch (Exception e) {
			responseObserver.onError(errorCodeToStatus(Result.ErrorCode.INTERNAL_ERROR));
		}
	}

	@Override
	public void deleteUser(DeleteUserArgs request, StreamObserver<DeleteUserResult> responseObserver) {
		try {
			Result<User> res = impl.deleteUser(request.getUserId(), request.getPassword());
			if (!res.isOK()) {
				responseObserver.onError(errorCodeToStatus(res.error()));
			} else {
				GrpcUser grpcUser = DataModelAdaptor.User_to_GrpcUser(res.value());
				responseObserver.onNext(DeleteUserResult.newBuilder().setUser(grpcUser).build());
			}
			responseObserver.onCompleted();
		} catch (Exception e) {
			responseObserver.onError(Status.INTERNAL.withCause(e).asRuntimeException());
		}
	}
	@Override
	public void searchUsers(SearchUserArgs request, StreamObserver<GrpcUser> responseObserver) {
		try {
			Result<List<User>> res = impl.searchUsers(request.getPattern());
			if (!res.isOK())
				responseObserver.onError(errorCodeToStatus(res.error()));
			else
				for (User u : res.value()) {
					responseObserver.onNext(DataModelAdaptor.User_to_GrpcUser(u));
				}
			responseObserver.onCompleted();
		} catch (Exception e) {
			responseObserver.onError(errorCodeToStatus(Result.ErrorCode.INTERNAL_ERROR));
		}
	}
    
    public static Throwable errorCodeToStatus(Result.ErrorCode error) {
    	var status =  switch( error) {
    	case NOT_FOUND -> io.grpc.Status.NOT_FOUND; 
    	case CONFLICT -> io.grpc.Status.ALREADY_EXISTS;
    	case FORBIDDEN -> io.grpc.Status.PERMISSION_DENIED;
    	case NOT_IMPLEMENTED -> io.grpc.Status.UNIMPLEMENTED;
    	case BAD_REQUEST -> io.grpc.Status.INVALID_ARGUMENT;
    	default -> io.grpc.Status.INTERNAL;
    	};
    	
    	return status.asException();
    }
}
