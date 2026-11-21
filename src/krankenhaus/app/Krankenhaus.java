package krankenhaus.app;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;

import krankenhaus.model.PatientMitZimmer;
import krankenhaus.model.PatientenHaus;
import krankenhaus.model.Zimmer;
import krankenhaus.repository.RepositoryException;
import krankenhaus.service.PatientVerwaltung;
import krankenhaus.view.MatrixPanel;
import krankenhaus.view.PatientFormular;

public class Krankenhaus extends JFrame {

	private static final long serialVersionUID = 1L;
	private MatrixPanel mp;
	private PatientVerwaltung verwaltung;

	public Krankenhaus(PatientVerwaltung verwaltung) throws IllegalArgumentException {
		this.verwaltung = verwaltung;
		PatientenHaus haus = verwaltung.getHaus();
		getContentPane().setLayout(new BorderLayout(0, 0));
		mp = new MatrixPanel(haus, this::zimmerGeklickt);
		getContentPane().add(mp, BorderLayout.CENTER);
		addToolBar();
		setSize(new Dimension(75 * haus.getZimmerAnzahl(), 150 * haus.getAnzahlEtagen()));
		setLocationRelativeTo(null);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setTitle("Mein Krankenhaus Projekt");
	}

	private void addToolBar() {
		JToolBar toolBar = new JToolBar();
		getContentPane().add(toolBar, BorderLayout.NORTH);

		JButton sucheById = new JButton("Suchen mit ID");
		sucheById.addActionListener(e -> {
			Zimmer z = frageIdUndSuche();
			if (z != null) zimmerGeklickt(z.getZimmerNr());
		});
		toolBar.add(sucheById);

		JButton sucheByName = new JButton("Suchen mit Name");
		sucheByName.addActionListener(e -> {
			String name = JOptionPane.showInputDialog(this, "Gib bitte den Patientennamen ein: ");
			if (name == null || name.isBlank()) return;
			name = name.strip();
			Zimmer z = verwaltung.findePatientByName(name);
			if (z == null) zeigeFehler(this, "Nicht gefunden", "Es gibt keinen Patienten mit dem Namen " + name);
			else zimmerGeklickt(z.getZimmerNr());
		});
		toolBar.add(sucheByName);

		JButton entlassen = new JButton("Patient entlassen");
		entlassen.addActionListener(e -> entlassen(frageIdUndSuche()));
		toolBar.add(entlassen);

		JButton aufnehmen = new JButton("Patient aufnehmen");
		aufnehmen.addActionListener(
				e -> frageBogen("Neuen Patienten aufnehmen", new PatientFormular(verwaltung.freieZimmer())));
		toolBar.add(aufnehmen);

		JButton aendern = new JButton("Patientdaten ändern");
		aendern.addActionListener(e -> aendern(frageIdUndSuche()));
		toolBar.add(aendern);
	}

	private Zimmer frageIdUndSuche() {
		String id = JOptionPane.showInputDialog(this, "Gib bitte die Patient-ID ein: ");
		if (id == null || id.isBlank()) return null;
		id = id.strip();
		Zimmer z = verwaltung.findePatientById(id);
		if (z == null) zeigeFehler(this, "Nicht gefunden", "Es gibt keinen Patienten mit der ID " + id);
		return z;
	}

	private void aendern(Zimmer z) {
		if (z != null) {
			PatientMitZimmer pmzAlt = new PatientMitZimmer(z.getEtage(), z.getNrAufEtage(), z.getPatient());
			PatientFormular formular = new PatientFormular(verwaltung.freieZimmer(), pmzAlt);
			frageBogen("Patientdaten ändern", formular);
		}
	}

	private void entlassen(Zimmer z) {
		if (z != null) {
			try {
				int antwort = JOptionPane.showConfirmDialog(this,
						"Patient " + z.getPatient().getName() + " wirklich entlassen und endgültig löschen?",
						"Sicherheitsfrage", JOptionPane.YES_NO_OPTION);
				if (antwort == JOptionPane.YES_OPTION) verwaltung.patientEntlassen(z.getZimmerNr());
			} catch (RepositoryException ex) {
				zeigeFehler(this, "Datenbankfehler", "Entlassen fehlgeschlagen: " + ex.getMessage());
			}
		}
	}

	private void zimmerGeklickt(int zimmerNr) {
		Zimmer z = verwaltung.getHaus().getZimmer(zimmerNr);
		if (!z.isBelegt()) {
			frageBogen("Neuen Patienten in Zimmer " + z.getBezeichnung() + " aufnehmen",
					new PatientFormular(verwaltung.freieZimmer(), z));
		} else {
			String[] optionen = { "Ändern", "Entlassen", "Schließen" };
			int wahl = JOptionPane.showOptionDialog(this, z.getZimmerInfo(), "Daten des Zimmers: " + z.getBezeichnung(),
					JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, optionen, optionen[2]);
			switch (wahl) {
				case 0 -> aendern(z);
				case 1 -> entlassen(z);
			}
		}
	}

	private void frageBogen(String title, PatientFormular formular) {
		while (true) {
			int antwort = JOptionPane.showConfirmDialog(this, formular, title, JOptionPane.OK_CANCEL_OPTION);
			if (antwort != JOptionPane.OK_OPTION) return;
			try {
				PatientMitZimmer alt = formular.getPmz();
				PatientMitZimmer neu = formular.getPatientMitZimmer();
				if (alt == null) verwaltung.patientAnmelden(neu);
				else verwaltung.patientAendern(alt, neu);
				return;
			} catch (IllegalArgumentException ex) {
				ex.printStackTrace();
				zeigeFehler(this, "Ungültige Eingabe", ex.getMessage());
			} catch (RepositoryException ex) {
				ex.printStackTrace();
				zeigeFehler(this, "Speichern nicht möglich", ex.getMessage());
			}
		}
	}

	private static void zeigeFehler(Component parent, String title, String message) {
		JOptionPane.showMessageDialog(parent, message, title, JOptionPane.ERROR_MESSAGE);
	}

	public static void main(String[] args) {
		try {
			PatientVerwaltung v = KrankenhausFactory.erstelle(4, 20, SpeicherArt.MYSQL);
			Krankenhaus kh = new Krankenhaus(v);
			v.alleLaden();
			v.getHaus().addObserver(o -> System.out.println(o));
			kh.setVisible(true);
		} catch (RepositoryException ex) {
			ex.printStackTrace();
			zeigeFehler(null, "Datenbankfehler", ex.getMessage());
		} catch (IllegalArgumentException ex) {
			ex.printStackTrace();
			zeigeFehler(null, "Fehlerhafte Daten in der Datenbank", ex.getMessage());
		}
	}
}
