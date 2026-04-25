package app.visa.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import app.visa.entity.Categorie;
import app.visa.entity.Demande;
import app.visa.entity.Demandeur;
import app.visa.entity.Dossier;
import app.visa.entity.HistoriqueStatut;
import app.visa.entity.Nationalite;
import app.visa.entity.Passeport;
import app.visa.entity.ReponseStatutVisa;
import app.visa.entity.SituationFamiliale;
import app.visa.entity.Statut;
import app.visa.entity.TypeDemande;
import app.visa.entity.VisaTransformable;
import app.visa.entity.LiaisonSansDonneeAnterieur;
import app.visa.repository.LiaisonSansDonneeAnterieurRepository;
import app.visa.repository.CategorieRepository;
import app.visa.repository.DossierRepository;
import app.visa.repository.HistoriqueStatutRepository;
import app.visa.repository.ReponseStatutVisaRepository;
import app.visa.repository.StatutRepository;
import app.visa.repository.TypeDemandeRepository;
import app.visa.repository.VisaRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LiaisonSansDonneeAnterieurService {

    private final LiaisonSansDonneeAnterieurRepository liaisonSansDonneeAnterieurRepository;

    public void saveLiaisonSansDonneeAnterieur(Integer identifiant, Demande demande) {
        LiaisonSansDonneeAnterieur liaison = new LiaisonSansDonneeAnterieur();
        liaison.setIdentifiant(identifiant);
        liaison.setDemande(demande);
        liaisonSansDonneeAnterieurRepository.save(liaison);
    }
}
