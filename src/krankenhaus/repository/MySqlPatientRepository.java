package krankenhaus.repository;

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

import krankenhaus.model.Geschlecht;
import krankenhaus.model.Patient;
import krankenhaus.model.PatientMitZimmer;

public final class MySqlPatientRepository implements PatientRepository {

	private static final String URL_MIT_CREATE = "jdbc:mysql://localhost:3306/krankenhaus?createDatabaseIfNotExist=true";
	private static final String URL = "jdbc:mysql://localhost:3306/krankenhaus";
	private static final String USER = "root";
	private static final String PWD = "";

	public MySqlPatientRepository() throws RepositoryException {
		dbErstellen();
	}

	private static void dbErstellen() throws RepositoryException {
		try (Connection con = DriverManager.getConnection(URL_MIT_CREATE, USER, PWD);
				Statement st = con.createStatement()) {
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
		} catch (SQLException ex) {
			throw uebersetze(ex);
		}
	}

	private static Connection verbinden() throws SQLException {
		return DriverManager.getConnection(URL, USER, PWD);
	}

	@Override
	public void speichern(PatientMitZimmer pmz) throws RepositoryException {
		String sql = "INSERT INTO patient (patientId, name, krankheit, geburtsdatum, aufnahmeDatum, geschlecht, etage, zimmer) VALUES (?,?,?,?,?,?,?,?)";
		try (Connection con = verbinden();
				PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setString(1, pmz.patient().getPatientId());
			ps.setString(2, pmz.patient().getName());
			ps.setString(3, pmz.patient().getKrankheit());
			ps.setObject(4, pmz.patient().getGeburtsdatum());
			ps.setObject(5, pmz.patient().getAufnahmeDatum());
			ps.setString(6, pmz.patient().getGeschlecht().name());
			ps.setInt(7, pmz.etage());
			ps.setInt(8, pmz.zimmer());
			ps.executeUpdate();
		} catch (SQLException ex) {
			throw uebersetze(ex);
		}
	}

	@Override
	public void aendern(PatientMitZimmer pmz) throws RepositoryException {
		String sql = "UPDATE patient SET name = ?, krankheit = ?, geburtsdatum = ?, aufnahmeDatum = ?, geschlecht = ?, etage = ?, zimmer = ? WHERE patientId = ?";
		try (Connection con = verbinden();
				PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setString(1, pmz.patient().getName());
			ps.setString(2, pmz.patient().getKrankheit());
			ps.setObject(3, pmz.patient().getGeburtsdatum());
			ps.setObject(4, pmz.patient().getAufnahmeDatum());
			ps.setString(5, pmz.patient().getGeschlecht().name());
			ps.setInt(6, pmz.etage());
			ps.setInt(7, pmz.zimmer());
			ps.setString(8, pmz.patient().getPatientId());
			ps.executeUpdate();
		} catch (SQLException ex) {
			throw uebersetze(ex);
		}
	}

	@Override
	public void entlassen(String patientId) throws RepositoryException {
		String sql = "DELETE FROM patient WHERE patientId = ?";
		try (Connection con = verbinden();
				PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setString(1, patientId);
			ps.executeUpdate();
		} catch (SQLException ex) {
			throw uebersetze(ex);
		}
	}

	@Override
	public List<PatientMitZimmer> alleLaden() throws IllegalArgumentException, RepositoryException {
		String sql = "SELECT * FROM patient";
		List<PatientMitZimmer> liste = new ArrayList<>();
		try (Connection con = verbinden();
				PreparedStatement ps = con.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				liste.add(new PatientMitZimmer(
						rs.getInt("etage"),
						rs.getInt("zimmer"),
						new Patient(
								rs.getString("patientId"),
								rs.getString("name"),
								rs.getString("krankheit"),
								rs.getObject("geburtsdatum", LocalDate.class),
								Geschlecht.valueOf(rs.getString("geschlecht")),
								rs.getObject("aufnahmeDatum", LocalDate.class))));
			}
		} catch (SQLException ex) {
			throw uebersetze(ex);
		}
		return liste;
	}

	private static RepositoryException uebersetze(SQLException ex) {
		if (ex.getSQLState() != null) {
			if (ex.getSQLState().startsWith("08"))
				return new RepositoryException("Die Datenbank ist nicht erreichbar. Läuft MySQL (XAMPP)?", ex);
			if (ex.getSQLState().startsWith("22001"))
				return new RepositoryException("Ein eingegebener Text ist zu lang.", ex);
			if (ex.getSQLState().startsWith("42"))
				return new RepositoryException("Die Datenbank ist nicht richtig eingerichtet", ex);
			if (ex instanceof SQLIntegrityConstraintViolationException) {
				if (ex.getMessage().contains("PRIMARY"))
					return new RepositoryException("Diese Patient-ID ist schon vergeben. Bitte eine andere ID eingeben.", ex);
				else
					return new RepositoryException("Dieses Zimmer ist in der Datenbank schon belegt. Bitte ein anderes Zimmer wählen.", ex);
			}
		}
		return new RepositoryException("Unerwarteter Datenbankfehler!", ex);
	}
}
