package krankenhaus.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import krankenhaus.model.PatientMitZimmer;

public class MemoryPatientRepository implements PatientRepository {

	private final Map<String, PatientMitZimmer> daten = new HashMap<>();

	@Override
	public void speichern(PatientMitZimmer pmz) throws RepositoryException {
		String id = pmz.patient().getPatientId();
		if (daten.containsKey(id)) throw new RepositoryException("Patient-ID ist schon gespeichert.", null);
		daten.put(id, pmz);
	}

	@Override
	public void aendern(PatientMitZimmer pmz) {
		String id = pmz.patient().getPatientId();
		daten.put(id, pmz);
	}

	@Override
	public void entlassen(String patientId) {
		daten.remove(patientId);
	}

	@Override
	public List<PatientMitZimmer> alleLaden() {
		return new ArrayList<>(daten.values());
	}
}
