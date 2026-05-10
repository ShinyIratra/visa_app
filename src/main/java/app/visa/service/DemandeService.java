
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
    private final app.visa.repository.StatutRepository statutRepository;
    private final app.visa.repository.VisaRepository visaRepository;
    private final app.visa.repository.PasseportRepository passeportRepository;
    private final CarteResidentService carteResidentService;

    public Statut getDernierStatus(Demande demande) {
        if (demande == null || demande.getId() == null) return null;
        return historiqueStatutRepository.findLatestByDemandeId(demande.getId())
            .map(HistoriqueStatut::getStatut)
            .orElse(null);
    }

    @Transactional(readOnly = true)
    public boolean isStatusOuPlus(Integer demandeId, String status_label) {
        if (demandeId == null) {
            return false;
        }

        Statut status = statutRepository.findByLibelle(status_label)
            .orElseThrow(() -> new IllegalArgumentException(
                "Statut '" + status_label + "' introuvable"
            ));

        return historiqueStatutRepository.existsByDemandeIdAndStatutOrdreGreaterThanEqual(
            demandeId,
            status.getOrdre()
        );
    }

    @Transactional(readOnly = true)
    public boolean isStatusOuPlus(Integer demandeId, Integer ordre) {
        if (demandeId == null) {
            return false;
        }

        Statut scanTermine = statutRepository.findByOrdre(ordre)
            .orElseThrow(() -> new IllegalArgumentException(
                "Statut '" + ordre + "' introuvable"
            ));

        return historiqueStatutRepository.existsByDemandeIdAndStatutOrdreGreaterThanEqual(
            demandeId,
            scanTermine.getOrdre()
        );
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

    public Demande findDemandeByCritere(String typeRecherche, String valeur) {
        if (typeRecherche == null || valeur == null || valeur.isEmpty()) {
            throw new IllegalArgumentException("Informations de recherche incompletes.");
        }

        switch (typeRecherche) {
            case "id_demande":
                Integer idDemande = Integer.parseInt(valeur);
                return demandeRepository.findById(idDemande)
                    .orElseThrow(() -> new IllegalArgumentException("Demande #" + idDemande + " introuvable."));
            case "id_visa":
                Integer idVisa = Integer.parseInt(valeur);
                Visa visa = visaRepository.findById(idVisa)
                    .orElseThrow(() -> new IllegalArgumentException("Visa #" + idVisa + " introuvable."));
                return visa.getDemande();
            case "passeport_original":
                return findDemandeByPasseportOriginal(valeur);
            case "carte_resident_passeport":
                // Otran le taloha ihany
                CarteResident carteResident = carteResidentService.findByLastNumeroPasseport(valeur);
                return carteResident.getDemande();
            case "passeport_actuel":
                return findDemandeByPasseportActuel(valeur);
            default:
                throw new IllegalArgumentException("Type de recherche '" + typeRecherche + "' non supporte.");
        }
    }

    private Demande findDemandeByPasseportOriginal(String numeroPasseport) {
        return demandeRepository.findAll().stream()
            .filter(d -> d.getPasseport().getNumero().equalsIgnoreCase(numeroPasseport))
            .filter(d -> d.getCategorie().getLibelle().equals("Nouveau titre"))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Aucune demande originale trouvee pour le passeport '" + numeroPasseport + "'."));
    }

    private Demande findDemandeByPasseportActuel(String numeroPasseport) {
        return visaRepository.findAll().stream()
            .filter(v -> v.getPasseports().stream().anyMatch(p -> p.getNumero().equalsIgnoreCase(numeroPasseport)))
            .filter(v -> v.getDemande().getCategorie().getLibelle().equals("Nouveau titre"))
            .sorted((v1, v2) -> v2.getDateCreation().compareTo(v1.getDateCreation()))
            .map(Visa::getDemande)
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Aucune demande trouvee pour le passeport '" + numeroPasseport + "'."));
    }
}
