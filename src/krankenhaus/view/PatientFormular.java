package krankenhaus.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.github.lgooddatepicker.components.DatePicker;

import krankenhaus.model.Geschlecht;
import krankenhaus.model.Patient;
import krankenhaus.model.PatientDTO;
import krankenhaus.model.Zimmer;

public class PatientFormular extends JPanel {

	private static final long serialVersionUID = 1L;
	private static final int MAX_ALTER = 120;
	private static final int MIN_ALTER = 14;

	private JTextField idField;
	private JTextField nameField;
	private JTextField diagnoseField;
	private DatePicker geburtsDatum;
	private JComboBox<Geschlecht> geschlechtBox;
	private JComboBox<Integer> etageBox;
	private JComboBox<Integer> zimmerBox;
	private List<Zimmer> freieZimmer;
	private JLabel hinweisLabel;
	private PatientDTO pmz;


	public PatientFormular(List<Zimmer> freieZList, PatientDTO pmz) throws IllegalArgumentException {
		this(freieZList);
		if (pmz == null) throw new IllegalArgumentException(
				"Für Änderungen wird ein vorhandener Patient benötigt, und darf nicht null sein");
		this.pmz = pmz;
		idField.setText(pmz.patient().getPatientId());
		idField.setEditable(false);
		nameField.setText(pmz.patient().getName());
		diagnoseField.setText(pmz.patient().getKrankheit());
		geburtsDatum.setDate(pmz.patient().getGeburtsdatum());
		geschlechtBox.setSelectedItem(pmz.patient().getGeschlecht());
		geschlechtBox.setEnabled(false);
		geburtsDatum.setEnabled(false);
		hinweisLabel.setText(grundHinweis());
	}

	public PatientFormular(List<Zimmer> freieZList, Zimmer vorgewaehlt) {
		this(freieZList);
		if (vorgewaehlt == null || !freieZList.contains(vorgewaehlt)) throw new IllegalArgumentException(
				"Vorgewähltes Zimmer muss frei sein und darf nicht null sein");
		etageBox.setSelectedItem(vorgewaehlt.getEtage());
		zimmerBox.setSelectedItem(vorgewaehlt.getNrAufEtage());
		etageBox.setEnabled(false);
		zimmerBox.setEnabled(false);
	}

	public PatientFormular(List<Zimmer> freieZList) {
		this.freieZimmer = freieZList;
		initGUI();
		Set<Integer> etagen = new TreeSet<>();
		for (Zimmer z : freieZList) etagen.add(z.getEtage());
		for (int e : etagen) etageBox.addItem(e);
		etageBox.setSelectedIndex(-1);
		zimmerBox.setEnabled(false);
		etageBox.addActionListener(e -> zimmerNeuLaden());
	}
	
	public PatientDTO getPmz() {
		return pmz;
	}

	private void initGUI() {
		setLayout(new GridLayout(0, 2, 0, 0));

		addLabel("Patient - ID");
		idField = new JTextField(10);
		add(idField);

		addLabel("Patient Name");
		nameField = new JTextField(10);
		add(nameField);

		addLabel("Diagnose");
		diagnoseField = new JTextField(10);
		add(diagnoseField);

		addLabel("Geburtsdatum");
		geburtsDatum = new DatePicker();
		add(geburtsDatum);

		addLabel("Geschlecht");
		geschlechtBox = new JComboBox<>(Geschlecht.values());
		add(geschlechtBox);

		addLabel("Etage");
		etageBox = new JComboBox<>();
		add(etageBox);

		addLabel("Zimmer");
		zimmerBox = new JComboBox<>();
		add(zimmerBox);

		add(new JLabel());
		hinweisLabel = new JLabel(" ");
		hinweisLabel.setForeground(Color.RED);
		add(hinweisLabel);
	}

	private void addLabel(String text) {
		JLabel label = new JLabel(text);
		label.setFont(new Font("Tahoma", Font.BOLD, 12));
		add(label);
	}

	private String grundHinweis() {
		return pmz == null ? " "
				: "Aktuell: Zimmer " + pmz.zimmerNr() + " – leer lassen = bleibt";
	}

	private void zimmerNeuLaden() {
		zimmerBox.removeAllItems();
		int index = etageBox.getSelectedIndex();
		if (index == -1) {
			zimmerBox.setEnabled(false);
			hinweisLabel.setText(" ");
			return;
		}
		int etage = etageBox.getItemAt(index);
		for (Zimmer z : freieZimmer) {
			if (z.getEtage() == etage) zimmerBox.addItem(z.getNrAufEtage());
		}
		boolean frei = zimmerBox.getItemCount() > 0;
		zimmerBox.setEnabled(frei);
		hinweisLabel.setText(frei ? grundHinweis() : "Auf dieser Etage ist kein Zimmer frei.");
	}

	public PatientDTO getPatientMitZimmer() throws IllegalArgumentException {
		StringBuilder fehler = new StringBuilder();

		String patientId = idField.getText().strip();
		if (patientId.isBlank()) fehler.append("Patient-ID fehlt!\n");
		else if (patientId.length() > 10) fehler.append("Patient-ID ist zu lang (Max 10 Zeichen)\n");

		String name = nameField.getText().strip();
		if (name.isBlank()) fehler.append("Patientname fehlt!\n");
		else if (name.length() > 50) fehler.append("Patientname ist zu lang (Max 50 Zeichen)\n");

		String diagnose = diagnoseField.getText().strip();
		if (diagnose.length() > 50) fehler.append("Diagnose ist zu lang (Max 50 Zeichen)\n");

		LocalDate geb = geburtsDatum.getDate();
		LocalDate heute = LocalDate.now();
		if (pmz == null) {
			if (geb == null) fehler.append("Geburtsdatum fehlt!\n");
			else if (geb.isAfter(heute.minusYears(MIN_ALTER)))
				fehler.append("Geburtsdatum prüfen, wir empfangen nur Patienten ab 14 Jahre!\n");
			else if (geb.isBefore(heute.minusYears(MAX_ALTER)))
				fehler.append("Geburtsdatum unwahrscheinlich, es liegt mehr als 120 Jahre zurück!\n");
		}

		Geschlecht geschlecht = (Geschlecht) geschlechtBox.getSelectedItem();
		int etage = 0;
		int etageIndex = etageBox.getSelectedIndex();
		int zimmer = 0;
		int zimmerIndex = zimmerBox.getSelectedIndex();
		boolean zimmerBleibt = pmz != null && etageIndex == -1;
		int zimmerNr;
		if (zimmerBleibt) {
			zimmerNr = pmz.zimmerNr();
		} else {
			if (etageIndex == -1) fehler.append("Bitte eine Etage wählen!\n");
			else etage = etageBox.getItemAt(etageIndex);
			if (zimmerIndex == -1) fehler.append("Bitte ein Zimmer wählen!\n");
			else zimmer = zimmerBox.getItemAt(zimmerIndex);
			zimmerNr = Zimmer.berechneZimmerNr(etage, zimmer);
		}

		LocalDate aufnahme = pmz == null ? LocalDate.now() : pmz.patient().getAufnahmeDatum();
		if (!fehler.isEmpty()) throw new IllegalArgumentException(fehler.toString());
		return new PatientDTO(zimmerNr, new Patient(patientId, name, diagnose, geb, geschlecht, aufnahme));
	}


}
