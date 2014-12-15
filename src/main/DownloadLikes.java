package main;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;
import javax.swing.text.BadLocationException;

import GsonObjects.Configuration;
import GsonObjects.RedirectResponse;
import GsonObjects.TrackInfo;
import GsonObjects.TrackStreams;
import GsonObjects.UserInfo;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.ID3v22Tag;

public class DownloadLikes {

	private String clientID;
	private int maxDuration;
	private ArrayList<Configuration> configs;
	private ArrayList<TrackInfo> likes;
	private Configuration currentConfig;
	private ID3v2 template;
	private SoundLoader load;
	private Boolean threadRunning;
	private String tempDir;
	private String defaultDownload;
	
	public DownloadLikes() throws Exception {
		// Create download locations if nonexistant
		String workingDirectory;
		String OS = (System.getProperty("os.name")).toUpperCase();
		if (OS.contains("WIN"))
		{
		    workingDirectory = System.getenv("AppData");
		} else {
		    workingDirectory = System.getProperty("user.home");
		    workingDirectory += "/Library/Application Support";
		}
		
		tempDir = workingDirectory + "/SoundClone";
		
		File tempFile = new File(tempDir);
		tempFile.mkdirs();
		
		defaultDownload = System.getProperty("user.home");
		
		threadRunning = false;
		BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/resources/config")));
		File oldConfig = new File(tempDir + "/config");
		Scanner config;
		if (oldConfig.exists())
			config = new Scanner(oldConfig);
		else
			config = new Scanner(reader);
		clientID = config.nextLine();
		maxDuration = Integer.parseInt(config.nextLine());
		
		configs = new ArrayList<>();
		currentConfig = null;
		
		// Load past configurations from config file's json
		while (config.hasNext()) {
			String nextConfig = config.nextLine();
			Configuration newConfig = new Gson().fromJson(nextConfig, Configuration.class);
			configs.add(newConfig);
		}
		
		config.close();

		// Load the template id3 tag from resource file
		InputStream is = getClass().getResourceAsStream("/resources/tagdata");
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();

		int nRead;
		byte[] data = new byte[16384];

		while ((nRead = is.read(data, 0, data.length)) != -1) {
		  buffer.write(data, 0, nRead);
		}

		buffer.flush();

		template = new ID3v22Tag(buffer.toByteArray());
		
	}
	
