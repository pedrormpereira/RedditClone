package fctreddit.impl.server.grpc;

import fctreddit.discovery.Discovery;
import fctreddit.impl.grpc.generated_java.ContentGrpc;
import io.grpc.Grpc;
import io.grpc.InsecureServerCredentials;
import io.grpc.Server;
import io.grpc.ServerCredentials;

import java.net.InetAddress;
import java.util.logging.Logger;

public class ContentServer {
    public static final int PORT = 9002;

    private static final String GRPC_CTX = "/grpc";
    private static final String SERVER_BASE_URI = "grpc://%s:%s%s";

    private static Logger Log = Logger.getLogger(ContentServer.class.getName());

    public static void main(String[] args) throws Exception {

        GrpcContentServerStub stub = new GrpcContentServerStub();
        ServerCredentials cred = InsecureServerCredentials.create();
        Server server = Grpc.newServerBuilderForPort(PORT, cred) .addService(stub).build();
        String serverURI = String.format(SERVER_BASE_URI, InetAddress.getLocalHost().getHostAddress(), PORT, GRPC_CTX);

        Discovery discovery = new Discovery(Discovery.DISCOVERY_ADDR, ContentGrpc.SERVICE_NAME, serverURI);
        discovery.start();

        Log.info(String.format("Content gRPC Server ready @ %s\n", serverURI));
        server.start().awaitTermination();
    }
}
