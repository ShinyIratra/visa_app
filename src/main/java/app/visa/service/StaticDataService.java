package app.visa.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import app.visa.entity.Categorie;
import app.visa.entity.Nationalite;
import app.visa.entity.Passeport;
import app.visa.entity.TypeDemande;
import app.visa.entity.VisaTransformable;
import app.visa.entity.SituationFamiliale;
import app.visa.entity.Dossier;
import app.visa.repository.CategorieRepository;
import app.visa.repository.NationaliteRepository;
import app.visa.repository.TypeDemandeRepository;
import app.visa.repository.SituationFamilialeRepository;
import app.visa.repository.DossierRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StaticDataService {

    private final NationaliteRepository nationaliteRepository;
    private final SituationFamilialeRepository situationFamilialeRepository;
    private final CategorieRepository categorieRepository;
    private final TypeDemandeRepository typeDemandeRepository;
    private final DossierRepository dossierRepository;

    public StaticDataService(NationaliteRepository nationaliteRepository,
                             SituationFamilialeRepository situationFamilialeRepository,
                             CategorieRepository categorieRepository,
                             TypeDemandeRepository typeDemandeRepository,
                             DossierRepository dossierRepository
    ) {
        this.nationaliteRepository = nationaliteRepository;
        this.situationFamilialeRepository = situationFamilialeRepository;
        this.categorieRepository = categorieRepository;
        this.typeDemandeRepository = typeDemandeRepository;
        this.dossierRepository = dossierRepository;
    }

    public Map<String, Object> getAllStaticData() {
        Map<String, Object> m = new HashMap<>();
        List<Nationalite> nationalites = nationaliteRepository.findAll();
        List<SituationFamiliale> situations = situationFamilialeRepository.findAll();
        List<Categorie> categories = categorieRepository.findAll();
        List<TypeDemande> types = typeDemandeRepository.findAll();
        List<Dossier> dossiers = dossierRepository.findAll();

        m.put("nationalites", nationalites);
        m.put("situationsFamiliales", situations);
        m.put("categories", categories);
        
        // Sans communs
        m.put("typesDemande", types.stream()
            .filter(t -> !t.getLibelle().equalsIgnoreCase("commun"))
            .collect(Collectors.toList()));

        // Dossiers by type
        m.put("dossiersCommuns", dossiers.stream()
            .filter(d -> d.getTypeDemande().getLibelle().equalsIgnoreCase("commun"))
            .collect(Collectors.toList()));
        
        m.put("dossiersTravailleur", dossiers.stream()
            .filter(d -> d.getTypeDemande().getLibelle().toLowerCase().contains("travailleur"))
            .collect(Collectors.toList()));
            
        m.put("dossiersInvestisseur", dossiers.stream()
            .filter(d -> d.getTypeDemande().getLibelle().toLowerCase().contains("investisseur"))
            .collect(Collectors.toList()));
        
        return m;
    }
}
