package krankenhaus.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import krankenhaus.model.PatientDTO;

public class MemoryPatientRepository implements PatientRepository {

	private final Map<String, PatientDTO> daten = new HashMap<>();

	@Override
	public void speichern(PatientDTO pmz) throws RepositoryException {
		String id = pmz.patient().getPatientId();
		if (daten.containsKey(id)) throw new RepositoryException("Patient-ID ist schon gespeichert.", null);
		daten.put(id, pmz);
	}

	@Override
	public void aendern(PatientDTO alt, PatientDTO neu) {
		String id = neu.patient().getPatientId();
		daten.put(id, neu);
	}

	@Override
	public void entlassen(String patientId) {
		daten.remove(patientId);
	}

	@Override
	public List<PatientDTO> alleLaden() {
		return new ArrayList<>(daten.values());
	}
}