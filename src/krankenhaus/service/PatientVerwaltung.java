package krankenhaus.service;

import java.util.ArrayList;
import java.util.List;

import krankenhaus.model.Patient;
import krankenhaus.model.PatientMitZimmer;
import krankenhaus.model.PatientenHaus;
import krankenhaus.model.Zimmer;
import krankenhaus.repository.PatientRepository;
import krankenhaus.repository.RepositoryException;

public class PatientVerwaltung {

	private final PatientenHaus haus;
	private final PatientRepository repo;

	public PatientVerwaltung(PatientenHaus haus, PatientRepository repo) {
		this.haus = haus;
		this.repo = repo;
	}

	public PatientenHaus getHaus() {
		return haus;
	}

	public void patientAnmelden(PatientMitZimmer pmz) throws IllegalArgumentException, RepositoryException {
		haus.pruefeZimmer(pmz.zimmerNr());
		doppelterPatient(pmz.patient().getPatientId());
		repo.speichern(pmz);
		haus.belege(pmz.zimmerNr(), pmz.patient());
	}

	private void doppelterPatient(String patientId) throws IllegalArgumentException {
		Zimmer altZ = findePatientById(patientId);
		if (altZ != null) throw new IllegalArgumentException("Patient " + altZ.getPatient().getName() + " ("
				+ altZ.getPatient().getPatientId() + ") liegt schon in Zimmer " + altZ.getBezeichnung());
	}

	public void patientAendern(PatientMitZimmer alt, PatientMitZimmer neu)
			throws RepositoryException, IllegalArgumentException {
		if (alt.zimmerNr() != neu.zimmerNr()) haus.pruefeZimmer(neu.zimmerNr());
		repo.aendern(neu);
		haus.loeschePatient(alt.zimmerNr());
		haus.belege(neu.zimmerNr(), neu.patient());
	}

	public void patientEntlassen(int zimmerNr) throws RepositoryException {
		Patient p = haus.getZimmer(zimmerNr).getPatient();
		if (p == null) return;
		repo.entlassen(p.getPatientId());
		haus.loeschePatient(zimmerNr);
	}

	public void alleLaden() throws RepositoryException, IllegalArgumentException {
		for (PatientMitZimmer pz : repo.alleLaden()) {
			doppelterPatient(pz.patient().getPatientId());
			haus.belege(pz.zimmerNr(), pz.patient());
		}
	}

	public Zimmer findePatientById(String patientId) {
		for (Zimmer z : haus.getAlleZimmer()) {
			if (z.getPatient() != null && z.getPatient().getPatientId().equalsIgnoreCase(patientId)) return z;
		}
		return null;
	}

	public Zimmer findePatientByName(String patientName) {
		for (Zimmer z : haus.getAlleZimmer()) {
			if (z.getPatient() != null && patientName.equalsIgnoreCase(z.getPatient().getName())) return z;
		}
		return null;
	}

	public List<Zimmer> freieZimmer() {
		List<Zimmer> fz = new ArrayList<>();
		for (Zimmer z : haus.getAlleZimmer()) {
			if (!z.isBelegt()) fz.add(z);
		}
		return fz;
	}
}
