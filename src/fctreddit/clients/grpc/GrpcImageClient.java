package fctreddit.clients.grpc;

import fctreddit.api.java.Result;
import fctreddit.clients.java.ImageClient;
import fctreddit.impl.grpc.generated_java.ImageProtoBuf.DeleteImageArgs;
import fctreddit.impl.grpc.generated_java.ImageGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;

import java.net.URI;


public class GrpcImageClient extends ImageClient {
    private final ImageGrpc.ImageBlockingStub stub;

    public GrpcImageClient(URI grpcUri) {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress(grpcUri.getHost(), grpcUri.getPort())
                .usePlaintext()
                .build();
        this.stub = ImageGrpc.newBlockingStub(channel);
    }

    @Override
    public Result<Void> deleteImage(String userId, String imageId, String password) {
        DeleteImageArgs req = DeleteImageArgs.newBuilder()
                .setUserId(userId)
                .setImageId(imageId)
                .setPassword(password)
                .build();

        try {
            stub.deleteImage(req);
            return Result.ok();
        } catch (StatusRuntimeException e) {
            return Result.error(grpcErrorToResultCode(e.getStatus()));
        } catch (Exception e) {
            return Result.error(Result.ErrorCode.INTERNAL_ERROR);
        }
    }

    private Result.ErrorCode grpcErrorToResultCode(Status status) {
        switch (status.getCode()) {
            case NOT_FOUND: return Result.ErrorCode.NOT_FOUND;
            case PERMISSION_DENIED: return Result.ErrorCode.FORBIDDEN;
            case INVALID_ARGUMENT: return Result.ErrorCode.BAD_REQUEST;
            default: return Result.ErrorCode.INTERNAL_ERROR;
        }
    }


}
