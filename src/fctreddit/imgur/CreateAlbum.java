package fctreddit.imgur;

import com.github.scribejava.apis.ImgurApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.google.gson.Gson;
import fctreddit.imgur.data.BasicResponse;
import fctreddit.imgur.data.CreateAlbumArguments;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class CreateAlbum {

	private static final String apiKey = "ebb811b16c8746f";
	private static final String apiSecret = "689b7315f1618818352e64b265bbe784a5a3ad75";
	private static final String accessTokenStr = "93ef46d224e1bc67bd1913dda73ef595e10c54cc";
	private static final String CREATE_ALBUM_URL = "https://api.imgur.com/3/album";
		
	private static final int HTTP_SUCCESS = 200;
	private static final String CONTENT_TYPE_HDR = "Content-Type";
	private static final String JSON_CONTENT_TYPE = "application/json; charset=utf-8";
	
	private final Gson json;
	private final OAuth20Service service;
	private final OAuth2AccessToken accessToken;
	
	public CreateAlbum() {
		json = new Gson();
		accessToken = new OAuth2AccessToken(accessTokenStr);
		service = new ServiceBuilder(apiKey).apiSecret(apiSecret).build(ImgurApi.instance());
	}
	
	public boolean execute(String albumName) {
		OAuthRequest request = new OAuthRequest(Verb.POST, CREATE_ALBUM_URL);
		
		request.addHeader(CONTENT_TYPE_HDR, JSON_CONTENT_TYPE);
		request.setPayload(json.toJson(new CreateAlbumArguments(albumName, albumName)));
		
		service.signRequest(accessToken, request);
		
		try {
			Response r = service.execute(request);
			
			if(r.getCode() != HTTP_SUCCESS) {
				//Operation failed
				System.err.println("Operation Failed\nStatus: " + r.getCode() + "\nBody: " + r.getBody());
				return false;
			} else {
				BasicResponse body = json.fromJson(r.getBody(), BasicResponse.class);
				System.err.println("Contents of Body: " + r.getBody());
				System.out.println("Operation Succedded\nAlbum name: " + albumName + "\nAlbum ID: " + body.getData().get("id"));			
				return body.isSuccess();
			}
			
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (ExecutionException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		return false;
	}
	
	public static void main(String[] args) throws Exception {

		if( args.length != 1 ) {
			System.err.println("usage: java " + CreateAlbum.class.getCanonicalName() +  " <album-name>");
			System.exit(0);
		}

		String albumName = args[0];
		CreateAlbum ca = new CreateAlbum();
		
		if(ca.execute(albumName))
			System.out.println("Album '" + albumName + "' created successfuly.");
		else
			System.err.println("Failed to create new album '" + albumName + "'");
	}
	
}