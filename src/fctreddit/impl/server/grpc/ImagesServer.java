package fctreddit.impl.server.grpc;
import fctreddit.discovery.Discovery;
import fctreddit.impl.grpc.generated_java.ImageGrpc;
import io.grpc.*;

import java.net.InetAddress;
import java.util.logging.Logger;

public class ImagesServer {
    public static final int PORT = 9001;

    private static final String GRPC_CTX = "/grpc";
    private static final String SERVER_BASE_URI = "grpc://%s:%s%s";

    private static Logger Log = Logger.getLogger(ImagesServer.class.getName());

    public static void main(String[] args) throws Exception {
        GrpcImagesServerStub stub = new GrpcImagesServerStub();
        ServerCredentials cred = InsecureServerCredentials.create();
        Server server = Grpc.newServerBuilderForPort(PORT, cred).addService(stub).build();
        String serverURI = String.format(SERVER_BASE_URI, InetAddress.getLocalHost().getHostAddress(), PORT, GRPC_CTX);

        Discovery discovery = new Discovery(Discovery.DISCOVERY_ADDR, ImageGrpc.SERVICE_NAME, serverURI);
        discovery.start();

        Log.info(String.format("Images gRPC Server ready @ %s\n", serverURI));
        server.start().awaitTermination();
    }
}
