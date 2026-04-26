package app.visa.service;

import java.util.HashMap;

import app.visa.entity.*;
import app.visa.repository.DemandeTransfertVisaRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TransfertVisaService {

    private final VisaRequestService visaRequestService;
    private final PasseportService passeportService;
    private final DemandeTransfertVisaRepository demandeTransfertVisaRepository;

    @Transactional(rollbackFor = Exception.class)
    public DemandeTransfertVisa creerDemandeTransfertSda(Map<String, Object> donnees) {
        Demande demande = visaRequestService.creerDemandeVisa(donnees, "Transfert de visa", "Visa accepte");

        // TODO: creation visa et carte resident

        Map<String, Object> nouveauPasseportData = getNouveauPasseportData(donnees);
        Passeport nouveauPasseport = visaRequestService.createPasseport(nouveauPasseportData, demande.getPasseport().getDemandeur().getId());

        DemandeTransfertVisa demandeTransfertVisa = new DemandeTransfertVisa();
        demandeTransfertVisa.setDemande(demande);
        demandeTransfertVisa.setNouveauPasseport(nouveauPasseport);
        demandeTransfertVisa.setDateCreation(demande.getDateCreation());

        // TODO: assignation visa au nouveau passeport

        return demandeTransfertVisaRepository.save(demandeTransfertVisa);
    }

    private Map<String, Object> getNouveauPasseportData(Map<String, Object> donnees) {
        Map<String, Object> nouveauPasseportData = (Map<String, Object>) donnees.get("nouveau passeport");
        if (nouveauPasseportData == null) {
            throw new IllegalArgumentException("Les donnees du nouveau passeport sont obligatoires pour un transfert");
        }
        return nouveauPasseportData;
    }
}
