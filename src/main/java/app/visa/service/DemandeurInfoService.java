package app.visa.service;

import app.visa.entity.*;
import app.visa.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class DemandeurInfoService {

    private final VisaRequestRepository visaRequestRepository;
    private final DemandeTransfertVisaRepository transfertRepository;
    private final DemandeDuplicataRepository duplicataRepository;
    private final VisaRequestService visaRequestService;
    private final TransfertVisaService transfertVisaService;
    private final DuplicataService duplicataService;

    @Transactional(readOnly = true)
    public Map<String, Object> getInfosByNumeroDemande(String numero) {
        // 1. Get demandeur
        Demandeur demandeur = findDemandeurByNumeroDemande(numero);
        if (demandeur == null) {
            throw new IllegalArgumentException("Aucun demandeur trouvé pour le numéro de demande : " + numero);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("demandeur", Map.of(
            "id", demandeur.getId(),
            "nom", demandeur.getNom(),
            "prenom", demandeur.getPrenom() != null ? demandeur.getPrenom() : ""
        ));

        // 2. Get all demandes (Transformation, Transfert, Duplicata)
        List<Map<String, Object>> toutesLesDemandes = new ArrayList<>();

        // Transformation (Nouveau Titre)
        List<Demande> transformations = visaRequestRepository.findByPasseportDemandeurId(demandeur.getId());
        for (Demande d : transformations) {
            toutesLesDemandes.add(Map.of(
                "type", "TRANSFORMATION",
                "numero", d.getNumero(),
                "dateCreation", d.getDateCreation(),
                "statut", getStatus(d)
            ));
        }

        // Transfert
        List<DemandeTransfertVisa> transferts = transfertRepository.findByDemandePasseportDemandeurId(demandeur.getId());
        for (DemandeTransfertVisa t : transferts) {
            toutesLesDemandes.add(Map.of(
                "type", "TRANSFERT",
                "numero", t.getNumero(),
                "dateCreation", t.getDateCreation(),
                "statut", getStatus(t)
            ));
        }

        // Duplicata
        List<DemandeDuplicata> duplicatas = duplicataRepository.findByDemandePasseportDemandeurId(demandeur.getId());
        for (DemandeDuplicata d : duplicatas) {
            toutesLesDemandes.add(Map.of(
                "type", "DUPLICATA",
                "numero", d.getNumero(),
                "dateCreation", d.getDateCreation(),
                "statut", getStatus(d)
            ));
        }

        // Tri decroissante fosiny hatreto
        toutesLesDemandes.sort((a, b) -> ((java.time.LocalDateTime) b.get("dateCreation"))
                .compareTo((java.time.LocalDateTime) a.get("dateCreation")));

        result.put("demandes", toutesLesDemandes);

        return result;
    }

    private Demandeur findDemandeurByNumeroDemande(String numero) {
        Optional<Demande> d = visaRequestRepository.findByNumero(numero);
        if (d.isPresent()) return d.get().getPasseport().getDemandeur();

        Optional<DemandeTransfertVisa> t = transfertRepository.findByNumero(numero);
        if (t.isPresent()) return t.get().getDemande().getPasseport().getDemandeur();

        Optional<DemandeDuplicata> dup = duplicataRepository.findByNumero(numero);
        if (dup.isPresent()) return dup.get().getDemande().getPasseport().getDemandeur();

        return null;
    }

    private String getStatus(Object entity) {
        if (entity instanceof Demande d) {
            return visaRequestService.getDernierStatus(d.getId());
        } else if (entity instanceof DemandeTransfertVisa t) {
            Statut s = transfertVisaService.getStatut(t);
            return s != null ? s.getLibelle() : "Aucun";
        } else if (entity instanceof DemandeDuplicata dup) {
            Statut s = duplicataService.getStatut(dup);
            return s != null ? s.getLibelle() : "Aucun";
        }
        return "Inconnu";
    }
}
