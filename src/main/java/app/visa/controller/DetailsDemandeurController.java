package app.visa.controller;

import app.visa.controller.response.ApiResponse;
import app.visa.service.DemandeurInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/demandeurs")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class DetailsDemandeurController {

    private final DemandeurInfoService demandeurInfoService;

    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllDemandeurs() {
        try {
            List<Map<String, Object>> list = demandeurInfoService.getAllDemandeurs();
            return ResponseEntity.ok(new ApiResponse<>(true, list, null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new ApiResponse<>(false, null, e.getMessage()));
        }
    }

    // http://localhost:8080/api/demandeurs/DEM-00001/historique?dateDebut=2026-01-01T00:00:00&dateFin=2026-12-31T23:59:59
    @GetMapping("/{numero}/historique")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getInfos(
            @PathVariable String numero,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateDebut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFin) {
        try {
            Map<String, Object> infos = demandeurInfoService.getInfos(numero, dateDebut, dateFin);
            return ResponseEntity.ok(new ApiResponse<>(true, infos, null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new ApiResponse<>(false, null, e.getMessage()));
        }
    }
}
