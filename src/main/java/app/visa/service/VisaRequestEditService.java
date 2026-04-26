package app.visa.service;

import java.util.*;
import java.time.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.visa.controller.VisaRequestController;
import app.visa.entity.*;
import app.visa.repository.*;
import lombok.RequiredArgsConstructor;

@Service
public class VisaRequestEditService extends VisaRequestService { // (Kamo be hanao statique an'i VisaRequestService.getBlock, VisaRequestServicereponseCreation) dia ataoko miextends

    private final DemandeService demandeService;

    public VisaRequestEditService(VisaRequestRepository visaRequestRepository,
                                  TypeDemandeRepository typeDemandeRepository,
                                  CategorieRepository categorieRepository,
                                  DossierRepository dossierRepository,
                                  ReponseStatutVisaRepository reponseStatutVisaRepository,
                                  HistoriqueStatutRepository historiqueStatutRepository,
                                  StatutRepository statutRepository,
                                  DemandeurService demandeurService,
                                  PasseportService passeportService,
                                  VisaTransformableService visaTransformableService,
                                  DemandeService demandeService) {
        super(visaRequestRepository, typeDemandeRepository, categorieRepository, dossierRepository,
              reponseStatutVisaRepository, historiqueStatutRepository, statutRepository,
              demandeurService, passeportService, visaTransformableService);
        this.demandeService = demandeService;
    }

    /**
     * 
     * 
     * 
     * Antsoin'ny front.getById
     * 
     * 
     * 
     */

    @Transactional(readOnly = true)
    public Map<String, Object> getDemandeFormData(Integer id) {
        Demande demande = visaRequestRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("demande introuvable: " + id));

        Passeport passeport = demande.getPasseport();
        Demandeur demandeur = (passeport != null) ? passeport.getDemandeur() : null;
        VisaTransformable vt = demande.getVisaTransformable();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("demandeId", demande.getId());
        result.put("typeDemandeId", (demande.getTypeDemande() != null) ? demande.getTypeDemande().getId() : null);

        result.put("etat civil", mapEtatCivil(demandeur));
        result.put("passeport", mapPasseport(passeport));
        result.put("visaTransformable", mapVisaTransformable(vt));
        result.put("dossiersFournis", getDossiersFournisIds(id));

