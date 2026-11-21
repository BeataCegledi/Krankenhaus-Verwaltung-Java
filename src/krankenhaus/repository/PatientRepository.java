package krankenhaus.repository;

import java.util.List;

import krankenhaus.model.PatientMitZimmer;

public interface PatientRepository {

	void speichern(PatientMitZimmer pmz) throws RepositoryException;

	void aendern(PatientMitZimmer pmz) throws RepositoryException;

	void entlassen(String patientId) throws RepositoryException;

	List<PatientMitZimmer> alleLaden() throws RepositoryException, IllegalArgumentException;
}
