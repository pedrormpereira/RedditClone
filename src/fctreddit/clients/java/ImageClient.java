package fctreddit.clients.java;

import fctreddit.api.java.Image;
import fctreddit.api.java.Result;

// The used methods are overwritten by the respective REST and gRPC classes
public abstract class ImageClient extends Client implements Image {
    @Override
    public Result<String> createImage(String userId, byte[] imageContents, String password) {
        return null;
    }

    @Override
    public Result<byte[]> getImage(String userId, String imageId) {
        return null;
    }

    @Override
    public Result<Void> deleteImage(String userId, String imageId, String password) {
        return null;
    }
}
