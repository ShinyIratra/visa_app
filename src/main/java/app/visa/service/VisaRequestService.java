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
import app.visa.repository.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.visa.entity.DemandeNouveauTitre;
import lombok.RequiredArgsConstructor;

@Service
public class VisaRequestService {
    protected final DemandeRepository demandeRepository;
    protected final VisaRequestRepository visaRequestRepository;
    protected final TypeDemandeRepository typeDemandeRepository;
    protected final CategorieRepository categorieRepository;
    protected final DossierRepository dossierRepository;
    protected final ReponseStatutVisaRepository reponseStatutVisaRepository;
    protected final HistoriqueStatutRepository historiqueStatutRepository;
    protected final StatutRepository statutRepository;
    protected final DemandeurService demandeurService;
    protected final PasseportService passeportService;
    protected final VisaTransformableService visaTransformableService;
    protected final CodeQrService codeQrService;
    protected final DemandeService demandeService;

    public VisaRequestService(
            DemandeRepository demandeRepository,
            VisaRequestRepository visaRequestRepository,
            TypeDemandeRepository typeDemandeRepository,
            CategorieRepository categorieRepository,
            DossierRepository dossierRepository,
            ReponseStatutVisaRepository reponseStatutVisaRepository,
            HistoriqueStatutRepository historiqueStatutRepository,
            StatutRepository statutRepository,
            DemandeurService demandeurService,
            PasseportService passeportService,
            VisaTransformableService visaTransformableService,
            CodeQrService codeQrService,
            DemandeService demandeService) {
        this.demandeRepository = demandeRepository;
        this.visaRequestRepository = visaRequestRepository;
        this.typeDemandeRepository = typeDemandeRepository;
        this.categorieRepository = categorieRepository;
        this.dossierRepository = dossierRepository;
        this.reponseStatutVisaRepository = reponseStatutVisaRepository;
        this.historiqueStatutRepository = historiqueStatutRepository;
        this.statutRepository = statutRepository;
        this.demandeurService = demandeurService;
        this.passeportService = passeportService;
        this.visaTransformableService = visaTransformableService;
        this.codeQrService = codeQrService;
        this.demandeService = demandeService;
    }

    public List<DemandeNouveauTitre> findAll() {
        return visaRequestRepository.findAll();
    }

