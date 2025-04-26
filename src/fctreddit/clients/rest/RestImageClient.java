package fctreddit.clients.rest;

import fctreddit.api.java.Result;
import fctreddit.api.rest.RestImage;
import fctreddit.clients.java.ImageClient;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;

import java.net.URI;
public class RestImageClient extends ImageClient {
    final URI serverURI;
    final Client client;
    final ClientConfig config;

    final WebTarget target;

    public RestImageClient( URI serverURI ) {
        super();
        this.serverURI = serverURI;

        this.config = new ClientConfig();

        config.property( ClientProperties.READ_TIMEOUT, READ_TIMEOUT);
        config.property( ClientProperties.CONNECT_TIMEOUT, CONNECT_TIMEOUT);

        this.client = ClientBuilder.newClient(config);

        target = client.target( serverURI ).path(RestImage.PATH);
    }

    @Override
    public Result<String> createImage(String userId, byte[] imageContents, String password) {
        Response r = executeOperationPost(target.path( userId )
                .queryParam(RestImage.PASSWORD, password).request()
                .accept(MediaType.APPLICATION_JSON), Entity.entity(imageContents, MediaType.APPLICATION_OCTET_STREAM));

        int status = r.getStatus();
        if( status != Response.Status.OK.getStatusCode() )
            return Result.error( getErrorCodeFrom(status));
        else
            return Result.ok( r.readEntity( String.class ));
    }

    @Override
    public Result<byte[]> getImage(String userId, String imageId) {
        Response r = executeOperationGet(target.path(userId).path(imageId)
                .request()
                .accept(MediaType.APPLICATION_OCTET_STREAM));

        int status = r.getStatus();
        if (status != Status.OK.getStatusCode())
            return Result.error(getErrorCodeFrom(status));
        else
            return Result.ok(r.readEntity(byte[].class));
    }

    @Override
    public Result<Void> deleteImage(String userId, String imageId, String password) {
        Response r = executeOperationDelete(target.path(userId).path(imageId)
                .queryParam(RestImage.PASSWORD, password).request());

        int status = r.getStatus();
        if (status != Status.NO_CONTENT.getStatusCode())
            return Result.error(getErrorCodeFrom(status));
        else
            return Result.ok();
    }
}