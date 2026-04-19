package app.visa.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import app.visa.controller.response.ApiResponse;
import app.visa.dto.VisaRequestDto;
import app.visa.entity.Categorie;
import app.visa.entity.Demande;
import app.visa.entity.Demandeur;
import app.visa.entity.Nationalite;
import app.visa.entity.Passeport;
import app.visa.entity.SituationFamiliale;
import app.visa.entity.TypeDemande;
import app.visa.entity.VisaTransformable;
import app.visa.repository.CategorieRepository;
import app.visa.repository.NationaliteRepository;
import app.visa.repository.SituationFamilialeRepository;
import app.visa.repository.TypeDemandeRepository;
import app.visa.service.VisaRequestService;

@Controller
@RequestMapping("/visa-requests")
public class VisaRequestController {

    private final VisaRequestService visaRequestService;
    private final NationaliteRepository nationaliteRepository;
    private final SituationFamilialeRepository situationFamilialeRepository;
    private final TypeDemandeRepository typeDemandeRepository;
    private final CategorieRepository categorieRepository;

    public VisaRequestController(VisaRequestService visaRequestService,
                                NationaliteRepository nationaliteRepository,
                                SituationFamilialeRepository situationFamilialeRepository,
                                TypeDemandeRepository typeDemandeRepository,
                                CategorieRepository categorieRepository) {
        this.visaRequestService = visaRequestService;
        this.nationaliteRepository = nationaliteRepository;
        this.situationFamilialeRepository = situationFamilialeRepository;
        this.typeDemandeRepository = typeDemandeRepository;
        this.categorieRepository = categorieRepository;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("demandes", visaRequestService.findAll());
        return "visa-requests/list";
    }

    @GetMapping("/new")
    public String showCreateForm() {
        return "visa-requests/form";
    }

