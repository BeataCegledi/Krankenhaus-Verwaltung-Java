package krankenhaus.repository;

import java.util.List;

import krankenhaus.model.PatientDTO;

public interface PatientRepository {

	void speichern(PatientDTO pmz) throws RepositoryException;

	void aendern(PatientDTO alt, PatientDTO neu) throws RepositoryException;

	void entlassen(String patientId) throws RepositoryException;

	List<PatientDTO> alleLaden() throws RepositoryException, IllegalArgumentException;

}