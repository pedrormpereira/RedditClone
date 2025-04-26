package fctreddit.impl.server.java;

import fctreddit.api.Post;
import fctreddit.api.User;
import fctreddit.api.Vote;
import fctreddit.api.java.Content;
import fctreddit.api.java.Result;
import fctreddit.clients.grpc.GrpcImageClient;
import fctreddit.clients.grpc.GrpcUsersClient;
import fctreddit.clients.java.ImageClient;
import fctreddit.clients.java.UsersClient;
import fctreddit.clients.rest.RestImageClient;
import fctreddit.clients.rest.RestUsersClient;
import fctreddit.discovery.Discovery;
import fctreddit.impl.server.persistence.Hibernate;
import fctreddit.impl.server.rest.ImagesServer;
import fctreddit.impl.server.rest.UsersServer;
import jakarta.inject.Singleton;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.*;


@Singleton
public class JavaContent implements Content {
    private final UsersClient userClient;
    private final ImageClient imageClient;
    private final Hibernate hibernate;

    public JavaContent() {
        try {
            hibernate = Hibernate.getInstance();

            Discovery discovery = new Discovery(Discovery.DISCOVERY_ADDR);
            discovery.start();

            URI[] uris = discovery.knownUrisOf(UsersServer.SERVICE, 1);
            URI restUri = null;
            URI grpcUri = null;

            // Check for REST (HTTP) or gRPC endpoints
            for (URI uri : uris) {
                if (uri.getScheme().startsWith("http")) {
                    restUri = uri;
                } else if (uri.getScheme().equals("grpc")) {
                    grpcUri = uri;
                }
            }

            if (restUri != null) {
                this.userClient = new RestUsersClient(restUri);
            } else if (grpcUri != null) {
                this.userClient = new GrpcUsersClient(grpcUri);
            } else {
                throw new WebApplicationException("No Users Service found");
            }

            uris = discovery.knownUrisOf(ImagesServer.SERVICE, 1);
            restUri = null;
            grpcUri = null;

            // Check for REST (HTTP) or gRPC endpoints
            for (URI uri : uris) {
                if (uri.getScheme().startsWith("http")) {
                    restUri = uri;
                } else if (uri.getScheme().equals("grpc")) {
                    grpcUri = uri;
                }
            }

            if (restUri != null) {
                this.imageClient = new RestImageClient(restUri);
            } else if (grpcUri != null) {
                this.imageClient = new GrpcImageClient(grpcUri);
            } else {
                throw new WebApplicationException("No Image Service found");
            }
        } catch (Exception e) {
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    private String extractFromUrl(String unparsed) {
        if (unparsed == null || unparsed.isBlank()) {
            return null;
        }
        int lastSlash = unparsed.lastIndexOf('/');
        return (lastSlash == -1) ? unparsed : unparsed.substring(lastSlash + 1);
    }

    @Override
    public Result<String> createPost(Post post, String userPassword) {
        if (post.getAuthorId() == null || post.getAuthorId().isBlank())
            return Result.error(Result.ErrorCode.BAD_REQUEST);

        Result<User> result = userClient.getUser(post.getAuthorId(), userPassword);

        if (!result.isOK())
            return Result.error(result.error());

        if (post.getParentUrl() != null && !post.getParentUrl().isBlank()) {
            String parentId = this.extractFromUrl(post.getParentUrl());
            try {
                Post parentPost = hibernate.get(Post.class, parentId);
                if (parentPost == null)
                    return Result.error(Result.ErrorCode.NOT_FOUND);
            } catch (Exception e) {
                return Result.error(Result.ErrorCode.INTERNAL_ERROR);
            }
        }


        if (post.getPostId() == null || post.getPostId().isBlank()) {
            post.setPostId(UUID.randomUUID().toString());
        }

        post.setCreationTimestamp(System.currentTimeMillis());

        try {
            hibernate.persist(post);
        } catch (Exception e) {
            return Result.error(Result.ErrorCode.INTERNAL_ERROR);
        }

        return Result.ok(post.getPostId());
    }


    private Result<Post> getPostFromDatabase(String postId, String password) {
        Result<Post> postRes = this.getPost(postId);
        if (!postRes.isOK())
            return Result.error(postRes.error());

        Post post = postRes.value();

        Result<User> result = this.userClient.getUser(post.getAuthorId(), password);
        if (!result.isOK())
            return Result.error(result.error());

        return Result.ok(post);
    }

    @Override
    public Result<List<String>> getPosts(long timestamp, String sortOrder) {
        try {
            String jpqlTemplate = "SELECT p.postId FROM Post p WHERE p.parentUrl IS NULL AND (%d <= 0 OR p.creationTimestamp >= %d) ";

            if (MOST_UP_VOTES.equals(sortOrder)) {
                jpqlTemplate += "ORDER BY p.upVote DESC, p.postId ASC";
            } else if (MOST_REPLIES.equals(sortOrder)) {
                jpqlTemplate += """
                    ORDER BY (
                    SELECT COUNT(r) FROM Post r WHERE SUBSTRING(r.parentUrl, LENGTH(r.parentUrl) - 35) = p.postId
                ) DESC, p.postId ASC""";
            } else {
                jpqlTemplate += "ORDER BY p.creationTimestamp ASC";
            }

            String jpql = String.format(jpqlTemplate, timestamp, timestamp);

            List<String> posts = hibernate.jpql(jpql, String.class);
            return Result.ok(posts);
        } catch (Exception e) {
            return Result.error(Result.ErrorCode.INTERNAL_ERROR);
        }
    }

    @Override
    public Result<Post> getPost(String postId) {
        if (postId == null || postId.isBlank())
            return Result.error(Result.ErrorCode.BAD_REQUEST);

        Post post;
        try {
            post = hibernate.get(Post.class, postId);
        } catch (Exception e) {
            return Result.error(Result.ErrorCode.INTERNAL_ERROR);
        }

        if (post == null)
            return Result.error(Result.ErrorCode.NOT_FOUND);

        return Result.ok(post);
    }

    @Override
    public Result<List<String>> getPostAnswers(String postId, long timeout) {
        try {
            String sanitizedPostId = postId.replace("'", "''");
            String jpql = String.format(
                    "SELECT p.postId FROM Post p WHERE p.parentUrl LIKE '%%/%s' ORDER BY p.creationTimestamp ASC",
                    sanitizedPostId
            );


            List<String> replies = hibernate.jpql(jpql, String.class);
            int previousSize = replies.size();
            long start = System.currentTimeMillis();
            long remaining = timeout;

            while (remaining > 0) {
                Thread.sleep(Math.min(100, remaining));

                replies = hibernate.jpql(jpql, String.class);
                if (replies.size() != previousSize)
                    return Result.ok(replies);

                long now = System.currentTimeMillis();
                remaining = timeout - (now - start);
            }

            return Result.ok(replies);
        } catch (Exception e) {
            return Result.error(Result.ErrorCode.INTERNAL_ERROR);
        }
    }

    @Override
    public Result<Post> updatePost(String postId, String userPassword, Post post) {
        Result<Post> res = getPostFromDatabase(postId, userPassword);
        if (!res.isOK())
            return Result.error(res.error());

        Post oldPost = res.value();

        Result<List<String>> answersResult = this.getPostAnswers(postId, 0);
        if (!answersResult.isOK())
            return Result.error(answersResult.error());

        int answers = answersResult.value().size();
        if (!(oldPost.getDownVote() == oldPost.getUpVote() && oldPost.getUpVote() == answers && answers == 0))
            return Result.error(Result.ErrorCode.BAD_REQUEST);

        String content = post.getContent();
        String mediaUrl = post.getMediaUrl();

        if (content != null && !content.isBlank()) {
            oldPost.setContent(content);
        }

        if (mediaUrl != null && !mediaUrl.isBlank()) {
            oldPost.setMediaUrl(mediaUrl);
        }

        try {
            hibernate.update(oldPost);
        } catch (Exception e) {
            return Result.error(Result.ErrorCode.INTERNAL_ERROR);
        }

        return Result.ok(oldPost);
    }



    @Override
    public Result<Void> deletePost(String postId, String userPassword) {
        if (postId == null || postId.isBlank()) {
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        }

        try {
            Result<Post> res = getPostFromDatabase(postId, userPassword);
            if (!res.isOK()) return Result.error(res.error());

            Post post = res.value();
            String authorId = post.getAuthorId();
            String mediaUrl = post.getMediaUrl();

            if (mediaUrl != null && !mediaUrl.isBlank()) {
                String imageId = extractFromUrl(mediaUrl);
                if (imageId == null || imageId.isBlank()) {
                    return Result.error(Result.ErrorCode.BAD_REQUEST);
                }
                Result<Void> deleteRes = imageClient.deleteImage(authorId, imageId, userPassword);
                if (!deleteRes.isOK()) return Result.error(deleteRes.error());
            }

            Set<Post> toDelete = new HashSet<>();
            collectPostsRecursively(post, toDelete);

            for (Post p : toDelete) {
                String jpql = String.format("SELECT v from Vote v WHERE v.postId = '%s'", p.getPostId());
                List<Vote> votes = hibernate.jpql(jpql, Vote.class);
                for (Vote v : votes)
                    hibernate.delete(v);
                hibernate.delete(p);
            }

            return Result.ok();
        } catch (Exception e) {

            return Result.error(Result.ErrorCode.INTERNAL_ERROR);
        }
    }


    private void collectPostsRecursively(Post root, Set<Post> collected) {
        Stack<Post> stack = new Stack<>();
        stack.push(root);
        while (!stack.isEmpty()) {
            Post current = stack.pop();
            if (collected.add(current)) { // Only process if not already collected
                Result<List<String>> repliesRes = this.getPostAnswers(current.getPostId(), 0);
                if (repliesRes.isOK()) {
                    List<String> replies = repliesRes.value();
                    for (String replyId : replies) {
                        Result<Post> replyRes = this.getPost(replyId);
                        if (replyRes.isOK()) {
                            stack.push(replyRes.value());
                        }
                    }
                }
            }
        }
    }

    @Override
    public Result<Void> upVotePost(String postId, String userId, String userPassword) {
        Result<Post> res = this.getPost(postId);
        if (!res.isOK())
            return Result.error(res.error());

        Post post = res.value();

        Result<User> result = this.userClient.getUser(userId, userPassword);
        if (!result.isOK())
            return Result.error(result.error());

        Vote vote;
        try {
            vote = hibernate.get(Vote.class, String.format("%s:%s", userId, postId));
        } catch (Exception e) {
            return Result.error(Result.ErrorCode.INTERNAL_ERROR);
        }

        if (vote != null)
            return Result.error(Result.ErrorCode.CONFLICT);

        post.setUpVote(post.getUpVote()+1);
        vote = new Vote(userId, postId, true);

        try {
            hibernate.update(post);
            hibernate.persist(vote);
        } catch (Exception e) {
            return Result.error(Result.ErrorCode.INTERNAL_ERROR);
        }

        return Result.ok();
    }

    @Override
    public Result<Void> removeUpVotePost(String postId, String userId, String userPassword) {
        Result<Post> res = this.getPost(postId);
        if (!res.isOK())
            return Result.error(res.error());

        Post post = res.value();

        Result<User> result = this.userClient.getUser(userId, userPassword);
        if (!result.isOK())
            return Result.error(result.error());

        if (post == null)
            return Result.error(Result.ErrorCode.NOT_FOUND);

        post.setUpVote(post.getUpVote()-1);
        try {
            hibernate.update(post);
            hibernate.delete(hibernate.get(Vote.class, String.format("%s:%s", userId, postId)));
        } catch (Exception e) {
            return Result.error(Result.ErrorCode.INTERNAL_ERROR);
        }

        return Result.ok();
    }

    @Override
    public Result<Void> downVotePost(String postId, String userId, String userPassword) {
        Result<Post> res = this.getPost(postId);
        if (!res.isOK())
            return Result.error(res.error());

        Post post = res.value();

        Result<User> result = this.userClient.getUser(userId, userPassword);
        if (!result.isOK())
            return Result.error(result.error());

        Vote vote;
        try {
            vote = hibernate.get(Vote.class, String.format("%s:%s", userId, postId));
        } catch (Exception e) {
            return Result.error(Result.ErrorCode.INTERNAL_ERROR);
        }

        if (vote != null)
            return Result.error(Result.ErrorCode.CONFLICT);

        post.setDownVote(post.getDownVote()+1);
        vote = new Vote(userId, postId, false);

        try {
            hibernate.update(post);
            hibernate.persist(vote);
        } catch (Exception e) {
            return Result.error(Result.ErrorCode.INTERNAL_ERROR);
        }

        return Result.ok();
    }

    @Override
    public Result<Void> removeDownVotePost(String postId, String userId, String userPassword) {
        Result<Post> res = this.getPost(postId);
        if (!res.isOK())
            return Result.error(res.error());

        Post post = res.value();

        Result<User> result = this.userClient.getUser(userId, userPassword);
        if (!result.isOK())
            return Result.error(result.error());

        if (post == null)
            return Result.error(Result.ErrorCode.NOT_FOUND);

        post.setDownVote(post.getDownVote()-1);
        try {
            hibernate.update(post);
            hibernate.delete(hibernate.get(Vote.class, String.format("%s:%s", userId, postId)));
        } catch (Exception e) {
            return Result.error(Result.ErrorCode.INTERNAL_ERROR);
        }

        return Result.ok();
    }

    @Override
    public Result<Integer> getupVotes(String postId) {
        Post post;
        try {
            post = hibernate.get(Post.class, postId);
        } catch (Exception e) {
            return Result.error(Result.ErrorCode.INTERNAL_ERROR);
        }

        if (post == null)
            return Result.error(Result.ErrorCode.NOT_FOUND);

        return Result.ok(post.getUpVote());
    }

    @Override
    public Result<Integer> getDownVotes(String postId) {
        Post post;
        try {
            post = hibernate.get(Post.class, postId);
        } catch (Exception e) {
            return Result.error(Result.ErrorCode.INTERNAL_ERROR);
        }

        if (post == null)
            return Result.error(Result.ErrorCode.NOT_FOUND);

        return Result.ok(post.getDownVote());
    }

    @Override
    public Result<Void> setAuthorToNull(String userId, String password) {
        // Sanitize userId for queries
        String sanitizedUserId = userId.replace("'", "''");

        // Delete all votes by this user
        String votesJpql = String.format("SELECT v FROM Vote v WHERE v.userId = '%s'", sanitizedUserId);
        List<Vote> votes;
        try {
            votes = hibernate.jpql(votesJpql, Vote.class);
            for (Vote vote : votes) {
                Post post = hibernate.get(Post.class, vote.getPostId());
                if (post != null) {
                    if (vote.isUpvote()) {
                        post.setUpVote(post.getUpVote() - 1);
                    } else {
                        post.setDownVote(post.getDownVote() - 1);
                    }
                    hibernate.update(post);
                }
                hibernate.delete(vote); // Delete the vote
            }
        } catch (Exception e) {
            return Result.error(Result.ErrorCode.INTERNAL_ERROR);
        }

        String postsJpql = String.format("SELECT p FROM Post p WHERE p.authorId = '%s'", sanitizedUserId);
        List<Post> posts;
        try {
            posts = hibernate.jpql(postsJpql, Post.class);
            for (Post post : posts) {
                post.setAuthorId(null);
                hibernate.update(post);
            }
        } catch (Exception e) {
            return Result.error(Result.ErrorCode.INTERNAL_ERROR);
        }
        return Result.ok();
    }
}
