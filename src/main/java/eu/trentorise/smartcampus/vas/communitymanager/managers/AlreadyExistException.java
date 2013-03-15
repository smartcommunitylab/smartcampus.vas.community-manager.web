package eu.trentorise.smartcampus.vas.communitymanager.managers;

public class AlreadyExistException extends Exception {

	private static final long serialVersionUID = -6987347874157636854L;

	public AlreadyExistException() {
		super();
	}

	public AlreadyExistException(String message, Throwable cause) {
		super(message, cause);
	}

	public AlreadyExistException(String message) {
		super(message);
	}

	public AlreadyExistException(Throwable cause) {
		super(cause);
	}

}
