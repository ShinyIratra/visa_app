
package app.visa.service;

import app.visa.entity.Demande;
import app.visa.dto.VisaRequestDto;
import app.visa.entity.Passeport;
import app.visa.entity.VisaTransformable;
import app.visa.entity.*;
import app.visa.repository.DemandeurRepository;
import app.visa.repository.NationaliteRepository;
import app.visa.repository.SituationFamilialeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DemandeService {

    private final app.visa.repository.DemandeRepository demandeRepository;
    private final app.visa.repository.TypeDemandeRepository typeDemandeRepository;
    private final app.visa.repository.CategorieRepository categorieRepository;
    private final app.visa.repository.HistoriqueStatutRepository historiqueStatutRepository;

    public Statut getDernierStatus(Demande demande) {
        if (demande == null || demande.getId() == null) return null;
        return historiqueStatutRepository.findLatestByDemandeId(demande.getId())
            .map(HistoriqueStatut::getStatut)
            .orElse(null);
    }

    public Demande getById(Integer id) {
        return demandeRepository.findById(id).orElse(null);
    }

    public Demande findById(Integer id) {
        return demandeRepository.findById(id).orElse(null);
    }
    
    public Demande buildDemande(VisaRequestDto dto, Passeport passeport, VisaTransformable vt) {
        Demande demande = new Demande();
        demande.setDateCreation(LocalDateTime.now());
        demande.setPasseport(passeport);
        demande.setVisaTransformable(vt);

        if (dto.getTypeDemandeId() != null) {
            demande.setTypeDemande(typeDemandeRepository.findById(dto.getTypeDemandeId()).orElse(null));
        }

        demande.setCategorie(categorieRepository.findAll().stream()
            .filter(c -> "Nouveau titre".equalsIgnoreCase(c.getLibelle()))
            .findFirst().orElse(null));

        return demande;
    }
}
