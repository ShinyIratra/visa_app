package app.visa.controller;

import app.visa.controller.response.ApiResponse;
import app.visa.entity.DemandeTransfertVisa;
import app.visa.service.TransfertVisaService;
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
        response.put("id", demandeTransfert.getId());
        response.put("nouveauPasseport", demandeTransfert.getNouveauPasseport());
        // Mbola tsy haiko ihany, asina visa ve ?
        // if (demandeTransfert.getVisa() != null) {
        //     response.put("visaSource", demandeTransfert.getVisa());
        // }
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
