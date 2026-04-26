package app.visa.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.PostMapping;

import app.visa.dto.VisaRequestDto;
import app.visa.service.*;
import app.visa.entity.*;
import app.visa.controller.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import java.util.Map;

@Controller
@RequestMapping("/duplicata")
public class DuplicataController {

    private final VisaRequestService visaRequestService;
    private final DuplicataService duplicataService;
    private final DemandeurService demandeurService;
    private final DemandeService demandeService;
    private final PasseportService passeportService;
    private final VisaTransformableService visaTransformableService;

    public DuplicataController(VisaRequestService visaRequestService,
                                DuplicataService duplicataService,
                                DemandeurService demandeurService,
                                DemandeService demandeService,
                                PasseportService passeportService,
                                VisaTransformableService visaTransformableService) {
        this.visaRequestService = visaRequestService;
        this.duplicataService = duplicataService;
        this.demandeurService = demandeurService;
        this.demandeService = demandeService;
        this.passeportService = passeportService;
        this.visaTransformableService = visaTransformableService;
    }

    @GetMapping
    public String list() {
        return "duplicata/list.html";
    }

    @GetMapping("/new/ada")
    public String newFormAvecDonneeAnterieure() {
        return "duplicata/new_ada.html";
    }

    @PostMapping("/new/ada")
    @ResponseBody
    public ResponseEntity<ApiResponse<Object>> createNewFormAvecDonneeAnterieure(@RequestBody Map<String, Object> donnees) {
        try {
            Map<String, Object> data = duplicataService.creerDemandeDuplicataAvecDonneeAnterieure(donnees);
            
			return ResponseEntity.ok(new ApiResponse<>(true, data, null));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, e.getMessage()));
		} catch (Exception e) {
			return ResponseEntity.internalServerError()
				.body(new ApiResponse<>(false, null, "Erreur Duplicata : " + e.getMessage()));
		}
    }

    @GetMapping("/new/sda")
    public String newFormSansDonneeAnterieure() {
        return "duplicata/new_sda.html";
    }

    @PostMapping("/new/sda")
    @ResponseBody
    public ResponseEntity<ApiResponse<Object>> createNewFormSansDonneeAnterieure(@RequestBody Map<String, Object> donnees) {
        try {
            Map<String, Object> data = duplicataService.creerDemandeDuplicataSansDonneeAnterieure(donnees);
            Demande demande_original = (Demande) demandeService.getById((Integer) data.get("demandeId"));

            Map<String, Object> response = duplicataService.creerDuplicata(demande_original);
			return ResponseEntity.ok(new ApiResponse<>(true, response, null));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, e.getMessage()));
		} catch (Exception e) {
			return ResponseEntity.internalServerError()
				.body(new ApiResponse<>(false, null, "Erreur Duplicata : " + e.getMessage()));
		}
    }
}
