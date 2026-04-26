package app.visa.controller;

import app.visa.controller.response.ApiResponse;
import app.visa.entity.DemandeTransfertVisa;
import app.visa.service.TransfertVisaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/transfert-visa")
@RequiredArgsConstructor
public class TransfertVisaController {

    private final TransfertVisaService transfertVisaService;

    @GetMapping
    public String list() {
        return "transfert-visa/list.html";
    }

    @GetMapping("/list")
    @ResponseBody
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listData() {
        try {
            List<Map<String, Object>> data = transfertVisaService.listTransfertsAvecInfos();
            return ResponseEntity.ok(new ApiResponse<>(true, data, null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(new ApiResponse<>(false, null, "Erreur lors de la recuperation des transferts de visa."));
        }
    }

    @GetMapping("/new/ada")
    public String newFormAvecDonneeAnterieure() {
        return "transfert-visa/new_ada.html";
    }

    @GetMapping("/new/sda")
    public String newFormSansDonneeAnterieure() {
        return "transfert-visa/new_sda.html";
    }

    @PostMapping("/api")
    @ResponseBody
    public ResponseEntity<ApiResponse<Map<String, Object>>> create(@RequestBody Map<String, Object> donnees) {
        try {
            DemandeTransfertVisa data = transfertVisaService.creerDemandeTransfertSda(donnees);
            return ResponseEntity.ok(new ApiResponse<>(true, Map.of("id", data.getId()), null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(new ApiResponse<>(false, null, "Erreur lors de la creation de la demande de transfert de visa."));
        }
    }
}
