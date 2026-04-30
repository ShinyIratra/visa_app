package app.visa.service;

import app.visa.entity.*;
import app.visa.repository.*;
import app.visa.dto.demande.DemandeDto;
import app.visa.dto.demande.HistoriqueDto;
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
        Map<String, Object> infos = buildDemandeurInfos(demandeur, numero);
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

    private Map<String, Object> buildDemandeurInfos(Demandeur demandeur, String numeroPrioritaire) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("demandeur", Map.of(
            "id", demandeur.getId(),
            "nom", demandeur.getNom(),
            "prenom", demandeur.getPrenom() != null ? demandeur.getPrenom() : ""
        ));

        // 2. Get all demandes (Transformation, Transfert, Duplicata)
        List<DemandeDto> toutesLesDemandes = new ArrayList<>();

        toutesLesDemandes.addAll(getTransformations(demandeur.getId()));
        toutesLesDemandes.addAll(getTransferts(demandeur.getId()));
        toutesLesDemandes.addAll(getDuplicatas(demandeur.getId()));

        // Tri decroissante, fa demande.numero == numeroPrioritaire no mandeha aloha
        toutesLesDemandes.sort((a, b) -> {
            String numA = a.getNumero();
            String numB = b.getNumero();

            if (numeroPrioritaire != null && numeroPrioritaire.equals(numA)) 
                return -1;
            if (numeroPrioritaire != null && numeroPrioritaire.equals(numB)) 
                return 1;

            return b.getDateCreation().compareTo(a.getDateCreation());
        });

        result.put("demandes", toutesLesDemandes);

        return result;
    }

    private List<DemandeDto> getTransformations(Integer demandeurId) {
        List<Demande> transformations = visaRequestRepository.findByPasseportDemandeurId(demandeurId);
        List<DemandeDto> result = new ArrayList<>();
        for (Demande d : transformations) {
            result.add(new DemandeDto(
                "TRANSFORMATION",
                d.getNumero(),
                d.getDateCreation(),
                getStatus(d),
                formatHistoriqueTransformation(d)
            ));
        }
        return result;
    }

    private List<DemandeDto> getTransferts(Integer demandeurId) {
        List<DemandeTransfertVisa> transferts = transfertRepository.findByDemandePasseportDemandeurId(demandeurId);
        List<DemandeDto> result = new ArrayList<>();
        for (DemandeTransfertVisa t : transferts) {
            result.add(new DemandeDto(
                "TRANSFERT",
                t.getNumero(),
                t.getDateCreation(),
                getStatus(t),
                formatHistoriqueTransfert(t)
            ));
        }
        return result;
    }

    private List<DemandeDto> getDuplicatas(Integer demandeurId) {
        List<DemandeDuplicata> duplicatas = duplicataRepository.findByDemandePasseportDemandeurId(demandeurId);
        List<DemandeDto> result = new ArrayList<>();
        for (DemandeDuplicata d : duplicatas) {
            result.add(new DemandeDto(
                "DUPLICATA",
                d.getNumero(),
                d.getDateCreation(),
                getStatus(d),
                formatHistoriqueDuplicata(d)
            ));
        }
        return result;
    }

    private List<HistoriqueDto> formatHistoriqueTransformation(Demande d) {
        if (d.getHistoriques() == null) return Collections.emptyList();
        return d.getHistoriques().stream()
            .sorted(Comparator.comparing(HistoriqueStatut::getDateModification).reversed())
            .map(h -> new HistoriqueDto(h.getStatut().getLibelle(), h.getDateModification()))
            .toList();
    }

    private List<HistoriqueDto> formatHistoriqueTransfert(DemandeTransfertVisa t) {
        if (t.getHistoriques() == null) return Collections.emptyList();
        return t.getHistoriques().stream()
            .sorted(Comparator.comparing(HistoriqueStatutDemandeTransfert::getDateModification).reversed())
            .map(h -> new HistoriqueDto(h.getStatut().getLibelle(), h.getDateModification()))
            .toList();
    }

    private List<HistoriqueDto> formatHistoriqueDuplicata(DemandeDuplicata d) {
        if (d.getHistoriques() == null) 
            return Collections.emptyList();

        return d.getHistoriques().stream()
            .sorted(Comparator.comparing(HistoriqueStatutDemandeDuplicata::getDateModification).reversed())
            .map(h -> new HistoriqueDto(h.getStatut().getLibelle(), h.getDateModification()))
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
