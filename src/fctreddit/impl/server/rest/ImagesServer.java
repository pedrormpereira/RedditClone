package fctreddit.impl.server.rest;

import fctreddit.discovery.Discovery;
import fctreddit.impl.server.resources.ImageResource;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.net.InetAddress;
import java.net.URI;
import java.util.logging.Logger;

public class ImagesServer {

    private static final Logger Log = Logger.getLogger(ImagesServer.class.getName());

    static {
        System.setProperty("java.net.preferIPv4Stack", "true");
        System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s\n");
    }
    public static final String SERVICE = "Image";
    public static final int PORT = 8081;
    public static final String SERVER_URI_FMT = "http://%s:%s/rest";
    public static String SERVER_IP;

    public static void main(String[] args) {
        try {
            ResourceConfig config = new ResourceConfig();
            config.register(ImageResource.class);

            SERVER_IP = InetAddress.getLocalHost().getHostAddress();
            String serverURI = String.format(SERVER_URI_FMT, SERVER_IP, PORT);
            JdkHttpServerFactory.createHttpServer( URI.create(serverURI), config);

            Discovery discovery = new Discovery(Discovery.DISCOVERY_ADDR, SERVICE, serverURI);
            discovery.start();

            Log.info(String.format("%s Service ready @ %s\n",  SERVICE, serverURI));
        } catch( Exception e) {
            Log.severe(e.getMessage());
        }
    }
}