	/**
	 * Called when the user either enters or selects a new
	 * user to be loaded. Loads the tracklist for the user
	 * 
	 * @param user The new user's username
	 * @param gui 
	 * @throws Exception 
	 * @throws JsonSyntaxException 
	 * @return Returns a new status for the program
	 */
	public void updateUser(final String user, final SoundCloneGUI gui) throws JsonSyntaxException, Exception {
		for (Configuration c : configs) {
			if (c.getUsername().equals(user))
				currentConfig = c;
		}
		
		if (currentConfig == null || user != currentConfig.getUsername()) {
			currentConfig = new Configuration(user, defaultDownload, null);
			configs.add(currentConfig);
		}
		
		try {
			load = new SoundLoader(currentConfig, clientID);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		gui.updateStatus("Loading user's likes");
		
		// Dispatch worker to download songs in background and update status
		SwingWorker<String, String> worker = new SwingWorker<String, String>() {

			@SuppressWarnings("unchecked")
			@Override
			protected String doInBackground() throws JsonSyntaxException, Exception {
				String redirect = "";
				try {
					redirect = load.getResponse("http://api.soundcloud.com/resolve.json?url=http://soundcloud.com/" + user
								+ "&client_id=" + clientID);
				} catch (Exception e) {
					gui.updateStatus("Username '" + user + "' not found", SoundCloneGUI.StatusType.WARNING);
					gui.unlockControls();
					threadRunning = false;
					e.printStackTrace();
				}
				
				RedirectResponse response = new Gson().fromJson(redirect, RedirectResponse.class);
				UserInfo info = new Gson().fromJson(load.getResponse(response.getLocation()), UserInfo.class);
				
				Type listType = new TypeToken<ArrayList<TrackInfo>>() {
				}.getType();
				
				likes = new ArrayList<>();
				
				for (int i = 0; i < info.getFavoritesCount(); i += 50) {
					String partLikes = load.getResponse("http://api.soundcloud.com/users/" + info.getId() + "/favorites.json?client_id=" + clientID + "&offset=" + i);
					likes.addAll((Collection<? extends TrackInfo>) new Gson().fromJson(partLikes, listType));
				}
				
				for (TrackInfo t : likes) {
					if (load.isInHistory(t.getId()) || t.getDuration() > maxDuration)
						t.setDownload(false);
				}
				
				int diff = (likes.size() - load.getHistoryLength());
				if (diff < 0) 
					diff = 0;
				return likes.size() + " tracks found!" + "\n" + diff + " new tracks";
			}
			
			@Override
			protected void done() {
				try {
					gui.updateStatus(get());
					gui.unlockControls();
					gui.resetTable();
					for (TrackInfo t : likes) {
						int seconds = t.getDuration() / 1000;
						int minutes = seconds / 60;
						Object[] row = new Object[] { t.getTitle(), minutes + ":" + seconds % 60, t.getDownload() };
						gui.addTableRow(row);
					}
					threadRunning = false;
				} catch (InterruptedException | ExecutionException | BadLocationException e) {
					e.printStackTrace();
				}
			}
		
		};
		
		threadRunning = true;
		worker.execute();
		
	}

	/**
	 * Called when the user presses start
	 * @param user 
	 * @param downloadPath
	 * @throws Exception 
	 * @throws JsonSyntaxException 
	 */
	public void downloadTracks(String user, String downloadPath, final SoundCloneGUI gui) throws JsonSyntaxException, Exception {
		// If a new path is specified, clear history on config so new files are downloaded
		if (user.equals(currentConfig.getUsername()) && !downloadPath.equals(currentConfig.getDownloadPath())) {
			currentConfig.setDownloadPath(downloadPath);
			load.clearHistory();
		}
		gui.updateStatus("Intializing downloads", SoundCloneGUI.StatusType.PROCESS);
		// Dispatch worker to download songs in background and update status
		SwingWorker<String, String> worker = new SwingWorker<String, String>() {

			@Override
			protected String doInBackground() throws Exception {
				Gson gson = new Gson();
				TrackStreams tStream;
				Mp3Downloader download = new Mp3Downloader(template, currentConfig, tempDir);
				int i = 1;
				int downloads = 0;
				for (TrackInfo t : likes) {
					if (threadRunning) {
						if (t.getDownload()) {
							publish(i + "/" + likes.size() + " - " + t.getTitle());
							tStream = gson.fromJson(load.getResponse("https://api.soundcloud.com/i1/tracks/" + t.getId() + "/streams?client_id=" + clientID)
									, TrackStreams.class);
							String mediaPath = tStream.getHttp_mp3_128_url();
							if (mediaPath != null) {
								if (t.getArtworkURL() == null) {
									// Load uploader profile picture url for track image
									UserInfo uploader = gson.fromJson(load.getResponse("https://api.soundcloud.com/users/" + t.getUserId() + ".json?client_id=" + clientID )
											, UserInfo.class);
									t.setArtworkURL(uploader.getAvatarURL());
								}
								if (download.generateMp3(mediaPath, t)) {
									load.writeToHistory(t.getId());
									downloads++;
								}
							}
						}
						i++;
					}
				}
			
				// update the current configuration's history
				load.closeHistory();
				
				// write the configurations to file
				PrintStream output = new PrintStream(tempDir + "/config");
				
				output.println(clientID);
				output.println(maxDuration);
				
				for (Configuration c : configs) {
					output.println(gson.toJson(c));
				}
				
				output.flush();
				output.close();
				return downloads + " songs downloaded successfully!";
			}
			
			@Override
			protected void done() {
				try {
					gui.updateStatus(get(), SoundCloneGUI.StatusType.COMPLETE);
					gui.unlockControls();
					threadRunning = false;
				} catch (InterruptedException | ExecutionException | BadLocationException e) {
					e.printStackTrace();
				}
			}
			
			@Override
			protected void process(List<String> chunks) {
				String recent = chunks.get(chunks.size() - 1);
				try {
					gui.updateStatus(recent, SoundCloneGUI.StatusType.PROCESS);
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
			}
		};
		
		threadRunning = true;
		worker.execute();
	}
	
	/**
	 * Checks if the given path conflicts with the download
	 * path of the current configuration
	 * @param downloadPath The new download path
	 * @return true if the downloadPath differs from the
	 * 	configuration's path 
	 */
	public boolean isNewPath(String downloadPath) {
		// If a new path is specified, clear history on config so new files are downloaded
		return !downloadPath.equals(currentConfig.getDownloadPath()) && load.getHistoryLength() > 0;
	}
	/**
	 * Returns the names of the users for which a
	 * configuration exists from prior runs
	 * @return
	 */
	public String[] getConfigNames() {
		String[] names = new String[configs.size()];
		int i = 0;
		for (Configuration c : configs) {
			names[i] = c.getUsername();
			i++;
		}
		return names;
	}
	
	/**
	 * Returns the username of the active selected configuration
	 * @return
	 */
	public String getCurrentUser() {
		if (currentConfig != null)
			return currentConfig.getUsername();
		return null;
	}

	public String getDownloadPath() {
		if (currentConfig != null)
			return currentConfig.getDownloadPath();
		return defaultDownload;
	}
	
	public Boolean isThreadRunning() {
		return threadRunning;
	}
	
	public void stopThread() {
		threadRunning = false;
	}
	
	public void toggleDownload(int index) {
		likes.get(index).setDownload(!likes.get(index).getDownload());
	}
	
}
