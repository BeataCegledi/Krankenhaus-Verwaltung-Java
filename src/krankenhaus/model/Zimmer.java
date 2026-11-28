package krankenhaus.model;

public class Zimmer {

	private final int zimmerNr;
	private Patient patient;

	public Zimmer(int zimmerNr) throws IllegalArgumentException {
		int etage = zimmerNr / 100;
		int nrAufEtage = zimmerNr % 100;
		if (etage < 1 || etage > 9 || nrAufEtage < 1) throw new IllegalArgumentException(
				"Ungültige Zimmernummer: " + zimmerNr + " (erlaubt: 101–999, Nr. auf Etage 01–99)");
		this.zimmerNr = zimmerNr;
	}

	public int getZimmerNr() {
		return zimmerNr;
	}
	
	public static int berechneZimmerNr(int etage, int nrAufEtage) {
		return etage * 100 + nrAufEtage;
	}

	public int getEtage() {
		return zimmerNr / 100;
	}

	public int getNrAufEtage() {
		return zimmerNr % 100;
	}

	public Abteilung getAbteilung() {
		return Abteilung.fromEtage(getEtage());
	}

	public String getBezeichnung() {
		return getAbteilung().getKuerzel() + "-" + zimmerNr;
	}

	public Patient getPatient() {
		return patient;
	}

	public boolean isBelegt() {
		return patient != null;
	}

	void belege(Patient p) throws IllegalArgumentException {
		if (p == null) throw new IllegalArgumentException("Patient darf nicht null sein!");
		if (isBelegt()) throw new IllegalArgumentException("Das Zimmer (" + getBezeichnung() + ") ist schon besetzt");
		this.patient = p;
	}

	void loeschePatient() {
		patient = null;
	}

	@Override
	public String toString() {
		return getBezeichnung() + (isBelegt() ? " – " + patient.getName() : " – frei");
	}

	public String getZimmerInfo() {
		String patientText = patient == null ? "Zimmer frei" : patient.getPatientInfo();
		return "<html>" + getAbteilung() + "<br>"
				+ getEtage() + ". Etage / " + getNrAufEtage() + ". Zimmer<br>"
				+ patientText
				+ "</html>";
	}
}
