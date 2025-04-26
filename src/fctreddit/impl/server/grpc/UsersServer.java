package fctreddit.impl.server.grpc;

import java.net.InetAddress;
import java.util.logging.Logger;

import fctreddit.discovery.Discovery;
import fctreddit.impl.grpc.generated_java.UsersGrpc;
import io.grpc.Grpc;
import io.grpc.InsecureServerCredentials;
import io.grpc.Server;
import io.grpc.ServerCredentials;

public class UsersServer {
public static final int PORT = 9000;

	private static final String GRPC_CTX = "/grpc";
	private static final String SERVER_BASE_URI = "grpc://%s:%s%s";
	
	private static Logger Log = Logger.getLogger(UsersServer.class.getName());

	// In UsersServer.java
	public static void main(String[] args) throws Exception {
		GrpcUsersServerStub stub = new GrpcUsersServerStub();
		ServerCredentials cred = InsecureServerCredentials.create();
		Server server = Grpc.newServerBuilderForPort(PORT, cred).addService(stub).build();

		// Advertise the actual IP via discovery (optional, but useful for logging)
		String serverURI = String.format(
				SERVER_BASE_URI,
				InetAddress.getLocalHost().getHostAddress(), // Still advertise the container's IP
				PORT,
				GRPC_CTX
		);

		Discovery discovery = new Discovery(Discovery.DISCOVERY_ADDR, UsersGrpc.SERVICE_NAME, serverURI);
		discovery.start();

		Log.info(String.format("Users gRPC Server ready @ %s\n", serverURI));
		server.start().awaitTermination();
	}
}

