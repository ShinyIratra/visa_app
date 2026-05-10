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
import org.springframework.web.bind.annotation.RequestParam;
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
    private final ScanService scanService;
    private final PhotoService photoService;

    public VisaRequestController(VisaRequestService visaRequestService,
                                DemandeurService demandeurService,
                                DemandeService demandeService,
                                PasseportService passeportService,
                                VisaTransformableService visaTransformableService,
                                NationaliteRepository nationaliteRepository,
                                SituationFamilialeRepository situationFamilialeRepository,
                                TypeDemandeRepository typeDemandeRepository,
                                CategorieRepository categorieRepository,
                                ScanService scanService,
                                PhotoService photoService) {
        this.visaRequestService = visaRequestService;
        this.demandeurService = demandeurService;
        this.demandeService = demandeService;
        this.passeportService = passeportService;
        this.visaTransformableService = visaTransformableService;
        this.nationaliteRepository = nationaliteRepository;
        this.situationFamilialeRepository = situationFamilialeRepository;
        this.typeDemandeRepository = typeDemandeRepository;
        this.categorieRepository = categorieRepository;
        this.scanService = scanService;
        this.photoService = photoService;
    }

    @GetMapping
    public String list(Model model, 
                      @RequestParam(required = false) String error,
                      @RequestParam(required = false) String start,
                      @RequestParam(required = false) String end) {
        if ("scan_termine".equals(error)) {
            model.addAttribute("errorMessage", "Action interdite. La demande est deja au statut Scan termine.");
        }
        else if ("photo_termine".equals(error)) {
            model.addAttribute("errorMessage", "Action interdite. La demande est deja au statut Photo terminee.");
        }
        
        List<Map<String, Object>> demandes = visaRequestService.listDemandesAvecInfos();
        
        if (start != null && !start.isEmpty()) {
            java.time.LocalDateTime startDate = java.time.LocalDateTime.parse(start);
            demandes = demandes.stream()
                .filter(d -> d.get("dateCreation") != null && !((java.time.LocalDateTime)d.get("dateCreation")).isBefore(startDate))
                .collect(java.util.stream.Collectors.toList());
        }
        if (end != null && !end.isEmpty()) {
            java.time.LocalDateTime endDate = java.time.LocalDateTime.parse(end);
            demandes = demandes.stream()
                .filter(d -> d.get("dateCreation") != null && !((java.time.LocalDateTime)d.get("dateCreation")).isAfter(endDate))
                .collect(java.util.stream.Collectors.toList());
        }

        model.addAttribute("demandes", demandes);
        model.addAttribute("start", start);
        model.addAttribute("end", end);
        return "visa-requests/list";
    }

    @GetMapping("/new")
    public String showCreateForm() {
        return "visa-requests/form";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Integer id, Model model) {
        Demande demande = visaRequestService.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("demande introuvable: " + id));

        if (demandeService.isStatusOuPlus(demande.getId(), UtilService.STATUS_SCAN_TERMINE)) {
            return "redirect:/visa-requests?error=scan_termine";
        }

        model.addAttribute("demandeId", id);
        return "visa-requests/form-edit";
    }

    @GetMapping("/dossiers/{id}")
    public String viewDossiers(@PathVariable Integer id, Model model) {
        try {
            Demande demande = visaRequestService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Demande introuvable: " + id));
            
            List<Map<String, Object>> dossiers = scanService.getFichiersScannes(id);
            Map<String, Object> photoEtSignature = photoService.getPhotoEtSignatureUrls(id);
            
            model.addAttribute("demande", demande);
            model.addAttribute("dossiers", dossiers);
            model.addAttribute("photoUrl", photoEtSignature.get("photoUrl"));
            model.addAttribute("signatureUrl", photoEtSignature.get("signatureUrl"));
            return "visa-requests/view-dossiers";
        } catch (Exception e) {
            return "redirect:/visa-requests?error=dossiers_read_error";
        }
    }
}
