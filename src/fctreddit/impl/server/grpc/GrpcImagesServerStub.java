package fctreddit.impl.server.grpc;

import fctreddit.impl.grpc.generated_java.ImageGrpc;
import io.grpc.BindableService;
import io.grpc.ServerServiceDefinition;

import fctreddit.api.java.Image;
import fctreddit.api.java.Result;
import fctreddit.impl.grpc.generated_java.ImageProtoBuf.*;
import fctreddit.impl.server.java.JavaImage;
import io.grpc.stub.StreamObserver;

import java.util.logging.Logger;

import static fctreddit.impl.server.grpc.GrpcUsersServerStub.errorCodeToStatus;

public class GrpcImagesServerStub implements ImageGrpc.AsyncService, BindableService {
    private final Image impl = new JavaImage();
    private static final Logger Log = Logger.getLogger(GrpcImagesServerStub.class.getName());

    @Override
    public ServerServiceDefinition bindService() {
        return ImageGrpc.bindService(this);
    }

    @Override
    public void createImage(CreateImageArgs request, StreamObserver<CreateImageResult> responseObserver) {
        try {
            String userId = request.getUserId();
            String password = request.getPassword();
            byte[] imageData = request.getImageContents().toByteArray();
            Result<String> res = impl.createImage(userId, imageData, password);
            if (!res.isOK())
                responseObserver.onError(errorCodeToStatus(res.error()));
            else
                responseObserver.onNext(CreateImageResult.newBuilder().setImageId(res.value()).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(errorCodeToStatus(Result.ErrorCode.INTERNAL_ERROR));
        }
    }

    @Override
    public void getImage(GetImageArgs request, StreamObserver<GetImageResult> responseObserver) {
        Log.info("Received getUser request: " + request.getUserId());
        try {
            Result<byte[]> res = impl.getImage(request.getUserId(), request.getImageId());
            if (!res.isOK())
                responseObserver.onError(errorCodeToStatus(res.error()));
            else
                responseObserver.onNext(GetImageResult.newBuilder()
                    .setData(com.google.protobuf.ByteString.copyFrom(res.value()))
                    .build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(errorCodeToStatus(Result.ErrorCode.INTERNAL_ERROR));
        }
    }

    @Override
    public void deleteImage(DeleteImageArgs request, StreamObserver<DeleteImageResult> responseObserver) {
        try {
            String userId = request.getUserId();
            String imageId = request.getImageId();
            String password = request.getPassword();

            Result<Void> res = impl.deleteImage(userId, imageId, password);

            if (res.isOK()) {
                responseObserver.onNext(DeleteImageResult.newBuilder().build());
                responseObserver.onCompleted();
            } else {
                responseObserver.onError(errorCodeToStatus(res.error()));
            }
        } catch (Exception e) {
            responseObserver.onError(errorCodeToStatus(Result.ErrorCode.INTERNAL_ERROR));
        }
    }


}
