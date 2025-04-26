package fctreddit.impl.server.resources;

import fctreddit.api.java.Result;
import fctreddit.api.java.Users;
import fctreddit.api.rest.RestUsers;
import fctreddit.impl.server.java.JavaUsers;
import jakarta.inject.Singleton;
import jakarta.ws.rs.WebApplicationException;
import fctreddit.api.User;

import java.util.List;

@Singleton
public class UsersResource implements RestUsers {
	private final Users impl;
	
	public UsersResource() {
		impl = new JavaUsers();
	}

	@Override
	public String createUser(User user) {
		Result<String> res = impl.createUser(user);
		if (!res.isOK())
			throw new WebApplicationException(errorCodeToInt(res.error()));

		return res.value();
	}

	@Override
	public User getUser(String userId, String password) {
		Result<User> res = impl.getUser(userId, password);
		if (!res.isOK())
			throw new WebApplicationException(errorCodeToInt(res.error()));

		return res.value();
	}

	@Override
	public User updateUser(String userId, String password, User user) {
		Result<User> res = impl.updateUser(userId, password, user);
		if (!res.isOK())
			throw new WebApplicationException(errorCodeToInt(res.error()));

		return res.value();
	}

	@Override
	public User deleteUser(String userId, String password) {
		Result<User> res = impl.deleteUser(userId, password);
		if (!res.isOK())
			throw new WebApplicationException(errorCodeToInt(res.error()));

		return res.value();
	}

	@Override
	public List<User> searchUsers(String pattern) {
		Result<List<User>> res = impl.searchUsers(pattern);
		if (!res.isOK())
			throw new WebApplicationException(errorCodeToInt(res.error()));

		return res.value();
	}

	public static int errorCodeToInt(Result.ErrorCode errorCode) {
		return switch (errorCode) {
			case BAD_REQUEST -> 400;
			case NOT_FOUND -> 404;
			case CONFLICT -> 409;
			case FORBIDDEN -> 403;
			default -> 500;
		};
	}
}
