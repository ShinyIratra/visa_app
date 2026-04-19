INSERT INTO SituationFamiliale (Libelle) VALUES
('Celibataire'),
('Marie(e)'),
('Divorce(e)'),
('Veuf(ve)');

INSERT INTO Nationalite (Libelle) VALUES
('Americaine'),
('Allemande'),
('Francaise');

INSERT INTO Categorie (Libelle) VALUES
('Nouveau titre'),
('Transfert'),
('Duplicata');

INSERT INTO TypeDemande (Libelle) VALUES
('Commun'),
('Investisseur'),
('Travailleur');

INSERT INTO Dossier (Libelle, Obligatoire, Id_TypeDemande) VALUES
('2 photos d''identité', true, 1),
('Notice de renseignement', false, 1),
('Demande adressée à Mr le Ministère de l''Intérieur et de la Décentralisation avec adresse e-mail et numéro téléphone portable', true, 1),
('Photocopie certifiée du visa en cours de validité', true, 1),
('Photocopie certifiée de la première page du passeport', true, 1),
('Photocopie certifiée de la carte résident en cours de validité', true, 1),
('Certificat de résidence à Madagascar', true, 1),
('Extrait de casier judiciaire moins de 3 mois', true, 1),

('Statut de la Société', true, 2),
('Extrait d''inscription au registre de commerce', true, 2),
('Carte fiscale', true, 2),

('Autorisation d''emploi délivrée à Madagascar par le Ministère de la Fonction publique', true, 3),
('Attestation d''emploi délivré par l''employeur (Original)', true, 3);

INSERT INTO Statut (Libelle) VALUES
('Demande créée'),
('Scan terminé'),
('Visa accepté');

