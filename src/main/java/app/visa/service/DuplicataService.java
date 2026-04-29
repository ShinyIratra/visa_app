package app.visa.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import app.visa.entity.CarteResident;
import app.visa.entity.Categorie;
import app.visa.entity.Demande;
import app.visa.entity.Demandeur;
import app.visa.entity.Dossier;
import app.visa.entity.HistoriqueStatut;
import app.visa.entity.Nationalite;
import app.visa.entity.Passeport;
import app.visa.entity.ReponseStatutVisa;
import app.visa.entity.SituationFamiliale;
import app.visa.entity.Statut;
import app.visa.entity.TypeDemande;
import app.visa.entity.Visa;
import app.visa.entity.VisaTransformable;
import app.visa.entity.LiaisonSansDonneeAnterieur;
import app.visa.repository.CarteResidentRepository;
import app.visa.repository.CategorieRepository;
import app.visa.repository.DemandeRepository;
import app.visa.repository.DossierRepository;
import app.visa.repository.HistoriqueStatutRepository;
import app.visa.repository.ReponseStatutVisaRepository;
import app.visa.repository.StatutRepository;
import app.visa.repository.TypeDemandeRepository;
import app.visa.repository.LiaisonSansDonneeAnterieurRepository;
import app.visa.repository.VisaRepository;
import app.visa.repository.VisaRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
public class DuplicataService extends VisaRequestService {

    private final DemandeService demandeService;
    private final DemandeRepository demandeRepository;
    private final CategorieService categorieService;
    private final LiaisonSansDonneeAnterieurService liaisonSansDonneeAnterieurService;
    private final LiaisonSansDonneeAnterieurRepository liaisonSansDonneeAnterieurRepository;
    private final CarteResidentService carteResidentService;
    private final CarteResidentRepository carteResidentRepository;
    private final AcceptationDemandeVisaService acceptationDemandeVisaService;
    private final VisaRepository visaRepository;

    public DuplicataService(VisaRequestRepository visaRequestRepository,
                            TypeDemandeRepository typeDemandeRepository,
                            CategorieRepository categorieRepository,
                            DossierRepository dossierRepository,
                            ReponseStatutVisaRepository reponseStatutVisaRepository,
                            HistoriqueStatutRepository historiqueStatutRepository,
                            StatutRepository statutRepository,
                            DemandeurService demandeurService,
                            PasseportService passeportService,
                            VisaTransformableService visaTransformableService,
                            DemandeService demandeService,
                            DemandeRepository demandeRepository,
                            CategorieService categorieService,
                            LiaisonSansDonneeAnterieurService liaisonSansDonneeAnterieurService,
                            LiaisonSansDonneeAnterieurRepository liaisonSansDonneeAnterieurRepository,
                            CarteResidentService carteResidentService,
                            CarteResidentRepository carteResidentRepository,
                            AcceptationDemandeVisaService acceptationDemandeVisaService,
                            VisaRepository visaRepository) {
        super(visaRequestRepository, typeDemandeRepository, categorieRepository, dossierRepository,
              reponseStatutVisaRepository, historiqueStatutRepository, statutRepository,
              demandeurService, passeportService, visaTransformableService);
        this.demandeService = demandeService;
        this.demandeRepository = demandeRepository;
        this.categorieService = categorieService;
        this.liaisonSansDonneeAnterieurService = liaisonSansDonneeAnterieurService;
        this.liaisonSansDonneeAnterieurRepository = liaisonSansDonneeAnterieurRepository;
        this.carteResidentService = carteResidentService;
        this.carteResidentRepository = carteResidentRepository;
        this.acceptationDemandeVisaService = acceptationDemandeVisaService;
        this.visaRepository = visaRepository;
    }

    @Transactional(rollbackFor = Exception.class)
    public Demande creerDemandeDuplicataSansDonneeAnterieure(Map<String, Object> donnees) {
        LocalDateTime dateCreation = null;
        if (donnees.get("dateCreation") != null && !donnees.get("dateCreation").toString().isBlank()) {
            dateCreation = LocalDateTime.parse(donnees.get("dateCreation").toString());
        }

        Demande demandeOriginale = this.creerDemandeVisa(donnees, "Nouveau titre", "Visa accepte");
        
        acceptationDemandeVisaService.creerVisaEtCarteResident(demandeOriginale);

        return this.creerDemandeDuplicata(demandeOriginale, dateCreation);
    }

    @Transactional(rollbackFor = Exception.class)
    public Demande creerDemandeDuplicataAvecDonneeAnterieure(Map<String, Object> donnees) {
        String typeRecherche = (String) donnees.get("type_recherche");
        String valeur = (String) donnees.get("valeur");

        LocalDateTime dateCreation = null;
        if (donnees.get("dateCreation") != null && !donnees.get("dateCreation").toString().isBlank()) {
            dateCreation = LocalDateTime.parse(donnees.get("dateCreation").toString());
        }

        Demande ancienneDemande = demandeService.findDemandeByCritere(typeRecherche, valeur);
        
        return creerDemandeDuplicata(ancienneDemande, dateCreation);
    }

