package fctreddit.impl.server.resources;

import fctreddit.api.java.Image;
import fctreddit.api.java.Result;
import fctreddit.impl.server.java.JavaImage;
import jakarta.inject.Singleton;
import jakarta.ws.rs.WebApplicationException;
import fctreddit.api.rest.RestImage;

import static fctreddit.impl.server.resources.UsersResource.errorCodeToInt;

@Singleton
public class ImageResource implements RestImage {
    private final Image impl;
    public ImageResource() {
        impl = new JavaImage();
    }

    @Override
    public String createImage(String userId, byte[] imageContents, String password) {
        Result<String> res = impl.createImage(userId, imageContents, password);
        if (!res.isOK())
            throw new WebApplicationException(errorCodeToInt(res.error()));

        return res.value();
    }

    @Override
    public byte[] getImage(String userId, String imageId) {
        Result<byte[]> res = impl.getImage(userId, imageId);
        if (!res.isOK())
            throw new WebApplicationException(errorCodeToInt(res.error()));

        return res.value();
    }

    @Override
    public void deleteImage(String userId, String imageId, String password) {
        Result<Void> res = impl.deleteImage(userId, imageId, password);
        if (!res.isOK())
            throw new WebApplicationException(errorCodeToInt(res.error()));
    }
}