    public Optional<DemandeNouveauTitre> findById(Integer id) {
        return visaRequestRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listDemandesAvecInfos() {
        List<DemandeNouveauTitre> demandes = visaRequestRepository.findAll();
        
        List<Map<String, Object>> result = new ArrayList<>();

        for (DemandeNouveauTitre demande : demandes) {
            Map<String, Object> item = new LinkedHashMap<>();
            Demandeur demandeur = demande.getPasseport() != null ? demande.getPasseport().getDemandeur() : null;

            item.put("demandeId", demande.getId());
            item.put("numeroDemande", demande.getNumero());
            item.put("nomDemandeur", demandeur != null ? demandeur.getNom() : null);
            item.put("prenomDemandeur", demandeur != null ? demandeur.getPrenom() : null);
            item.put("referencePasseport", demande.getPasseport() != null ? demande.getPasseport().getNumero() : null);
            item.put("categorie", demande.getCategorie() != null ? demande.getCategorie().getLibelle() : null);
            item.put(
                "referenceVisaTransformable",
                demande.getVisaTransformable() != null ? demande.getVisaTransformable().getReference() : null
            );
            item.put("typeDemande", demande.getTypeDemande() != null ? demande.getTypeDemande().getLibelle() : null);
            item.put("statut", getDernierStatutLibelle(demande.getId()));
            item.put("dateCreation", demande.getDateCreation());

            result.add(item);
        }

        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public Demande save(DemandeNouveauTitre demande) {
        return visaRequestRepository.save(demande);
    }

    @Transactional(rollbackFor = Exception.class)
    public Demande saveGeneric(Demande demande) {
        return demandeRepository.save(demande);
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> creerDemandeVisa(Map<String, Object> donnees) {
        if (donnees == null) {
            throw new IllegalArgumentException("donnees de demande obligatoires.");
        }

        Map<String, Object> etatCivilData = UtilService.getBloc(donnees, "etat civil");
        Map<String, Object> passeportData = UtilService.getBloc(donnees, "passeport");
        Map<String, Object> visaTransformableData = UtilService.getBloc(donnees, "visaTransformable");
        Integer typeDemandeId = toLong(donnees.get("typeDemandeId"));
        List<Integer> dossiersFournisIds = getDossiersFournis(donnees);
        
        LocalDateTime dateCreation = null;
        if (donnees.get("dateCreation") != null && !donnees.get("dateCreation").toString().isBlank()) {
            dateCreation = LocalDateTime.parse(donnees.get("dateCreation").toString());
        }

        TypeDemande typeDemande = getTypeDemandeValide(typeDemandeId);
        List<Dossier> dossiersApplicables = getDossiersApplicables(typeDemande);
        checkDossiersObligatoires(dossiersApplicables, dossiersFournisIds);

        Demandeur demandeur = createDemandeur(etatCivilData);
        Passeport passeport = createPasseport(passeportData, demandeur.getId());
        VisaTransformable visaTransformable = createVisaTransformable(
            visaTransformableData,
            demandeur.getId(),
            passeport.getId()
        );

        controlerDatesDemande(dateCreation, passeport, visaTransformable);

        Demande demande = createDemande(typeDemande, "Nouveau titre", passeport, visaTransformable, dateCreation);
        saveReponseDossier(demande, dossiersApplicables, dossiersFournisIds);
        demandeService.ajouterHistoriqueStatut(demande, "Demande creee", dateCreation);

        return reponseCreation(demandeur, passeport, visaTransformable, demande, dossiersFournisIds);
    }

    /**
     * Ohatran'ilay eo ambony ihany fa + argument categorieLibelle + statutDemandeLibelle
     * tsy tiako kitihana intsony ze classe efa miantso an le fonction eo ambony
     */
    @Transactional(rollbackFor = Exception.class)
    public Demande creerDemandeVisa(Map<String, Object> donnees, String categorieLibelle, String statutDemandeLibelle) {
        if (donnees == null) {
            throw new IllegalArgumentException("donnees de demande obligatoires.");
        }

        Map<String, Object> etatCivilData = UtilService.getBloc(donnees, "etat civil");
        Map<String, Object> passeportData = UtilService.getBloc(donnees, "passeport");
        Map<String, Object> visaTransformableData = UtilService.getBloc(donnees, "visaTransformable");
        Integer typeDemandeId = toLong(donnees.get("typeDemandeId"));
        List<Integer> dossiersFournisIds = getDossiersFournis(donnees);
        
        LocalDateTime dateCreation = null;
        if (donnees.get("dateCreation") != null && !donnees.get("dateCreation").toString().isBlank()) {
            dateCreation = LocalDateTime.parse(donnees.get("dateCreation").toString());
        }

        TypeDemande typeDemande = getTypeDemandeValide(typeDemandeId);
        List<Dossier> dossiersApplicables = getDossiersApplicables(typeDemande);
        checkDossiersObligatoires(dossiersApplicables, dossiersFournisIds);

        Demandeur demandeur = createDemandeur(etatCivilData);
        Passeport passeport = createPasseport(passeportData, demandeur.getId());
        VisaTransformable visaTransformable = createVisaTransformable(
            visaTransformableData,
            demandeur.getId(),
            passeport.getId()
        );

        controlerDatesDemande(dateCreation, passeport, visaTransformable);

        Demande demande = createDemande(typeDemande, categorieLibelle, passeport, visaTransformable, dateCreation);
        saveReponseDossier(demande, dossiersApplicables, dossiersFournisIds);
        demandeService.ajouterHistoriqueStatut(demande, statutDemandeLibelle, dateCreation);

        return demande;
    }

    private void controlerDatesDemande(LocalDateTime dateCreation, Passeport passeport, VisaTransformable visaTransformable) {
        LocalDateTime dc = dateCreation != null ? dateCreation : LocalDateTime.now();
        
        if (passeport != null && passeport.getDateExpiration() != null) {
            if (dc.isAfter(passeport.getDateExpiration())) {
                throw new IllegalArgumentException("La date de creation de la demande (" + dc + ") ne peut pas etre posterieure a la date d'expiration du passeport (" + passeport.getDateExpiration() + ")");
            }
        }
        
        if (visaTransformable != null && visaTransformable.getDateExpiration() != null) {
            if (dc.isAfter(visaTransformable.getDateExpiration())) {
                throw new IllegalArgumentException("La date de creation de la demande (" + dc + ") ne peut pas etre posterieure a la date d'expiration du visa transformable (" + visaTransformable.getDateExpiration() + ")");
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Integer id) {
        visaRequestRepository.deleteById(id);
    }

    protected Demandeur createDemandeur(Map<String, Object> etatCivilData) {
        Demandeur demandeur = new Demandeur();
        demandeur.setNom((String) etatCivilData.get("nom"));
        demandeur.setPrenom((String) etatCivilData.get("prenom"));
        demandeur.setNomJeuneFille((String) etatCivilData.get("nomJeuneFille"));
        demandeur.setEmail((String) etatCivilData.get("email"));
        demandeur.setNumTel((String) etatCivilData.get("numTel"));
        demandeur.setAdresse((String) etatCivilData.get("adresse"));

        Object dateNaissance = etatCivilData.get("dateNaissance");
        if (dateNaissance != null && !dateNaissance.toString().isBlank()) {
            demandeur.setDateNaissance(LocalDate.parse(dateNaissance.toString()));
        }

        Integer nationaliteId = toLong(etatCivilData.get("nationalite"));
        if (nationaliteId != null) {
            Nationalite nationalite = new Nationalite();
            nationalite.setId(nationaliteId);
            demandeur.setNationalite(nationalite);
        }

        Integer situationFamilialeId = toLong(etatCivilData.get("situationFamiliale"));
        if (situationFamilialeId != null) {
            SituationFamiliale situationFamiliale = new SituationFamiliale();
            situationFamiliale.setId(situationFamilialeId);
            demandeur.setSituationFamiliale(situationFamiliale);
        }

        return demandeurService.createDemandeur(demandeur);
    }

    public Passeport createPasseport(Map<String, Object> passeportData, Integer demandeurId) {
        Passeport passeport = new Passeport();
        passeport.setNumero((String) passeportData.get("numero"));

        Object dateDelivrance = passeportData.get("dateDelivrance");
        if (dateDelivrance != null && !dateDelivrance.toString().isBlank()) {
            passeport.setDateDelivrance(LocalDateTime.parse(dateDelivrance.toString()));
        }

        Object dateExpiration = passeportData.get("dateExpiration");
        if (dateExpiration != null && !dateExpiration.toString().isBlank()) {
            passeport.setDateExpiration(LocalDateTime.parse(dateExpiration.toString()));
        }

        Demandeur demandeurRef = new Demandeur();
        demandeurRef.setId(demandeurId);
        passeport.setDemandeur(demandeurRef);

        return passeportService.createPasseport(passeport);
    }

    protected VisaTransformable createVisaTransformable(
        Map<String, Object> visaTransformableData,
        Integer demandeurId,
        Integer passeportId
    ) {
        VisaTransformable visaTransformable = new VisaTransformable();
        visaTransformable.setReference((String) visaTransformableData.get("reference"));
        visaTransformable.setLieuEntree((String) visaTransformableData.get("lieuEntree"));

        Object dateEntree = visaTransformableData.get("dateEntree");
        if (dateEntree != null && !dateEntree.toString().isBlank()) {
            visaTransformable.setDateEntree(LocalDateTime.parse(dateEntree.toString()));
        }

        Object dateExpiration = visaTransformableData.get("dateExpiration");
        if (dateExpiration != null && !dateExpiration.toString().isBlank()) {
            visaTransformable.setDateExpiration(LocalDateTime.parse(dateExpiration.toString()));
        }

        Passeport passeportRef = new Passeport();
        passeportRef.setId(passeportId);
        visaTransformable.setPasseport(passeportRef);

        Demandeur demandeurRef = new Demandeur();
        demandeurRef.setId(demandeurId);
        visaTransformable.setDemandeur(demandeurRef);

        return visaTransformableService.createVisaTransformable(visaTransformable);
    }

    protected Demande createDemande(TypeDemande typeDemande, String libelle, Passeport passeport, VisaTransformable visaTransformable) {
        return createDemande(typeDemande, libelle, passeport, visaTransformable, null);
    }

    protected Demande createDemande(TypeDemande typeDemande, String libelle, Passeport passeport, VisaTransformable visaTransformable, LocalDateTime dateCreation) {
        DemandeNouveauTitre dnt = new DemandeNouveauTitre();
        demandeService.setupBaseDemande(dnt, libelle, dateCreation);
        
        dnt.setPasseport(passeport);
        dnt.setTypeDemande(typeDemande);
        dnt.setVisaTransformable(visaTransformable);

        return visaRequestRepository.save(dnt);
    }

    protected TypeDemande getTypeDemandeValide(Integer typeId) {
        if (typeId == null) {
            throw new IllegalArgumentException("il faut choisir un type de demande Travailleur ou Investisseur.");
        }

        TypeDemande typeDem = typeDemandeRepository.findById(typeId)
            .orElseThrow(() -> new IllegalArgumentException("type de demande introuvable: " + typeId));

        String libelle = typeDem.getLibelle() == null ? "" : typeDem.getLibelle().trim();
        if (libelle.equalsIgnoreCase("commun")) {
            throw new IllegalArgumentException("il faut choisir un type de demande Travailleur ou Investisseur.");
        }

        if (!libelle.equalsIgnoreCase("travailleur") && !libelle.equalsIgnoreCase("investisseur")) {
            throw new IllegalArgumentException("type de demande invalide. valeurs attendues: Travailleur ou Investisseur.");
        }

        return typeDem;
    }

    protected List<Dossier> getDossiersApplicables(TypeDemande typeDem) {
        List<Dossier> doss = dossierRepository.findDossiersPourTypeDemande(typeDem.getId());
        if (doss.isEmpty()) {
            throw new IllegalArgumentException("aucun dossier configure pour ce type de demande.");
        }

        return doss;
    }

    protected void checkDossiersObligatoires(List<Dossier> doss, List<Integer> dossIds) {
        Set<Integer> dossiersFournis = new HashSet<>(dossIds);
        List<Integer> manquants = new ArrayList<>();

        for (Dossier dossier : doss) {
            if (Boolean.TRUE.equals(dossier.getObligatoire()) && !dossiersFournis.contains(dossier.getId())) {
                manquants.add(dossier.getId());
            }
        }

        if (!manquants.isEmpty()) {
            throw new IllegalArgumentException("tous les dossiers obligatoires doivent etre coches. dossiers manquants: " + manquants);
        }
    }

    protected void saveReponseDossier(Demande dem, List<Dossier> doss, List<Integer> dossIds) {
        Set<Integer> dossiersFournis = new HashSet<>(dossIds);
        List<ReponseStatutVisa> reponses = new ArrayList<>();

        for (Dossier dossier : doss) {
            ReponseStatutVisa reponse = new ReponseStatutVisa();
            reponse.setDemande(dem);
            reponse.setDossier(dossier);
            reponse.setValeur(dossiersFournis.contains(dossier.getId()));
            reponses.add(reponse);
        }

        reponseStatutVisaRepository.saveAll(reponses);
    }

    protected List<Integer> getDossiersFournis(Map<String, Object> donnees) {
        List<Integer> dossiersFournisIds = new ArrayList<>();
        Object dossiersObj = donnees.get("dossiersFournis");

        if (!(dossiersObj instanceof List<?> dossiersBruts)) {
            return dossiersFournisIds;
        }

        for (Object idObj : dossiersBruts) {
            Integer id = toLong(idObj);
            if (id != null) {
                dossiersFournisIds.add(id);
            }
        }

        return dossiersFournisIds;
    }

    protected Integer toLong(Object value) {
        if (value == null) {
            return null;
        }

        String text = value.toString().trim();
        if (text.isEmpty()) {
            return null;
        }

        try {
            return Integer.valueOf(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public String getDernierStatus(Integer demandeId) {
        return getDernierStatutLibelle(demandeId);
    }

    private String getDernierStatutLibelle(Integer demandeId) {
        return historiqueStatutRepository.findLatestByDemandeId(demandeId)
            .map(HistoriqueStatut::getStatut)
            .map(Statut::getLibelle)
            .orElse(null);
    }

    protected static Map<String, Object> reponseCreation(
        Demandeur dem,
        Passeport pass,
        VisaTransformable vt,
        Demande demSave,
        List<Integer> dossIds
    ) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("demandeurId", dem.getId());
        data.put("passeportId", pass.getId());
        data.put("visaTransformableId", vt.getId());
        data.put("demandeId", demSave.getId());
        data.put("categorie", demSave.getCategorie().getLibelle());
        data.put("statut", "Demande creee");
        data.put("dossiersFournis", dossIds);
        return data;
    }
}