    @PostMapping
    @ResponseBody
    public ResponseEntity<ApiResponse<Object>> create(@RequestBody VisaRequestDto dto) {
        try {
            Demandeur demandeur = buildDemandeur(dto.getEtatCivil());
            Passeport passeport = buildPasseport(dto.getPasseport(), demandeur);
            VisaTransformable vt = buildVisaTransformable(dto.getVisaTransformable(), demandeur, passeport);
            Demande demande = buildDemande(dto, passeport, vt);

            // Tsy tonga dia ilay entite no nalefako en response fa tratrana olana recursion zah teo
            // Mahakamo be koa raha fenoina anotation le manala json
            // Dia aleo averina atao anaty variables hafa  
            Map<String, Object> debugData = buildDebugData(demandeur, passeport, vt, demande, dto.getDossiersFournis());

            return ResponseEntity.ok(new ApiResponse<>(true, debugData, null)); // Tonga dia hitan'ny client hafa ankotran navigateur nefa tsy nanamboatra CORS akory isika ? Eny e zarantsika aza
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, e.getMessage()));
        }
    }

    private Demandeur buildDemandeur(Map<String, Object> ec) {
        Demandeur demandeur = new Demandeur();
        demandeur.setNom((String) ec.get("nom"));
        demandeur.setPrenom((String) ec.get("prenom"));
        demandeur.setNomJeuneFille((String) ec.get("nomJeuneFille"));
        demandeur.setEmail((String) ec.get("email"));
        demandeur.setNumTel((String) ec.get("numTel"));
        demandeur.setAdresse((String) ec.get("adresse"));
        demandeur.setDateNaissance(LocalDate.parse((String) ec.get("dateNaissance")));

        Long nationaliteId = Long.valueOf(ec.get("nationalite").toString());
        demandeur.setNationalite(nationaliteRepository.findById(nationaliteId).orElse(null));

        Long situationId = Long.valueOf(ec.get("situationFamiliale").toString());
        demandeur.setSituationFamiliale(situationFamilialeRepository.findById(situationId).orElse(null));

        return demandeur;
    }

    private Passeport buildPasseport(Map<String, Object> passMap, Demandeur demandeur) {
        Passeport passeport = new Passeport();
        passeport.setNumero((String) passMap.get("numero"));
        passeport.setDateDelivrance(LocalDateTime.parse((String) passMap.get("dateDelivrance")));
        passeport.setDateExpiration(LocalDateTime.parse((String) passMap.get("dateExpiration")));
        passeport.setDemandeur(demandeur);
        return passeport;
    }

    private VisaTransformable buildVisaTransformable(Map<String, Object> vtMap, Demandeur demandeur, Passeport passeport) {
        VisaTransformable vt = new VisaTransformable();
        vt.setReference((String) vtMap.get("reference"));
        vt.setDateEntree(LocalDateTime.parse((String) vtMap.get("dateEntree")));
        vt.setLieuEntree((String) vtMap.get("lieuEntree"));
        vt.setDateExpiration(LocalDateTime.parse((String) vtMap.get("dateExpiration")));
        vt.setDemandeur(demandeur);
        vt.setPasseport(passeport);
        return vt;
    }

    private Demande buildDemande(VisaRequestDto dto, Passeport passeport, VisaTransformable vt) {
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

    private Map<String, Object> buildDebugData(Demandeur demandeur, Passeport passeport, VisaTransformable vt, Demande demande, List<Long> dossiersIds) {
        Map<String, Object> debugData = new HashMap<>();

        Map<String, Object> demandeurMap = new HashMap<>();
        demandeurMap.put("nom", demandeur.getNom());
        demandeurMap.put("prenom", demandeur.getPrenom());
        demandeurMap.put("nomJeuneFille", demandeur.getNomJeuneFille());
        demandeurMap.put("email", demandeur.getEmail());
        demandeurMap.put("numTel", demandeur.getNumTel());
        demandeurMap.put("adresse", demandeur.getAdresse());
        demandeurMap.put("dateNaissance", demandeur.getDateNaissance() != null ? demandeur.getDateNaissance().toString() : null);
        demandeurMap.put("nationalite", demandeur.getNationalite() != null ? demandeur.getNationalite().getLibelle() : null);
        demandeurMap.put("situationFamiliale", demandeur.getSituationFamiliale() != null ? demandeur.getSituationFamiliale().getLibelle() : null);

        Map<String, Object> passeportMap = new HashMap<>();
        passeportMap.put("numero", passeport.getNumero());
        passeportMap.put("dateDelivrance", passeport.getDateDelivrance() != null ? passeport.getDateDelivrance().toString() : null);
        passeportMap.put("dateExpiration", passeport.getDateExpiration() != null ? passeport.getDateExpiration().toString() : null);

        Map<String, Object> vtMapOut = new HashMap<>();
        vtMapOut.put("reference", vt.getReference());
        vtMapOut.put("dateEntree", vt.getDateEntree() != null ? vt.getDateEntree().toString() : null);
        vtMapOut.put("lieuEntree", vt.getLieuEntree());
        vtMapOut.put("dateExpiration", vt.getDateExpiration() != null ? vt.getDateExpiration().toString() : null);

        Map<String, Object> demandeMap = new HashMap<>();
        demandeMap.put("dateCreation", demande.getDateCreation() != null ? demande.getDateCreation().toString() : null);
        demandeMap.put("typeDemande", demande.getTypeDemande() != null ? demande.getTypeDemande().getLibelle() : null);
        demandeMap.put("categorie", demande.getCategorie() != null ? demande.getCategorie().getLibelle() : null);

        debugData.put("demandeur", demandeurMap);
        debugData.put("passeport", passeportMap);
        debugData.put("visaTransformable", vtMapOut);
        debugData.put("demande", demandeMap);
        debugData.put("dossiersIds", dossiersIds);

        return debugData;
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        Optional<Demande> demandeOptional = visaRequestService.findById(id);
        if (demandeOptional.isEmpty()) {
            return "redirect:/visa-requests";
        }

        model.addAttribute("demande", demandeOptional.get());
        return "visa-requests/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @ModelAttribute("demande") Demande demande) {
        demande.setId(id);
        if (demande.getDateCreation() == null) {
            demande.setDateCreation(LocalDateTime.now());
        }
        visaRequestService.save(demande);
        return "redirect:/visa-requests";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        visaRequestService.deleteById(id);
        return "redirect:/visa-requests";
    }
}
