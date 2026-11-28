CREATE TABLE abteilung (
    etage INTEGER PRIMARY KEY,
    name VARCHAR(20) NOT NULL,
    kuerzel VARCHAR(2) NOT NULL
);

CREATE TABLE zimmer (
    nummer INTEGER PRIMARY KEY,
    etage INTEGER NOT NULL,
    FOREIGN KEY (etage) REFERENCES abteilung(etage)
);

CREATE TABLE patient (
    patientID VARCHAR(10) PRIMARY KEY,
    patientName VARCHAR(100) NOT NULL,
    geburtsdatum DATE NOT NULL,
    geschlecht VARCHAR(10) NOT NULL
);

CREATE TABLE aktuell (
    patientID VARCHAR(10) PRIMARY KEY,
    zimmerNr INTEGER UNIQUE NOT NULL,
    krankheit VARCHAR(100),
    aufnahmeDatum DATE NOT NULL,
    FOREIGN KEY (patientID) REFERENCES patient(patientID),
    FOREIGN KEY (zimmerNr) REFERENCES zimmer(nummer)
);

-- nur abgeschlossene Aufenthalte (eine Zeile pro Entlassung)
CREATE TABLE verlauf (
    verlaufID INTEGER AUTO_INCREMENT PRIMARY KEY,
    patientID VARCHAR(10) NOT NULL,
    zimmerNr INTEGER NOT NULL,
    krankheit VARCHAR(100),
    von DATE NOT NULL,
    bis DATE NOT NULL,
    FOREIGN KEY (patientID) REFERENCES patient(patientID),
    FOREIGN KEY (zimmerNr) REFERENCES zimmer(nummer)
);

-- Änderungsprotokoll: alte und neue Werte (Zimmer, Name, Diagnose)
CREATE TABLE aenderung (
    aenderungID INTEGER AUTO_INCREMENT PRIMARY KEY,
    patientID VARCHAR(10) NOT NULL,
    datum DATE NOT NULL,
    alteDaten VARCHAR(100),
    neueDaten VARCHAR(100) NULL,
    aenderung ENUM('ZIMMER', 'NAME', 'DIAGNOSE') NOT NULL,
    FOREIGN KEY (patientID) REFERENCES patient(patientID)
);


INSERT IGNORE INTO abteilung (etage, kuerzel, name) VALUES 
	(1, 'IN', 'Innere Medizin'),
    (2, 'CH', 'Chirurgie'),
    (3, 'KA', 'Kardiologie'),
    (4, 'NE', 'Neurologie'),
    (5, 'OR', 'Orthopädie'),
    (6, 'PA', 'Pädiatrie'),
    (7, 'GY', 'Gynäkologie'),
    (8, 'ON', 'Onkologie'),
    (9, 'GE', 'Geriatrie');

-- 80 Zimmer: 4 Etagen x 20 Zimmer (Nummer = Etage * 100 + Zimmer)
INSERT IGNORE INTO zimmer (nummer, etage) VALUES
    (101, 1), (102, 1), (103, 1), (104, 1), (105, 1), (106, 1), (107, 1), (108, 1), (109, 1), (110, 1), (111, 1), (112, 1), (113, 1), (114, 1), (115, 1), (116, 1), (117, 1), (118, 1), (119, 1), (120, 1),
    (201, 2), (202, 2), (203, 2), (204, 2), (205, 2), (206, 2), (207, 2), (208, 2), (209, 2), (210, 2), (211, 2), (212, 2), (213, 2), (214, 2), (215, 2), (216, 2), (217, 2), (218, 2), (219, 2), (220, 2),
    (301, 3), (302, 3), (303, 3), (304, 3), (305, 3), (306, 3), (307, 3), (308, 3), (309, 3), (310, 3), (311, 3), (312, 3), (313, 3), (314, 3), (315, 3), (316, 3), (317, 3), (318, 3), (319, 3), (320, 3),
    (401, 4), (402, 4), (403, 4), (404, 4), (405, 4), (406, 4), (407, 4), (408, 4), (409, 4), (410, 4), (411, 4), (412, 4), (413, 4), (414, 4), (415, 4), (416, 4), (417, 4), (418, 4), (419, 4), (420, 4);

-- Einmalige Uebernahme der Patienten aus der alten DB (krankenhaus)
INSERT INTO krankenhaus_neu.patient (patientID, patientName, geschlecht, geburtsdatum)
SELECT patientId, name, geschlecht, geburtsdatum FROM krankenhaus.patient;

INSERT INTO krankenhaus_neu.aktuell (patientID, aufnahmeDatum, krankheit, zimmerNr)
SELECT patientId, aufnahmeDatum, krankheit, (etage * 100 + zimmer) FROM krankenhaus.patient;