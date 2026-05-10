package app.visa.controller;

import java.util.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import app.visa.controller.response.ApiResponse;
import app.visa.entity.Demande;
import app.visa.service.UtilService;
import app.visa.service.ScanService;
import app.visa.service.VisaRequestService;
import app.visa.service.DemandeService;
import lombok.RequiredArgsConstructor;


@Controller
@RequestMapping("/visa-requests/scan")
@RequiredArgsConstructor
public class ScanController {

    private final ScanService scanService;
    private final VisaRequestService visaRequestService;
    private final DemandeService demandeService;

    @GetMapping("/{id}")
    public String showScanPage(@PathVariable Integer id, Model model) {
        try {
            Demande demande = visaRequestService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Demande introuvable: " + id));

            // Vérifier contrôle 1: tous dossiers cochés
            boolean allDossiersCoches = scanService.controleAllDossiersCoches(id);
            if (!allDossiersCoches) {
                model.addAttribute("error", "Tous les dossiers doivent être cochés. Veuillez mettre à jour la demande.");
                model.addAttribute("demandeId", id);
                return "visa-requests/scan-error";
            }

            if (demandeService.isStatusOuPlus(demande.getId(), UtilService.STATUS_SCAN_TERMINE)) {
                return "redirect:/visa-requests?error=scan_termine";
            }

            // Charger les dossiers pour affichage
            List<Map<String, Object>> dossiers = scanService.getDossiersAvecStatut(id);
            
            model.addAttribute("demandeId", id);
            model.addAttribute("demande", demande);
            model.addAttribute("dossiers", dossiers);

            return "visa-requests/scan";
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'accès à la page de scan: " + e.getMessage(), e);
        }
    }

    @PostMapping("/api/{id}/validate")
    @ResponseBody
    public ResponseEntity<ApiResponse<Map<String, Object>>> validateScan(
            @PathVariable Integer id,
            @RequestBody(required = false) Map<String, Object> formData) {
        try {
            // Contrôle 1: tous dossiers cochés
            boolean allDossiersCoches = scanService.controleAllDossiersCoches(id);

            // Contrôle 2: tous champs obligatoires remplis (si formData fourni)
            boolean allChampsOk = true;
            if (formData != null && !formData.isEmpty()) {
                allChampsOk = scanService.controleChampsobligatoires(formData);
            }

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("dossiersOk", allDossiersCoches);
            response.put("champsOk", allChampsOk);
            response.put("canScan", allDossiersCoches && allChampsOk);

            if (!allDossiersCoches) {
                response.put("dossiersMessage", "Tous les dossiers doivent être cochés");
            }
            if (!allChampsOk) {
                response.put("champsMessage", "Tous les champs obligatoires doivent être remplis");
            }

            return ResponseEntity.ok(new ApiResponse<>(true, response, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse<>(false, null, "Erreur validation: " + e.getMessage()));
        }
    }

    @PostMapping(value = "/api/{id}/terminer", consumes = "multipart/form-data")
    @ResponseBody
    public ResponseEntity<ApiResponse<Map<String, Object>>> terminerScan(
            @PathVariable Integer id,
            @RequestParam Map<String, MultipartFile> files) {
        try {
            // Vérification finale des contrôles avant de marquer comme terminé
            boolean allDossiersCoches = scanService.controleAllDossiersCoches(id);
            if (!allDossiersCoches) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, null, "Tous les dossiers doivent être cochés pour terminer le scan"));
            }

            scanService.sauvegarderFichiersScan(id, files);

            // Marquer comme scan terminé
            scanService.marquerScanTermine(id);

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("demandeId", id);
            response.put("statut", "Scan terminé");
            response.put("message", "Scan marqué comme terminé et fichiers enregistrés avec succès");

            return ResponseEntity.ok(new ApiResponse<>(true, response, null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse<>(false, null, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(new ApiResponse<>(false, null, "Erreur lors de la terminaison du scan: " + e.getMessage()));
        }
    }
}
