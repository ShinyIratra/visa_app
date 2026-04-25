CREATE Table LiaisonSansDonneeAnterieur(
   Id SERIAL,
   Identifiant INTEGER NOT NULL,
   Id_Demande INTEGER NOT NULL,
   PRIMARY KEY(Id),
   FOREIGN KEY(Id_Demande) REFERENCES Demande(Id)
);