package krankenhaus.repository;

public class RepositoryException extends Exception {

	private static final long serialVersionUID = 1L;

	public RepositoryException(String meldung, Throwable ursache) {
		super(meldung, ursache);
	}
}
