# 🖥️ Krankenhaus-Verwaltung-Java

> Swing-Anwendung zur Verwaltung von Patienten und Zimmerbelegung in einem Krankenhaus, mit MySQL-Datenbank über JDBC. Entwickelt im Rahmen der Berufsschulausbildung zur Fachinformatikerin Anwendungsentwicklung.

## 📋 Projektbeschreibung

Das Krankenhaus wird als Raster aus Etagen und Zimmern dargestellt (Standard: 4 Etagen × 20 Zimmer). Jedes Zimmer ist ein Button: grün bedeutet frei, rosa bedeutet belegt, und auf dem Button steht der Name des Patienten.
Über eine Werkzeugleiste kann man Patienten suchen, aufnehmen, ihre Daten ändern und sie entlassen. Jede Aktion wird sofort in der MySQL-Datenbank gespeichert. Beim Start lädt das Programm alle Patienten wieder aus der Datenbank.

## 🔧 Funktionsumfang

### Eingaben
- **Formular (`PatientFormular`)** mit Patient-ID, Name, Diagnose, Geburtsdatum (DatePicker), Geschlecht, Etage und Zimmer
- Bei der Etage werden nur Etagen angeboten, die es gibt. Bei Zimmer zeigt die Auswahl nur die freien Zimmer der gewählten Etage
- Eingabedialoge für die Suche nach ID bzw. Name

### Werkzeugleiste
| Button | Ergebnis |
|---|---|
| Suchen mit ID / Suchen mit Name | zeigt die Daten des Patienten, sonst Meldung „Nicht gefunden“ |
| Patient aufnehmen | leeres Formular → Patient wird in der DB gespeichert, Zimmer wird rosa |
| Patientdaten ändern | Formular mit vorhandenen Daten; ID, Geburtsdatum und Geschlecht sind gesperrt. Etage/Zimmer leer lassen = Patient bleibt im Zimmer |
| Patient entlassen | Sicherheitsfrage → Patient wird aus der DB gelöscht, Zimmer wird grün |
| Klick auf ein Zimmer | zeigt die Daten des Patienten bzw. „Frei“ |

### Regeln / Validierung
| Bedingung | Ergebnis |
|---|---|
| Patient-ID oder Name fehlt | Fehlermeldung, Formular bleibt offen |
| ID > 10 Zeichen, Name/Diagnose > 50 Zeichen | Fehlermeldung (passend zu den Spaltenlängen in der DB) |
| Patient jünger als 14 Jahre | wird nicht aufgenommen |
| Geburtsdatum liegt mehr als 120 Jahre zurück | Fehlermeldung „unwahrscheinlich“ |
| Zimmer existiert nicht oder ist belegt | Fehlermeldung, Formular bleibt offen |
| Patient-ID schon vergeben (`PRIMARY KEY`) | Meldung „Patient existiert schon“ |
| Zimmer in der DB schon belegt (`UNIQUE (etage, zimmer)`) | Meldung „Zimmer besetzt“ |
| Datenbank nicht erreichbar (SQLState `08…`) | Meldung „Läuft MySQL (XAMPP)?“ |

Alle Fehler eines Formulars werden gesammelt und zusammen in einem Dialog angezeigt. Nach einem Fehler öffnet sich das Formular mit den bisherigen Eingaben erneut.

### Ausgabe
- Farbiges Zimmer-Raster mit Patientennamen
- Informations- und Fehlerdialoge (`JOptionPane`)
- Tabelle `patient` in der Datenbank `krankenhaus`

## 💡 Verwendete Java-Konzepte

| Konzept | Anwendung im Projekt |
|---|---|
| Schichtentrennung (GUI / Logik / DAO) | `Krankenhaus` zeigt Dialoge und Fehler, `MatrixPanel` verwaltet die Zimmer, `PatientDAO` spricht mit der Datenbank |
| DAO-Pattern | `PatientDAO` mit `patientSpeichern`, `patientUpdate`, `patientLoeschen`, `getPatientById`, `alleLadenAusDB` |
| Hilfsklasse | `PatientDAO` ist `final` und hat einen `private` Konstruktor; alle Methoden sind `static` |
| JDBC mit `PreparedStatement` | Platzhalter `?` statt Stringverkettung, damit keine SQL-Injection möglich ist |
| try-with-resources | `Connection`, `PreparedStatement` und `ResultSet` werden automatisch geschlossen |
| `record` | `PatientMitZimmer(int etage, int zimmer, Patient patient)` |
| `enum` | `Geschlecht`, gespeichert als Name (`name()` / `valueOf()`) |
| Exceptions: checked und unchecked | DAO wirft `SQLException` nach oben; eigene Validierung mit `IllegalArgumentException`; `SQLIntegrityConstraintViolationException` für `PRIMARY`/`UNIQUE`-Konflikte |
| Mehrere `catch`-Äste | Unterschiedliche Meldungen je Fehlertyp in `frageBogen` und `main` |
| Konstruktorverkettung (`this(...)`) | `Patient` (mit/ohne Aufnahmedatum), `PatientFormular` (Aufnehmen/Ändern) |
| `equals` / `hashCode` | `Patient`: Vergleich von App- und DB-Daten |
| 2D-Array | `PatientButton[][]` als Zimmer-Raster |
| `List<List<Integer>>` | Freie Zimmer pro Etage für die abhängigen ComboBoxen |
| `LocalDate` | Geburts- und Aufnahmedatum, Altersprüfung mit `minusYears` |
| `StringBuilder` | Sammelt alle Validierungsfehler des Formulars |
| Lambda-`ActionListener` | Buttons der Werkzeugleiste und der Zimmer |
| Swing (`JFrame`, `JPanel`, `JToolBar`, `JButton`, `JComboBox`, `JTextField`, `GridLayout`) | Oberfläche und Formular |
| Externe Bibliothek | LGoodDatePicker (`DatePicker`) für das Geburtsdatum |
| Java-Module (`module-info.java`) | `requires java.desktop`, `java.sql`, `com.github.lgooddatepicker` |

## ▶️ Ausführen

1. MySQL/MariaDB starten (z. B. über XAMPP) – `localhost:3306`, Benutzer `root`, kein Passwort.
   Die Datenbank `krankenhaus` und die Tabelle `patient` legt das Programm beim Start selbst an.
2. Optional: Testdaten einspielen (53 Patienten):
   ```bash
   mysql -u root < testpatienten.sql
   ```
3. Die beiden JARs in einen Ordner `lib/` legen (sie sind nicht im Repo):
   `mysql-connector-j-*.jar` und `LGoodDatePicker-11.2.1.jar`
4. Kompilieren und starten (Windows, Modulepath):
   ```bash
   javac -p lib -d bin src/module-info.java src/KrankenHausGUI/*.java
   java -p "bin;lib" -m KrankenhausVerwaltung/KrankenHausGUI.Krankenhaus
   ```

> **Voraussetzungen:** Java 25 (entwickelt mit Eclipse), MySQL/MariaDB, MySQL Connector/J, LGoodDatePicker 11.2.1

## 👩‍💻 Über die Entwicklerin

Dieses Projekt entstand als Übungsaufgabe in der Berufsschule.
Es zeigt meine Fähigkeit, eine Swing-Oberfläche mit einer relationalen Datenbank über JDBC zu verbinden, die Anwendung in Schichten (GUI, Logik, Datenzugriff) aufzuteilen, Benutzereingaben gründlich zu prüfen und Fehler aus Datenbank und Validierung verständlich anzuzeigen.
