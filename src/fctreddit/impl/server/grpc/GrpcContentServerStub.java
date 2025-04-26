package fctreddit.impl.server.grpc;

import fctreddit.api.Post;
import fctreddit.api.java.Result;
import fctreddit.impl.grpc.generated_java.ContentProtoBuf;
import fctreddit.impl.grpc.generated_java.ContentProtoBuf.*;
import fctreddit.impl.grpc.generated_java.ContentGrpc;
import fctreddit.impl.server.java.JavaContent;
import io.grpc.BindableService;
import io.grpc.ServerServiceDefinition;
import io.grpc.stub.StreamObserver;

import java.util.List;

import static fctreddit.impl.server.grpc.GrpcUsersServerStub.errorCodeToStatus;

public class GrpcContentServerStub implements ContentGrpc.AsyncService, BindableService {
    private final JavaContent impl = new JavaContent();

    @Override
    public ServerServiceDefinition bindService() {
        return ContentGrpc.bindService(this);
    }

    @Override
    public void createPost(ContentProtoBuf.CreatePostArgs request,
                           StreamObserver<ContentProtoBuf.CreatePostResult> responseObserver) {
        try {
            Post post = convertToPost(request.getPost());
            String password = request.getPassword();

            post.setCreationTimestamp(System.currentTimeMillis());

            Result<String> result = impl.createPost(post, password);

            if (result.isOK()) {
                responseObserver.onNext(ContentProtoBuf.CreatePostResult.newBuilder()
                        .setPostId(result.value())
                        .build());
                responseObserver.onCompleted();
            } else {
                responseObserver.onError(errorCodeToStatus(result.error()));
            }
        } catch (Exception e) {
            responseObserver.onError(errorCodeToStatus(Result.ErrorCode.INTERNAL_ERROR));
        }
    }


    @Override
    public void getPost(GetPostArgs request, StreamObserver<ContentProtoBuf.GrpcPost> responseObserver) {
        try {
            Result<Post> res = impl.getPost(request.getPostId());
            if (res.isOK()) {
                ContentProtoBuf.GrpcPost grpcPost = convertToGrpcPost(res.value());
                responseObserver.onNext(grpcPost);
                responseObserver.onCompleted();
            } else {
                responseObserver.onError(errorCodeToStatus(res.error()));
            }
        } catch (Exception e) {
            responseObserver.onError(errorCodeToStatus(Result.ErrorCode.INTERNAL_ERROR));
        }
    }

    @Override
    public void getPosts(GetPostsArgs request, StreamObserver<GetPostsResult> responseObserver) {
        try {
            long timestamp = request.getTimestamp();
            String sortOrder = request.getSortOrder();
            Result<List<String>> res = impl.getPosts(timestamp, sortOrder);
            if (res.isOK()) {
                GetPostsResult result = GetPostsResult.newBuilder()
                        .addAllPostId(res.value())
                        .build();
                responseObserver.onNext(result);
                responseObserver.onCompleted();
            } else {
                responseObserver.onError(errorCodeToStatus(res.error()));
            }
        } catch (Exception e) {
            responseObserver.onError(errorCodeToStatus(Result.ErrorCode.INTERNAL_ERROR));
        }
    }

    @Override
    public void getPostAnswers(GetPostAnswersArgs request, StreamObserver<GetPostsResult> responseObserver) {
        try {
            String postId = request.getPostId();
            long timeout = request.getTimeout();
            Result<List<String>> res = impl.getPostAnswers(postId, timeout);
            if (res.isOK()) {
                GetPostsResult result = GetPostsResult.newBuilder()
                        .addAllPostId(res.value())
                        .build();
                responseObserver.onNext(result);
                responseObserver.onCompleted();
            } else {
                responseObserver.onError(errorCodeToStatus(res.error()));
            }
        } catch (Exception e) {
            responseObserver.onError(errorCodeToStatus(Result.ErrorCode.INTERNAL_ERROR));
        }
    }
    @Override
    public void updatePost(ContentProtoBuf.UpdatePostArgs request, StreamObserver<ContentProtoBuf.GrpcPost> responseObserver) {
        try {
            String postId = request.getPostId();
            String password = request.getPassword();
            Post updatedPost = convertToPost(request.getPost());

            Result<Post> result = impl.updatePost(postId, password, updatedPost);

            if (result.isOK()) {
                ContentProtoBuf.GrpcPost grpcPost = convertToGrpcPost(result.value());
                responseObserver.onNext(grpcPost);
                responseObserver.onCompleted();
            } else {
                responseObserver.onError(errorCodeToStatus(result.error()));
            }
        } catch (Exception e) {
            responseObserver.onError(errorCodeToStatus(Result.ErrorCode.INTERNAL_ERROR));
        }
    }

    @Override
    public void deletePost(DeletePostArgs request, StreamObserver<EmptyMessage> responseObserver) {
        try {
            String postId = request.getPostId();
            String password = request.getPassword();

            Result<Void> res = impl.deletePost(postId, password);

            if (res.isOK()) {
                responseObserver.onNext(EmptyMessage.getDefaultInstance());
                responseObserver.onCompleted();
            } else {
                responseObserver.onError(errorCodeToStatus(res.error()));
            }
        } catch (Exception e) {
            responseObserver.onError(errorCodeToStatus(Result.ErrorCode.INTERNAL_ERROR));
        }
    }

