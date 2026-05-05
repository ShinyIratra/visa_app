package app.visa.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import app.visa.controller.response.ApiResponse;
import app.visa.service.*;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/demandes-visa")
@RequiredArgsConstructor
public class DemandeVisaController {

	private final VisaRequestService visaRequestService;
	private final VisaRequestEditService visaRequestEditService;
	private final CodeQrService codeQrService;

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

	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<Map<String, Object>>> get(@PathVariable Integer id) {
		try {
			Map<String, Object> data = visaRequestEditService.getDemandeFormData(id);
			return ResponseEntity.ok(new ApiResponse<>(true, data, null));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, e.getMessage()));
		} catch (Exception e) {
			return ResponseEntity.internalServerError()
				.body(new ApiResponse<>(false, null, "Erreur lors de la recuperation de la demande (id=" + id + "): " + e.getMessage()));
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

	@PutMapping("/{id}")
	public ResponseEntity<ApiResponse<Map<String, Object>>> update(@PathVariable Integer id, @RequestBody Map<String, Object> donnees) {
		try {
			Map<String, Object> data = visaRequestEditService.updateDemandeVisa(id, donnees);
			return ResponseEntity.ok(new ApiResponse<>(true, data, null));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, e.getMessage()));
		} catch (Exception e) {
			return ResponseEntity.internalServerError()
				.body(new ApiResponse<>(false, null, "Erreur lors de la mise a jour de la demande (id=" + id + "): " + e.getMessage()));
		}
	}
	
    @GetMapping(value = "/qr/{numero}", produces = "image/png")
    public byte[] showQr(@PathVariable String numero) {
        return codeQrService.genererCodeQrDemandeBytes(numero);
    }
}
