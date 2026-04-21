package app.visa.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.visa.controller.response.ApiResponse;
import app.visa.entity.Passeport;
import app.visa.service.PasseportService;

@RestController
@RequestMapping("/api/passeports")
public class PasseportController {

    private final PasseportService passeportService;

    public PasseportController(PasseportService passeportService) {
        this.passeportService = passeportService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> createPasseport(@RequestBody Passeport passeport) {
        try {
            Passeport passeportCree = passeportService.createPasseport(passeport);

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("id", passeportCree.getId());
            data.put("numero", passeportCree.getNumero());
            data.put("dateDelivrance", passeportCree.getDateDelivrance());
            data.put("dateExpiration", passeportCree.getDateExpiration());
            data.put("demandeurId", passeportCree.getDemandeur().getId());

            return ResponseEntity.ok(new ApiResponse<>(true, data, null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(new ApiResponse<>(false, null, "Erreur lors de la creation du passeport."));
        }
    }
}