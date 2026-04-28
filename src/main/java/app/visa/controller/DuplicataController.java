package app.visa.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.*;

import app.visa.service.*;
import app.visa.entity.*;
import app.visa.controller.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import java.util.Map;
import java.util.List;

@Controller
@RequestMapping("/duplicata")
public class DuplicataController {

    private final DuplicataService duplicataService;

    public DuplicataController(DuplicataService duplicataService) {
        this.duplicataService = duplicataService;
    }

    @GetMapping
    public String list() {
        return "duplicata/list.html";
    }

    @GetMapping("/new/ada")
    public String newFormAvecDonneeAnterieure() {
        return "duplicata/new_ada.html";
    }

    @PostMapping("/new/ada")
    @ResponseBody
    public ResponseEntity<ApiResponse<Object>> createNewFormAvecDonneeAnterieure(@RequestBody Map<String, Object> donnees) {
        try {
            Demande demande = duplicataService.creerDemandeDuplicataAvecDonneeAnterieure(donnees);
            
			return ResponseEntity.ok(new ApiResponse<Object>(true, (Object) demande, null));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(new ApiResponse<Object>(false, null, (Object) e.getMessage()));
		} catch (Exception e) {
			return ResponseEntity.internalServerError()
				.body(new ApiResponse<Object>(false, null, (Object) ("Erreur Duplicata : " + e.getMessage())));
		}
    }

    @GetMapping("/new/sda")
    public String newFormSansDonneeAnterieure() {
        return "duplicata/new_sda.html";
    }

    @PostMapping("/new/sda")
    @ResponseBody
    public ResponseEntity<ApiResponse<Object>> createNewFormSansDonneeAnterieure(@RequestBody Map<String, Object> donnees) {
        try {
            Demande demande = duplicataService.creerDemandeDuplicataSansDonneeAnterieure(donnees);
			return ResponseEntity.ok(new ApiResponse<Object>(true, (Object) demande, null));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, e.getMessage()));
		} catch (Exception e) {
			return ResponseEntity.internalServerError()
				.body(new ApiResponse<>(false, null, "Erreur Duplicata : " + e.getMessage()));
		}
    }

    
    @GetMapping("/list")
    @ResponseBody
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listData() {
        try {
            List<Map<String, Object>> data = duplicataService.listDuplicataAvecInfos();
            return ResponseEntity.ok(new ApiResponse<List<Map<String, Object>>>(true, data, null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(new ApiResponse<>(false, null, "Erreur lors de la recuperation des demandes de duplicata"));
        }
    }

    @PostMapping("/{id}/accepter")
    @ResponseBody
    public ResponseEntity<ApiResponse<Void>> accepter(@PathVariable Integer id) {
        try {
            duplicataService.accepterDuplicata(id);
            return ResponseEntity.ok(new ApiResponse<>(true, null, null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(new ApiResponse<>(false, null, "Erreur lors de l'acceptation: " + e.getMessage()));
        }
    }
}
