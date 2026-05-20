
package app.visa.service;

import app.visa.entity.Demande;
import app.visa.entity.Passeport;
import app.visa.entity.VisaTransformable;
import app.visa.entity.*;
import app.visa.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DemandeService {

    private final DemandeRepository demandeRepository;
    private final TypeDemandeRepository typeDemandeRepository;
    private final CategorieRepository categorieRepository;
    private final HistoriqueStatutRepository historiqueStatutRepository;
    private final StatutRepository statutRepository;
    private final VisaRepository visaRepository;
    private final PasseportRepository passeportRepository;
    private final CarteResidentService carteResidentService;

    public Statut getDernierStatus(Demande demande) {
        if (demande == null || demande.getId() == null) return null;
        return historiqueStatutRepository.findLatestByDemandeId(demande.getId())
            .map(HistoriqueStatut::getStatut)
            .orElse(null);
    }

    @Transactional(readOnly = true)
    public boolean isStatusOuPlus(Integer demandeId, String status_label) {
        if (demandeId == null) return false;

        Statut status = statutRepository.findByLibelle(status_label)
            .orElseThrow(() -> new IllegalArgumentException("Statut '" + status_label + "' introuvable"));

        return historiqueStatutRepository.existsByDemandeIdAndStatutOrdreGreaterThanEqual(
            demandeId,
            status.getOrdre()
        );
    }

    @Transactional(readOnly = true)
    public boolean isStatusOuPlus(Integer demandeId, Float ordre) {
        if (demandeId == null) return false;

        return historiqueStatutRepository.existsByDemandeIdAndStatutOrdreGreaterThanEqual(
            demandeId,
            ordre
        );
    }

    public Demande getById(Integer id) {
        return demandeRepository.findById(id).orElse(null);
    }

    public Demande findById(Integer id) {
        return demandeRepository.findById(id).orElse(null);
    }

    public Passeport getPasseport(Demande d) {
        if (d instanceof DemandeNouveauTitre dnt) {
            return dnt.getPasseport();
        } else if (d instanceof DemandeTransfertVisa dtv) {
            return dtv.getNouveauPasseport();
        } else if (d instanceof DemandeDuplicata dd) {
            return getPasseport(dd.getDemandeOrigine());
        }
        return null;
    }

    public DemandeNouveauTitre getDemandeNouveauTitre(Demande d) {
        if (d instanceof DemandeNouveauTitre dnt) {
            return dnt;
        } else if (d instanceof DemandeTransfertVisa dtv) {
            return getDemandeNouveauTitre(dtv.getDemandeOrigine());
        } else if (d instanceof DemandeDuplicata dd) {
            return getDemandeNouveauTitre(dd.getDemandeOrigine());
        }
        return null;
    }

    public <T extends Demande> T setupBaseDemande(T d, String categorieLibelle, LocalDateTime dateCreation) {
        Categorie categorie = categorieRepository.findByLibelle(categorieLibelle)
            .orElseThrow(() -> new IllegalArgumentException("categorie '" + categorieLibelle + "' introuvable."));
        d.setCategorie(categorie);
        if (d.getCategorie() == null) {
            throw new IllegalStateException("Failed to set categorie on Demande object!");
        }
        d.setDateCreation(dateCreation != null ? dateCreation : LocalDateTime.now());
        return d;
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
                if (carteResident == null) 
                    throw new IllegalArgumentException("Carte resident introuvable for '" + valeur + "'.");
                return carteResident.getDemande();
            case "passeport_actuel":
                return findDemandeByPasseportActuel(valeur);
            default:
                throw new IllegalArgumentException("Critere de recherche inconnu: " + typeRecherche);
        }
    }

    private Demande findDemandeByPasseportOriginal(String numeroPasseport) {
        return demandeRepository.findAll().stream()
            .filter(d -> {
                Passeport p = getPasseport(d);
                return p != null && p.getNumero().equalsIgnoreCase(numeroPasseport);
            })
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

    @Transactional(rollbackFor = Exception.class)
    public void ajouterHistoriqueStatut(Demande dem, String statutLibelle) {
        ajouterHistoriqueStatut(dem, statutLibelle, null);
    }

    @Transactional(rollbackFor = Exception.class)
    public void ajouterHistoriqueStatut(Demande dem, String statutLibelle, LocalDateTime dateModification) {
        Statut statut = statutRepository.findByLibelle(statutLibelle)
            .orElseThrow(() -> new IllegalArgumentException("Statut '" + statutLibelle + "' introuvable."));

        HistoriqueStatut historique = new HistoriqueStatut();
        historique.setDemande(dem);
        historique.setStatut(statut);
        historique.setDateModification(dateModification != null ? dateModification : LocalDateTime.now());

        historiqueStatutRepository.save(historique);
    }

    @Transactional(rollbackFor = Exception.class)
    public void verifierDroitDePasserA(Demande dem, String statutCibleLibelle) {
        Statut statutCible = statutRepository.findByLibelle(statutCibleLibelle)
            .orElseThrow(() -> new IllegalArgumentException("Statut '" + statutCibleLibelle + "' introuvable."));
        
        Statut actuel = getDernierStatus(dem);
        if (actuel != null && actuel.getOrdre() >= statutCible.getOrdre()) {
            throw new IllegalStateException("La demande est deja au statut '" + actuel.getLibelle() + "' (ordre " + actuel.getOrdre() + "), ne peut pas repasser a '" + statutCibleLibelle + "' (ordre " + statutCible.getOrdre() + ").");
        }
    }

    public boolean isStatutDejaAtteint(Demande dem, String statutLibelle) {
        return isStatusOuPlus(dem.getId(), statutLibelle);
    }
}
