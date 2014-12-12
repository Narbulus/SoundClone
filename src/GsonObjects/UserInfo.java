package GsonObjects;

public class UserInfo {

	private int id;
	private int public_favorites_count;
	
	public UserInfo (int id, int public_favorites_count) {
		this.id = id;
		this.public_favorites_count = public_favorites_count;
	}
	
	public int getId() {
		return id;
	}
	
	public int getFavoritesCount() {
		return public_favorites_count;
	}
	
}
