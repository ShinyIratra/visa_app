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

import app.visa.service.*;

@Controller
@RequestMapping("/visa-requests")
public class VisaRequestController {

    private final VisaRequestService visaRequestService;
    private final DemandeurService demandeurService;
    private final DemandeService demandeService;
    private final PasseportService passeportService;
    private final VisaTransformableService visaTransformableService;

    private final NationaliteRepository nationaliteRepository;
    private final SituationFamilialeRepository situationFamilialeRepository;
    private final TypeDemandeRepository typeDemandeRepository;
    private final CategorieRepository categorieRepository;

    public VisaRequestController(VisaRequestService visaRequestService,
                                DemandeurService demandeurService,
                                DemandeService demandeService,
                                PasseportService passeportService,
                                VisaTransformableService visaTransformableService,
                                NationaliteRepository nationaliteRepository,
                                SituationFamilialeRepository situationFamilialeRepository,
                                TypeDemandeRepository typeDemandeRepository,
                                CategorieRepository categorieRepository) {
        this.visaRequestService = visaRequestService;
        this.demandeurService = demandeurService;
        this.demandeService = demandeService;
        this.passeportService = passeportService;
        this.visaTransformableService = visaTransformableService;
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

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Integer id, Model model) {
        model.addAttribute("demandeId", id);
        return "visa-requests/form-edit";
    }

    @PostMapping
    @ResponseBody
    public ResponseEntity<ApiResponse<Object>> create(@RequestBody VisaRequestDto dto) {
        try {
            Demandeur demandeur = demandeurService.buildDemandeur(dto.getEtatCivil());
            Passeport passeport = passeportService.buildPasseport(dto.getPasseport(), demandeur);
            VisaTransformable vt = visaTransformableService.buildVisaTransformable(dto.getVisaTransformable(), demandeur, passeport);
            Demande demande = demandeService.buildDemande(dto, passeport, vt);

            // Tsy tonga dia ilay entite no nalefako en response fa tratrana olana recursion zah teo
            // Mahakamo be koa raha fenoina anotation le manala json
            // Dia aleo averina atao anaty variables hafa  
            Map<String, Object> debugData = UtilService.buildDebugData(demandeur, passeport, vt, demande, dto.getDossiersFournis());

            return ResponseEntity.ok(new ApiResponse<>(true, debugData, null)); // Tonga dia hitan'ny client hafa ankotran navigateur nefa tsy nanamboatra CORS akory isika ? Eny e zarantsika aza
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, e.getMessage()));
        }
    } 

    @PostMapping("/{id}")
    public String update(@PathVariable Integer id, @ModelAttribute("demande") Demande demande) {
        demande.setId(id);
        if (demande.getDateCreation() == null) {
            demande.setDateCreation(LocalDateTime.now());
        }
        visaRequestService.save(demande);
        return "redirect:/visa-requests";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Integer id) {
        visaRequestService.deleteById(id);
        return "redirect:/visa-requests";
    }
}
