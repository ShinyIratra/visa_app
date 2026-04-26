package app.visa.service;

import app.visa.entity.Demande;
import app.visa.dto.VisaRequestDto;
import app.visa.entity.Passeport;
import app.visa.entity.VisaTransformable;
import app.visa.entity.Demandeur;
import app.visa.entity.Nationalite;
import app.visa.entity.SituationFamiliale;
import app.visa.repository.DemandeurRepository;
import app.visa.repository.NationaliteRepository;
import app.visa.repository.SituationFamilialeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;


public class UtilService {

    public static final String STATUS_DEMANDE_CREEE = "Demande creee";
    public static final String STATUS_SCAN_TERMINE = "Scan termine";
    public static final String STATUS_VISA_ACCEPTE = "Visa accepte";
    public static final String STATUS_DEMANDE_ACCEPTEE = "Demande acceptee";

    public static Map<String, Object> buildDebugData(Demandeur demandeur, Passeport passeport, VisaTransformable vt, Demande demande, List<Integer> dossiersIds) {
        Map<String, Object> debugData = new HashMap<>();

        Map<String, Object> demandeurMap = new HashMap<>();
        demandeurMap.put("nom", demandeur.getNom());
        demandeurMap.put("prenom", demandeur.getPrenom());
        demandeurMap.put("nomJeuneFille", demandeur.getNomJeuneFille());
        demandeurMap.put("email", demandeur.getEmail());
        demandeurMap.put("numTel", demandeur.getNumTel());
        demandeurMap.put("adresse", demandeur.getAdresse());
        demandeurMap.put("dateNaissance", demandeur.getDateNaissance() != null ? demandeur.getDateNaissance().toString() : null);
        demandeurMap.put("nationalite", demandeur.getNationalite() != null ? demandeur.getNationalite().getLibelle() : null);
        demandeurMap.put("situationFamiliale", demandeur.getSituationFamiliale() != null ? demandeur.getSituationFamiliale().getLibelle() : null);

        Map<String, Object> passeportMap = new HashMap<>();
        passeportMap.put("numero", passeport.getNumero());
        passeportMap.put("dateDelivrance", passeport.getDateDelivrance() != null ? passeport.getDateDelivrance().toString() : null);
        passeportMap.put("dateExpiration", passeport.getDateExpiration() != null ? passeport.getDateExpiration().toString() : null);

        Map<String, Object> vtMapOut = new HashMap<>();
        vtMapOut.put("reference", vt.getReference());
        vtMapOut.put("dateEntree", vt.getDateEntree() != null ? vt.getDateEntree().toString() : null);
        vtMapOut.put("lieuEntree", vt.getLieuEntree());
        vtMapOut.put("dateExpiration", vt.getDateExpiration() != null ? vt.getDateExpiration().toString() : null);

        Map<String, Object> demandeMap = new HashMap<>();
        demandeMap.put("dateCreation", demande.getDateCreation() != null ? demande.getDateCreation().toString() : null);
        demandeMap.put("typeDemande", demande.getTypeDemande() != null ? demande.getTypeDemande().getLibelle() : null);
        demandeMap.put("categorie", demande.getCategorie() != null ? demande.getCategorie().getLibelle() : null);

        debugData.put("demandeur", demandeurMap);
        debugData.put("passeport", passeportMap);
        debugData.put("visaTransformable", vtMapOut);
        debugData.put("demande", demandeMap);
        debugData.put("dossiersIds", dossiersIds);

        return debugData;
    }

    public static Map<String, Object> getBloc(Map<String, Object> donnees, String nomBloc) {
        Object bloc = donnees.get(nomBloc);
        if (!(bloc instanceof Map<?, ?>)) {
            return new java.util.LinkedHashMap<>();
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) bloc;
        return map;
    }
}