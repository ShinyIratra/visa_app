package app.visa.service;

import app.visa.entity.DemandeTransfertVisa;
import app.visa.entity.Passeport;
import app.visa.entity.Statut;
import app.visa.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class UpdateTransfertVisaService extends TransfertVisaService {

    public UpdateTransfertVisaService(
            VisaRequestService visaRequestService,
            AcceptationDemandeVisaService acceptationDemandeVisaService,
            DemandeTransfertVisaRepository demandeTransfertVisaRepository,
            VisaRepository visaRepository,
            StatutRepository statutRepository,
            DemandeService demandeService) {
        super(visaRequestService, acceptationDemandeVisaService, demandeTransfertVisaRepository, visaRepository, statutRepository, demandeService);
    }

    public Map<String, Object> getTransfertForEdit(Integer id) {
        DemandeTransfertVisa t = demandeTransfertVisaRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Demande de transfert " + id + " introuvable"));

        Statut s = getStatut(t);
        if (s != null && s.getLibelle().equalsIgnoreCase("Demande acceptee")) {
            throw new IllegalStateException("Impossible de modifier une demande déjà acceptee");
        }

        Map<String, Object> result = new HashMap<>();
        if (t.getNouveauPasseport() != null) {
            Passeport p = t.getNouveauPasseport();
            result.put("numero", p.getNumero());
            result.put("dateDelivrance", p.getDateDelivrance() != null ? p.getDateDelivrance().toString() : null);
            result.put("dateExpiration", p.getDateExpiration() != null ? p.getDateExpiration().toString() : null);
        }
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateNouveauPasseport(Integer id, Map<String, Object> donnees) {
        DemandeTransfertVisa t = demandeTransfertVisaRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Demande de transfert " + id + " introuvable"));

        Statut s = getStatut(t);
        if (s != null && s.getLibelle().equalsIgnoreCase(UtilService.STATUS_DEMANDE_ACCEPTEE)) {
            throw new IllegalStateException("La demande a deja ete acceptee et ne peut plus etre modifiee");
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> pData = (Map<String, Object>) donnees.get("nouveau passeport");
        if (pData == null) {
            throw new IllegalArgumentException("Donnees du passeport manquantes");
        }

        Passeport p = t.getNouveauPasseport();
        p.setNumero((String) pData.get("numero"));
        
        String dateDelivranceStr = (String) pData.get("dateDelivrance");
        if (dateDelivranceStr != null) {
            p.setDateDelivrance(LocalDateTime.parse(dateDelivranceStr));
        }
        
        String dateExpirationStr = (String) pData.get("dateExpiration");
        if (dateExpirationStr != null) {
            p.setDateExpiration(LocalDateTime.parse(dateExpirationStr));
        }

        demandeTransfertVisaRepository.save(t);
    }
}
