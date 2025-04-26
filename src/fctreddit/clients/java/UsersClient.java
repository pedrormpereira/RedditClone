package fctreddit.clients.java;

import fctreddit.api.User;
import fctreddit.api.java.Result;
import fctreddit.api.java.Users;

import java.util.List;

// The used methods are overwritten by the respective REST and gRPC classes
public abstract class UsersClient extends Client implements Users {
    @Override
    public Result<String> createUser(User user) {
        return null;
    }

    @Override
    public Result<User> getUser(String userId, String password) {
        return null;
    }

    @Override
    public Result<User> updateUser(String userId, String password, User user) {
        return null;
    }

    @Override
    public Result<User> deleteUser(String userId, String password) {
        return null;
    }

    @Override
    public Result<List<User>> searchUsers(String pattern) {
        return null;
    }
}
