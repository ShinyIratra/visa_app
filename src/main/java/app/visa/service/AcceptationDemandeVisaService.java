package app.visa.service;

import app.visa.entity.*;
import app.visa.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;

@Service
@RequiredArgsConstructor
public class AcceptationDemandeVisaService {

    private final VisaRepository visaRepository;
    private final CarteResidentRepository carteResidentRepository;
    private final DemandeService demandeService;
    private final StatutRepository statutRepository;
    

    @Transactional(rollbackFor = Exception.class)
    public Visa creerVisaEtCarteResident(Demande demande) {
        // Controle
        controlerStatusDemande(demande);

        // Creation Visa
        Visa visa = new Visa();
        visa.setDateCreation(LocalDateTime.now());
        visa.setDemande(demande);
        
        // Assigner visa au passeport de la demande
        TransfertVisaService.assignerVisaAuPasseport(visa, demande.getPasseport(), visaRepository);
        
        // Creation carte resident
        CarteResident carteResident = new CarteResident();
        carteResident.setDateCreation(LocalDateTime.now());
        carteResident.setDemande(demande);
        carteResident.setPasseport(demande.getPasseport());
        
        carteResidentRepository.save(carteResident);

        return visa;
    }

    private void controlerStatusDemande(Demande demande) {
        Statut actuel = demandeService.getDernierStatus(demande);
        Statut cible = statutRepository.findByLibelle(UtilService.STATUS_SCAN_TERMINE)
            .orElseThrow(() -> new IllegalArgumentException("Statut '" + UtilService.STATUS_SCAN_TERMINE + "' introuvable"));

        if (actuel == null || actuel.getOrdre() < cible.getOrdre()) {
            throw new IllegalStateException("Les dossiers doivent etre scannes avant que la demande puisse etre acceptee");
        }
    }
}
