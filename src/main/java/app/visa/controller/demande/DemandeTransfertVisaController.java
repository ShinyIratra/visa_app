package app.visa.controller.demande;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import app.visa.controller.response.ApiResponse;
import java.util.HashMap;
import app.visa.entity.DemandeTransfertVisa;
import app.visa.service.TransfertVisaService;

@Controller
@RequestMapping("/api/demande/transfert-visa")
public class DemandeTransfertVisaController {

    private final TransfertVisaService transfertVisaService;

    public DemandeTransfertVisaController(TransfertVisaService transfertVisaService) {
        this.transfertVisaService = transfertVisaService;
    }

    @PostMapping("/sda")
    public ResponseEntity<ApiResponse<Map<String, Object>>> create(@RequestBody Map<String, Object> donnees) {
        try {
            DemandeTransfertVisa demandeTransfert = transfertVisaService.creerDemandeTransfertSda(donnees);
            return ResponseEntity.ok(new ApiResponse<>(true, buildResponse(demandeTransfert), null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                .body(new ApiResponse<>(false, null, "Erreur lors de la creation de la demande de transfert de visa: " + e.getMessage()));
        }
    }

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
}
