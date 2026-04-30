package app.visa.service;

import app.visa.entity.*;
import app.visa.repository.*;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DuplicataService {

    private final VisaRequestService visaRequestService;
    private final AcceptationDemandeVisaService acceptationDemandeVisaService;
    private final DemandeDuplicataRepository demandeDuplicataRepository;
    private final VisaRepository visaRepository;
    private final CarteResidentRepository carteResidentRepository;
    private final StatutRepository statutRepository;
    private final DemandeService demandeService;

    @Transactional(rollbackFor = Exception.class)
    public DemandeDuplicata creerDemandeDuplicataAda(Map<String, Object> donnees) {
        String typeRecherche = (String) donnees.get("type_recherche");
        String valeur = (String) donnees.get("valeur");
        
        LocalDateTime dateCreation = null;
        if (donnees.get("dateCreation") != null && !donnees.get("dateCreation").toString().isBlank()) {
            dateCreation = LocalDateTime.parse(donnees.get("dateCreation").toString());
        }

        Demande demandeOrigine = demandeService.findDemandeByCritere(typeRecherche, valeur);
        
        DemandeDuplicata demandeDuplicata = buildDemandeDuplicata(demandeOrigine, dateCreation);

        return demandeDuplicataRepository.save(demandeDuplicata);
    }

    @Transactional(rollbackFor = Exception.class)
    public DemandeDuplicata creerDemandeDuplicataSda(Map<String, Object> donnees) {
        LocalDateTime dateCreation = null;
        if (donnees.get("dateCreation") != null && !donnees.get("dateCreation").toString().isBlank()) {
            dateCreation = LocalDateTime.parse(donnees.get("dateCreation").toString());
        }

        LocalDateTime dateDebut = null;
        if (donnees.get("dateDebutVisa") != null && !donnees.get("dateDebutVisa").toString().isBlank()) {
            dateDebut = LocalDateTime.parse(donnees.get("dateDebutVisa").toString());
        }
        LocalDateTime dateExpiration = null;
        if (donnees.get("dateExpirationVisa") != null && !donnees.get("dateExpirationVisa").toString().isBlank()) {
            dateExpiration = LocalDateTime.parse(donnees.get("dateExpirationVisa").toString());
        }

        Demande demande = visaRequestService.creerDemandeVisa(donnees, "Nouveau titre", "Visa accepte");
        Visa visa = acceptationDemandeVisaService.creerVisaEtCarteResident(demande, dateDebut, dateExpiration);

        DemandeDuplicata demandeDuplicata = buildDemandeDuplicata(demande, dateCreation);

        return demandeDuplicataRepository.save(demandeDuplicata);
    }

    private DemandeDuplicata buildDemandeDuplicata(Demande demande, LocalDateTime dateCreation) {
        LocalDateTime dateFinale = dateCreation != null ? dateCreation : LocalDateTime.now();

        if (dateFinale.isBefore(demande.getDateCreation())) {
            throw new IllegalArgumentException("La date de demande de duplicata ne peut pas etre anterieure a la date de la demande originale (" + demande.getDateCreation() + ")");
        }

        DemandeDuplicata demandeDuplicata = new DemandeDuplicata();
        demandeDuplicata.setDemande(demande);
        demandeDuplicata.setDateCreation(dateFinale);

        setStatut(demandeDuplicata, "Demande creee", dateFinale);

        return demandeDuplicata;
    }

    public void setStatut(DemandeDuplicata duplicata, String statutLibelle) {
        setStatut(duplicata, statutLibelle, null);
    }

    public void setStatut(DemandeDuplicata duplicata, String statutLibelle, LocalDateTime dateModification) {
        Statut statut = statutRepository.findByLibelle(statutLibelle)
            .orElseThrow(() -> new IllegalArgumentException("statut '" + statutLibelle + "' introuvable"));

        HistoriqueStatutDemandeDuplicata historique = new HistoriqueStatutDemandeDuplicata();
        historique.setDuplicata(duplicata);
        historique.setStatut(statut);
        historique.setDateModification(dateModification != null ? dateModification : LocalDateTime.now());

        if (duplicata.getHistoriques() == null) {
            duplicata.setHistoriques(new ArrayList<>());
        }
        duplicata.getHistoriques().add(historique);

        demandeDuplicataRepository.save(duplicata);
    }

    public Statut getStatut(DemandeDuplicata duplicata) {
        if (duplicata.getHistoriques() == null || duplicata.getHistoriques().isEmpty()) {
            return null;
        }
        return duplicata.getHistoriques().stream()
            .max(Comparator.comparing(HistoriqueStatutDemandeDuplicata::getDateModification))
            .map(HistoriqueStatutDemandeDuplicata::getStatut)
            .orElse(null);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listDuplicataAvecInfos() {
        List<DemandeDuplicata> duplicatas = demandeDuplicataRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();

        for (DemandeDuplicata d : duplicatas) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", d.getId());
            
            Demande dm = d.getDemande();
            if (dm != null && dm.getPasseport() != null && dm.getPasseport().getDemandeur() != null) {
                Demandeur dr = dm.getPasseport().getDemandeur();
                map.put("demandeur", dr.getNom() + " " + dr.getPrenom());
                map.put("numeroPasseport", dm.getPasseport().getNumero());
                if (dr.getNationalite() != null) {
                    map.put("nationalite", dr.getNationalite().getLibelle());
                } else {
                    map.put("nationalite", "Inconnue");
                }
            } else {
                map.put("demandeur", "Inconnu");
                map.put("numeroPasseport", "Inconnu");
                map.put("nationalite", "Inconnue");
            }

            Statut s = getStatut(d);
            map.put("statut", s != null ? s.getLibelle() : "Aucun");
            map.put("dateCreation", d.getDateCreation());

            result.add(map);
        }

        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public void accepterDuplicata(Integer duplicataId) {
        // Controle
        DemandeDuplicata demandeDuplicata = demandeDuplicataRepository.findById(duplicataId)
            .orElseThrow(() -> new IllegalArgumentException("Demande de duplicata " + duplicataId + " introuvable"));
            controleStatusDemandeDuplicata(demandeDuplicata);
            
        // Metier
        Demande demande = demandeDuplicata.getDemande();

        CarteResident nouvelleCarte = new CarteResident();
        nouvelleCarte.setDateCreation(LocalDateTime.now());
        nouvelleCarte.setPasseport(demande.getPasseport()); // TODO: tokony dernier passeport ao @ base ?
        nouvelleCarte.setDemande(demande);
        
        // Persistence 
        carteResidentRepository.save(nouvelleCarte);

        setStatut(demandeDuplicata, UtilService.STATUS_DEMANDE_ACCEPTEE);
    }

    private void controleStatusDemandeDuplicata(DemandeDuplicata duplicata) {
        Statut actuel = getStatut(duplicata);
        Statut cible = statutRepository.findByLibelle(UtilService.STATUS_DEMANDE_ACCEPTEE)
            .orElseThrow(() -> new IllegalArgumentException("Statut '" + UtilService.STATUS_DEMANDE_ACCEPTEE + "' introuvable"));

        if (actuel != null) {
            if (actuel.getOrdre() >= cible.getOrdre()) {
                throw new IllegalStateException("Demande deja acceptee");
            }
        }
    }
}
