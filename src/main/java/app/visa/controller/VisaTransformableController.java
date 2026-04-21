package app.visa.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.visa.controller.response.ApiResponse;
import app.visa.entity.VisaTransformable;
import app.visa.service.VisaTransformableService;

@RestController
@RequestMapping("/api/visa-transformables")
public class VisaTransformableController {

    private final VisaTransformableService visaTransformableService;

    public VisaTransformableController(VisaTransformableService visaTransformableService) {
        this.visaTransformableService = visaTransformableService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> createVisaTransformable(
        @RequestBody VisaTransformable visaTransformable
    ) {
        try {
            VisaTransformable visaTransformableCree = visaTransformableService.createVisaTransformable(visaTransformable);

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("id", visaTransformableCree.getId());
            data.put("reference", visaTransformableCree.getReference());
            data.put("dateEntree", visaTransformableCree.getDateEntree());
            data.put("lieuEntree", visaTransformableCree.getLieuEntree());
            data.put("dateExpiration", visaTransformableCree.getDateExpiration());
            data.put("passeportId", visaTransformableCree.getPasseport().getId());
            data.put("demandeurId", visaTransformableCree.getDemandeur().getId());

            return ResponseEntity.ok(new ApiResponse<>(true, data, null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(new ApiResponse<>(false, null, "Erreur lors de la creation du visa transformable."));
        }
    }
}