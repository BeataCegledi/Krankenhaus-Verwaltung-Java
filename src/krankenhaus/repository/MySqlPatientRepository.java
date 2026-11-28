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
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import krankenhaus.model.Abteilung;
import krankenhaus.model.Geschlecht;
import krankenhaus.model.Patient;
import krankenhaus.model.PatientDTO;
import krankenhaus.model.Zimmer;

public final class MySqlPatientRepository implements PatientRepository {

	private static final String URL_MIT_CREATE = "jdbc:mysql://localhost:3306/krankenhaus_neu?createDatabaseIfNotExist=true";
	private static final String URL = "jdbc:mysql://localhost:3306/krankenhaus_neu";
	private static final String USER = "root";
	private static final String PWD = "";

	public MySqlPatientRepository(Collection<Zimmer> zimmer) throws RepositoryException {
		dbErstellen(zimmer);
	}

	private static void dbErstellen(Collection<Zimmer> zimmer) throws RepositoryException {
		try (Connection con = DriverManager.getConnection(URL_MIT_CREATE, USER, PWD);
				Statement st = con.createStatement();
			){
				st.executeUpdate("""
					CREATE TABLE IF NOT EXISTS abteilung (
					    etage INTEGER PRIMARY KEY,
					    name VARCHAR(20) NOT NULL,
					    kuerzel VARCHAR(2) NOT NULL
					);""");
				
				String sql = "INSERT IGNORE INTO abteilung (etage, name, kuerzel) VALUES (?,?,?)";
				try (PreparedStatement ps = con.prepareStatement(sql);){
					for (Abteilung a : Abteilung.values()) { 
						ps.setInt(1, a.getEtage());
						ps.setString(2, a.getAnzeigeName());
						ps.setString(3, a.getKuerzel());
						ps.addBatch();
					}
					ps.executeBatch();
				}
				
				st.executeUpdate("""		
					CREATE TABLE  IF NOT EXISTS zimmer (
					    nummer INTEGER PRIMARY KEY,
					    etage INTEGER NOT NULL,
					    FOREIGN KEY (etage) REFERENCES abteilung(etage)
							);""");
				
				sql = "INSERT IGNORE INTO zimmer (nummer, etage) VALUES (?,?)";
				try (PreparedStatement ps = con.prepareStatement(sql);){
					for (Zimmer z : zimmer) { 
						ps.setInt(1, z.getZimmerNr());
						ps.setInt(2,z.getEtage());
						ps.addBatch();
					}
					ps.executeBatch();
				}
				
				st.executeUpdate("""		
					CREATE TABLE  IF NOT EXISTS patient (
					    patientID VARCHAR(10) PRIMARY KEY,
					    patientName VARCHAR(100) NOT NULL,
					    geburtsdatum DATE NOT NULL,
					    geschlecht VARCHAR(10) NOT NULL
					);""");
				st.executeUpdate("""		
					CREATE TABLE  IF NOT EXISTS aktuell (
					    patientID VARCHAR(10) PRIMARY KEY,
					    zimmerNr INTEGER UNIQUE NOT NULL,
					    krankheit VARCHAR(100),
					    aufnahmeDatum DATE NOT NULL,
					    FOREIGN KEY (patientID) REFERENCES patient(patientID),
					    FOREIGN KEY (zimmerNr) REFERENCES zimmer(nummer)
					);""");
				st.executeUpdate("""		
					CREATE TABLE  IF NOT EXISTS verlauf (
					    verlaufID INTEGER AUTO_INCREMENT PRIMARY KEY,
					    patientID VARCHAR(10) NOT NULL,
					    zimmerNr INTEGER NOT NULL,
					    krankheit VARCHAR(100),
					    von DATE NOT NULL,
					    bis DATE NOT NULL,
					    FOREIGN KEY (patientID) REFERENCES patient(patientID),
					    FOREIGN KEY (zimmerNr) REFERENCES zimmer(nummer)
					);""");
				st.executeUpdate("""
				CREATE TABLE  IF NOT EXISTS aenderung (
						aenderungID INTEGER AUTO_INCREMENT PRIMARY KEY,
					    patientID VARCHAR(10) NOT NULL,
					    datum DATE NOT NULL,
					    alteDaten VARCHAR(100) NULL,
					    neueDaten VARCHAR(100),
					    aenderung ENUM('ZIMMER', 'NAME', 'DIAGNOSE') NOT NULL,

					    FOREIGN KEY (patientID) REFERENCES patient(patientID)
					);""");
				
		} catch (SQLException ex) {throw uebersetze(ex); }
	}

	private static Connection verbinden() throws SQLException {
		return DriverManager.getConnection(URL, USER, PWD);
	}

