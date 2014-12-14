package main;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Scanner;
import java.io.*;
import java.lang.reflect.Type;

import GsonObjects.*;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.ID3v24Tag;
import com.mpatric.mp3agic.Mp3File;

public class DownloadLikes {

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception {
	
		@SuppressWarnings("resource")
		Scanner config = new Scanner(new File("config"));
		String user = config.nextLine().replace(".", "-");
		String downloadPath = config.nextLine();
		String clientID = config.nextLine();
		
		Configuration currentConfig = null;
		
		ArrayList<Configuration> configs = new ArrayList<>();
		
		// Load past configurations from config file's json
		while (config.hasNext()) {
			String nextConfig = config.nextLine();
			Configuration newConfig = new Gson().fromJson(nextConfig, Configuration.class);
			if (newConfig.getUsername().equals(user))
				currentConfig = newConfig;
			configs.add(newConfig);
		}
		
		if (currentConfig == null) {
			currentConfig = new Configuration(user, downloadPath, null);
			configs.add(currentConfig);
		}
		
		
		// Strip the id3 tag from the template mp3
		Mp3File mp3file = new Mp3File("sampletag.mp3");
		ID3v2 template;
		if (mp3file.hasId3v2Tag()) {
		  template = mp3file.getId3v2Tag();
		}else{
			template = new ID3v24Tag();
		}
		
		SoundLoader load = new SoundLoader(currentConfig, clientID);
		
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
		Mp3Downloader download = new Mp3Downloader(template, currentConfig);
		for (TrackInfo t : list) {
			if (!load.isInHistory(t.getId())) {
				tStream = streams.fromJson(load.getResponse("https://api.soundcloud.com/i1/tracks/" + t.getId() + "/streams?client_id=" + clientID)
						, TrackStreams.class);
				String mediaPath = tStream.getHttp_mp3_128_url();
				if (mediaPath != null) {
					if (download.generateMp3(mediaPath, t));
						load.writeToHistory(t.getId());
				}
			}
		}
		
		// update the current configuration's history
		load.closeHistory();
		
		// write the configurations to file
		PrintStream output = new PrintStream(new File("config"));
		
		output.println(user);
		output.println(downloadPath);
		output.println(clientID);
		
		Gson gson = new Gson();
		for (Configuration c : configs) {
			output.println(gson.toJson(c));
		}
		
		output.close();
			
		
	}

}
