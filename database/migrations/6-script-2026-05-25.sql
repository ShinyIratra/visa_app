CREATE TABLE FichierDossier (
    Id SERIAL PRIMARY KEY,
    Id_Dossier INTEGER NOT NULL,
    Id_Demande INTEGER NOT NULL,
    CheminFichier TEXT NOT NULL,
    DateModification TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (Id_Dossier, Id_Demande) REFERENCES ReponseStatutVisa(Id_Dossier, Id_Demande)
);
