package GsonObjects;

public class RedirectResponse {
	
	private String status;
	private String location;
	
	public RedirectResponse(String status, String location) {
		this.status = status;
		this.location = location;
	}
	
	public String getStatus() {
		return status;
	}

	public String getLocation() {
		return location;
	}
	
}