	@Override
	public void speichern(PatientDTO pmz) throws RepositoryException {
		String sql = "SELECT patientID FROM patient WHERE patientID = ?";
		try (Connection con = verbinden();
				PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setString(1, pmz.patient().getPatientId());
			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next()) {
					sql = "INSERT INTO patient (patientId, patientName, geburtsdatum, geschlecht) VALUES (?,?,?,?)";
					try (PreparedStatement ps1 = con.prepareStatement(sql)) {
						ps1.setString(1, pmz.patient().getPatientId());
						ps1.setString(2, pmz.patient().getName());
						ps1.setObject(3, pmz.patient().getGeburtsdatum());
						ps1.setString(4, pmz.patient().getGeschlecht().name());
						ps1.executeUpdate();
					}
				} else System.out.println("Patient schon in Tabelle Patient!");
			}
			sql = "INSERT INTO aktuell (patientId, zimmerNr, krankheit, aufnahmeDatum) VALUES (?,?,?,?)";
			try (PreparedStatement ps2 = con.prepareStatement(sql)) {
				ps2.setString(1, pmz.patient().getPatientId());
				ps2.setInt(2, pmz.zimmerNr());
				ps2.setString(3, pmz.patient().getKrankheit());
				ps2.setObject(4, pmz.patient().getAufnahmeDatum());
				ps2.executeUpdate();
			}
		} catch (SQLException ex) {
			throw uebersetze(ex);
		}
	}

	@Override
	public void aendern(PatientDTO alt, PatientDTO neu) throws RepositoryException {
		try (Connection con = verbinden()) {
			con.setAutoCommit(false);
			try {
				if (!alt.patient().getName().equals(neu.patient().getName())) {
					try (PreparedStatement ps = con.prepareStatement(
							"UPDATE patient SET patientName = ? WHERE patientID = ?")) {
						ps.setString(1, neu.patient().getName());
						ps.setString(2, neu.patient().getPatientId());
						ps.executeUpdate();
					}
					aenderungSchreiben(con, neu.patient().getPatientId(), "NAME",
							alt.patient().getName(), neu.patient().getName());
				}
				if (!Objects.equals(alt.patient().getKrankheit(), neu.patient().getKrankheit())) {
					try (PreparedStatement ps = con.prepareStatement(
							"UPDATE aktuell SET krankheit = ? WHERE patientID = ?")) {
						ps.setString(1, neu.patient().getKrankheit());
						ps.setString(2, neu.patient().getPatientId());
						ps.executeUpdate();
					}
					aenderungSchreiben(con, neu.patient().getPatientId(), "DIAGNOSE",
							alt.patient().getKrankheit(), neu.patient().getKrankheit());
				}

				if (alt.zimmerNr() != neu.zimmerNr()) {
					try (PreparedStatement ps = con.prepareStatement(
							"UPDATE aktuell SET zimmerNr = ? WHERE patientID = ?")) {
						ps.setInt(1, neu.zimmerNr());
						ps.setString(2, neu.patient().getPatientId());
						ps.executeUpdate();
					}
					aenderungSchreiben(con, neu.patient().getPatientId(), "ZIMMER",
							String.valueOf(alt.zimmerNr()), String.valueOf(neu.zimmerNr()));
				}
				con.commit();
			} catch (SQLException ex) {
				con.rollback();
				throw uebersetze(ex);
			}
		} catch (SQLException ex) {
			throw uebersetze(ex);
		}
	}

	private static void aenderungSchreiben(Connection con, String patientId, String art,
			String alteDaten, String neueDaten) throws SQLException {
		String sql = "INSERT INTO aenderung (patientID, datum, alteDaten, neueDaten, aenderung) VALUES (?,?,?,?,?)";
		try (PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setString(1, patientId);
			ps.setObject(2, LocalDate.now());
			ps.setString(3, alteDaten);
			ps.setString(4, neueDaten);
			ps.setString(5, art);
			ps.executeUpdate();
		}
	}

	@Override
	public void entlassen(String patientId) throws RepositoryException {
		try (Connection con = verbinden()) {
			con.setAutoCommit(false);
			try {
				String sql = "INSERT INTO verlauf (patientID, zimmerNr, krankheit, von, bis) "
						+ "SELECT patientID, zimmerNr, krankheit, aufnahmeDatum, ? FROM aktuell WHERE patientID = ?";
				try (PreparedStatement ps = con.prepareStatement(sql)) {
					ps.setObject(1, LocalDate.now());
					ps.setString(2, patientId);
					ps.executeUpdate();
				}
				try (PreparedStatement ps = con.prepareStatement("DELETE FROM aktuell WHERE patientID = ?")) {
					ps.setString(1, patientId);
					ps.executeUpdate();
				}
				con.commit();
			} catch (SQLException ex) {
				con.rollback();
				throw uebersetze(ex);
			}
		} catch (SQLException ex) {
			throw uebersetze(ex);
		}
	}

	@Override
	public List<PatientDTO> alleLaden() throws IllegalArgumentException, RepositoryException {
		String sql = "SELECT a.patientID, a.zimmerNr, a.krankheit, a.aufnahmeDatum, p.patientName, p.geburtsdatum, p.geschlecht"
					+ "	 FROM aktuell a JOIN patient p ON a.patientID = p.patientID";
		List<PatientDTO> liste = new ArrayList<>();
		try (Connection con = verbinden();
				PreparedStatement ps = con.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				liste.add(new PatientDTO(
						rs.getInt("zimmerNr"),
						new Patient(
								rs.getString("patientID"),
								rs.getString("patientName"),
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
				String meldung = ex.getMessage() == null ? "" : ex.getMessage();
				if (meldung.contains("PRIMARY") || meldung.contains("patient.PRIMARY"))
					return new RepositoryException("Diese Patient-ID ist schon vergeben. Bitte eine andere ID eingeben.", ex);
				if (meldung.contains("zimmerNr") || meldung.contains("aktuell.zimmerNr") || meldung.contains("Zimmernummer darf nicht doppelt vergeben werden"))
					return new RepositoryException("Dieses Zimmer ist in der Datenbank schon belegt. Bitte ein anderes Zimmer wählen.", ex);
				if (meldung.contains("FOREIGN KEY") || meldung.contains("Cannot add or update"))
					return new RepositoryException("Die Patienten-, Zimmer- oder Abteilungsangaben sind ungültig.", ex);
				return new RepositoryException("Eine Datenbankbedingung wurde verletzt.", ex);
			}
		}
		return new RepositoryException("Unerwarteter Datenbankfehler!", ex);
	}
}
