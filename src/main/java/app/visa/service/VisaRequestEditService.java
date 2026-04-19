package app.visa.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.visa.entity.Demande;
import app.visa.entity.Demandeur;
import app.visa.entity.Passeport;
import app.visa.entity.ReponseStatutVisa;
import app.visa.entity.VisaTransformable;
import app.visa.repository.ReponseStatutVisaRepository;
import app.visa.repository.VisaRequestRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VisaRequestEditService {

    private final VisaRequestRepository visaRequestRepository;
    private final ReponseStatutVisaRepository reponseStatutVisaRepository;

    @Transactional(readOnly = true)
    public Map<String, Object> getDemandeFormData(Long id) {
        Demande demande = visaRequestRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("demande introuvable: " + id));

        Passeport passeport = demande.getPasseport();
        Demandeur demandeur = (passeport != null) ? passeport.getDemandeur() : null;
        VisaTransformable vt = demande.getVisaTransformable();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("demandeId", demande.getId());
        result.put("typeDemandeId", (demande.getTypeDemande() != null) ? demande.getTypeDemande().getId() : null);

        result.put("etat civil", mapEtatCivil(demandeur));
        result.put("passeport", mapPasseport(passeport));
        result.put("visaTransformable", mapVisaTransformable(vt));
        result.put("dossiersFournis", getDossiersFournisIds(id));

        return result;
    }

    private Map<String, Object> mapEtatCivil(Demandeur demandeur) {
        Map<String, Object> etatCivil = new LinkedHashMap<>();
        if (demandeur != null) {
            etatCivil.put("nom", demandeur.getNom());
            etatCivil.put("prenom", demandeur.getPrenom());
            etatCivil.put("nomJeuneFille", demandeur.getNomJeuneFille());
            etatCivil.put("situationFamiliale", (demandeur.getSituationFamiliale() != null) ? demandeur.getSituationFamiliale().getId() : null);
            etatCivil.put("nationalite", (demandeur.getNationalite() != null) ? demandeur.getNationalite().getId() : null);
            etatCivil.put("dateNaissance", demandeur.getDateNaissance());
            etatCivil.put("adresse", demandeur.getAdresse());
            etatCivil.put("email", demandeur.getEmail());
            etatCivil.put("numTel", demandeur.getNumTel());
        }
        return etatCivil;
    }

    private Map<String, Object> mapPasseport(Passeport passeport) {
        Map<String, Object> pass = new LinkedHashMap<>();
        if (passeport != null) {
            pass.put("numero", passeport.getNumero());
            pass.put("dateDelivrance", passeport.getDateDelivrance());
            pass.put("dateExpiration", passeport.getDateExpiration());
        }
        return pass;
    }

    private Map<String, Object> mapVisaTransformable(VisaTransformable vt) {
        Map<String, Object> visa = new LinkedHashMap<>();
        if (vt != null) {
            visa.put("reference", vt.getReference());
            visa.put("dateEntree", vt.getDateEntree());
            visa.put("lieuEntree", vt.getLieuEntree());
            visa.put("dateExpiration", vt.getDateExpiration());
        }
        return visa;
    }

    private List<Long> getDossiersFournisIds(Long demandeId) {
        return reponseStatutVisaRepository.findByDemandeId(demandeId).stream()
            .filter(ReponseStatutVisa::getValeur)
            .map(r -> r.getDossier().getId())
            .toList();
    }
}
