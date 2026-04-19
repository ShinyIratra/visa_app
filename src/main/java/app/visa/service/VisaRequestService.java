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
import app.visa.repository.CategorieRepository;
import app.visa.repository.DossierRepository;
import app.visa.repository.HistoriqueStatutRepository;
import app.visa.repository.ReponseStatutVisaRepository;
import app.visa.repository.StatutRepository;
import app.visa.repository.TypeDemandeRepository;
import app.visa.repository.VisaRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VisaRequestService {

    private final VisaRequestRepository visaRequestRepository;
    private final TypeDemandeRepository typeDemandeRepository;
    private final CategorieRepository categorieRepository;
    private final DossierRepository dossierRepository;
    private final ReponseStatutVisaRepository reponseStatutVisaRepository;
    private final HistoriqueStatutRepository historiqueStatutRepository;
    private final StatutRepository statutRepository;
    private final DemandeurService demandeurService;
    private final PasseportService passeportService;
    private final VisaTransformableService visaTransformableService;

    public List<Demande> findAll() {
        return visaRequestRepository.findAll();
    }

    public Optional<Demande> findById(Long id) {
        return visaRequestRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listDemandesAvecInfos() {
        List<Demande> demandes = visaRequestRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Demande demande : demandes) {
            Map<String, Object> item = new LinkedHashMap<>();
            Demandeur demandeur = demande.getPasseport() != null ? demande.getPasseport().getDemandeur() : null;

            item.put("demandeId", demande.getId());
            item.put("nomDemandeur", demandeur != null ? demandeur.getNom() : null);
            item.put("prenomDemandeur", demandeur != null ? demandeur.getPrenom() : null);
            item.put("referencePasseport", demande.getPasseport() != null ? demande.getPasseport().getNumero() : null);
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
    public Demande save(Demande demande) {
        return visaRequestRepository.save(demande);
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> creerDemandeVisa(Map<String, Object> donnees) {
        if (donnees == null) {
            throw new IllegalArgumentException("donnees de demande obligatoires.");
        }

        Map<String, Object> etatCivilData = getBloc(donnees, "etat civil");
        Map<String, Object> passeportData = getBloc(donnees, "passeport");
        Map<String, Object> visaTransformableData = getBloc(donnees, "visaTransformable");
        Long typeDemandeId = toLong(donnees.get("typeDemandeId"));
        List<Long> dossiersFournisIds = getDossiersFournis(donnees);

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

        Demande demande = createDemande(typeDemande, passeport, visaTransformable);
        saveReponseDossier(demande, dossiersApplicables, dossiersFournisIds);
        saveStatutDemande(demande);

        return reponseCreation(demandeur, passeport, visaTransformable, demande, dossiersFournisIds);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        visaRequestRepository.deleteById(id);
    }

    private Demandeur createDemandeur(Map<String, Object> etatCivilData) {
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

        Long nationaliteId = toLong(etatCivilData.get("nationalite"));
        if (nationaliteId != null) {
            Nationalite nationalite = new Nationalite();
            nationalite.setId(nationaliteId);
            demandeur.setNationalite(nationalite);
        }

        Long situationFamilialeId = toLong(etatCivilData.get("situationFamiliale"));
        if (situationFamilialeId != null) {
            SituationFamiliale situationFamiliale = new SituationFamiliale();
            situationFamiliale.setId(situationFamilialeId);
            demandeur.setSituationFamiliale(situationFamiliale);
        }

        return demandeurService.createDemandeur(demandeur);
    }

    private Passeport createPasseport(Map<String, Object> passeportData, Long demandeurId) {
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

    private VisaTransformable createVisaTransformable(
        Map<String, Object> visaTransformableData,
        Long demandeurId,
        Long passeportId
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

    private Demande createDemande(TypeDemande typeDemande, Passeport passeport, VisaTransformable visaTransformable) {
        Categorie categorie = categorieRepository.findByLibelle("Nouveau titre")
            .orElseThrow(() -> new IllegalArgumentException("categorie 'Nouveau titre' introuvable."));

        Demande demande = new Demande();
        demande.setDateCreation(LocalDateTime.now());
        demande.setTypeDemande(typeDemande);
        demande.setCategorie(categorie);
        demande.setPasseport(passeport);
        demande.setVisaTransformable(visaTransformable);
        return visaRequestRepository.save(demande);
    }

    private TypeDemande getTypeDemandeValide(Long typeId) {
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

    private List<Dossier> getDossiersApplicables(TypeDemande typeDem) {
        List<Dossier> doss = dossierRepository.findDossiersPourTypeDemande(typeDem.getId());
        if (doss.isEmpty()) {
            throw new IllegalArgumentException("aucun dossier configure pour ce type de demande.");
        }

        return doss;
    }

    private void checkDossiersObligatoires(List<Dossier> doss, List<Long> dossIds) {
        Set<Long> dossiersFournis = new HashSet<>(dossIds);
        List<Long> manquants = new ArrayList<>();

        for (Dossier dossier : doss) {
            if (Boolean.TRUE.equals(dossier.getObligatoire()) && !dossiersFournis.contains(dossier.getId())) {
                manquants.add(dossier.getId());
            }
        }

        if (!manquants.isEmpty()) {
            throw new IllegalArgumentException("tous les dossiers obligatoires doivent etre coches. dossiers manquants: " + manquants);
        }
    }

    private void saveReponseDossier(Demande dem, List<Dossier> doss, List<Long> dossIds) {
        Set<Long> dossiersFournis = new HashSet<>(dossIds);
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

    private void saveStatutDemande(Demande dem) {
        Statut statut = statutRepository.findByLibelle("Demande creee")
            .orElseThrow(() -> new IllegalArgumentException("statut 'Demande creee' introuvable."));

        HistoriqueStatut historique = new HistoriqueStatut();
        historique.setDemande(dem);
        historique.setStatut(statut);
        historique.setDateModification(LocalDateTime.now());

        historiqueStatutRepository.save(historique);
    }

    private Map<String, Object> getBloc(Map<String, Object> donnees, String nomBloc) {
        Object bloc = donnees.get(nomBloc);
        if (!(bloc instanceof Map<?, ?>)) {
            return new LinkedHashMap<>();
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) bloc;
        return map;
    }

    private List<Long> getDossiersFournis(Map<String, Object> donnees) {
        List<Long> dossiersFournisIds = new ArrayList<>();
        Object dossiersObj = donnees.get("dossiersFournis");

        if (!(dossiersObj instanceof List<?> dossiersBruts)) {
            return dossiersFournisIds;
        }

        for (Object idObj : dossiersBruts) {
            Long id = toLong(idObj);
            if (id != null) {
                dossiersFournisIds.add(id);
            }
        }

        return dossiersFournisIds;
    }

    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }

        String text = value.toString().trim();
        if (text.isEmpty()) {
            return null;
        }

        try {
            return Long.valueOf(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String getDernierStatutLibelle(Long demandeId) {
        return historiqueStatutRepository.findLatestByDemandeId(demandeId)
            .map(HistoriqueStatut::getStatut)
            .map(Statut::getLibelle)
            .orElse(null);
    }

    private Map<String, Object> reponseCreation(
        Demandeur dem,
        Passeport pass,
        VisaTransformable vt,
        Demande demSave,
        List<Long> dossIds
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
