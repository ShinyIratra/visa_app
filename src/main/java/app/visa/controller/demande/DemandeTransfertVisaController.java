package app.visa.controller.demande;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import app.visa.controller.response.ApiResponse;
import app.visa.dto.VisaRequestDto;
import app.visa.entity.Demande;
import app.visa.entity.Demandeur;
import app.visa.entity.Passeport;
import app.visa.entity.VisaTransformable;
import app.visa.service.DemandeService;
import app.visa.service.DemandeurService;
import app.visa.service.PasseportService;
import app.visa.service.VisaRequestService;
import app.visa.service.VisaTransformableService;
import app.visa.service.UtilService;

@Controller
@RequestMapping("/api/demande/transfert-visa")
public class DemandeTransfertVisaController {

    private final DemandeurService demandeurService;
    private final PasseportService passeportService;
    private final VisaTransformableService visaTransformableService;
    private final DemandeService demandeService;
    private final VisaRequestService visaRequestService;

    public DemandeTransfertVisaController(
        DemandeurService demandeurService,
        PasseportService passeportService,
        VisaTransformableService visaTransformableService,
        DemandeService demandeService,
        VisaRequestService visaRequestService
    ) {
        this.demandeurService = demandeurService;
        this.passeportService = passeportService;
        this.visaTransformableService = visaTransformableService;
        this.demandeService = demandeService;
        this.visaRequestService = visaRequestService;
    }

    @PostMapping("/sda")
	public ResponseEntity<ApiResponse<Map<String, Object>>> create(@RequestBody Map<String, Object> donnees) {
		try {
			Map<String, Object> data = visaRequestService.creerDemandeVisa(donnees, "Transfert de visa", "Visa accepte");
			return ResponseEntity.ok(new ApiResponse<>(true, data, null));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, e.getMessage()));
		} catch (Exception e) {
			return ResponseEntity.internalServerError()
				.body(new ApiResponse<>(false, null, "Erreur lors de la creation de la demande de visa."));
		}
	}
}
