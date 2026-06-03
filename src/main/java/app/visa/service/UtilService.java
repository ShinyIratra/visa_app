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
    public static final String CATEGORIE_DEMANDE_NOUVEAU_TITRE = "Nouveau titre";


    public static final String STATUS_DEMANDE_CREEE = "Demande creee";
    public static final String STATUS_PHOTO_SCANNEE = "Photo scannee";
    public static final String STATUS_SCAN_TERMINE = "Scan termine";
    public static final String STATUS_VISA_ACCEPTE = "Visa accepte";
    public static final String STATUS_DEMANDE_ACCEPTEE = "Demande acceptee";

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