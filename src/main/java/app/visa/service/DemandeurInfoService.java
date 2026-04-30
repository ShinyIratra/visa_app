package app.visa.service;

import app.visa.entity.*;
import app.visa.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.time.*;

@Service
@RequiredArgsConstructor
public class DemandeurInfoService {

    private final VisaRequestRepository visaRequestRepository;
    private final DemandeTransfertVisaRepository transfertRepository;
    private final DemandeDuplicataRepository duplicataRepository;
    private final VisaRequestService visaRequestService;
    private final TransfertVisaService transfertVisaService;
    private final DuplicataService duplicataService;
    private final PasseportRepository passeportRepository;

    @Transactional(readOnly = true)
    public Map<String, Object> getInfos(String numero) {
        Demandeur demandeur = getDemandeurByNumero(numero);
        Map<String, Object> infos = buildDemandeurInfos(demandeur);
        return infos;
    }

    private Demandeur getDemandeurByNumero(String numero) {
        Demandeur demandeur;
        
        // DEM-..., DEMTRF-..., DEMDUP-...
        if (numero != null && numero.toUpperCase().startsWith("DEM")) {
            demandeur = findDemandeurByNumeroDemande(numero);
            if (demandeur == null) {
                throw new IllegalArgumentException("Aucun demandeur trouve pour le numero de demande : " + numero);
            }
        } else {
            Passeport passeport = passeportRepository.findByNumero(numero).orElse(null);
            if (passeport == null || passeport.getDemandeur() == null) {
                throw new IllegalArgumentException("Aucun demandeur trouve pour le numero de passeport : " + numero);
            }
            demandeur = passeport.getDemandeur();
        }

        return demandeur;
    }

    private Map<String, Object> buildDemandeurInfos(Demandeur demandeur) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("demandeur", Map.of(
            "id", demandeur.getId(),
            "nom", demandeur.getNom(),
            "prenom", demandeur.getPrenom() != null ? demandeur.getPrenom() : ""
        ));

        // 2. Get all demandes (Transformation, Transfert, Duplicata)
        List<Map<String, Object>> toutesLesDemandes = new ArrayList<>();

        toutesLesDemandes.addAll(getTransformations(demandeur.getId()));
        toutesLesDemandes.addAll(getTransferts(demandeur.getId()));
        toutesLesDemandes.addAll(getDuplicatas(demandeur.getId()));

        // Tri decroissante fosiny hatreto
        toutesLesDemandes.sort((a, b) -> ((LocalDateTime) b.get("dateCreation"))
                .compareTo((LocalDateTime) a.get("dateCreation")));

        result.put("demandes", toutesLesDemandes);

        return result;
    }

    private List<Map<String, Object>> getTransformations(Integer demandeurId) {
        List<Demande> transformations = visaRequestRepository.findByPasseportDemandeurId(demandeurId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Demande d : transformations) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("type", "TRANSFORMATION");
            map.put("numero", d.getNumero());
            map.put("dateCreation", d.getDateCreation());
            map.put("statut", getStatus(d));
            map.put("historique", formatHistoriqueTransformation(d));
            result.add(map);
        }
        return result;
    }

    private List<Map<String, Object>> getTransferts(Integer demandeurId) {
        List<DemandeTransfertVisa> transferts = transfertRepository.findByDemandePasseportDemandeurId(demandeurId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (DemandeTransfertVisa t : transferts) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("type", "TRANSFERT");
            map.put("numero", t.getNumero());
            map.put("dateCreation", t.getDateCreation());
            map.put("statut", getStatus(t));
            map.put("historique", formatHistoriqueTransfert(t));
            result.add(map);
        }
        return result;
    }

    private List<Map<String, Object>> getDuplicatas(Integer demandeurId) {
        List<DemandeDuplicata> duplicatas = duplicataRepository.findByDemandePasseportDemandeurId(demandeurId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (DemandeDuplicata d : duplicatas) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("type", "DUPLICATA");
            map.put("numero", d.getNumero());
            map.put("dateCreation", d.getDateCreation());
            map.put("statut", getStatus(d));
            map.put("historique", formatHistoriqueDuplicata(d));
            result.add(map);
        }
        return result;
    }

    private List<Map<String, Object>> formatHistoriqueTransformation(Demande d) {
        if (d.getHistoriques() == null) return Collections.emptyList();
        return d.getHistoriques().stream()
            .sorted(Comparator.comparing(HistoriqueStatut::getDateModification).reversed())
            .map(h -> {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("statut", h.getStatut().getLibelle());
                map.put("date", h.getDateModification());
                return map;
            })
            .toList();
    }

    private List<Map<String, Object>> formatHistoriqueTransfert(DemandeTransfertVisa t) {
        if (t.getHistoriques() == null) return Collections.emptyList();
        return t.getHistoriques().stream()
            .sorted(Comparator.comparing(HistoriqueStatutDemandeTransfert::getDateModification).reversed())
            .map(h -> {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("statut", h.getStatut().getLibelle());
                map.put("date", h.getDateModification());
                return map;
            })
            .toList();
    }

    private List<Map<String, Object>> formatHistoriqueDuplicata(DemandeDuplicata d) {
        if (d.getHistoriques() == null) 
            return Collections.emptyList();

        return d.getHistoriques().stream()
            .sorted(Comparator.comparing(HistoriqueStatutDemandeDuplicata::getDateModification).reversed())
            .map(h -> {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("statut", h.getStatut().getLibelle());
                map.put("date", h.getDateModification());
                return map;
            })
            .toList();
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
