CREATE Table LiaisonSansDonneeAnterieur(
   Id SERIAL,
   Identifiant INTEGER NOT NULL,
   Id_Demande INTEGER NOT NULL,
   PRIMARY KEY(Id),
   FOREIGN KEY(Id_Demande) REFERENCES Demande(Id)
);

-- Many to many VISA - PASSEPORT
CREATE TABLE VisaPasseport (
   Id_Visa INTEGER NOT NULL,
   Id_Passeport INTEGER NOT NULL,
   DateCreation TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, -- Asiko default amzay tsy mila mamorona an'ito entite ito, fa possible atao input koa angamba ity any aoriana any ?
   PRIMARY KEY (Id_Visa, Id_Passeport),
   FOREIGN KEY (Id_Visa) REFERENCES Visa(Id),
   FOREIGN KEY (Id_Passeport) REFERENCES Passeport(Id)
);

ALTER TABLE Visa DROP COLUMN IF EXISTS Id_Passeport;

ALTER TABLE CarteResident ADD COLUMN Liaison INTEGER;

CREATE TABLE DemandeTransfertVisa (
   Id SERIAL,
   Id_Demande INTEGER NOT NULL,
   Id_NouveauPasseport INTEGER NOT NULL, -- TODO: Asina id visa ve ?
   DateCreation TIMESTAMP NOT NULL, 
   PRIMARY KEY(Id),
   FOREIGN KEY (Id_Demande) REFERENCES Demande(Id),
   FOREIGN KEY (Id_NouveauPasseport) REFERENCES Passeport(Id)
);

CREATE TABLE HistoriqueStatutDemandeTransfert (
   Id SERIAL,
   Id_Transfert INTEGER NOT NULL,
   Id_Statut INTEGER NOT NULL,
   DateModification TIMESTAMP NOT NULL,
   PRIMARY KEY(Id),
   FOREIGN KEY(Id_Transfert) REFERENCES DemandeTransfertVisa(Id),
   FOREIGN KEY(Id_Statut) REFERENCES Statut(Id)
);

ALTER TABLE Statut ADD COLUMN Ordre REAL NOT NULL; -- Asina , amzay afaka misy ordre intermediaire