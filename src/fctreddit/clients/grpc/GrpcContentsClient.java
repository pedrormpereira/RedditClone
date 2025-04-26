package fctreddit.clients.grpc;

import fctreddit.api.java.Result;
import fctreddit.clients.java.ContentsClient;
import fctreddit.impl.grpc.generated_java.ContentGrpc;
import fctreddit.impl.grpc.generated_java.ContentProtoBuf.SetAuthorToNullArgs;
import io.grpc.*;
import io.grpc.internal.PickFirstLoadBalancerProvider;

import java.net.URI;

public class GrpcContentsClient extends ContentsClient {
    private final ContentGrpc.ContentBlockingStub blockingStub;

    static {
        LoadBalancerRegistry reg = LoadBalancerRegistry.getDefaultRegistry();
        if (reg.getProvider("pick_first") == null) {
            reg.register(new PickFirstLoadBalancerProvider());
        }
    }
    public GrpcContentsClient(URI grpcUri) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(grpcUri.getHost(), grpcUri.getPort())
                .usePlaintext()
                .build();
        this.blockingStub = ContentGrpc.newBlockingStub(channel);
    }

    @Override
    public Result<Void> setAuthorToNull(String userId, String password) {
        try {
            SetAuthorToNullArgs args = SetAuthorToNullArgs.newBuilder()
                    .setUserId(userId)
                    .setPassword(password)
                    .build();
            blockingStub.setAuthorToNull(args);
            return Result.ok();
        } catch (StatusRuntimeException e) {
            return Result.error(translateGrpcError(e.getStatus()));
        }
    }

    private Result.ErrorCode translateGrpcError(Status status) {
        return switch (status.getCode()) {
            case NOT_FOUND -> Result.ErrorCode.NOT_FOUND;
            case PERMISSION_DENIED -> Result.ErrorCode.FORBIDDEN;
            default -> Result.ErrorCode.INTERNAL_ERROR;
        };
    }
}
