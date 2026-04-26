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
}
