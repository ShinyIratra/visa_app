package app.visa.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.visa.controller.response.ApiResponse;
import app.visa.entity.Demandeur;
import app.visa.service.DemandeurService;

@RestController
@RequestMapping("/api/demandeurs")
public class DemandeurController {

    private final DemandeurService demandeurService;

    public DemandeurController(DemandeurService demandeurService) {
        this.demandeurService = demandeurService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Demandeur>> createDemandeur(@RequestBody Demandeur demandeur) {
        try {
            Demandeur demandeurCree = demandeurService.createDemandeur(demandeur);
            return ResponseEntity.ok(new ApiResponse<>(true, demandeurCree, null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(new ApiResponse<>(false, null, "Erreur lors de la creation du demandeur."));
        }
    }
}
