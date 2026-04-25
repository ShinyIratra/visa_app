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
('2 photos d''identite', true, 1),
('Notice de renseignement', false, 1),
('Demande adressee à Mr le Ministère de l''Interieur et de la Decentralisation avec adresse e-mail et numero telephone portable', true, 1),
('Photocopie certifiee du visa en cours de validite', true, 1),
('Photocopie certifiee de la première page du passeport', true, 1),
('Photocopie certifiee de la carte resident en cours de validite', true, 1),
('Certificat de residence à Madagascar', true, 1),
('Extrait de casier judiciaire moins de 3 mois', true, 1),

('Statut de la Societe', true, 2),
('Extrait d''inscription au registre de commerce', true, 2),
('Carte fiscale', true, 2),

('Autorisation d''emploi delivree à Madagascar par le Ministère de la Fonction publique', true, 3),
('Attestation d''emploi delivre par l''employeur (Original)', true, 3);

INSERT INTO Statut (Libelle) VALUES
('Demande creee'),
('Scan termine'),
('Visa accepte');

