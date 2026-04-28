package app.visa.service;

import java.util.*;
import app.visa.entity.*;
import app.visa.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ScanService {

    private final VisaRequestRepository visaRequestRepository;
    private final DossierRepository dossierRepository;
    private final ReponseStatutVisaRepository reponseStatutVisaRepository;
    private final StatutRepository statutRepository;
    private final HistoriqueStatutRepository historiqueStatutRepository;
    private final DemandeService demandeService;

    /**
     * CONTRÔLE 1: Vérifie que tous les dossiers (obligatoires ET facultatifs) 
     * de la demande sont cochés dans ReponseStatutVisa.
     */
    @Transactional(readOnly = true)
    public boolean controleAllDossiersCoches(Integer demandeId) {
        Demande demande = visaRequestRepository.findById(demandeId)
            .orElseThrow(() -> new IllegalArgumentException("Demande introuvable: " + demandeId));

        List<ReponseStatutVisa> reponses = reponseStatutVisaRepository.findByDemandeId(demandeId);
        
        for (ReponseStatutVisa r : reponses) {
            if (!Boolean.TRUE.equals(r.getValeur())) {
                return false;
            }
        }
        
        return !reponses.isEmpty();
    }

    public boolean controleChampsobligatoires(Map<String, Object> formData) {
        if (formData == null) {
            return false;
        }

        Map<String, Object> etatCivil = UtilService.getBloc(formData, "etat civil");
        if (!checkChampString(etatCivil, "nom") ||
            !checkChampString(etatCivil, "prenom") ||
            !checkChampString(etatCivil, "numTel") ||
            !checkChampString(etatCivil, "adresse") ||
            !checkChampString(etatCivil, "dateNaissance") ||
            !checkChampId(etatCivil, "nationalite") ||
            !checkChampId(etatCivil, "situationFamiliale")) {
            return false;
        }

        Map<String, Object> passeport = UtilService.getBloc(formData, "passeport");
        if (!checkChampString(passeport, "numero") ||
            !checkChampString(passeport, "dateDelivrance") ||
            !checkChampString(passeport, "dateExpiration")) {
            return false;
        }

        Map<String, Object> visaTransformable = UtilService.getBloc(formData, "visaTransformable");
        if (!checkChampString(visaTransformable, "reference") ||
            !checkChampString(visaTransformable, "dateEntree") ||
            !checkChampString(visaTransformable, "lieuEntree") ||
            !checkChampString(visaTransformable, "dateExpiration")) {
            return false;
        }

        Object dossiersFournisObj = formData.get("dossiersFournis");
        if (!(dossiersFournisObj instanceof List<?> dossiersListe) || dossiersListe.isEmpty()) {
            return false;
        }

        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public void marquerScanTermine(Integer demandeId) {
        Demande demande = visaRequestRepository.findById(demandeId)
            .orElseThrow(() -> new IllegalArgumentException("Demande introuvable: " + demandeId));

        // Créer un historique avec le nouveau statut
        Statut statut = statutRepository.findByLibelle(UtilService.STATUS_SCAN_TERMINE)
            .orElseThrow(() -> new IllegalArgumentException("Statut 'Scan terminé' introuvable"));

        HistoriqueStatut historique = new HistoriqueStatut();
        historique.setDemande(demande);
        historique.setStatut(statut);
        historique.setDateModification(java.time.LocalDateTime.now());

        historiqueStatutRepository.save(historique);
    }

    /**
     * Récupère les dossiers applicables pour une demande (pour l'affichage sur la page de scan).
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getDossiersAvecStatut(Integer demandeId) {
        Demande demande = visaRequestRepository.findById(demandeId)
            .orElseThrow(() -> new IllegalArgumentException("Demande introuvable: " + demandeId));

        List<Dossier> dossiers = dossierRepository.findDossiersPourTypeDemande(demande.getTypeDemande().getId());
        List<ReponseStatutVisa> reponses = reponseStatutVisaRepository.findByDemandeId(demandeId);

        Map<Integer, Boolean> reponseMap = new HashMap<>();
        for (ReponseStatutVisa r : reponses) {
            reponseMap.put(r.getDossier().getId(), r.getValeur());
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Dossier d : dossiers) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", d.getId());
            item.put("libelle", d.getLibelle());
            item.put("obligatoire", d.getObligatoire());
            item.put("coche", reponseMap.getOrDefault(d.getId(), false));
            result.add(item);
        }

        return result;
    }

    // ==================== HELPERS PRIVÉS ====================

    private boolean checkChampString(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val == null) {
            return false;
        }
        String str = val.toString().trim();
        return !str.isEmpty();
    }

    private boolean checkChampId(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val == null) {
            return false;
        }
        String str = val.toString().trim();
        if (str.isEmpty()) {
            return false;
        }
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
