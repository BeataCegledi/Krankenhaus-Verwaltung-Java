package krankenhaus.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class PatientenHaus {

	private final int zimmerAnzahl;
	private final int anzahlEtagen;
	private final Map<Integer, Zimmer> zimmerList = new TreeMap<>();
	private final List<ZimmerObserver> observerList = new ArrayList<>();

	public PatientenHaus(int anzahlEtagen, int zimmerProEtage) {
		if (anzahlEtagen < 1 || anzahlEtagen > 9)
			throw new IllegalArgumentException("Ungültige Etagenzahl! Sie soll zwischen 1 und 9 liegen!");
		if (zimmerProEtage < 1 || zimmerProEtage > 99)
			throw new IllegalArgumentException("Ungültige Zimmerzahl für die Etage! Sie soll zwischen 1 und 99 liegen!");
		this.anzahlEtagen = anzahlEtagen;
		this.zimmerAnzahl = zimmerProEtage;
		int zimmerNr;
		for (int i = 1; i <= anzahlEtagen; i++) {
			for (int j = 1; j <= zimmerProEtage; j++) {
				zimmerNr = Zimmer.berechneZimmerNr(i, j);
				zimmerList.put(zimmerNr, new Zimmer(zimmerNr));
			}
		}
	}
	
	public void addObserver(ZimmerObserver observer) {
		observerList.add(observer);
	}

	public void removeObserver(ZimmerObserver observer) {
		observerList.remove(observer);
	}

	private void bescheid(ZimmerAenderung zA) {
		for (ZimmerObserver observer : observerList) observer.aktualisiere(zA);
	}

	public Zimmer getZimmer(int zimmerNr) {
		if (zimmerList.containsKey(zimmerNr)) return zimmerList.get(zimmerNr);
		else throw new IllegalArgumentException("Die Zimmernummer " + zimmerNr + " existiert nicht!");
	}

	public void belege(int zimmerNr, Patient p) {
		Zimmer zimmer = getZimmer(zimmerNr);
		Patient vorher = zimmer.getPatient();
		zimmer.belege(p);
		Patient nachher = zimmer.getPatient();
		bescheid(new ZimmerAenderung(zimmer, vorher, nachher));
	}

	public void loeschePatient(int zimmerNr) {
		Zimmer zimmer = getZimmer(zimmerNr);
		Patient vorher = zimmer.getPatient();
		if (vorher == null) return;
		zimmer.loeschePatient();
		Patient nachher = zimmer.getPatient();
		bescheid(new ZimmerAenderung(zimmer, vorher, nachher));
	}

	public int getAnzahlEtagen() {
		return anzahlEtagen;
	}

	public int getZimmerAnzahl() {
		return zimmerAnzahl;
	}

	public Collection<Zimmer> getAlleZimmer() {
		return Collections.unmodifiableCollection(zimmerList.values());
	}

	public void pruefeZimmer(int zimmerNr) throws IllegalArgumentException {
		Zimmer z = getZimmer(zimmerNr);
		if (z.isBelegt()) throw new IllegalArgumentException("Das Zimmer (" + z.getBezeichnung() + ") ist schon besetzt.");
	}
}
