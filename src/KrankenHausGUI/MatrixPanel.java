package KrankenHausGUI;

import java.awt.GridLayout;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class MatrixPanel extends JPanel  {

	private static final long serialVersionUID = 1L;
	private int anzahlRows = 1;
	private int anzahlColumns = 1;
	private PatientButton[][] buttons = new PatientButton[1][1];
	
	public MatrixPanel(int rows, int columns) {
		setAnzahlRows(rows);
		setAnzahlColumns(columns);
	}
	
	public PatientButton getButton(int row, int column) {
		return buttons[row][column];
	}
	
	public int getAnzahlRows() {
		return anzahlRows;
	}
	public int getAnzahlColumns() {
		return anzahlColumns;
	}
	public PatientButton[][] getButtons() {
		return buttons;
	}
	public void setAnzahlRows(int anzahlRows) throws IllegalArgumentException {
		if (anzahlRows <= 0 || anzahlRows > 10) throw new IllegalArgumentException ("Ungültiger Wert für Anzahl Rows (max 10)");
		this.anzahlRows = anzahlRows;
		neuAufbauen();
	}
	
	public void setAnzahlColumns(int anzahlColumns) throws IllegalArgumentException {
		if (anzahlColumns <= 0)  throw new IllegalArgumentException ("Ungültiger Wert! Anzahl Columns kann nicht negativ sein!");
		this.anzahlColumns = anzahlColumns;
		neuAufbauen();
	}
	
		
	private void neuAufbauen() {
		removeAll();
		int r = getAnzahlRows();		
		int c = getAnzahlColumns();
		setLayout(new GridLayout(r, c));
		buttons = new PatientButton[r][c];
		for (int i=0; i<r; i++) {
			for (int j=0; j<c; j++) {
				PatientButton b = new PatientButton(i+1, j+1);
				this.add(b);
				buttons[i][j]=b;
			}
		}
		revalidate();
		repaint();
	}
	
	public void zeigeZimmer(int etage, int zimmer) {
	    getButton(etage - 1, zimmer - 1).zeigeDaten();
	}
	
	public PatientButton findePatientById(String patientId) {
		int r = getAnzahlRows();		
		int c = getAnzahlColumns();
		for (int i=0; i<r; i++) {
			for (int j=0; j<c; j++) {
				if (buttons[i][j].getPatient() != null && buttons[i][j].getPatient().getPatientId().equalsIgnoreCase(patientId)) {
					return buttons[i][j];
				}
			}
		}
		return null;
	}
	
	public PatientButton findePatientByName(String patientName) {
		int r = getAnzahlRows();		
		int c = getAnzahlColumns();
		for (int i=0; i<r; i++) {
			for (int j=0; j<c; j++) {
				if (buttons[i][j].getPatient() != null && patientName.equalsIgnoreCase(buttons[i][j].getPatient().getName())) {
					return buttons[i][j];
				}
			}
		}
		return null;
	}
	
	public List<List<Integer>> freieZimmer() {
		List<List<Integer>> fz = new ArrayList<>();
		for (int i=0; i<getAnzahlRows(); i++) {
			List<Integer> frei = new ArrayList<>(); 
			for (int j=0; j<getAnzahlColumns(); j++) {
				if (buttons[i][j].getPatient() == null) frei.add(j+1);
			}
			fz.add(frei);	
		}
		return fz;
	}
	
	public void patientAnmelden(PatientMitZimmer pz) throws SQLException, IllegalArgumentException {
		int etage = pz.etage();
		int zimmer = pz.zimmer();
		zimmerPruefen(etage, zimmer);
		PatientDAO.patientSpeichern(etage, zimmer, pz.patient());
		buttons[etage-1][zimmer-1].setPatient(pz.patient());
	}
	
	public void patientAendern(PatientMitZimmer alt, PatientMitZimmer neu) throws SQLException, IllegalArgumentException {
		if (alt.etage() != neu.etage() || alt.zimmer() != neu.zimmer()) zimmerPruefen(neu.etage(), neu.zimmer());
		PatientDAO.patientUpdate(neu.etage(),neu.zimmer(),neu.patient());		
		buttons[alt.etage()-1][alt.zimmer()-1].setPatient(null);
		buttons[neu.etage()-1][neu.zimmer()-1].setPatient(neu.patient());
	}
	
	private void zimmerPruefen(int etage, int zimmer) throws IllegalArgumentException {
		if (etage < 1 || etage > anzahlRows || zimmer < 1 || zimmer > anzahlColumns) {
			throw new IllegalArgumentException("Das Zimmer (E" + etage + " / Z" + zimmer + ") gibt es nicht.");
		}
		PatientButton button = buttons[etage-1][zimmer-1];
		if (button.getPatient() != null) {
			throw new IllegalArgumentException("Das Zimmer (E" + etage + " / Z" + zimmer + ") ist schon besetzt.");
		}
	}

	
// nicht Vollständig, später wird entscheiden ob diese zwei Methode eigentlich nötig sind
	
	public void alleLadenAusDB() throws SQLException, IllegalArgumentException {
		for (PatientMitZimmer pz : PatientDAO.alleLadenAusDB()) {
			zimmerPruefen(pz.etage(), pz.zimmer());
		    buttons[pz.etage()-1][pz.zimmer()-1].setPatient(pz.patient());
		}
	}
	
	public void alleSpeichern() throws SQLException, IllegalArgumentException {
		int antwort;
		Patient appPatient;
		List<PatientMitZimmer> nichtGespeichert = new ArrayList<>();
		for (int i=0; i<getAnzahlRows(); i++) {
			for (int j=0; j<getAnzahlColumns(); j++) {
				appPatient = buttons[i][j].getPatient();
				if (appPatient != null) {
					try {
						PatientDAO.patientSpeichern(i+1, j+1, appPatient);
					} catch (SQLIntegrityConstraintViolationException ex) {
						if (ex.getMessage().contains("PRIMARY")) {	
							PatientMitZimmer dbPatient =  PatientDAO.getPatientById(appPatient.getPatientId());
							if (!dbPatient.equals(new PatientMitZimmer(i+1,j+1,appPatient))){
								antwort = JOptionPane.showConfirmDialog(this,"Diese Patient-ID existiert schon. Überschreiben?","Patient Existiert Fehler", JOptionPane.YES_NO_OPTION);
								if (antwort == JOptionPane.YES_OPTION) {
									PatientDAO.patientUpdate(i+1, j+1, buttons[i][j].getPatient());
								}
							}
						} else {
							nichtGespeichert.add(new PatientMitZimmer(i+1, j+1, buttons[i][j].getPatient()));
						}
					}
				}
			}
		}
	}
}