        return result;
    }

    private Map<String, Object> mapEtatCivil(Demandeur demandeur) {
        Map<String, Object> etatCivil = new LinkedHashMap<>();
        if (demandeur != null) {
            etatCivil.put("nom", demandeur.getNom());
            etatCivil.put("prenom", demandeur.getPrenom());
            etatCivil.put("nomJeuneFille", demandeur.getNomJeuneFille());
            etatCivil.put("situationFamiliale", (demandeur.getSituationFamiliale() != null) ? demandeur.getSituationFamiliale().getId() : null);
            etatCivil.put("nationalite", (demandeur.getNationalite() != null) ? demandeur.getNationalite().getId() : null);
            etatCivil.put("dateNaissance", demandeur.getDateNaissance());
            etatCivil.put("adresse", demandeur.getAdresse());
            etatCivil.put("email", demandeur.getEmail());
            etatCivil.put("numTel", demandeur.getNumTel());
        }
        return etatCivil;
    }

    private Map<String, Object> mapPasseport(Passeport passeport) {
        Map<String, Object> pass = new LinkedHashMap<>();
        if (passeport != null) {
            pass.put("numero", passeport.getNumero());
            pass.put("dateDelivrance", passeport.getDateDelivrance());
            pass.put("dateExpiration", passeport.getDateExpiration());
        }
        return pass;
    }

    private Map<String, Object> mapVisaTransformable(VisaTransformable vt) {
        Map<String, Object> visa = new LinkedHashMap<>();
        if (vt != null) {
            visa.put("reference", vt.getReference());
            visa.put("dateEntree", vt.getDateEntree());
            visa.put("lieuEntree", vt.getLieuEntree());
            visa.put("dateExpiration", vt.getDateExpiration());
        }
        return visa;
    }

    private List<Integer> getDossiersFournisIds(Integer demandeId) {
        return reponseStatutVisaRepository.findByDemandeId(demandeId).stream()
            .filter(ReponseStatutVisa::getValeur)
            .map(r -> r.getDossier().getId())
            .toList();
    }

    /**
     * 
     * 
     * 
     * Antsoin'ny front.updateDemande (put)
     * 
     * 
     * 
     */
    
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> updateDemandeVisa(Integer id, Map<String, Object> donnees) {
        if (donnees == null) {
            throw new IllegalArgumentException("donnees de demande obligatoires.");
        }

        Demande demande = visaRequestRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("demande introuvable: " + id));

        controleStatut(demande);

        Map<String, Object> etatCivilData = UtilService.getBloc(donnees, "etat civil");
        Map<String, Object> passeportData = UtilService.getBloc(donnees, "passeport");
        Map<String, Object> visaTransformableData = UtilService.getBloc(donnees, "visaTransformable");
        Integer typeDemandeId = toLong(donnees.get("typeDemandeId"));
        List<Integer> dossiersFournisIds = getDossiersFournis(donnees);

        TypeDemande typeDemande = getTypeDemandeValide(typeDemandeId);
        List<Dossier> dossiersApplicables = getDossiersApplicables(typeDemande);
        checkDossiersObligatoires(dossiersApplicables, dossiersFournisIds);

        // Update entites (maka id izay vao miupdate)
        Demandeur demandeur = demande.getPasseport().getDemandeur();
        updateDemandeurData(demandeur.getId(), etatCivilData);

        Passeport passeport = demande.getPasseport();
        updatePasseportData(passeport.getId(), passeportData);

        VisaTransformable vt = demande.getVisaTransformable();
        updateVisaTransformableData(vt.getId(), visaTransformableData);

        demande.setTypeDemande(typeDemande);
        visaRequestRepository.save(demande);

        // Update responses dossier 
        reponseStatutVisaRepository.deleteAll(reponseStatutVisaRepository.findByDemandeId(id));
        saveReponseDossier(demande, dossiersApplicables, dossiersFournisIds);

        return reponseCreation(demandeur, passeport, vt, demande, dossiersFournisIds);
    }

    private void controleStatut(Demande demande) {
        Statut actuel = demandeService.getDernierStatus(demande);
        Statut cible = statutRepository.findByLibelle(UtilService.STATUS_SCAN_TERMINE)
            .orElseThrow(() -> new IllegalArgumentException("Statut '" + UtilService.STATUS_SCAN_TERMINE + "' introuvable"));

        if (actuel != null && actuel.getOrdre() > cible.getOrdre()) {
            throw new IllegalStateException("Impossible de modifier une demande dont les documents ont deja ete scannes");
        }
    }

    private Demandeur updateDemandeurData(Integer id, Map<String, Object> data) {
        Demandeur demandeur = new Demandeur();
        demandeur.setNom((String) data.get("nom"));
        demandeur.setPrenom((String) data.get("prenom"));
        demandeur.setNomJeuneFille((String) data.get("nomJeuneFille"));
        demandeur.setEmail((String) data.get("email"));
        demandeur.setNumTel((String) data.get("numTel"));
        demandeur.setAdresse((String) data.get("adresse"));

        Object dateNaissance = data.get("dateNaissance");
        if (dateNaissance != null && !dateNaissance.toString().isBlank()) {
            demandeur.setDateNaissance(LocalDate.parse(dateNaissance.toString()));
        }

        Integer nationaliteId = toLong(data.get("nationalite"));
        if (nationaliteId != null) {
            Nationalite nationalite = new Nationalite();
            nationalite.setId(nationaliteId);
            demandeur.setNationalite(nationalite);
        }

        Integer situationFamilialeId = toLong(data.get("situationFamiliale"));
        if (situationFamilialeId != null) {
            SituationFamiliale situationFamiliale = new SituationFamiliale();
            situationFamiliale.setId(situationFamilialeId);
            demandeur.setSituationFamiliale(situationFamiliale);
        }

        return demandeurService.updateDemandeur(id, demandeur);
    }

    private Passeport updatePasseportData(Integer id, Map<String, Object> data) {
        Passeport p = new Passeport();
        p.setNumero((String) data.get("numero"));

        Object dateDelivrance = data.get("dateDelivrance");
        if (dateDelivrance != null && !dateDelivrance.toString().isBlank()) {
            p.setDateDelivrance(LocalDateTime.parse(dateDelivrance.toString()));
        }

        Object dateExpiration = data.get("dateExpiration");
        if (dateExpiration != null && !dateExpiration.toString().isBlank()) {
            p.setDateExpiration(LocalDateTime.parse(dateExpiration.toString()));
        }

        return passeportService.updatePasseport(id, p);
    }

    private VisaTransformable updateVisaTransformableData(Integer id, Map<String, Object> data) {
        VisaTransformable vt = new VisaTransformable();
        vt.setReference((String) data.get("reference"));
        vt.setLieuEntree((String) data.get("lieuEntree"));

        Object dateEntree = data.get("dateEntree");
        if (dateEntree != null && !dateEntree.toString().isBlank()) {
            vt.setDateEntree(LocalDateTime.parse(dateEntree.toString()));
        }

        Object dateExpiration = data.get("dateExpiration");
        if (dateExpiration != null && !dateExpiration.toString().isBlank()) {
            vt.setDateExpiration(LocalDateTime.parse(dateExpiration.toString()));
        }

        return visaTransformableService.updateVisaTransformable(id, vt);
    }
}
