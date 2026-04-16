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

CREATE TABLE Passeport(
   Id SERIAL,
   Numero VARCHAR(50)  NOT NULL,
   DateDelivrance TIMESTAMP NOT NULL,
   DateExpiration TIMESTAMP NOT NULL,
   PRIMARY KEY(Id)
);

CREATE TABLE VisaTransformable(
   Id SERIAL,
   Reference VARCHAR(50)  NOT NULL,
   DateEntree TIMESTAMP NOT NULL,
   LieuEntree VARCHAR(200)  NOT NULL,
   DateExpiration TIMESTAMP NOT NULL,
   PRIMARY KEY(Id)
);

CREATE TABLE Categorie(
   Id SERIAL,
   Libelle VARCHAR(200)  NOT NULL,
   PRIMARY KEY(Id)
);

CREATE TABLE StatutVisa(
   Id SERIAL,
   Libelle VARCHAR(50) ,
   PRIMARY KEY(Id)
);

CREATE TABLE Dossier(
   Id SERIAL,
   Libelle TEXT NOT NULL,
   Id_StatutVisa INTEGER NOT NULL,
   PRIMARY KEY(Id),
   FOREIGN KEY(Id_StatutVisa) REFERENCES StatutVisa(Id)
);

CREATE TABLE EtatCivil(
   Id SERIAL,
   Nom VARCHAR(100)  NOT NULL,
   Prenom VARCHAR(100)  NOT NULL,
   NomJeuneFille VARCHAR(100) ,
   Email VARCHAR(200)  NOT NULL,
   NumTel VARCHAR(50)  NOT NULL,
   DateNaissance DATE NOT NULL,
   Profession VARCHAR(100)  NOT NULL,
   Adresse VARCHAR(255)  NOT NULL,
   Id_Nationalite INTEGER NOT NULL,
   Id_SituationFamiliale INTEGER NOT NULL,
   PRIMARY KEY(Id),
   FOREIGN KEY(Id_Nationalite) REFERENCES Nationalite(Id),
   FOREIGN KEY(Id_SituationFamiliale) REFERENCES SituationFamiliale(Id)
);

CREATE TABLE RequeteVisa(
   Id SERIAL,
   Id_StatutVisa INTEGER NOT NULL,
   Id_Passeport INTEGER NOT NULL,
   Id_Categorie INTEGER NOT NULL,
   Id_VisaTransformable INTEGER NOT NULL,
   Id_EtatCivil INTEGER NOT NULL,
   PRIMARY KEY(Id),
   FOREIGN KEY(Id_StatutVisa) REFERENCES StatutVisa(Id),
   FOREIGN KEY(Id_Passeport) REFERENCES Passeport(Id),
   FOREIGN KEY(Id_Categorie) REFERENCES Categorie(Id),
   FOREIGN KEY(Id_VisaTransformable) REFERENCES VisaTransformable(Id),
   FOREIGN KEY(Id_EtatCivil) REFERENCES EtatCivil(Id)
);

CREATE TABLE ReponseStatutVisa(
   Id_Dossier INTEGER,
   Id_RequeteVisa INTEGER,
   Valeur BOOLEAN NOT NULL,
   PRIMARY KEY(Id_Dossier, Id_RequeteVisa),
   FOREIGN KEY(Id_Dossier) REFERENCES Dossier(Id),
   FOREIGN KEY(Id_RequeteVisa) REFERENCES RequeteVisa(Id)
);