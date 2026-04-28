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

    private final VisaRequestService visaRequestService;
    private final AcceptationDemandeVisaService acceptationDemandeVisaService;
    private final DemandeTransfertVisaRepository demandeTransfertVisaRepository;
    private final VisaRepository visaRepository;
    private final StatutRepository statutRepository;

    @Transactional(rollbackFor = Exception.class)
    public DemandeTransfertVisa creerDemandeTransfertAda(Map<String, Object> donnees) {
        Integer idVisa = (Integer) donnees.get("idVisa");
        Visa visa = visaRepository.findById(idVisa)
            .orElseThrow(() -> new IllegalArgumentException("Visa " + idVisa + " introuvable"));
        
        Demande demandeOrigine = visa.getDemande();
        Passeport nouveauPasseport = creerNouveauPasseport(donnees, demandeOrigine);

        DemandeTransfertVisa demandeTransfertVisa = buildDemandeTransfert(demandeOrigine, nouveauPasseport);

        return demandeTransfertVisaRepository.save(demandeTransfertVisa);
    }

    @Transactional(rollbackFor = Exception.class)
    public DemandeTransfertVisa creerDemandeTransfertSda(Map<String, Object> donnees) {
        Demande demande = visaRequestService.creerDemandeVisa(donnees, "Nouveau titre", "Visa accepte");
        Visa visa = acceptationDemandeVisaService.creerVisaEtCarteResident(demande);

        Passeport nouveauPasseport = creerNouveauPasseport(donnees, demande);

        DemandeTransfertVisa demandeTransfertVisa = buildDemandeTransfert(demande, nouveauPasseport);

        // assignerVisaAuPasseport(visa, nouveauPasseport); // Decommentena raha tonga dia omena an le visa le passeport fa tsy mandalo validation

        return demandeTransfertVisaRepository.save(demandeTransfertVisa);
    }

    private Passeport creerNouveauPasseport(Map<String, Object> donnees, Demande demande) {

        Map<String, Object> nouveauPasseportData = (Map<String, Object>) donnees.get("nouveau passeport");
        if (nouveauPasseportData == null) {
            throw new IllegalArgumentException("Les donnees du nouveau passeport sont obligatoires pour un transfert");
        }

        Passeport nouveauPasseport = visaRequestService.createPasseport(nouveauPasseportData, demande.getPasseport().getDemandeur().getId());
        return nouveauPasseport;
    }

    private DemandeTransfertVisa buildDemandeTransfert(Demande demande, Passeport nouveauPasseport) {
        DemandeTransfertVisa demandeTransfertVisa = new DemandeTransfertVisa();
        
        demandeTransfertVisa.setDemande(demande);
        demandeTransfertVisa.setNouveauPasseport(nouveauPasseport);
        demandeTransfertVisa.setDateCreation(demande.getDateCreation());

        setStatut(demandeTransfertVisa, "Demande creee");

        return demandeTransfertVisa;
    }

    /**
     * 
     * 
     * UTILS
     * 
     * 
     */

    public void setStatut(DemandeTransfertVisa transfert, String statutLibelle) {
        Statut statut = statutRepository.findByLibelle(statutLibelle)
            .orElseThrow(() -> new IllegalArgumentException("statut '" + statutLibelle + "' introuvable"));

        HistoriqueStatutDemandeTransfert historique = new HistoriqueStatutDemandeTransfert();
        historique.setTransfert(transfert);
        historique.setStatut(statut);
        historique.setDateModification(LocalDateTime.now());

        if (transfert.getHistoriques() == null) {
            transfert.setHistoriques(new ArrayList<>());
        }
        transfert.getHistoriques().add(historique);

        demandeTransfertVisaRepository.save(transfert);
    }

    // Tonga dia foroniko na sy ampiasaiko zao aza
    public Statut getStatut(DemandeTransfertVisa transfert) {
        if (transfert.getHistoriques() == null || transfert.getHistoriques().isEmpty()) {
            return null;
        }
        return transfert.getHistoriques().stream()
            .max(Comparator.comparing(HistoriqueStatutDemandeTransfert::getDateModification))
            .map(HistoriqueStatutDemandeTransfert::getStatut)
            .orElse(null);
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
     * Mamadika statut ho lasa: "Demande Acceptee"
     * 
     */

    @Transactional(rollbackFor = Exception.class)
    public void accepterTransfert(Integer transferId) {
        DemandeTransfertVisa transfert = demandeTransfertVisaRepository.findById(transferId)
            .orElseThrow(() -> new IllegalArgumentException("Demande de transfert " + transferId + " introuvable"));

        controleStatusTransfert(transfert);

        Visa visa = visaRepository.findAll().stream()
            .filter(v -> v.getDemande().getId().equals(transfert.getDemande().getId()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Aucun visa trouve pour la demande de visa: " + transfert.getDemande().getId()));

        assignerVisaAuPasseport(visa, transfert.getNouveauPasseport(), visaRepository);

        setStatut(transfert, UtilService.STATUS_DEMANDE_ACCEPTEE);
    }

    private void controleStatusTransfert(DemandeTransfertVisa transfert) {
        Statut actuel = getStatut(transfert);
        Statut cible = statutRepository.findByLibelle(UtilService.STATUS_DEMANDE_ACCEPTEE)
            .orElseThrow(() -> new IllegalArgumentException("Statut '" + UtilService.STATUS_DEMANDE_ACCEPTEE + "' introuvable"));

        if (actuel != null) {
            if (actuel.getOrdre() >= cible.getOrdre()) {
                throw new IllegalStateException("Demande deja acceptee");
            }
        }
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
            
            Demande d = t.getDemande();
            if (d != null && d.getPasseport() != null && d.getPasseport().getDemandeur() != null) {
                Demandeur dr = d.getPasseport().getDemandeur();
                map.put("demandeur", dr.getNom() + " " + dr.getPrenom());
                map.put("ancienPasseport", d.getPasseport().getNumero());
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

            Statut s = getStatut(t);
            map.put("statut", s != null ? s.getLibelle() : "Aucun");

            result.add(map);
        }

        return result;
    }
}
