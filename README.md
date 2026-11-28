# Krankenhaus-Verwaltung-Java

> Swing-Anwendung zur Verwaltung von Patienten und Zimmerbelegung in einem Krankenhaus, mit MySQL-Datenbank über JDBC und austauschbarer Speicherung. Entwickelt im Rahmen der Berufsschulausbildung zur Fachinformatikerin Anwendungsentwicklung.

## Projektbeschreibung

Das Krankenhaus wird als Raster aus Etagen und Zimmern dargestellt (Standard: 4 Etagen × 20 Zimmer). Jede Etage gehört zu einer Abteilung, jedes Zimmer hat eine dreistellige Nummer (z. B. `KA-312` = Kardiologie, 3. Etage, Zimmer 12). Jedes Zimmer ist ein Button: grün bedeutet frei, rosa bedeutet belegt, und auf dem Button steht der Name des Patienten.
Über eine Werkzeugleiste oder einen Klick auf ein Zimmer kann man Patienten suchen, aufnehmen, ihre Daten ändern und sie entlassen. Jede Aktion wird sofort gespeichert – in MySQL oder, zum Testen ohne Datenbank, nur im Arbeitsspeicher.

## Aufbau (Packages)

| Package | Inhalt | Aufgabe |
|---|---|---|
| `krankenhaus.model` | `Patient`, `Zimmer`, `PatientenHaus`, `Abteilung`, `Geschlecht`, `PatientDTO`, `ZimmerAenderung`, `ZimmerObserver` | Daten und Regeln, kennt weder GUI noch Datenbank |
| `krankenhaus.service` | `PatientVerwaltung` | Ablauf: prüfen → speichern → Modell ändern |
| `krankenhaus.repository` | `PatientRepository`, `MySqlPatientRepository`, `MemoryPatientRepository`, `RepositoryException` | Speicherung; nur hier kommt `java.sql` vor |
| `krankenhaus.view` | `MatrixPanel`, `ZimmerButton`, `PatientFormular`, `ZimmerKlickListener` | Swing-Oberfläche |
| `krankenhaus.app` | `Krankenhaus` (mit `main`), `KrankenhausFactory`, `SpeicherArt` | Dialoge, Fehleranzeige, Zusammenbau |

```
Krankenhaus (GUI) ──► PatientVerwaltung ──► «interface» PatientRepository
                             │                   ├── MySqlPatientRepository
                             ▼                   └── MemoryPatientRepository
                       PatientenHaus ──(Observer)──► MatrixPanel ──► ZimmerButton
```

## Funktionsumfang

### Eingaben
- **Formular (`PatientFormular`)** mit Patient-ID, Name, Diagnose, Geburtsdatum (DatePicker), Geschlecht, Etage und Zimmer
- Es werden nur Etagen mit freien Zimmern angeboten; die Zimmer-Auswahl zeigt nur die freien Zimmer der gewählten Etage
- Eingabedialoge für die Suche nach ID bzw. Name

### Bedienung
| Aktion | Ergebnis |
|---|---|
| Suchen mit ID / Suchen mit Name | öffnet das Zimmer des Patienten, sonst Meldung „Nicht gefunden“ |
| Patient aufnehmen | leeres Formular → Patient wird gespeichert, Zimmer wird rosa |
| Klick auf ein freies Zimmer | Aufnahme-Formular mit diesem Zimmer vorausgewählt |
| Klick auf ein belegtes Zimmer | Zimmer- und Patientendaten mit Auswahl „Ändern / Entlassen / Schließen“ |
| Patientdaten ändern | Formular mit vorhandenen Daten; ID, Geburtsdatum und Geschlecht sind gesperrt. Etage/Zimmer leer lassen = Patient bleibt im Zimmer |
| Patient entlassen | Sicherheitsfrage → Patient wird gelöscht, Zimmer wird grün |

### Regeln / Validierung
| Bedingung | Ergebnis |
|---|---|
| Patient-ID oder Name fehlt | Fehlermeldung, Formular bleibt offen |
| ID > 10 Zeichen, Name/Diagnose > 50 Zeichen | Fehlermeldung (passend zu den Spaltenlängen in der DB) |
| Patient jünger als 14 Jahre | wird nicht aufgenommen |
| Geburtsdatum liegt mehr als 120 Jahre zurück | Fehlermeldung „unwahrscheinlich“ |
| Zimmer existiert nicht oder ist belegt | Fehlermeldung, Formular bleibt offen |
| Patient liegt schon in einem anderen Zimmer | Fehlermeldung (ein Patient nur in einem Zimmer) |
| Patient-ID schon vergeben (`PRIMARY KEY`) | „Diese Patient-ID ist schon vergeben“ |
| Zimmer in der DB schon belegt (`UNIQUE (etage, zimmer)`) | „Dieses Zimmer ist in der Datenbank schon belegt“ |
| Datenbank nicht erreichbar (SQLState `08…`) | „Die Datenbank ist nicht erreichbar. Läuft MySQL (XAMPP)?“ |

