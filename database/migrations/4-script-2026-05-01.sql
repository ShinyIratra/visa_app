ALTER TABLE Demande ADD COLUMN Numero VARCHAR(50) UNIQUE;
ALTER TABLE DemandeTransfertVisa ADD COLUMN Numero VARCHAR(50) UNIQUE;
ALTER TABLE DemandeDuplicata ADD COLUMN Numero VARCHAR(50) UNIQUE;
ALTER TABLE Visa ADD COLUMN Numero VARCHAR(50) UNIQUE;
ALTER TABLE CarteResident ADD COLUMN Numero VARCHAR(50) UNIQUE;

CREATE OR REPLACE FUNCTION generer_numero()
RETURNS TRIGGER AS $$
DECLARE
    prefix TEXT;
BEGIN
    CASE TG_TABLE_NAME
        WHEN 'demande' THEN prefix := 'DEM';
        WHEN 'demandetransfertvisa' THEN prefix := 'DEMTRF';
        WHEN 'demandeduplicata' THEN prefix := 'DEMDUP';
        WHEN 'visa' THEN prefix := 'VISA';
        WHEN 'carteresident' THEN prefix := 'CARTE';
        ELSE prefix := 'UNK';
    END CASE;

    -- LPAD mameno ny banga
    NEW.Numero := prefix || '-' || LPAD(NEW.Id::TEXT, 5, '0');
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_gen_numero_demande
BEFORE INSERT ON Demande
FOR EACH ROW EXECUTE FUNCTION generer_numero();

CREATE TRIGGER trg_gen_numero_transfert
BEFORE INSERT ON DemandeTransfertVisa
FOR EACH ROW EXECUTE FUNCTION generer_numero();

CREATE TRIGGER trg_gen_numero_duplicata
BEFORE INSERT ON DemandeDuplicata
FOR EACH ROW EXECUTE FUNCTION generer_numero();

CREATE TRIGGER trg_gen_numero_visa
BEFORE INSERT ON Visa
FOR EACH ROW EXECUTE FUNCTION generer_numero();

CREATE TRIGGER trg_gen_numero_carte
BEFORE INSERT ON CarteResident
FOR EACH ROW EXECUTE FUNCTION generer_numero();
