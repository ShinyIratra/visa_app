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
import app.visa.repository.CategorieRepository;
import app.visa.repository.NationaliteRepository;
import app.visa.repository.TypeDemandeRepository;
import app.visa.repository.SituationFamilialeRepository;

import java.util.List;

@Service
public class StaticDataService {

    private final NationaliteRepository nationaliteRepository;
    private final SituationFamilialeRepository situationFamilialeRepository;
    private final CategorieRepository categorieRepository;
    private final TypeDemandeRepository typeDemandeRepository;

    public StaticDataService(NationaliteRepository nationaliteRepository,
                             SituationFamilialeRepository situationFamilialeRepository,
                             CategorieRepository categorieRepository,
                             TypeDemandeRepository typeDemandeRepository
    ) {
        this.nationaliteRepository = nationaliteRepository;
        this.situationFamilialeRepository = situationFamilialeRepository;
        this.categorieRepository = categorieRepository;
        this.typeDemandeRepository = typeDemandeRepository;
    }

    public Map<String, Object> getAllStaticData() {
        Map<String, Object> m = new HashMap<>();
        List<Nationalite> nationalites = nationaliteRepository.findAll();
        List<SituationFamiliale> situations = situationFamilialeRepository.findAll();
        List<Categorie> categories = categorieRepository.findAll();
        List<TypeDemande> types = typeDemandeRepository.findAll();

        m.put("nationalites", nationalites);
        m.put("situationsFamiliales", situations);
        m.put("categories", categories);
        m.put("typesDemande", types);

        return m;
    }
}