    @Transactional(rollbackFor = Exception.class)
    public Demande creerDemandeDuplicata(Demande demande_original) {
        return creerDemandeDuplicata(demande_original, null);
    }

    @Transactional(rollbackFor = Exception.class)
    public Demande creerDemandeDuplicata(Demande demande_original, LocalDateTime dateCreation) {
        if (demande_original == null) {
            throw new IllegalArgumentException("Erreur Duplicata : Demande originale obligatoire.");
        }

        // 1. Nouvelle demande de duplicata
        Demande demande_duplicata = new Demande();
        demande_duplicata.setDateCreation(dateCreation != null ? dateCreation : LocalDateTime.now());
        demande_duplicata.setPasseport(demande_original.getPasseport());
        demande_duplicata.setVisaTransformable(demande_original.getVisaTransformable());
        demande_duplicata.setTypeDemande(demande_original.getTypeDemande());
        demande_duplicata.setCategorie(categorieRepository.findByLibelle("Duplicata")
            .orElseThrow(() -> new IllegalArgumentException("Categorie Duplicata introuvable")));

        demande_duplicata = demandeRepository.save(demande_duplicata);  
        
        // 2. Statut initial
        saveStatutDemande(demande_duplicata, "Demande creee", dateCreation);

        // 3. Liaison ?
        Integer dernier_identifiant = liaisonSansDonneeAnterieurRepository.findTopByOrderByIdentifiantDesc()
            .map(LiaisonSansDonneeAnterieur::getIdentifiant)
            .orElse(0);

        int nouvelIdentifiant = dernier_identifiant + 1;
        liaisonSansDonneeAnterieurService.saveLiaisonSansDonneeAnterieur(nouvelIdentifiant, demande_original);
        liaisonSansDonneeAnterieurService.saveLiaisonSansDonneeAnterieur(nouvelIdentifiant, demande_duplicata);

        return demande_duplicata;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listDuplicataAvecInfos() {
        // Get demandes de duplicata
        Categorie categorieDuplicata = categorieRepository.findByLibelle("Duplicata")
            .orElseThrow(() -> new IllegalArgumentException("Categorie 'Duplicata' introuvable"));

        List<Demande> demandes = demandeRepository.findAll().stream()
            .filter(d -> d.getCategorie() != null && d.getCategorie().getId().equals(categorieDuplicata.getId()))
            .toList();

        List<Map<String, Object>> result = new ArrayList<>();
        for (Demande d : demandes) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", d.getId());
            
            if (d.getPasseport() != null && d.getPasseport().getDemandeur() != null) {
                Demandeur dr = d.getPasseport().getDemandeur();
                map.put("demandeur", dr.getNom() + " " + dr.getPrenom());
                map.put("numeroPasseport", d.getPasseport().getNumero());
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

            // LAst statut
            String statutLibelle = historiqueStatutRepository.findLatestByDemandeId(d.getId())
                .map(h -> h.getStatut().getLibelle())
                .orElse("Aucun");
            map.put("statut", statutLibelle);
            map.put("dateCreation", d.getDateCreation());

            result.add(map);
        }

        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public void accepterDuplicata(Integer demandeId) {
        Demande demande = demandeRepository.findById(demandeId)
            .orElseThrow(() -> new IllegalArgumentException("Demande de duplicata " + demandeId + " introuvable"));

        // 1. Controle
        controleStatusDuplicata(demande);

        // 2. Marquer comme acceptee
        saveStatutDemande(demande, UtilService.STATUS_DEMANDE_ACCEPTEE);

        // 3. Creation carte resident dupliquee
        CarteResident nouvelleCarte = new CarteResident();
        nouvelleCarte.setDateCreation(LocalDateTime.now());
        nouvelleCarte.setPasseport(demande.getPasseport());
        nouvelleCarte.setDemande(demande);
        
        // Calcul du liaison ID pour la carte resident
        Integer maxLiaison = carteResidentRepository.findByLiaison().orElse(0);
        nouvelleCarte.setLiaison(maxLiaison + 1);
        
        carteResidentRepository.save(nouvelleCarte);
    }

    private void controleStatusDuplicata(Demande demande) {
        Statut actuel = demandeService.getDernierStatus(demande);
        Statut cible = statutRepository.findByLibelle(UtilService.STATUS_DEMANDE_ACCEPTEE)
            .orElseThrow(() -> new IllegalArgumentException("Statut '" + UtilService.STATUS_DEMANDE_ACCEPTEE + "' introuvable"));

        if (actuel != null) {
            if (actuel.getOrdre() >= cible.getOrdre()) {
                throw new IllegalStateException("Demande deja acceptee");
            }
        }
    }

}
