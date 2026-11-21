package krankenhaus.app;

import krankenhaus.model.PatientenHaus;
import krankenhaus.repository.MemoryPatientRepository;
import krankenhaus.repository.MySqlPatientRepository;
import krankenhaus.repository.PatientRepository;
import krankenhaus.repository.RepositoryException;
import krankenhaus.service.PatientVerwaltung;

public class KrankenhausFactory {

	private KrankenhausFactory() {
	}

	public static PatientVerwaltung erstelle(int anzahlEtagen, int zimmerProEtage, SpeicherArt art)
			throws RepositoryException {
		PatientenHaus haus = new PatientenHaus(anzahlEtagen, zimmerProEtage);
		PatientRepository repo = switch (art) {
			case MYSQL -> new MySqlPatientRepository();
			case MEMORY -> new MemoryPatientRepository();
		};
		return new PatientVerwaltung(haus, repo);
	}
}
