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
    public DemandeTransfertVisa creerDemandeTransfertSda(Map<String, Object> donnees) {
        Demande demande = visaRequestService.creerDemandeVisa(donnees, "Transfert de visa", "Visa accepte");
        Visa visa = acceptationDemandeVisaService.creerVisaEtCarteResident(demande);

        Map<String, Object> nouveauPasseportData = getNouveauPasseportData(donnees);
        Passeport nouveauPasseport = visaRequestService.createPasseport(nouveauPasseportData, demande.getPasseport().getDemandeur().getId());

        DemandeTransfertVisa demandeTransfertVisa = buildDemandeTransfert(demande, nouveauPasseport);

        // assignerVisaAuPasseport(visa, nouveauPasseport);

        return demandeTransfertVisaRepository.save(demandeTransfertVisa);
    }

    private Map<String, Object> getNouveauPasseportData(Map<String, Object> donnees) {
        Map<String, Object> nouveauPasseportData = (Map<String, Object>) donnees.get("nouveau passeport");
        if (nouveauPasseportData == null) {
            throw new IllegalArgumentException("Les donnees du nouveau passeport sont obligatoires pour un transfert");
        }
        return nouveauPasseportData;
    }

    private DemandeTransfertVisa buildDemandeTransfert(Demande demande, Passeport nouveauPasseport) {
        DemandeTransfertVisa demandeTransfertVisa = new DemandeTransfertVisa();
        
        demandeTransfertVisa.setDemande(demande);
        demandeTransfertVisa.setNouveauPasseport(nouveauPasseport);
        demandeTransfertVisa.setDateCreation(demande.getDateCreation());

        setStatut(demandeTransfertVisa, "Demande creee");

        return demandeTransfertVisa;
    }

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

    
    public static void assignerVisaAuPasseport(Visa visa, Passeport passeport) {
        if (visa.getPasseports() == null) {
            visa.setPasseports(new HashSet<>());
        }
        visa.getPasseports().add(passeport);
    }

    @Transactional(rollbackFor = Exception.class)
    public void accepterTransfert(Integer transferId) {
        DemandeTransfertVisa transfert = demandeTransfertVisaRepository.findById(transferId)
            .orElseThrow(() -> new IllegalArgumentException("Demande de transfert " + transferId + " introuvable"));

        // TODO: check statut

        Visa visa = visaRepository.findAll().stream()
            .filter(v -> v.getDemande().getId().equals(transfert.getDemande().getId()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Aucun visa trouve pour la demande de visa: " + transfert.getDemande().getId()));

        assignerVisaAuPasseport(visa, transfert.getNouveauPasseport());
        visaRepository.save(visa);

        setStatut(transfert, "Demande acceptee");
        demandeTransfertVisaRepository.save(transfert);
    }

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
