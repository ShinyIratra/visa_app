-- Insertion d'un demandeur
INSERT INTO Demandeur (Nom, Prenom, NumTel, DateNaissance, Adresse, Id_Nationalite, Id_SituationFamiliale) VALUES
('Doe', 'John', '0340000000', '1990-01-01', 'Antananarivo', 1, 1);

-- Insertion de son passeport
INSERT INTO Passeport (Numero, DateDelivrance, DateExpiration, Id_Demandeur) VALUES
('P123456789', '2020-01-01', '2030-01-01', 1);

-- Insertion du visa transformable original avec lequel il est entré
INSERT INTO VisaTransformable (Reference, DateEntree, LieuEntree, DateExpiration, Id_Passeport, Id_Demandeur) VALUES
('VT001', '2025-01-01', 'Aéroport International Ivato', '2025-04-01', 1, 1);

-- Insertion de la demande (Type commun, Categorie Nouveau titre)
INSERT INTO Demande (DateCreation, Id_VisaTransformable, Id_TypeDemande, Id_Passeport, Id_Categorie) VALUES
('2025-02-01', 1, 1, 1, 1);

-- Le visa est accepté, on lui crée donc un Visa 
INSERT INTO Visa (DateCreation, Id_Demande) VALUES
('2025-03-01', 1);

-- Et on le lie à son passeport
INSERT INTO VisaPasseport (Id_Visa, Id_Passeport, DateCreation) VALUES
(1, 1, '2025-03-01');

-- Il reçoit aussi une carte de résident
INSERT INTO CarteResident (DateCreation, Id_Passeport, Id_Demande) VALUES
('2025-03-01', 1, 1);