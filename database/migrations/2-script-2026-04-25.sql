CREATE Table LiaisonSansDonneeAnterieur(
   Id SERIAL,
   Identifiant INTEGER NOT NULL,
   Id_Demande INTEGER NOT NULL,
   PRIMARY KEY(Id),
   FOREIGN KEY(Id_Demande) REFERENCES Demande(Id)
);

-- Many to many VISA - PASSEPORT
CREATE TABLE IF NOT EXISTS visa_passeport (
   Id_Visa INTEGER NOT NULL,
   Id_Passeport INTEGER NOT NULL,
   DateCreation TIMESTAMP NOT NULL,
   PRIMARY KEY (Id_Visa, Id_Passeport),
   FOREIGN KEY (Id_Visa) REFERENCES Visa(Id),
   FOREIGN KEY (Id_Passeport) REFERENCES Passeport(Id)
);

ALTER TABLE Visa DROP COLUMN IF EXISTS Id_Passeport;