    @Override
    public void upVotePost(ChangeVoteArgs request, StreamObserver<EmptyMessage> responseObserver) {
        try {
            String postId = request.getPostId();
            String userId = request.getUserId();
            String password = request.getPassword();
            Result<Void> res = impl.upVotePost(postId, userId, password);
            if (res.isOK()) {
                responseObserver.onNext(EmptyMessage.getDefaultInstance());
                responseObserver.onCompleted();
            } else {
                responseObserver.onError(errorCodeToStatus(res.error()));
            }
        } catch (Exception e) {
            responseObserver.onError(errorCodeToStatus(Result.ErrorCode.INTERNAL_ERROR));
        }
    }

    @Override
    public void removeUpVotePost(ChangeVoteArgs request, StreamObserver<EmptyMessage> responseObserver) {
        try {
            String postId = request.getPostId();
            String userId = request.getUserId();
            String password = request.getPassword();
            Result<Void> res = impl.removeUpVotePost(postId, userId, password);
            if (res.isOK()) {
                responseObserver.onNext(EmptyMessage.getDefaultInstance());
                responseObserver.onCompleted();
            } else {
                responseObserver.onError(errorCodeToStatus(res.error()));
            }
        } catch (Exception e) {
            responseObserver.onError(errorCodeToStatus(Result.ErrorCode.INTERNAL_ERROR));
        }
    }

    @Override
    public void downVotePost(ChangeVoteArgs request, StreamObserver<EmptyMessage> responseObserver) {
        try {
            String postId = request.getPostId();
            String userId = request.getUserId();
            String password = request.getPassword();
            Result<Void> res = impl.downVotePost(postId, userId, password);
            if (res.isOK()) {
                responseObserver.onNext(EmptyMessage.getDefaultInstance());
                responseObserver.onCompleted();
            } else {
                responseObserver.onError(errorCodeToStatus(res.error()));
            }
        } catch (Exception e) {
            responseObserver.onError(errorCodeToStatus(Result.ErrorCode.INTERNAL_ERROR));
        }
    }

    @Override
    public void removeDownVotePost(ChangeVoteArgs request, StreamObserver<EmptyMessage> responseObserver) {
        try {
            String postId = request.getPostId();
            String userId = request.getUserId();
            String password = request.getPassword();
            Result<Void> res = impl.removeDownVotePost(postId, userId, password);
            if (res.isOK()) {
                responseObserver.onNext(EmptyMessage.getDefaultInstance());
                responseObserver.onCompleted();
            } else {
                responseObserver.onError(errorCodeToStatus(res.error()));
            }
        } catch (Exception e) {
            responseObserver.onError(errorCodeToStatus(Result.ErrorCode.INTERNAL_ERROR));
        }
    }

    @Override
    public void getUpVotes(GetPostArgs request, StreamObserver<VoteCountResult> responseObserver) {
        try {
            String postId = request.getPostId();
            Result<Integer> res = impl.getupVotes(postId);
            if (res.isOK()) {
                VoteCountResult result = VoteCountResult.newBuilder()
                        .setCount(res.value())
                        .build();
                responseObserver.onNext(result);
                responseObserver.onCompleted();
            } else {
                responseObserver.onError(errorCodeToStatus(res.error()));
            }
        } catch (Exception e) {
            responseObserver.onError(errorCodeToStatus(Result.ErrorCode.INTERNAL_ERROR));
        }
    }

    @Override
    public void getDownVotes(GetPostArgs request, StreamObserver<VoteCountResult> responseObserver) {
        try {
            String postId = request.getPostId();
            Result<Integer> res = impl.getDownVotes(postId);
            if (res.isOK()) {
                VoteCountResult result = VoteCountResult.newBuilder()
                        .setCount(res.value())
                        .build();
                responseObserver.onNext(result);
                responseObserver.onCompleted();
            } else {
                responseObserver.onError(errorCodeToStatus(res.error()));
            }
        } catch (Exception e) {
            responseObserver.onError(errorCodeToStatus(Result.ErrorCode.INTERNAL_ERROR));
        }
    }

    @Override
    public void setAuthorToNull(SetAuthorToNullArgs request, StreamObserver<EmptyMessage> responseObserver) {
        try {
            Result<Void> result = impl.setAuthorToNull(request.getUserId(), request.getPassword());
            if (result.isOK()) {
                responseObserver.onNext(EmptyMessage.getDefaultInstance());
                responseObserver.onCompleted();
            } else {
                responseObserver.onError(errorCodeToStatus(result.error()));
            }
        } catch (Exception e) {
            responseObserver.onError(errorCodeToStatus(Result.ErrorCode.INTERNAL_ERROR));
        }
    }

    private Post convertToPost(ContentProtoBuf.GrpcPost grpcPost) {
        Post post = new Post();
        post.setPostId(grpcPost.getPostId());
        post.setAuthorId(grpcPost.getAuthorId());
        post.setContent(grpcPost.getContent());
        post.setMediaUrl(grpcPost.getMediaUrl());
        post.setParentUrl(grpcPost.getParentUrl());
        post.setUpVote(grpcPost.getUpVote());
        post.setDownVote(grpcPost.getDownVote());
        return post;
    }

    private ContentProtoBuf.GrpcPost convertToGrpcPost(Post post) {
        ContentProtoBuf.GrpcPost.Builder builder = ContentProtoBuf.GrpcPost.newBuilder()
                .setPostId(post.getPostId())
                .setCreationTimestamp(post.getCreationTimestamp())
                .setContent(post.getContent())
                .setMediaUrl(post.getMediaUrl())
                .setUpVote(post.getUpVote())
                .setDownVote(post.getDownVote());

        if (post.getAuthorId() != null) {
            builder.setAuthorId(post.getAuthorId());
        }

        if (post.getParentUrl() != null) {
            builder.setParentUrl(post.getParentUrl());
        }

        return builder.build();
    }

}