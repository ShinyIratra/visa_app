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

        Passeport passeportOrigine = demandeService.getPasseport(demande);

        if (passeportOrigine != null && passeportOrigine.getDateExpiration() != null) {
            if (dateFinale.isAfter(passeportOrigine.getDateExpiration())) {
                throw new IllegalArgumentException("La date de la demande de duplicata (" + dateFinale + ") ne peut pas être postérieure à la date d'expiration du passeport (" + passeportOrigine.getDateExpiration() + ")");
            }
        }

        DemandeDuplicata duplicata = new DemandeDuplicata();
        demandeService.setupBaseDemande(duplicata, "Duplicata", dateFinale);
        duplicata.setDemandeOrigine(demande);

        duplicata = demandeDuplicataRepository.save(duplicata);
        demandeService.ajouterHistoriqueStatut(duplicata, UtilService.STATUS_DEMANDE_CREEE, dateFinale);

        return duplicata;
    }

    
    @Transactional(rollbackFor = Exception.class)
    public void marquerCommeScanne(Integer duplicataId) {
        DemandeDuplicata duplicata = findDuplicataById(duplicataId);
        
        demandeService.verifierDroitDePasserA(duplicata, UtilService.STATUS_SCAN_TERMINE);
        
        demandeService.ajouterHistoriqueStatut(duplicata, UtilService.STATUS_SCAN_TERMINE);
    }

    public Statut getStatutActuel(DemandeDuplicata duplicata) {
        return demandeService.getDernierStatus(duplicata);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listDuplicataAvecInfos() {
        List<DemandeDuplicata> duplicatas = demandeDuplicataRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();

        for (DemandeDuplicata d : duplicatas) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", d.getId());
            
            Passeport p = demandeService.getPasseport(d);
            if (p != null && p.getDemandeur() != null) {
                Demandeur dr = p.getDemandeur();
                map.put("demandeur", dr.getNom() + " " + dr.getPrenom());
                map.put("numeroPasseport", p.getNumero());
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

            Statut s = getStatutActuel(d);
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
        
        demandeService.verifierDroitDePasserA(demandeDuplicata, UtilService.STATUS_DEMANDE_ACCEPTEE);
            
        if (!demandeService.isStatutDejaAtteint(demandeDuplicata, UtilService.STATUS_SCAN_TERMINE)) {
            throw new IllegalStateException("Impossible d'accepter : le dossier doit d'abord etre scanne");
        }

        // Metier
        CarteResident nouvelleCarte = new CarteResident();
        nouvelleCarte.setDateCreation(LocalDateTime.now());
        nouvelleCarte.setPasseport(demandeService.getPasseport(demandeDuplicata));
        nouvelleCarte.setDemande(demandeDuplicata);
        
        // Persistence 
        carteResidentRepository.save(nouvelleCarte);

        demandeService.ajouterHistoriqueStatut(demandeDuplicata, UtilService.STATUS_DEMANDE_ACCEPTEE);
    }

    /***
     * 
     * Utils
     * 
     */

    private DemandeDuplicata findDuplicataById(Integer id) {
        return demandeDuplicataRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Demande de duplicata #" + id + " introuvable."));
    }

    private Statut findStatutByLibelle(String libelle) {
        return statutRepository.findByLibelle(libelle)
            .orElseThrow(() -> new IllegalArgumentException("Statut '" + libelle + "' introuvable."));
    }
}
