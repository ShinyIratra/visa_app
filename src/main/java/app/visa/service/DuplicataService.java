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
                            CarteResidentRepository carteResidentRepository) {
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
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> creerDemandeDuplicataSansDonneeAnterieure(Map<String, Object> donnees) {
        Map<String, Object> response = this.creerDemandeVisa(donnees);

        // Logique pour directement accepter le visa
        Demande demande_nouveau_titre = demandeService.getById((Integer) response.get("demandeId"));
        if(demande_nouveau_titre == null) {
            throw new IllegalArgumentException("Erreur Duplicata : Demande introuvable pour l'ID: " + response.get("demandeId"));
        }
        
        // Let's create an HistoriqueStatut manually since saveStatutDemande is private in super
        Statut statut = statutRepository.findByLibelle("Visa accepte")
            .orElseThrow(() -> new IllegalArgumentException("statut 'Visa accepte' introuvable."));
        HistoriqueStatut historique = new HistoriqueStatut();
        historique.setDemande(demande_nouveau_titre);
        historique.setStatut(statut);
        historique.setDateModification(LocalDateTime.now());
        historiqueStatutRepository.save(historique);
 
        return response;
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> creerDemandeDuplicataAvecDonneeAnterieure(Map<String, Object> donnees) {
        String numeroPasseport = (String) donnees.get("numero passeport");

        if (numeroPasseport == null || numeroPasseport.isEmpty()) {
            throw new IllegalArgumentException("Erreur Duplicata : Numero de passeport obligatoire.");
        }

        CarteResident carteResident = carteResidentService.findByLastNumeroPasseport(numeroPasseport);
        Demande ancienneDemande = carteResident.getDemande();
        
        Demande demande = new Demande();
        demande.setDateCreation(LocalDateTime.now());
        demande.setPasseport(ancienneDemande.getPasseport());
        demande.setVisaTransformable(ancienneDemande.getVisaTransformable());
        demande.setTypeDemande(ancienneDemande.getTypeDemande());
        demande.setCategorie(categorieRepository.findByLibelle("Duplicata")
            .orElseThrow(() -> new IllegalArgumentException("Erreur Duplicata : Categorie 'Duplicata' introuvable")));
        Statut statut = statutRepository.findByLibelle("Demande creee")
            .orElseThrow(() -> new IllegalArgumentException("Erreur Duplicata : statut 'Demande creee' introuvable."));
        // Pour récuperer l'id de la demande, il faut d'abord sauvegarder la demande pour que l'ID soit généré
        demande = demandeRepository.save(demande);

        HistoriqueStatut historique = new HistoriqueStatut();
        historique.setDemande(demande);
        historique.setStatut(statut);
        historique.setDateModification(LocalDateTime.now());
        historiqueStatutRepository.save(historique);

        CarteResident nouvelle_carteResident = new CarteResident();
        nouvelle_carteResident.setDateCreation(LocalDateTime.now());
        nouvelle_carteResident.setPasseport(carteResident.getPasseport());
        nouvelle_carteResident.setDemande(demande);
        nouvelle_carteResident.setLiaison(carteResident.getLiaison());

        carteResidentRepository.save(nouvelle_carteResident);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Demande de duplicata créée avec succès.");
        response.put("demandeId", demande.getId());
        response.put("nouveauStatut", statut.getLibelle());
        return response;
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> creerDuplicata(Demande demande_original)
    {
        if (demande_original == null) {
            throw new IllegalArgumentException("Erreur Duplicata : Demandes obligatoires.");
        }

        Demande demande_duplicata = new Demande();
        demande_duplicata.setDateCreation(LocalDateTime.now());
        demande_duplicata.setPasseport(demande_original.getPasseport());
        demande_duplicata.setVisaTransformable(demande_original.getVisaTransformable());
        demande_duplicata.setTypeDemande(demande_original.getTypeDemande());
        demande_duplicata.setCategorie(categorieRepository.findByLibelle("Duplicata")
            .orElseThrow(() -> new IllegalArgumentException("Categorie Duplicata introuvable")));

        demande_duplicata = visaRequestRepository.save(demande_duplicata);  
        
        Statut statut = statutRepository.findByLibelle("Demande creee")
            .orElseThrow(() -> new IllegalArgumentException("statut 'Demande creee' introuvable."));
        HistoriqueStatut historique = new HistoriqueStatut();
        historique.setDemande(demande_duplicata);
        historique.setStatut(statut);
        historique.setDateModification(LocalDateTime.now());
        historiqueStatutRepository.save(historique);

        Integer dernier_identifiant = liaisonSansDonneeAnterieurRepository.findTopByOrderByIdentifiantDesc()
            .map(LiaisonSansDonneeAnterieur::getIdentifiant)
            .orElse(0);

        liaisonSansDonneeAnterieurService.saveLiaisonSansDonneeAnterieur(dernier_identifiant + 1, demande_original);
        liaisonSansDonneeAnterieurService.saveLiaisonSansDonneeAnterieur(dernier_identifiant + 1, demande_duplicata);
        
        CarteResident nouvelle_carteResident = new CarteResident();
        nouvelle_carteResident.setDateCreation(LocalDateTime.now());
        nouvelle_carteResident.setPasseport(demande_duplicata.getPasseport());
        nouvelle_carteResident.setDemande(demande_duplicata);
        nouvelle_carteResident.setLiaison((carteResidentRepository.findByLiaison().orElse(0) + 1));
        carteResidentRepository.save(nouvelle_carteResident);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Demande marquée comme duplicata avec succès.");
        response.put("demandeId", demande_duplicata.getId());
        response.put("nouveauStatut", statut.getLibelle());
        return response;
    }
}
