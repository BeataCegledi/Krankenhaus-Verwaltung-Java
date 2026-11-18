package KrankenHausGUI;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;

public class Krankenhaus extends JFrame {
	
	private static final long serialVersionUID = 1L;
	private MatrixPanel mp;
	
	public Krankenhaus(int rows, int columns) throws IllegalArgumentException {
		getContentPane().setLayout(new BorderLayout(0, 0));
		mp = new MatrixPanel(rows, columns);
		getContentPane().add(mp, BorderLayout.CENTER);
		
		JToolBar toolBar = new JToolBar();
		getContentPane().add(toolBar, BorderLayout.NORTH);
		
		JButton sucheById = new JButton("Suchen mit ID");
		sucheById.addActionListener(e ->{
			PatientButton pb = findeByID();
			if (pb != null) pb.zeigeDaten();
		});
		toolBar.add(sucheById);
		
		JButton sucheByName = new JButton("Suchen mit Name");
		sucheByName.addActionListener(e ->{
			String name = JOptionPane.showInputDialog(this, "Gib bitte den Patientennamen ein: ");
			if (name== null || name.isBlank()) return;
			name = name.strip();
			PatientButton pb = mp.findePatientByName(name);
			if (pb == null) zeigeFehler(this, "Nicht gefunden", "Es gibt keinen Patienten mit dem Namen " + name);
			else pb.zeigeDaten();
		});
		toolBar.add(sucheByName);
		
		JButton entlassen = new JButton("Patient entlassen");
		entlassen.addActionListener(e->{
			PatientButton pb = findeByID();
			if (pb != null) {
				try {
					int antwort = JOptionPane.showConfirmDialog(entlassen, "Patient " + pb.getPatient().getName() + " wirklich entlassen und endgültig löschen?" , "Sicherheitsfrage", JOptionPane.YES_NO_OPTION);
					if (antwort == JOptionPane.YES_OPTION) pb.patientEntlassen();
				}catch (SQLException ex) {
					zeigeFehler(this, "Datenbankfehler", "Entlassen fehlgeschlagen: " + uebersetzeDBFehler(ex));
				}
			}
		});
		toolBar.add(entlassen);
		
		JButton aufnehmen = new JButton("Patient aufnehmen");
		aufnehmen.addActionListener(e -> frageBogen("Neuen Patienten aufnehmen", new PatientFormular(mp.freieZimmer())));
		toolBar.add(aufnehmen);
		
		JButton aendern= new JButton("Patientdaten ändern");
		
		aendern.addActionListener(e -> {
			PatientButton pb = findeByID();
			if (pb != null) {
				PatientMitZimmer pmzAlt= new PatientMitZimmer (pb.getEtage(), pb.getZimmer(), pb.getPatient());
				PatientFormular formular = new PatientFormular(mp.freieZimmer(), pmzAlt);
				frageBogen("Patientdaten ändern", formular);
			}
		});
		toolBar.add(aendern);
		
		
		setSize(new Dimension(1200,600));
		setLocationRelativeTo(null);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setTitle("Mein Krankenhaus Projekt");
	}
	
	private PatientButton findeByID() {
		String id = JOptionPane.showInputDialog(this, "Gib bitte die Patient-ID ein: ");
		if (id== null || id.isBlank()) return null;
		id = id.strip();
		PatientButton pb = mp.findePatientById(id);
		if (pb == null) zeigeFehler(this, "Nicht gefunden", "Es gibt keinen Patienten mit der Patient-ID " + id);
		return pb;
	}
	
	public MatrixPanel getMatrixPanel() {
		return this.mp;
	}
 
	private void frageBogen(String title, PatientFormular formular) {
		while (true) {
			int antwort = JOptionPane.showConfirmDialog(this, formular, title, JOptionPane.OK_CANCEL_OPTION);
			if (antwort != JOptionPane.OK_OPTION) return;
			try {
				PatientMitZimmer alt = formular.getPmz();
				PatientMitZimmer neu = formular.getPatientMitZimmer();
				if (alt == null) mp.patientAnmelden(neu);
				else  mp.patientAendern(alt, neu);
				return;
			} catch (IllegalArgumentException ex) { //eigene Fehler geworfen
				ex.printStackTrace();
				zeigeFehler(this, "Ungültige Eingabe", ex.getMessage());
			} catch (SQLIntegrityConstraintViolationException ex) { //DB Unique Fehler
				ex.printStackTrace();
				if (ex.getMessage().contains("PRIMARY")) zeigeFehler(this, "Patient existiert schon", "Diese Patient-ID ist schon vergeben. Bitte eine andere ID eingeben.");
				else zeigeFehler(this, "Zimmer besetzt", "Dieses Zimmer ist in der Datenbank schon belegt. Bitte ein anderes Zimmer wählen.");
			} catch (SQLException ex) {
				ex.printStackTrace();
				zeigeFehler(this,"Datenbankfehler", uebersetzeDBFehler(ex));
			}
		}
	}
		
	 
	private static void zeigeFehler(Component parent, String title, String message) {
		JOptionPane.showMessageDialog(parent, message,title, JOptionPane.ERROR_MESSAGE);	
	}

	private static String uebersetzeDBFehler(SQLException ex) {
		if (ex.getSQLState() != null) {
			if (ex.getSQLState().startsWith("08")) return "Die Datenbank ist nicht erreichbar. Läuft MySQL (XAMPP)?";
			if (ex.getSQLState().startsWith("22001")) return "Ein eingegebener Text ist zu lang.";
			if (ex.getSQLState().startsWith("42")) return "Die Datenbank ist nicht richtig eingerichtet";
		}
		return "Unerwarteter Datenbankfehler!";
	}
	
	public static void main(String[] args) {

		try {
			PatientDAO.dbErstellen();
			Krankenhaus kh = new Krankenhaus(4, 20);
			kh.getMatrixPanel().alleLadenAusDB();
			kh.setVisible(true);
		} catch (SQLException ex) {
			ex.printStackTrace();
			zeigeFehler(null,"Datenbankfehler!", uebersetzeDBFehler(ex));
		} catch (IllegalArgumentException ex) {
			ex.printStackTrace();
			zeigeFehler(null, "Fehler! Hat die Datenbank fehlerhafte Daten?", ex.getMessage());
		}
	}
	

}
