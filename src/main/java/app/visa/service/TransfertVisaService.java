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
public class TransfertVisaService {

    protected final VisaRequestService visaRequestService;
    protected final AcceptationDemandeVisaService acceptationDemandeVisaService;
    protected final DemandeTransfertVisaRepository demandeTransfertVisaRepository;
    protected final VisaRepository visaRepository;
    protected final StatutRepository statutRepository;
    protected final DemandeService demandeService;

    @Transactional(rollbackFor = Exception.class)
    public DemandeTransfertVisa creerDemandeTransfertAda(Map<String, Object> donnees) {
        String typeRecherche = (String) donnees.get("type_recherche");
        String valeur = (String) donnees.get("valeur");
        
        LocalDateTime dateCreation = null;
        if (donnees.get("dateCreation") != null && !donnees.get("dateCreation").toString().isBlank()) {
            dateCreation = LocalDateTime.parse(donnees.get("dateCreation").toString());
        }

        Demande demandeOrigine = demandeService.findDemandeByCritere(typeRecherche, valeur);
        if (!demandeOrigine.getCategorie().getLibelle().equals(UtilService.CATEGORIE_DEMANDE_NOUVEAU_TITRE)) {
            throw new IllegalArgumentException("L'origine de la demande n'est pas une demande de nouveau titre");
        }
        Passeport nouveauPasseport = creerNouveauPasseport(donnees, demandeOrigine);

        return buildDemandeTransfert(demandeOrigine, nouveauPasseport, dateCreation);
    }

    @Transactional(rollbackFor = Exception.class)
    public DemandeTransfertVisa creerDemandeTransfertSda(Map<String, Object> donnees) {
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

        Passeport nouveauPasseport = creerNouveauPasseport(donnees, demande);

        // assignerVisaAuPasseport(visa, nouveauPasseport); // Decommentena raha tonga dia omena an le visa le passeport fa tsy mandalo validation

        return buildDemandeTransfert(demande, nouveauPasseport, dateCreation);
    }

    private Passeport creerNouveauPasseport(Map<String, Object> donnees, Demande demande) {

        Map<String, Object> nouveauPasseportData = (Map<String, Object>) donnees.get("nouveau passeport");
        if (nouveauPasseportData == null) {
            throw new IllegalArgumentException("Les donnees du nouveau passeport sont obligatoires pour un transfert");
        }

        Passeport nouveauPasseport = visaRequestService.createPasseport(nouveauPasseportData, demandeService.getPasseport(demande).getDemandeur().getId());
        return nouveauPasseport;
    }

    private DemandeTransfertVisa buildDemandeTransfert(Demande demande, Passeport nouveauPasseport) {
        return buildDemandeTransfert(demande, nouveauPasseport, null);
    }

    private DemandeTransfertVisa buildDemandeTransfert(Demande demande, Passeport nouveauPasseport, LocalDateTime dateCreation) {
        LocalDateTime dateFinale = dateCreation != null ? dateCreation : LocalDateTime.now();

        if (dateFinale.isBefore(demande.getDateCreation())) {
            throw new IllegalArgumentException("La date de demande de transfert ne peut pas être antérieure à la date de la demande originale (" + demande.getDateCreation() + ")");
        }

        if (nouveauPasseport != null && nouveauPasseport.getDateExpiration() != null) {
            if (dateFinale.isAfter(nouveauPasseport.getDateExpiration())) {
                throw new IllegalArgumentException("La date de la demande de transfert (" + dateFinale + ") ne peut pas être postérieure à la date d'expiration du nouveau passeport (" + nouveauPasseport.getDateExpiration() + ")");
            }
        }

        DemandeTransfertVisa dtv = new DemandeTransfertVisa();
        demandeService.setupBaseDemande(dtv, "Transfert de visa", dateFinale);
        
        dtv.setDemandeOrigine(demande);
        dtv.setNouveauPasseport(nouveauPasseport);

        dtv = demandeTransfertVisaRepository.save(dtv);
        demandeService.ajouterHistoriqueStatut(dtv, UtilService.STATUS_DEMANDE_CREEE, dateFinale);

        return dtv;
    }

