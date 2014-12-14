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
		
		Mp3File mp3file = new Mp3File("sampletag.mp3");
		ID3v2 template;
		if (mp3file.hasId3v2Tag()) {
		  template = mp3file.getId3v2Tag();
		}else{
			template = new ID3v24Tag();
		}
		//downloads.generateMp3(mediaPath, downloadPath, track)
		
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
		Mp3Downloader download = new Mp3Downloader(template);
		for (TrackInfo t : list) {
			if (!load.isInHistory("" + t.getId())) {
				tStream = streams.fromJson(load.getResponse("https://api.soundcloud.com/i1/tracks/" + t.getId() + "/streams?client_id=" + clientID)
						, TrackStreams.class);
				String mediaPath = tStream.getHttp_mp3_128_url();
				if (mediaPath != null) {
					if (download.generateMp3(mediaPath, downloadPath, t));
						load.writeToHistory("" + t.getId());
				}
			}
		}
		
		load.closeHistory();
			
		
	}

}
