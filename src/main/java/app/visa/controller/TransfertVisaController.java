package app.visa.controller;

import app.visa.controller.response.ApiResponse;
import app.visa.entity.DemandeTransfertVisa;
import app.visa.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/transfert-visa")
@RequiredArgsConstructor
public class TransfertVisaController {

    private final TransfertVisaService transfertVisaService;
    private final UpdateTransfertVisaService updateTransfertVisaService;

    @GetMapping
    public String list() {
        return "transfert-visa/list.html";
    }

    @GetMapping("/list")
    @ResponseBody
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listData(
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end) {
        try {
            List<Map<String, Object>> data = transfertVisaService.listTransfertsAvecInfos();

            if (start != null && !start.isEmpty()) {
                java.time.LocalDateTime startDate = java.time.LocalDateTime.parse(start);
                data = data.stream()
                        .filter(t -> t.get("dateCreation") != null && !((java.time.LocalDateTime) t.get("dateCreation")).isBefore(startDate))
                        .toList();
            }
            if (end != null && !end.isEmpty()) {
                java.time.LocalDateTime endDate = java.time.LocalDateTime.parse(end);
                data = data.stream()
                        .filter(t -> t.get("dateCreation") != null && !((java.time.LocalDateTime) t.get("dateCreation")).isAfter(endDate))
                        .toList();
            }

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

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Integer id, org.springframework.ui.Model model) {
        model.addAttribute("transfertId", id);
        return "transfert-visa/form-edit.html";
    }

    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<ApiResponse<Map<String, Object>>> getForEdit(@PathVariable Integer id) {
        try {
            Map<String, Object> data = updateTransfertVisaService.getTransfertForEdit(id);
            return ResponseEntity.ok(new ApiResponse<>(true, data, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @ResponseBody
    public ResponseEntity<ApiResponse<Void>> update(@PathVariable Integer id, @RequestBody Map<String, Object> donnees) {
        try {
            updateTransfertVisaService.updateNouveauPasseport(id, donnees);
            return ResponseEntity.ok(new ApiResponse<>(true, null, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, e.getMessage()));
        }
    }
    
    @PostMapping("/sda")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createSda(@RequestBody Map<String, Object> donnees) {
        try {
            DemandeTransfertVisa demandeTransfert = transfertVisaService.creerDemandeTransfertSda(donnees);
            return ResponseEntity.ok(new ApiResponse<>(true, buildResponse(demandeTransfert), null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, e.getMessage()));
        }
    }

    @PostMapping("/ada")
    @ResponseBody
    public ResponseEntity<ApiResponse<Map<String, Object>>> createAda(@RequestBody Map<String, Object> donnees) {
        try {
            DemandeTransfertVisa demandeTransfert = transfertVisaService.creerDemandeTransfertAda(donnees);
            return ResponseEntity.ok(new ApiResponse<>(true, buildResponse(demandeTransfert), null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, e.getMessage()));
        }
    }

    // Tsisy ilaivana azy le reponse rehefa ok fa apetako eto ihany au cas ou ilaina
    private Map<String, Object> buildResponse(DemandeTransfertVisa demandeTransfert) {
        Map<String, Object> response = new HashMap<>();
        if (demandeTransfert.getNouveauPasseport() != null) {
            Map<String, Object> passMap = new HashMap<>();
            passMap.put("id", demandeTransfert.getNouveauPasseport().getId());
            passMap.put("numPasseport", demandeTransfert.getNouveauPasseport().getNumero());
            response.put("nouveauPasseport", passMap);
        }
        return response;
    }

    @PostMapping("/{id}/accepter")
    @ResponseBody
    public ResponseEntity<ApiResponse<Void>> accepter(@PathVariable Integer id) {
        try {
            transfertVisaService.accepterTransfert(id);
            return ResponseEntity.ok(new ApiResponse<>(true, null, null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(new ApiResponse<>(false, null, "Erreur lors de l'acceptation: " + e.getMessage()));
        }
    }
}