    /**
     * Statut = Scan termine
     */
    @Transactional(rollbackFor = Exception.class)
    public void marquerCommeScanne(Integer transferId) {
        DemandeTransfertVisa transfert = findTransfertById(transferId);
        
        demandeService.verifierDroitDePasserA(transfert, UtilService.STATUS_SCAN_TERMINE);
        
        demandeService.ajouterHistoriqueStatut(transfert, UtilService.STATUS_SCAN_TERMINE);
    }

    /* Logic moved to VisaRequestService / DemandeService */

    public Statut getStatutActuel(DemandeTransfertVisa transfert) {
        return demandeService.getDernierStatus(transfert);
    }

    // TODO: mitady fika tsy mampa static an'ito amzay visaRepository tsy atao argument
    public static void assignerVisaAuPasseport(Visa visa, Passeport passeport, VisaRepository visaRepository) {
        if (visa.getPasseports() == null) {
            visa.setPasseports(new HashSet<>());
        }
        visa.getPasseports().add(passeport);
        visaRepository.save(visa);
    }

    /**
     * 
     * Statut ho lasa: "Demande Acceptee"
     * 
     */

    @Transactional(rollbackFor = Exception.class)
    public void accepterTransfert(Integer transferId) {
        DemandeTransfertVisa transfert = findTransfertById(transferId);

        demandeService.verifierDroitDePasserA(transfert, UtilService.STATUS_DEMANDE_ACCEPTEE);

        // Controle bonus
        if (!demandeService.isStatutDejaAtteint(transfert, UtilService.STATUS_SCAN_TERMINE)) {
            throw new IllegalStateException("Impossible d'accepter : le dossier doit d'abord etre scanne");
        }

        Visa visa = getVisa(transfert);

        assignerVisaAuPasseport(visa, transfert.getNouveauPasseport(), visaRepository);

        demandeService.ajouterHistoriqueStatut(transfert, UtilService.STATUS_DEMANDE_ACCEPTEE);
    }

    /**
     * 
     * Solon'ny getAll()
     * ho an'ny page /transfert-visa
     * 
     */

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listTransfertsAvecInfos() {
        List<DemandeTransfertVisa> transferts = demandeTransfertVisaRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();

        for (DemandeTransfertVisa t : transferts) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", t.getId());
            
            Passeport pOld = (t.getDemandeOrigine() != null) ? demandeService.getPasseport(t.getDemandeOrigine()) : null;
            if (pOld != null && pOld.getDemandeur() != null) {
                Demandeur dr = pOld.getDemandeur();
                map.put("demandeur", dr.getNom() + " " + (dr.getPrenom() != null ? dr.getPrenom() : ""));
                map.put("ancienPasseport", pOld.getNumero());
            } else {
                map.put("demandeur", "Inconnu");
                map.put("ancienPasseport", "Inconnu");
            }

            if (t.getNouveauPasseport() != null) {
                map.put("nouveauPasseport", t.getNouveauPasseport().getNumero());
                if (t.getNouveauPasseport().getDemandeur() != null && t.getNouveauPasseport().getDemandeur().getNationalite() != null) {
                    map.put("nationalite", t.getNouveauPasseport().getDemandeur().getNationalite().getLibelle()); // 💀
                } else {
                    map.put("nationalite", "Inconnue");
                }
            } else {
                map.put("numeroPasseport", "Inconnu");
                map.put("nationalite", "Inconnue");
            }

            Statut s = getStatutActuel(t);
            map.put("statut", s != null ? s.getLibelle() : "Aucun");
            map.put("dateCreation", t.getDateCreation());

            result.add(map);
        }

        return result;
    }

    /**
     * 
     * 
     * Utils
     * 
     * Tokony ho ato koa le ajouterHistoriqueStatut (3 arguments)
     * Fa aleo zao amzay mora vakiana ilay historique git
     *  
     */

    private Visa getVisa(DemandeTransfertVisa transfert) {
        Visa visa = visaRepository.findAll().stream()
            .filter(v -> v.getDemande().getId().equals(transfert.getDemandeOrigine().getId()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Aucun visa trouvé pour la demande d'origine : " + transfert.getDemandeOrigine().getId()));
        return visa;
    }
    
    private DemandeTransfertVisa findTransfertById(Integer id) {
        return demandeTransfertVisaRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Demande de transfert #" + id + " introuvable."));
    }

    private Statut findStatutByLibelle(String libelle) {
        return statutRepository.findByLibelle(libelle)
            .orElseThrow(() -> new IllegalArgumentException("Statut '" + libelle + "' introuvable en base."));
    }
}
