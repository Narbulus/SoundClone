package GsonObjects;

public class TrackInfo {
	
	private String kind;
	private int id;
	private String title;
	private String stream_url;
	
	public TrackInfo (String kind, int id, String title, String stream_url) {
		this.kind = kind;
		this.id = id;
		this.title = title;
		this.stream_url = stream_url;
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
	
	
}
