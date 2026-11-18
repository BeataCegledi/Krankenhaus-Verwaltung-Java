package KrankenHausGUI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class PatientDAO {

	private static final String URLSERVERCREATEDB = "jdbc:mysql://localhost:3306/krankenhaus?createDatabaseIfNotExist=true";
	private static final String URLDB = "jdbc:mysql://localhost:3306/krankenhaus";
	
	private PatientDAO() {}
	
	public static void dbErstellen() throws SQLException {
			try (Connection con = DriverManager.getConnection(URLSERVERCREATEDB, "root", "");
			Statement st = con.createStatement()){
			st.executeUpdate("""
							CREATE TABLE IF NOT EXISTS patient(
								patientId VARCHAR(10) PRIMARY KEY,
								name VARCHAR(50) NOT NULL,
								krankheit VARCHAR(50),
								geburtsdatum DATE,
								aufnahmeDatum DATE NOT NULL,
								geschlecht VARCHAR(10) NOT NULL,
								etage INT NOT NULL,
								zimmer INT NOT NULL,
								CONSTRAINT uq_Zimmer UNIQUE (etage, zimmer)
								);""");
			}	
	}
	
	public static void patientSpeichern(int etage, int zimmer, Patient patient) throws SQLIntegrityConstraintViolationException, SQLException {
		String sql = "INSERT INTO patient (patientId, name, krankheit, geburtsdatum, aufnahmeDatum, geschlecht, etage, zimmer) VALUES (?,?,?,?,?,?,?,?)";
		try (Connection con = DriverManager.getConnection(URLDB,"root","");
				PreparedStatement ps = con.prepareStatement(sql)){
			ps.setString(1, patient.getPatientId());
			ps.setString(2, patient.getName());
			ps.setString(3, patient.getKrankheit());
			ps.setObject(4, patient.getGeburtsdatum());
			ps.setObject(5, patient.getAufnahmeDatum());
			ps.setString(6, patient.getGeschlecht().name());
			ps.setInt(7, etage);
			ps.setInt(8, zimmer);
			ps.executeUpdate();
		}
	}
	
	public static PatientMitZimmer getPatientById(String patientId) throws SQLException, IllegalArgumentException {
		PatientMitZimmer dbPatient = null;
		String sql = "SELECT * FROM patient WHERE patientId = ?";

		try (Connection con = DriverManager.getConnection(URLDB,"root","");
			PreparedStatement ps = con.prepareStatement(sql)){
			ps.setString(1, patientId);
			try (ResultSet rs = ps.executeQuery()){
				if (rs.next()) {
					dbPatient = new PatientMitZimmer(
							rs.getInt("etage"),
							rs.getInt("zimmer"),
							(new Patient (
								rs.getString("patientId"),
								rs.getString("name"),
								rs.getString("krankheit"),
								rs.getObject("geburtsdatum", LocalDate.class),
								Geschlecht.valueOf(rs.getString("geschlecht")),
								rs.getObject("aufnahmeDatum",LocalDate.class)
								)
							)
					);
				}
			}
		}
		return dbPatient;
	}
	
	
	public static void patientUpdate(int etage, int zimmer, Patient patient) throws SQLException{
		String sql = "UPDATE patient SET name = ?, krankheit = ?, geburtsdatum = ?, aufnahmeDatum = ?, geschlecht = ?, etage = ?, zimmer = ? WHERE patientId = ?";
		try (Connection con = DriverManager.getConnection(URLDB,"root","");
				PreparedStatement ps = con.prepareStatement(sql)){
			ps.setString(1, patient.getName());
			ps.setString(2, patient.getKrankheit());
			ps.setObject(3, patient.getGeburtsdatum());
			ps.setObject(4, patient.getAufnahmeDatum());
			ps.setString(5, patient.getGeschlecht().name());
			ps.setInt(6, etage);
			ps.setInt(7, zimmer);
			ps.setString(8, patient.getPatientId());
			ps.executeUpdate();
		}
	}
	
	public static void patientLoeschen(String patientId)  throws SQLException {
		String sql = "DELETE FROM patient WHERE patientId = ?";
		try (Connection con = DriverManager.getConnection(URLDB,"root","");
				PreparedStatement ps = con.prepareStatement(sql)){
			ps.setString(1, patientId);
			ps.executeUpdate();
		}
	}
	
	public static List<PatientMitZimmer> alleLadenAusDB() throws SQLException, IllegalArgumentException{
		String sql = "SELECT * FROM patient";
		List<PatientMitZimmer> liste = new ArrayList<>();
		try (Connection con = DriverManager.getConnection(URLDB,"root","");
			PreparedStatement ps = con.prepareStatement(sql);
			ResultSet rs = ps.executeQuery()){
			while (rs.next()) {
				liste.add(new PatientMitZimmer(
						rs.getInt("etage"), 
						rs.getInt("zimmer"),
						new Patient (
							rs.getString("patientId"),
							rs.getString("name"),
							rs.getString("krankheit"),
							rs.getObject("geburtsdatum", LocalDate.class),
							Geschlecht.valueOf(rs.getString("geschlecht")),
							rs.getObject("aufnahmeDatum",LocalDate.class)
							)
						)
				);
			}
		}
		return liste;
	}
	
}
