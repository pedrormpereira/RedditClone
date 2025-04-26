package fctreddit.api;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/**
 * Represents a Vote on a Post by a User
 */
@Entity
public class Vote {
    @Id
    private String id; // Format: "userId:postId"

    private String userId;
    private String postId;

    // true = upvote, false = downvote
    private boolean type;

    public Vote() {}

    public Vote(String userId, String postId, boolean type) {
        this.userId = userId;
        this.postId = postId;
        this.type = type;
        this.id = userId + ":" + postId;
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getPostId() {
        return postId;
    }

    public boolean isUpvote() {
        return type;
    }

    public void setUserId(String userId) {
        this.userId = userId;
        updateId();
    }

    public void setPostId(String postId) {
        this.postId = postId;
        updateId();
    }

    public void setType(boolean type) {
        this.type = type;
    }

    private void updateId() {
        if (userId != null && postId != null) {
            this.id = userId + ":" + postId;
        }
    }

    @Override
    public String toString() {
        return "Vote [userId=" + userId + ", postId=" + postId + ", type=" + (type ? "upvote" : "downvote") + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Vote other = (Vote) obj;
        return id != null && id.equals(other.id) && type == other.type;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = (id == null ? 0 : id.hashCode());
        result = prime * result + (type ? 1 : 0);
        return result;
    }
}