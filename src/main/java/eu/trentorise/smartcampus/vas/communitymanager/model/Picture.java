package eu.trentorise.smartcampus.vas.communitymanager.model;

public class Picture {
	private String path;
	private String id;

	public Picture(String path, String id) {
		this.path = path;
		this.id = id;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
