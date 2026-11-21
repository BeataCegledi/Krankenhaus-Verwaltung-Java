package krankenhaus.model;

public enum Abteilung {
	INNERE_MEDIZIN("Innere Medizin", "IN", 1),
	CHIRURGIE("Chirurgie", "CH", 2),
	KARDIOLOGIE("Kardiologie", "KA", 3),
	NEUROLOGIE("Neurologie", "NE", 4),
	ORTHOPAEDIE("Orthopädie", "OR", 5),
	PAEDIATRIE("Pädiatrie", "PA", 6),
	GYNAEKOLOGIE("Gynäkologie", "GY", 7),
	ONKOLOGIE("Onkologie", "ON", 8),
	GERIATRIE("Geriatrie", "GE", 9);

	private final String anzeigeName;
	private final String kuerzel;
	private final int etage;

	Abteilung(String anzeigeName, String kuerzel, int etage) {
		this.anzeigeName = anzeigeName;
		this.kuerzel = kuerzel;
		this.etage = etage;
	}

	public String getKuerzel() {
		return kuerzel;
	}

	@Override
	public String toString() {
		return anzeigeName;
	}

	public static Abteilung fromEtage(int etage) {
		for (Abteilung v : Abteilung.values()) {
			if (v.etage == etage) return v;
		}
		throw new IllegalArgumentException("Es gibt die Etage " + etage + " nicht oder keine Abteilung auf dieser Etage");
	}
}
