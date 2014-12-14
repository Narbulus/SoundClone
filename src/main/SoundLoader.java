package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import GsonObjects.Configuration;

public class SoundLoader {

	private String clientID;
	
	private ArrayList<Integer> history;
	private Configuration config;
	
	public SoundLoader (Configuration config, String clientID) throws IOException {
		this.clientID = clientID;
		this.config = config;
		
		history = new ArrayList<Integer>();
		
		if (config.getHistory() != null) {
			for (int i : config.getHistory()) {
				history.add(i);
			}
		}
		
	}
	
	public String getResponse(String urlPath) throws Exception {
		URL url = new URL(urlPath);
		HttpURLConnection connect = (HttpURLConnection) url.openConnection();
		connect.setDoOutput(true);
		connect.setDoInput(true);
		
		connect.setRequestMethod("GET");
		int responseCode = connect.getResponseCode();
		
		String line;
		StringBuffer buffer = new StringBuffer();
		BufferedReader reader = new BufferedReader(new InputStreamReader(connect.getInputStream()));
	
		while ((line = reader.readLine()) != null) {
			buffer.append(line);
		}
		
		return buffer.toString();
				
	}
	
	public String getClientID () {
		return this.clientID;
	}
	
	public boolean isInHistory(int id) {
		return history.contains(id);
	}
	
	public void writeToHistory(int id) {
		history.add(id);
	}
	
	public void closeHistory() {
		// copy list of history to an array and update the configuration
		int[] newHistory = new int[history.size()];
		int p = 0;
		for (Integer i : history) {
			newHistory[p] = i;
		}
		config.setHistory(newHistory);
		
	}

}
