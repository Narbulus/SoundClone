package main;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class SoundLoader {

	private String user;
	private String path;
	private String clientID;
	
	public SoundLoader (String user, String path, String clientID) {
		this.user = user;
		this.path = path;
		this.clientID = clientID;
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

}
