package app.visa.service;

import app.visa.entity.Demandeur;
import app.visa.entity.Passeport;
import app.visa.entity.VisaTransformable;
import app.visa.repository.DemandeurRepository;
import app.visa.repository.PasseportRepository;
import app.visa.repository.VisaTransformableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VisaTransformableService {

    private final VisaTransformableRepository visaTransformableRepository;
    private final PasseportRepository passeportRepository;
    private final DemandeurRepository demandeurRepository;

    @Transactional
    public VisaTransformable createVisaTransformable(VisaTransformable visaTransformable) {
        validerVisaTransformable(visaTransformable);

        Long passeportId = visaTransformable.getPasseport().getId();
        Long demandeurId = visaTransformable.getDemandeur().getId();

        Passeport passeport = passeportRepository.findById(passeportId)
            .orElseThrow(() -> new IllegalArgumentException("passeport introuvable: " + passeportId));

        Demandeur demandeur = demandeurRepository.findById(demandeurId)
            .orElseThrow(() -> new IllegalArgumentException("demandeur introuvable: " + demandeurId));

        if (!passeport.getDemandeur().getId().equals(demandeur.getId())) {
            throw new IllegalArgumentException("le passeport ne correspond pas au demandeur.");
        }

        visaTransformable.setReference(visaTransformable.getReference().trim());
        visaTransformable.setLieuEntree(visaTransformable.getLieuEntree().trim());
        visaTransformable.setPasseport(passeport);
        visaTransformable.setDemandeur(demandeur);

        return visaTransformableRepository.save(visaTransformable);
    }

    private void validerVisaTransformable(VisaTransformable visaTransformable) {
        if (visaTransformable == null) {
            throw new IllegalArgumentException("visa transformable obligatoire.");
        }
        if (estVide(visaTransformable.getReference())) {
            throw new IllegalArgumentException("reference obligatoire.");
        }
        if (visaTransformable.getDateEntree() == null) {
            throw new IllegalArgumentException("date d'entree obligatoire.");
        }
        if (estVide(visaTransformable.getLieuEntree())) {
            throw new IllegalArgumentException("lieu d'entree obligatoire.");
        }
        if (visaTransformable.getDateExpiration() == null) {
            throw new IllegalArgumentException("date d'expiration obligatoire.");
        }
        if (visaTransformable.getDateExpiration().isBefore(visaTransformable.getDateEntree())) {
            throw new IllegalArgumentException("la date d'expiration doit etre apres la date d'entree.");
        }
        if (visaTransformable.getPasseport() == null || visaTransformable.getPasseport().getId() == null) {
            throw new IllegalArgumentException("passeport obligatoire.");
        }
        if (visaTransformable.getDemandeur() == null || visaTransformable.getDemandeur().getId() == null) {
            throw new IllegalArgumentException("demandeur obligatoire.");
        }
    }

    private boolean estVide(String valeur) {
        return valeur == null || valeur.isBlank();
    }
}