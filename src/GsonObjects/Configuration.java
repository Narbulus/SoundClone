package GsonObjects;

public class Configuration {
	
	private String username;
	private String downloadPath;
	private int[] history;
	
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

}
