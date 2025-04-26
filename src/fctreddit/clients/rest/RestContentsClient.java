package fctreddit.clients.rest;

import fctreddit.api.Post;
import fctreddit.api.java.Result;
import fctreddit.api.rest.RestContent;
import fctreddit.clients.java.ContentsClient;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;

import java.net.URI;

public class RestContentsClient extends ContentsClient {
    final URI serverURI;
    final Client client;
    final ClientConfig config;

    final WebTarget target;

    public RestContentsClient( URI serverURI ) {
        super();
        this.serverURI = serverURI;

        this.config = new ClientConfig();

        config.property( ClientProperties.READ_TIMEOUT, READ_TIMEOUT);
        config.property( ClientProperties.CONNECT_TIMEOUT, CONNECT_TIMEOUT);

        this.client = ClientBuilder.newClient(config);

        target = client.target( serverURI ).path(RestContent.PATH);
    }

    @Override
    public Result<Void> deletePost(String postId, String userPassword) {
        Response r = executeOperationDelete(target.path(postId)
                .queryParam(RestContent.PASSWORD, userPassword).request());

        if (r == null)
            return Result.error(Result.ErrorCode.TIMEOUT);

        int status = r.getStatus();
        if (status != Response.Status.NO_CONTENT.getStatusCode())
            return Result.error(getErrorCodeFrom(status));
        else
            return Result.ok();
    }

    public Result<Post> getPost(String postId) {
        Response r = executeOperationGet(target.path( postId )
                .request()
                .accept(MediaType.APPLICATION_JSON));

        if (r == null)
            return Result.error(Result.ErrorCode.TIMEOUT);

        int status = r.getStatus();
        if( status != Response.Status.OK.getStatusCode() )
            return Result.error( getErrorCodeFrom(status));
        else
            return Result.ok( r.readEntity( Post.class ));
    }

    @Override
    public Result<Post> updatePost(String postId, String password, Post post) {
        Response r = executeOperationPost(target.path( postId )
                .queryParam(RestContent.PASSWORD, password).request()
                .accept(MediaType.APPLICATION_JSON), Entity.entity(post, MediaType.APPLICATION_JSON));

        int status = r.getStatus();
        if( status != Response.Status.OK.getStatusCode() )
            return Result.error( getErrorCodeFrom(status));
        else
            return Result.ok( r.readEntity( Post.class ));
    }

    @Override
    public Result<Void> setAuthorToNull(String userId, String password) {
        Response r = executeOperationPut(target
                .path("nullify/" + userId)
                .queryParam(RestContent.PASSWORD, password).request()
                .accept(MediaType.APPLICATION_JSON), Entity.json("{}"));

        int status = r.getStatus();
        if( status != Response.Status.NO_CONTENT.getStatusCode() )
            return Result.error( getErrorCodeFrom(status));
        else
            return Result.ok();
    }
}
