package app.visa.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.visa.controller.response.ApiResponse;
import app.visa.service.VisaRequestService;

@RestController
@RequestMapping("/api/demandes-visa")
public class DemandeVisaController {

	private final VisaRequestService visaRequestService;

	public DemandeVisaController(VisaRequestService visaRequestService) {
		this.visaRequestService = visaRequestService;
	}

	@GetMapping
	public ResponseEntity<ApiResponse<List<Map<String, Object>>>> list() {
		try {
			List<Map<String, Object>> data = visaRequestService.listDemandesAvecInfos();
			return ResponseEntity.ok(new ApiResponse<>(true, data, null));
		} catch (Exception e) {
			return ResponseEntity.internalServerError()
				.body(new ApiResponse<>(false, null, "Erreur lors de la recuperation des demandes de visa."));
		}
	}

	@PostMapping
	public ResponseEntity<ApiResponse<Map<String, Object>>> create(@RequestBody Map<String, Object> donnees) {
		try {
			Map<String, Object> data = visaRequestService.creerDemandeVisa(donnees);
			return ResponseEntity.ok(new ApiResponse<>(true, data, null));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, e.getMessage()));
		} catch (Exception e) {
			return ResponseEntity.internalServerError()
				.body(new ApiResponse<>(false, null, "Erreur lors de la creation de la demande de visa."));
		}
	}
}
