package fctreddit.impl.server.java;

import java.net.URI;
import java.util.List;

import fctreddit.api.User;
import fctreddit.api.java.Result;
import fctreddit.api.java.Users;
import fctreddit.clients.grpc.GrpcContentsClient;
import fctreddit.clients.java.ContentsClient;
import fctreddit.clients.rest.RestContentsClient;
import fctreddit.discovery.Discovery;
import fctreddit.impl.server.persistence.Hibernate;
import fctreddit.impl.server.rest.ContentsServer;
import jakarta.inject.Singleton;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

@Singleton
public class JavaUsers implements Users {
	private final Hibernate hibernate;
	private ContentsClient contentsClient;

	private final Discovery discovery;

	public JavaUsers() {
		try {
			hibernate = Hibernate.getInstance();
			discovery = new Discovery(Discovery.DISCOVERY_ADDR);
			discovery.start();
		} catch (Exception e) {
			throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
		}
	}

	private ContentsClient getContentsClient() {
		if (contentsClient == null) {
			URI[] uris = discovery.knownUrisOf(ContentsServer.SERVICE, 1);
			URI restUri = null;
			URI grpcUri = null;

			for (URI uri : uris) {
				if (uri.getScheme().startsWith("http"))
					restUri = uri;
				else if (uri.getScheme().equals("grpc"))
					grpcUri = uri;
			}
			if (restUri != null) {
				this.contentsClient = new RestContentsClient(restUri);
			} else if (grpcUri != null) {
				this.contentsClient = new GrpcContentsClient(grpcUri);
			} else {
				throw new WebApplicationException("No Content Service found");
			}
		}
		return contentsClient;
	}

	@Override
	public Result<String> createUser(User user) {
		if (user == null)
			return Result.error(Result.ErrorCode.BAD_REQUEST);

		String userId = user.getUserId();
		String pwd = user.getPassword();
		String fullName = user.getFullName();
		String email = user.getEmail();

		if (userId == null || userId.isBlank() || pwd == null || pwd.isBlank() || fullName == null || fullName.isBlank()
				|| email == null || email.isBlank())
			return Result.error(Result.ErrorCode.BAD_REQUEST);

		User oldUser;
		try {
			oldUser = hibernate.get(User.class, userId);
		} catch (Exception e) {
			return Result.error(Result.ErrorCode.INTERNAL_ERROR);
		}

		if (oldUser != null)
			return Result.error(Result.ErrorCode.CONFLICT);

		try {
			hibernate.persist(user);
		} catch (Exception e) {
			return Result.error(Result.ErrorCode.INTERNAL_ERROR);
		}

		return Result.ok(userId);
	}

	private void determineUpdatedInfo(User oldUser, User user) {
		if (user.getUserId() == null || user.getUserId().isBlank())
			user.setUserId(oldUser.getUserId());
		if (user.getPassword() == null || user.getPassword().isBlank())
			user.setPassword(oldUser.getPassword());
		if (user.getEmail() == null || user.getEmail().isBlank())
			user.setEmail(oldUser.getEmail());
		if (user.getFullName() == null || user.getFullName().isBlank())
			user.setFullName(oldUser.getFullName());
	}

	@Override
	public Result<User> getUser(String userId, String password) {
		User user;
		try {
			user = hibernate.get(User.class, userId);
		} catch (Exception e) {
			return Result.error(Result.ErrorCode.INTERNAL_ERROR);
		}

		if (user == null)
			return Result.error(Result.ErrorCode.NOT_FOUND);

		if (!user.getPassword().equals(password))
			return Result.error(Result.ErrorCode.FORBIDDEN);

		return Result.ok(user);
	}

	@Override
	public Result<User> updateUser(String userId, String password, User user) {
		Result<User> res = this.getUser(userId, password);
		if (!res.isOK())
			return Result.error(res.error());

		determineUpdatedInfo(res.value(), user);

		try {
			hibernate.update(user);
		} catch (Exception e) {
			return Result.error(Result.ErrorCode.INTERNAL_ERROR);
		}

		return Result.ok(user);
	}

	@Override
	public Result<User> deleteUser(String userId, String password) {
		Result<User> res = this.getUser(userId, password);
		if (!res.isOK())
			return Result.error(res.error());

		User user = res.value();

		getContentsClient().setAuthorToNull(userId, password);

		try {
			hibernate.delete(user);
		} catch (Exception e) {
			return Result.error(Result.ErrorCode.INTERNAL_ERROR);
		}

		return Result.ok(user);
	}

	@Override
	public Result<List<User>> searchUsers(String pattern) {
		try {
			List<User> list = hibernate.jpql("SELECT u FROM User u WHERE u.userId LIKE '%" + pattern +"%'", User.class);
			System.out.println(list);
			return Result.ok(list);
		} catch (Exception e) {
			return Result.error(Result.ErrorCode.INTERNAL_ERROR);
		}
	}
}
