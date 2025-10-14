package fctreddit.impl.server.java;

import fctreddit.api.User;
import fctreddit.api.java.Image;
import fctreddit.api.java.Result;
import fctreddit.api.rest.RestImage;
import fctreddit.clients.grpc.GrpcUsersClient;
import fctreddit.clients.java.UsersClient;
import fctreddit.clients.rest.RestUsersClient;
import fctreddit.discovery.Discovery;
import fctreddit.imgur.AddImageToAlbum;
import fctreddit.imgur.CreateAlbum;
import fctreddit.imgur.ImageUpload;
import fctreddit.impl.server.rest.ImagesServer;
import fctreddit.impl.server.rest.UsersServer;
import jakarta.inject.Singleton;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static fctreddit.impl.server.rest.ImagesServer.SERVER_URI_FMT;

@Singleton
public class JavaImgurImage implements Image {
    private static final String IMAGE_DIRECTORY = "imageFiles";
    private final AddImageToAlbum addImageToAlbum;
    private final ImageUpload imageUpload;
    private final String albumName;
    private final UsersClient userClient;
    public JavaImgurImage() {
        try {
            albumName = UUID.randomUUID().toString();
            CreateAlbum createAlbum = new CreateAlbum();
            createAlbum.execute(albumName);
            addImageToAlbum = new AddImageToAlbum();
            imageUpload = new ImageUpload();
            Discovery discovery = new Discovery(Discovery.DISCOVERY_ADDR);
            discovery.start();

            URI[] uris = discovery.knownUrisOf(UsersServer.SERVICE, 1);
            URI restUri = null;
            URI grpcUri = null;

            for (URI uri : uris) {
                if (uri.getScheme().startsWith("http")) {
                    restUri = uri;
                } else if (uri.getScheme().equals("grpc")) {
                    grpcUri = uri;
                }
            }

            if (restUri != null) {
                this.userClient = new RestUsersClient(restUri);
            } else if (grpcUri != null) {
                this.userClient = new GrpcUsersClient(grpcUri);
            } else {
                throw new WebApplicationException("No Users Service found");
            }
        } catch (Exception e) {
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    public Result<String> createImage(String userId, byte[] imageContents, String password) {
        if (imageContents == null || imageContents.length == 0 || password == null || password.isBlank())
            return Result.error(Result.ErrorCode.BAD_REQUEST);

        Result<User> result = this.userClient.getUser(userId, password);
        if (!result.isOK())
            return Result.error(result.error());

        String imageId = UUID.randomUUID().toString();

        String serverURI = String.format(SERVER_URI_FMT, ImagesServer.SERVER_IP, ImagesServer.PORT);
        String imageURI = String.format("%s%s/%s/%s", serverURI, RestImage.PATH, userId, imageId);

        try {
            this.imageUpload.execute(imageId, imageContents);
            this.addImageToAlbum.execute(albumName, imageId);
        } catch (Exception e) {
            return Result.error(Result.ErrorCode.INTERNAL_ERROR);
        }

        return Result.ok(imageURI);
    }

    @Override
    public Result<byte[]> getImage(String userId, String imageId) {
        Path pathToFile = getPathToImage(imageId);

        try {
            if (Files.exists(pathToFile))
                return Result.ok(Files.readAllBytes(pathToFile));
            else
                return Result.error(Result.ErrorCode.NOT_FOUND);
        } catch (Exception e) {
            return Result.error(Result.ErrorCode.INTERNAL_ERROR);
        }
    }

    @Override
    public Result<Void> deleteImage(String userId, String imageId, String password) {
        if (userId == null || userId.isBlank() || imageId == null || imageId.isBlank() || password == null || password.isBlank()) {
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        }

        Result<User> userResult = this.userClient.getUser(userId, password);
        if (!userResult.isOK()) {
            return Result.error(userResult.error());
        }

        Path pathToFile = getPathToImage(imageId);
        if (!Files.exists(pathToFile)) {
            return Result.error(Result.ErrorCode.NOT_FOUND);
        }

        try {
            Files.deleteIfExists(pathToFile);
            return Result.ok();
        } catch (Exception e) {
            return Result.error(Result.ErrorCode.INTERNAL_ERROR);
        }
    }

    private Path getPathToImage(String imageId) {
        return Paths.get(IMAGE_DIRECTORY + File.separator + imageId + ".png");
    }
}