Alle Fehler eines Formulars werden gesammelt und zusammen in einem Dialog angezeigt. Nach einem Fehler öffnet sich das Formular mit den bisherigen Eingaben erneut.

### Ausgabe
- Farbiges Zimmer-Raster mit Abteilung, Zimmerbezeichnung und Patientenname
- Informations- und Fehlerdialoge (`JOptionPane`)
- Konsolenausgabe jeder Zimmeränderung (zweiter Observer)
- Tabelle `patient` in der Datenbank `krankenhaus`

## Verwendete Java-Konzepte

| Konzept | Anwendung im Projekt |
|---|---|
| Schichtentrennung (MVC + Service + Repository) | GUI, Ablauf, Modell und Speicherung in eigenen Packages |
| Observer-Pattern | `PatientenHaus` meldet jede Belegung als `ZimmerAenderung` (vorher/nachher) an alle `ZimmerObserver`; `MatrixPanel` leitet an den passenden `ZimmerButton` weiter |
| Repository-Pattern + Interface | `PatientVerwaltung` kennt nur `PatientRepository`; MySQL und Arbeitsspeicher sind austauschbar |
| Factory + Dependency Injection | `KrankenhausFactory.erstelle(etagen, zimmer, SpeicherArt)` erzeugt das Repository und gibt es per Konstruktor an die Verwaltung |
| Exception Translation | `SQLException` wird im Repository in eine eigene checked `RepositoryException` mit verständlichem Text übersetzt (Ursache bleibt als `cause` erhalten) |
| Eigenes funktionales Interface | `ZimmerKlickListener`, als Methodenreferenz `this::zimmerGeklickt` übergeben |
| JDBC mit `PreparedStatement` | Platzhalter `?` statt Stringverkettung, damit keine SQL-Injection möglich ist |
| try-with-resources | `Connection`, `PreparedStatement` und `ResultSet` werden automatisch geschlossen |
| `record` | `PatientDTO`, `ZimmerAenderung` |
| `enum` mit Feldern | `Abteilung` (Anzeigename, Kürzel, Etage, `fromEtage`), `Geschlecht`, `SpeicherArt` |
| `switch`-Ausdruck | Auswahl des Repositorys in der Factory, Optionsdialog beim Zimmerklick |
| Kapselung | `Zimmer.belege` nur package-private → Änderungen nur über `PatientenHaus`; `getAlleZimmer()` als `unmodifiableCollection`; `Patient` unveränderlich (`final`-Felder) |
| `TreeMap` / `TreeSet` | Zimmer nach Nummer sortiert; Etagen im Formular ohne Duplikate |
| Konstruktorverkettung (`this(...)`) | `Patient` (mit/ohne Aufnahmedatum), `PatientFormular` (Aufnehmen / Zimmer vorausgewählt / Ändern) |
| `equals` / `hashCode` | `Patient` |
| `LocalDate` | Geburts- und Aufnahmedatum, Altersprüfung mit `minusYears` |
| `StringBuilder` | Sammelt alle Validierungsfehler des Formulars |
| Swing (`JFrame`, `JPanel`, `JToolBar`, `JButton`, `JComboBox`, `JTextField`, `GridLayout`) | Oberfläche und Formular |
| Externe Bibliothek | LGoodDatePicker (`DatePicker`) für das Geburtsdatum |
| Java-Module (`module-info.java`) | `requires java.desktop`, `java.sql`, `com.github.lgooddatepicker` |

## Ausführen

1. MySQL/MariaDB starten (z. B. über XAMPP) – `localhost:3306`, Benutzer `root`, kein Passwort.
   Die Datenbank `krankenhaus` und die Tabelle `patient` legt das Programm beim Start selbst an.
2. Optional: Testdaten einspielen:
   ```bash
   mysql -u root < testpatienten.sql
   ```
3. Die beiden JARs in einen Ordner `lib/` legen (sie sind nicht im Repo):
   `mysql-connector-j-*.jar` und `LGoodDatePicker-11.2.1.jar`
4. Kompilieren und starten (Windows, Modulepath):
   ```bash
   javac -p lib -d bin src/module-info.java src/krankenhaus/*/*.java
   java -p "bin;lib" -m KrankenhausVerwaltung/krankenhaus.app.Krankenhaus
   ```

Ohne Datenbank testen: in `Krankenhaus.main` `SpeicherArt.MYSQL` durch `SpeicherArt.MEMORY` ersetzen.

> **Voraussetzungen:** Java 17 oder neuer (entwickelt mit Java 25 in Eclipse), MySQL/MariaDB, MySQL Connector/J, LGoodDatePicker 11.2.1

## Über die Entwicklerin

Dieses Projekt entstand als Übungsaufgabe in der Berufsschule.
Es zeigt meine Fähigkeit, eine bestehende Swing-Anwendung schrittweise in saubere Schichten umzubauen (Observer, Service, Repository, Factory), eine relationale Datenbank über JDBC anzubinden, die Speicherung über ein Interface austauschbar zu machen und Fehler aus Datenbank und Validierung verständlich anzuzeigen.
