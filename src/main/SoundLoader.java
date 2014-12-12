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

public class SoundLoader {

	private String user;
	private String path;
	private String clientID;
	
	private ArrayList<String> history;
	private PrintStream output;
	
	public SoundLoader (String user, String path, String clientID) throws IOException {
		this.user = user;
		this.path = path;
		this.clientID = clientID;
		
		// Create a new download history file specific to user
		File f = new File(user);
		if (!f.exists())
			f.createNewFile();
		
		FileReader reader = new FileReader(user);
		BufferedReader buffer = new BufferedReader(reader);
		history = new ArrayList<String>();
		String line = null;
        while ((line = buffer.readLine()) != null) {
            history.add(line);
        }
        buffer.close();
		
		output = new PrintStream(new FileOutputStream(user, true));
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
	
	public boolean isInHistory(String id) {
		return history.contains(id);
	}
	
	public void writeToHistory(String id) {
		output.println(id);
	}
	
	public void closeHistory() {
		output.close();
	}

}
