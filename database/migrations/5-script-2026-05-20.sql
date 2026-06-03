DROP TRIGGER IF EXISTS trg_gen_numero_transfert ON DemandeTransfertVisa;
DROP TRIGGER IF EXISTS trg_gen_numero_duplicata ON DemandeDuplicata;

DROP TABLE IF EXISTS HistoriqueStatutDemandeTransfert;
DROP TABLE IF EXISTS HistoriqueStatutDemandeDuplicata;

-- Fantako fa tsy mety le midrop table
-- fa amiko izao izy no mora vakiana noho ilay mialter table im-be dia be

ALTER TABLE Demande DROP COLUMN Id_VisaTransformable;
ALTER TABLE Demande DROP COLUMN Id_TypeDemande;
ALTER TABLE Demande DROP COLUMN Id_Passeport;

DROP TABLE IF EXISTS DemandeTransfertVisa;
DROP TABLE IF EXISTS DemandeDuplicata;

CREATE TABLE DemandeTransfertVisa (
   Id SERIAL PRIMARY KEY,
   Id_Demande INTEGER NOT NULL,
   Id_DemandeOrigine INTEGER NOT NULL,
   Id_NouveauPasseport INTEGER NOT NULL,
   FOREIGN KEY(Id_Demande) REFERENCES Demande(Id),
   FOREIGN KEY(Id_DemandeOrigine) REFERENCES Demande(Id),
   FOREIGN KEY(Id_NouveauPasseport) REFERENCES Passeport(Id)
);

CREATE TABLE DemandeDuplicata (
   Id SERIAL PRIMARY KEY,
   Id_Demande INTEGER NOT NULL,
   Id_DemandeOrigine INTEGER NOT NULL,
   FOREIGN KEY(Id_Demande) REFERENCES Demande(Id),
   FOREIGN KEY(Id_DemandeOrigine) REFERENCES Demande(Id)
);

CREATE TABLE DemandeNouveauTitre (
   Id SERIAL PRIMARY KEY,
   Id_Demande INTEGER UNIQUE NOT NULL,
   
   Id_VisaTransformable INTEGER NOT NULL,
   FOREIGN KEY(Id_VisaTransformable) REFERENCES VisaTransformable(Id),

   Id_Passeport INTEGER NOT NULL,
   FOREIGN KEY(Id_Passeport) REFERENCES Passeport(Id),
   
   Id_TypeDemande INTEGER NOT NULL,
   FOREIGN KEY(Id_TypeDemande) REFERENCES TypeDemande(Id),

   FOREIGN KEY(Id_Demande) REFERENCES Demande(Id),
   FOREIGN KEY(Id_TypeDemande) REFERENCES TypeDemande(Id)
);
