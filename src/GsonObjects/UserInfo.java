package GsonObjects;

public class UserInfo {

	private int id;
	private int public_favorites_count;
	private String avatar_url;

	public UserInfo (int id, int public_favorites_count, String avatar_url) {
		this.id = id;
		this.public_favorites_count = public_favorites_count;
		this.avatar_url = avatar_url;
	}
	
	public int getId() {
		return id;
	}
	
	public int getFavoritesCount() {
		return public_favorites_count;
	}
	
	public String getAvatarURL() {
		return avatar_url;
	}
	
}
