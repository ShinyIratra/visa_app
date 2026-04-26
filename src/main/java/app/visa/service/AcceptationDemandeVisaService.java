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
    

    @Transactional(rollbackFor = Exception.class)
    public Visa creerVisaEtCarteResident(Demande demande) {
        String status = demandeService.getDernierStatus(demande);
        if (!"Visa accepte".equals(status)) { // TODO: atao >= fa tsy equals, asina ordre ny status
            throw new IllegalStateException("La demande doit etre acceptee pour creer un visa et une carte de resident");
        }

        // Creation Visa
        Visa visa = new Visa();
        visa.setDateCreation(LocalDateTime.now());
        visa.setDemande(demande);
        
        // Assigner visa au passeport de la demande
        if (demande.getPasseport() != null) {
            TransfertVisaService.assignerVisaAuPasseport(visa, demande.getPasseport());
        }
        
        visa = visaRepository.save(visa);

        // Creation carte resident
        CarteResident carteResident = new CarteResident();
        carteResident.setDateCreation(LocalDateTime.now());
        carteResident.setDemande(demande);
        carteResident.setPasseport(demande.getPasseport());
        
        carteResidentRepository.save(carteResident);

        return visa;
    }

    
}
