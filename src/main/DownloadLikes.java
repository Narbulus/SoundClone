package main;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ForkJoinPool;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import javax.imageio.ImageIO;

import GsonObjects.*;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mpatric.mp3agic.ByteBufferUtils;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.ID3v24Tag;
import com.mpatric.mp3agic.Mp3File;

public class DownloadLikes {
	
	private static final int BUFFER_SIZE = 2097152;

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception {
	
		@SuppressWarnings("resource")
		Scanner config = new Scanner(new File("config"));
		String user = config.nextLine();
		String downloadPath = config.nextLine();
		String clientID = config.nextLine();
		
		SoundLoader load = new SoundLoader(user, downloadPath, clientID);
		
		String redirect = load.getResponse("http://api.soundcloud.com/resolve.json?url=http://soundcloud.com/" + user
				+ "&client_id=" + clientID);
		
		RedirectResponse response = new Gson().fromJson(redirect, RedirectResponse.class);
		UserInfo info = new Gson().fromJson(load.getResponse(response.getLocation()), UserInfo.class);
		
		Type listType = new TypeToken<ArrayList<TrackInfo>>() {
		}.getType();
		
		ArrayList<TrackInfo> list = new ArrayList<>();
		
		for (int i = 0; i < info.getFavoritesCount(); i += 50) {
			String partLikes = load.getResponse("http://api.soundcloud.com/users/" + info.getId() + "/favorites.json?client_id=" + clientID + "&offset=" + i);
			list.addAll((Collection<? extends TrackInfo>) new Gson().fromJson(partLikes, listType));
		} 
		
		Gson streams = new Gson();
		TrackStreams tStream;
		for (TrackInfo t : list) {
			if (!load.isInHistory("" + t.getId())) {
				tStream = streams.fromJson(load.getResponse("https://api.soundcloud.com/i1/tracks/" + t.getId() + "/streams?client_id=" + clientID)
						, TrackStreams.class);
				if (tStream.getHttp_mp3_128_url() != null) {
					URL website = new URL(tStream.getHttp_mp3_128_url());
					ReadableByteChannel rbc = Channels.newChannel(website.openStream());
					String fuzzTitle = t.getTitle();
					fuzzTitle = fuzzTitle.replaceAll("[<>?*:|/\\\\]", " ");
					fuzzTitle = fuzzTitle.replaceAll("\"", "'");
					String tempPath = downloadPath + "\\%" + fuzzTitle + ".mp3";
					String finalPath = downloadPath + "\\" + fuzzTitle + ".mp3";
					FileOutputStream fos = new FileOutputStream(tempPath);
					fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
					fos.close();
					
					File f = new File(tempPath);
					
					if (t.getArtworkURL() != null) {
						URL artworkURL = new URL(t.getArtworkURL().replace("large", "t500x500"));
						
						BufferedImage image = ImageIO.read(artworkURL.openStream());
						ByteArrayOutputStream out = new ByteArrayOutputStream();
						ImageIO.write(image, "jpg", out);
						out.flush();
						byte[] bytes = out.toByteArray();
						out.close();
						
						OutputStream outs = new FileOutputStream(fuzzTitle + ".jpg");
						outs.write(bytes);
						outs.flush();
						outs.close();
						
						Mp3File mp3file = new Mp3File(tempPath);
						ID3v2 id3v2Tag;
						if (mp3file.hasId3v2Tag()) {
						  id3v2Tag = mp3file.getId3v2Tag();
						} else {
						  // mp3 does not have an ID3v2 tag, let's create one..
						  id3v2Tag = new ID3v24Tag();
						  mp3file.setId3v2Tag(id3v2Tag);
						}
						
						id3v2Tag.setAlbumImage(bytes, "image/jpeg");
						
						mp3file.save(finalPath);
						f.delete();
					}else{
						f.renameTo(new File(finalPath));
					}
					
					load.writeToHistory("" + t.getId());
				}
			}
		}
		
		load.closeHistory();
			
		
	}

}
