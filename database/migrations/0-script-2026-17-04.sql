CREATE TABLE SituationFamiliale(
   Id SERIAL,
   Libelle VARCHAR(100)  NOT NULL,
   PRIMARY KEY(Id)
);

CREATE TABLE Nationalite(
   Id SERIAL,
   Libelle VARCHAR(200)  NOT NULL,
   PRIMARY KEY(Id)
);

-- Nouveau titre, transfert, duplicata
CREATE TABLE Categorie(
   Id SERIAL,
   Libelle VARCHAR(200)  NOT NULL,
   PRIMARY KEY(Id)
);

-- travailleur, investisseur
CREATE TABLE TypeDemande(
   Id SERIAL,
   Libelle VARCHAR(50) ,
   PRIMARY KEY(Id)
);

CREATE TABLE Dossier(
   Id SERIAL,
   Libelle TEXT NOT NULL,
   Obligatoire BOOLEAN,
   Id_TypeDemande INTEGER NOT NULL,
   PRIMARY KEY(Id),
   FOREIGN KEY(Id_TypeDemande) REFERENCES TypeDemande(Id)
);

-- demande créée, scan terminé, visa accepté
CREATE TABLE Statut(
   Id SERIAL,
   Libelle VARCHAR(100)  NOT NULL,
   PRIMARY KEY(Id)
);

CREATE TABLE Demandeur(
   Id SERIAL,
   Nom VARCHAR(100)  NOT NULL,
   Prenom VARCHAR(100) ,
   NomJeuneFille VARCHAR(100) ,
   Email VARCHAR(200) ,
   NumTel VARCHAR(50)  NOT NULL,
   DateNaissance DATE NOT NULL,
   Adresse VARCHAR(255)  NOT NULL,
   Id_Nationalite INTEGER NOT NULL,
   Id_SituationFamiliale INTEGER NOT NULL,
   PRIMARY KEY(Id),
   FOREIGN KEY(Id_Nationalite) REFERENCES Nationalite(Id),
   FOREIGN KEY(Id_SituationFamiliale) REFERENCES SituationFamiliale(Id)
);

CREATE TABLE Passeport(
   Id SERIAL,
   Numero VARCHAR(50)  NOT NULL,
   DateDelivrance TIMESTAMP NOT NULL,
   DateExpiration TIMESTAMP NOT NULL,
   Id_Demandeur INTEGER NOT NULL,
   PRIMARY KEY(Id),
   FOREIGN KEY(Id_Demandeur) REFERENCES Demandeur(Id)
);

CREATE TABLE VisaTransformable(
   Id SERIAL,
   Reference VARCHAR(50)  NOT NULL,
   DateEntree TIMESTAMP NOT NULL,
   LieuEntree VARCHAR(200)  NOT NULL,
   DateExpiration TIMESTAMP NOT NULL,
   Id_Passeport INTEGER NOT NULL,
   Id_Demandeur INTEGER NOT NULL,
   PRIMARY KEY(Id),
   FOREIGN KEY(Id_Passeport) REFERENCES Passeport(Id),
   FOREIGN KEY(Id_Demandeur) REFERENCES Demandeur(Id)
);

CREATE TABLE Demande(
   Id SERIAL,
   DateCreation TIMESTAMP NOT NULL,
   Id_VisaTransformable INTEGER NOT NULL,
   Id_TypeDemande INTEGER NOT NULL,
   Id_Passeport INTEGER NOT NULL,
   Id_Categorie INTEGER NOT NULL,
   PRIMARY KEY(Id),
   FOREIGN KEY(Id_VisaTransformable) REFERENCES VisaTransformable(Id),
   FOREIGN KEY(Id_TypeDemande) REFERENCES TypeDemande(Id),
   FOREIGN KEY(Id_Passeport) REFERENCES Passeport(Id),
   FOREIGN KEY(Id_Categorie) REFERENCES Categorie(Id)
);

CREATE TABLE Visa(
   Id SERIAL,
   DateCreation TIMESTAMP NOT NULL,
   Id_Passeport INTEGER NOT NULL,
   Id_Demande INTEGER NOT NULL,
   PRIMARY KEY(Id),
   FOREIGN KEY(Id_Passeport) REFERENCES Passeport(Id),
   FOREIGN KEY(Id_Demande) REFERENCES Demande(Id)
);

CREATE TABLE CarteResident(
   Id SERIAL,
   DateCreation TIMESTAMP NOT NULL,
   Id_Passeport INTEGER NOT NULL,
   Id_Demande INTEGER NOT NULL,
   PRIMARY KEY(Id),
   FOREIGN KEY(Id_Passeport) REFERENCES Passeport(Id),
   FOREIGN KEY(Id_Demande) REFERENCES Demande(Id)
);

CREATE TABLE ReponseStatutVisa(
   Id_Dossier INTEGER,
   Id_Demande INTEGER,
   Valeur BOOLEAN NOT NULL,
   PRIMARY KEY(Id_Dossier, Id_Demande),
   FOREIGN KEY(Id_Dossier) REFERENCES Dossier(Id),
   FOREIGN KEY(Id_Demande) REFERENCES Demande(Id)
);

CREATE TABLE HistoriqueStatut(
   Id_Demande INTEGER,
   Id_Statut INTEGER,
   DateModification TIMESTAMP NOT NULL,
   PRIMARY KEY(Id_Demande, Id_Statut),
   FOREIGN KEY(Id_Demande) REFERENCES Demande(Id),
   FOREIGN KEY(Id_Statut) REFERENCES Statut(Id)
);
