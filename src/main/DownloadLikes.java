package main;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;
import java.io.*;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import GsonObjects.*;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class DownloadLikes {

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception {
	
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
		
		// Create a new download history file specific to user
		File f = new File(user);
		if (!f.exists())
			f.createNewFile();
		
		FileReader reader = new FileReader(user);
		BufferedReader buffer = new BufferedReader(reader);
		List<String> history = new ArrayList<String>();
		String line = null;
        while ((line = buffer.readLine()) != null) {
            history.add(line);
        }
        buffer.close();
		
		PrintStream historyOutput = new PrintStream(new FileOutputStream(user, true)); 
		
		Gson streams = new Gson();
		TrackStreams tStream;
		for (TrackInfo t : list) {
			if (!history.contains(t.getId())) {
				tStream = streams.fromJson(load.getResponse("https://api.soundcloud.com/i1/tracks/" + t.getId() + "/streams?client_id=" + clientID)
						, TrackStreams.class);
				if (tStream.getHttp_mp3_128_url() != null) {
					URL website = new URL(tStream.getHttp_mp3_128_url());
					ReadableByteChannel rbc = Channels.newChannel(website.openStream());
					String fuzzTitle = t.getTitle();
					fuzzTitle = fuzzTitle.replaceAll("[<>?*:|/\\\\]", " ");
					fuzzTitle.replaceAll("\"", "'");
					FileOutputStream fos = new FileOutputStream(downloadPath + "\\" + fuzzTitle + ".mp3");
					fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
					fos.close();
					historyOutput.println(t.getId());
				}
			}
		}
		
		historyOutput.close();
			
		
	}

}
