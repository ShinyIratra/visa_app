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
@RequestMapping("/visa-requests/photo-signature")
@RequiredArgsConstructor
public class PhotoController {

    private final ScanService scanService;
    private final VisaRequestService visaRequestService;
    private final DemandeService demandeService;
    private final app.visa.service.PhotoService photoService;

    @GetMapping("/{id}")
    public String redirectPhotoProcess(@PathVariable Integer id, Model model) {
        try {
            Demande demande = visaRequestService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Demande introuvable: " + id));

            if (demandeService.isStatusOuPlus(demande.getId(), UtilService.STATUS_PHOTO_SCANNEE)) {
                return "redirect:/visa-requests?error=photo_termine";
            }

            return "visa-requests/photo-signature";
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'accès à la page de scan: " + e.getMessage(), e);
        }
    }

    @PostMapping(value = "/api/{id}/terminer", consumes = "multipart/form-data")
    @ResponseBody
    public ResponseEntity<ApiResponse<Map<String, Object>>> terminerScan(
            @PathVariable Integer id,
            @RequestParam("photo") MultipartFile photo,
            @RequestParam("signature") MultipartFile signature) {
        try {
            photoService.sauvegarderPhotoEtSignature(id, photo, signature);

            // Marquer comme photo et signature terminées
            photoService.marquerPhotoTerminee(id);

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("demandeId", id);
            response.put("statut", "Photo terminée");
            response.put("message", "Photo marqué comme terminé et fichiers enregistrés avec succès");

            return ResponseEntity.ok(new ApiResponse<>(true, response, null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse<>(false, null, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(new ApiResponse<>(false, null, "Erreur lors de la terminaison du scan: " + e.getMessage()));
        }
    }

    @GetMapping("/dossiers/{id}")
    public String viewDossiers(@PathVariable Integer id, Model model) {
        try {
            Demande demande = visaRequestService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Demande introuvable: " + id));
            
            List<Map<String, Object>> dossiers = scanService.getFichiersScannes(id);
            
            model.addAttribute("demande", demande);
            model.addAttribute("dossiers", dossiers);
            return "visa-requests/view-dossiers";
        } catch (Exception e) {
            return "redirect:/visa-requests?error=dossiers_read_error";
        }
    }
}
