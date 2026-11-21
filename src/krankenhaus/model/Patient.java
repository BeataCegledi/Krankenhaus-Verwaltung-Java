package krankenhaus.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class Patient {

	private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
	private final String patientId;
	private final String name;
	private final String krankheit;
	private final LocalDate geburtsdatum;
	private final Geschlecht geschlecht;
	private final LocalDate aufnahmeDatum;

	public Patient(String patientId, String name, String krankheit, LocalDate geburtsdatum, Geschlecht geschlecht,
			LocalDate aufnahmeDatum) throws IllegalArgumentException {
		if (patientId == null || patientId.isBlank()) throw new IllegalArgumentException("Patient-ID fehlt");
		this.patientId = patientId;
		this.aufnahmeDatum = aufnahmeDatum;
		this.name = name;
		this.krankheit = krankheit;
		this.geburtsdatum = geburtsdatum;
		this.geschlecht = geschlecht;
	}

	public Patient(String patientId, String name, String krankheit, LocalDate geburtsdatum, Geschlecht geschlecht)
			throws IllegalArgumentException {
		this(patientId, name, krankheit, geburtsdatum, geschlecht, LocalDate.now());
	}

	public LocalDate getAufnahmeDatum() {
		return aufnahmeDatum;
	}

	public String getPatientId() {
		return patientId;
	}

	public String getName() {
		return name;
	}

	public String getKrankheit() {
		return krankheit;
	}

	public LocalDate getGeburtsdatum() {
		return geburtsdatum;
	}

	public Geschlecht getGeschlecht() {
		return geschlecht;
	}

	@Override
	public int hashCode() {
		return Objects.hash(geburtsdatum, aufnahmeDatum, geschlecht, krankheit, name, patientId);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Patient other = (Patient) obj;
		return Objects.equals(geburtsdatum, other.geburtsdatum) && Objects.equals(aufnahmeDatum, other.aufnahmeDatum)
				&& geschlecht == other.geschlecht && Objects.equals(krankheit, other.krankheit)
				&& Objects.equals(name, other.name) && Objects.equals(patientId, other.patientId);
	}

	public String getPatientInfo() {
		return "Patient ID: " + patientId + "<br>"
				+ "Name: " + name + "<br>"
				+ "Krankheit: " + krankheit + "<br>"
				+ "Geburtsdatum: " + (geburtsdatum == null ? "unbekannt" : geburtsdatum.format(FORMAT)) + "<br>"
				+ "Geschlecht: " + geschlecht + "<br>"
				+ "Aufgenommen am: " + aufnahmeDatum.format(FORMAT);
	}
}
