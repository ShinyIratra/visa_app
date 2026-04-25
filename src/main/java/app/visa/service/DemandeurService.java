
package app.visa.service;

import app.visa.entity.Demandeur;
import app.visa.entity.Nationalite;
import app.visa.entity.SituationFamiliale;
import app.visa.repository.DemandeurRepository;
import app.visa.repository.NationaliteRepository;
import app.visa.repository.SituationFamilialeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DemandeurService {

    private final DemandeurRepository demandeurRepository;
    private final NationaliteRepository nationaliteRepository;
    private final SituationFamilialeRepository situationFamilialeRepository;

    public Demandeur buildDemandeur(Map<String, Object> ec) {
        Demandeur demandeur = new Demandeur();
        demandeur.setNom((String) ec.get("nom"));
        demandeur.setPrenom((String) ec.get("prenom"));
        demandeur.setNomJeuneFille((String) ec.get("nomJeuneFille"));
        demandeur.setEmail((String) ec.get("email"));
        demandeur.setNumTel((String) ec.get("numTel"));
        demandeur.setAdresse((String) ec.get("adresse"));
        demandeur.setDateNaissance(LocalDate.parse((String) ec.get("dateNaissance")));

        Integer nationaliteId = Integer.valueOf(ec.get("nationalite").toString());
        demandeur.setNationalite(nationaliteRepository.findById(nationaliteId).orElse(null));

        Integer situationId = Integer.valueOf(ec.get("situationFamiliale").toString());
        demandeur.setSituationFamiliale(situationFamilialeRepository.findById(situationId).orElse(null));

        return demandeur;
    }

    @Transactional(rollbackFor = Exception.class)
    public Demandeur createDemandeur(Demandeur demandeur) {
        validerDemandeur(demandeur);

        Integer nationaliteId = demandeur.getNationalite().getId();
        Integer situationFamilialeId = demandeur.getSituationFamiliale().getId();

        Nationalite nationalite = nationaliteRepository.findById(nationaliteId)
            .orElseThrow(() -> new IllegalArgumentException("Nationalite introuvable: " + nationaliteId));

        SituationFamiliale situationFamiliale = situationFamilialeRepository.findById(situationFamilialeId)
            .orElseThrow(() -> new IllegalArgumentException("Situation familiale introuvable: " + situationFamilialeId));

        demandeur.setNationalite(nationalite);
        demandeur.setSituationFamiliale(situationFamiliale);

        return demandeurRepository.save(demandeur);
    }

    @Transactional(rollbackFor = Exception.class)
    public Demandeur updateDemandeur(Integer id, Demandeur details) {
        Demandeur demandeur = demandeurRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("demandeur introuvable: " + id));

        demandeur.setNom(details.getNom());
        demandeur.setPrenom(details.getPrenom());
        demandeur.setNomJeuneFille(details.getNomJeuneFille());
        demandeur.setEmail(details.getEmail());
        demandeur.setNumTel(details.getNumTel());
        demandeur.setAdresse(details.getAdresse());
        demandeur.setDateNaissance(details.getDateNaissance());

        if (details.getNationalite() != null) {
            Nationalite nationalite = nationaliteRepository.findById(details.getNationalite().getId())
                .orElseThrow(() -> new IllegalArgumentException("Nationalite introuvable: " + details.getNationalite().getId()));
            demandeur.setNationalite(nationalite);
        }

        if (details.getSituationFamiliale() != null) {
            SituationFamiliale situationFamiliale = situationFamilialeRepository.findById(details.getSituationFamiliale().getId())
                .orElseThrow(() -> new IllegalArgumentException("Situation familiale introuvable: " + details.getSituationFamiliale().getId()));
            demandeur.setSituationFamiliale(situationFamiliale);
        }

        validerDemandeur(demandeur);
        return demandeurRepository.save(demandeur);
    }

    private void validerDemandeur(Demandeur demandeur) {
        if (demandeur == null) {
            throw new IllegalArgumentException("demandeur obligatoire.");
        }
        if (estVide(demandeur.getNom())) {
            throw new IllegalArgumentException("nom obligatoire.");
        }
        if (estVide(demandeur.getNumTel())) {
            throw new IllegalArgumentException("numero de telephone obligatoire.");
        }
        if (demandeur.getDateNaissance() == null) {
            throw new IllegalArgumentException("date de naissance obligatoire.");
        }
        if (estVide(demandeur.getAdresse())) {
            throw new IllegalArgumentException("adresse obligatoire.");
        }
        if (demandeur.getNationalite() == null || demandeur.getNationalite().getId() == null) {
            throw new IllegalArgumentException("nationalite obligatoire.");
        }
        if (demandeur.getSituationFamiliale() == null || demandeur.getSituationFamiliale().getId() == null) {
            throw new IllegalArgumentException("situation familiale obligatoire.");
        }
    }

    private boolean estVide(String valeur) {
        return valeur == null || valeur.isBlank();
    }
}
