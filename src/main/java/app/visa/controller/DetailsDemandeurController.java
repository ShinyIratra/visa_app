package app.visa.controller;

import app.visa.controller.response.ApiResponse;
import app.visa.service.DemandeurInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/demandeur")
@RequiredArgsConstructor
public class DetailsDemandeurController {

    private final DemandeurInfoService demandeurInfoService;

    @GetMapping("/infos/{numero}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getInfos(@PathVariable String numero) {
        try {
            Map<String, Object> infos = demandeurInfoService.getInfos(numero);
            return ResponseEntity.ok(new ApiResponse<>(true, infos, null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new ApiResponse<>(false, null, "Une erreur est survenue : " + e.getMessage()));
        }
    }
}
