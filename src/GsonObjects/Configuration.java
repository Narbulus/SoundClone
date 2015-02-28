package GsonObjects;

/**
 * A Gson object representing the current configuration
 * of the program. Saved to file after each download session.
 * Contains previous download history so songs aren't
 * redownloaded.
 * @author Spencer Anderson
 *
 */
public class Configuration {
	
	// The soundcloud url-username of this configuration
	private String username;
	// The path last used to download songs to
	private String downloadPath;
	// the integer track-ids of previously downloaded songs
	private int[] history;
	
	/**
	 * Instantiates a new Configuration object related to the given 
	 * @param username
	 * @param downloadPath
	 * @param history
	 */
	public Configuration (String username, String downloadPath, int[] history) {
		this.username = username;
		this.downloadPath = downloadPath;
		this.history = history;
	}

	public int[] getHistory() {
		return history;
	}

	public void setHistory(int[] history) {
		this.history = history;
	}

	public String getUsername() {
		return username;
	}

	public String getDownloadPath() {
		return downloadPath;
	}
	
	public void setDownloadPath(String downloadPath) {
		this.downloadPath = downloadPath;
	}

}
