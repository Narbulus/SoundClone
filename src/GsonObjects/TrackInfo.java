package GsonObjects;

public class TrackInfo {
	
	private String kind;
	private int id;
	private String title;
	private String stream_url;
	private String artwork_url;
	private int duration;
	private int user_id;
	
	public TrackInfo (String kind, int id, String title, String stream_url, String artwork_url, int duration, int user_id) {
		this.kind = kind;
		this.id = id;
		this.title = title;
		this.stream_url = stream_url;
		this.artwork_url = artwork_url;
		this.duration = duration;
		this.user_id = user_id;
	}

	public String getKind() {
		return kind;
	}

	public int getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}
	
	public String getStreamURL() {
		return stream_url;
	}
	
	public String getArtworkURL() {
		return artwork_url;
	}
	
	public void setArtworkURL(String url) {
		this.artwork_url = url;
	}
	
	public int getDuration() {
		return duration;
	}
	
	public int getUserId() {
		return user_id;
	}
	
	
}
