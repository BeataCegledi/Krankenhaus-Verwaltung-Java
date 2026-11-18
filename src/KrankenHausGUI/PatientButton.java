package KrankenHausGUI;

import java.awt.Color;
import java.sql.SQLException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;

public class PatientButton extends JButton {
	
	
	private static final long serialVersionUID = 1L;
	private final int zimmer;
	private final int etage;
	private Patient patient;
	
	
	public PatientButton() {
		this(0,0);
	}
	

	public PatientButton(int etage, int zimmer) {
		this.zimmer = zimmer;
		this.etage = etage;
		aktualisiere();
		addActionListener(e -> zeigeDaten());
	}

	public int getZimmer() {
		return zimmer;
	}

	public int getEtage() {
		return etage;
	}


	public Patient getPatient() {
		return this.patient;
	}

	public void setPatient(Patient patient) throws IllegalArgumentException {
		if (this.patient != null && patient!=null) throw new IllegalArgumentException("Das Zimmer (E" + etage + " / Z" + zimmer + ") ist schon besetzt");
		this.patient = patient;		
		aktualisiere();
	}
	
	private void aktualisiere() {
		String n;
		 if (patient == null) {
			 n = "Zimmer frei";
			 setBackground(new Color(177, 248, 165));
		}else {
			n = patient.getName();
			setBackground(new Color(253, 153, 179));
		}

		setBorder(BorderFactory.createRaisedBevelBorder());
		setText("<html><center>Zimmer:<br>" + zimmer + "<br>Etage:<br>"+ etage +"<br>" + n + "</center></html>");
	}
	
	public void zeigeDaten (){
		String text = patient == null ? "Frei" : patient.getPatientInfo();
		JOptionPane.showMessageDialog(this, text, "Daten von : " + etage + ". Etage / " + zimmer + ". Zimmer",JOptionPane.INFORMATION_MESSAGE);
	}
	
	

	public void patientEntlassen() throws SQLException {
		if (patient == null) return;
		PatientDAO.patientLoeschen(this.getPatient().getPatientId());
		setPatient(null);
	}
}



