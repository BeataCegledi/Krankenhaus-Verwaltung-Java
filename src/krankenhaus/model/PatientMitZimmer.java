package krankenhaus.model;

public record PatientMitZimmer(int etage, int zimmer, Patient patient) {

	public int zimmerNr() {
		return etage * 100 + zimmer;
	}
}
