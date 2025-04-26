package fctreddit.clients.grpc;

import fctreddit.api.User;
import fctreddit.api.java.Result;
import fctreddit.clients.java.UsersClient;
import fctreddit.impl.grpc.generated_java.UsersProtoBuf;
import fctreddit.impl.grpc.generated_java.UsersProtoBuf.GetUserArgs;
import fctreddit.impl.grpc.generated_java.UsersProtoBuf.GetUserResult;
import fctreddit.impl.grpc.generated_java.UsersGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;

import java.net.URI;

import io.grpc.LoadBalancerRegistry;
import io.grpc.internal.PickFirstLoadBalancerProvider;

public class GrpcUsersClient extends UsersClient {
    private final UsersGrpc.UsersBlockingStub stub;

    static {
        LoadBalancerRegistry reg = LoadBalancerRegistry.getDefaultRegistry();
        if (reg.getProvider("pick_first") == null) {
            reg.register(new PickFirstLoadBalancerProvider());
        }
    }
    public GrpcUsersClient(URI grpcUri) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(grpcUri.getHost(), grpcUri.getPort())
                .usePlaintext()
                .build();
        this.stub = UsersGrpc.newBlockingStub(channel);
    }

    @Override
    public Result<User> getUser(String userId, String password) {
        try {
            GetUserArgs request = GetUserArgs.newBuilder()
                    .setUserId(userId)
                    .setPassword(password)
                    .build();

            GetUserResult response = stub.getUser(request);

            if (response.hasUser()) {
                return Result.ok(convertGrpcUser(response.getUser()));
            } else {
                return Result.error(Result.ErrorCode.NOT_FOUND);
            }
        } catch (StatusRuntimeException e) {
            return Result.error(grpcErrorToResultCode(e.getStatus()));
        } catch (Exception e) {
            return Result.error(Result.ErrorCode.INTERNAL_ERROR);
        }
    }

    private Result.ErrorCode grpcErrorToResultCode(Status status) {
        return switch (status.getCode()) {
            case NOT_FOUND -> Result.ErrorCode.NOT_FOUND;
            case PERMISSION_DENIED -> Result.ErrorCode.FORBIDDEN;
            default -> Result.ErrorCode.INTERNAL_ERROR;
        };
    }

    private User convertGrpcUser(UsersProtoBuf.GrpcUser grpcUser) {
        return new User(
                grpcUser.getUserId(),
                grpcUser.getFullName(),
                grpcUser.getEmail(),
                grpcUser.getPassword(),
                grpcUser.getAvatarUrl()
        );
    }
}