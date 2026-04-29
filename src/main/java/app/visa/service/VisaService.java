package app.visa.service;

import app.visa.entity.Demande;
import app.visa.entity.Demandeur;
import app.visa.entity.Passeport;
import app.visa.entity.Visa;
import app.visa.repository.VisaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VisaService {

    private final VisaRepository visaRepository;

    public List<Visa> findAll() {
        return visaRepository.findAll();
    }

    public Visa save(Visa visa) {
        return visaRepository.save(visa);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listVisasAvecInfos(LocalDateTime start, LocalDateTime end) {
        List<Visa> visas = visaRepository.findAll();
        
        return visas.stream()
            .filter(v -> (start == null || !v.getDateCreation().isBefore(start)) &&
                         (end == null || !v.getDateCreation().isAfter(end)))
            .map(this::mapToInfo)
            .collect(Collectors.toList());
    }

    private Map<String, Object> mapToInfo(Visa visa) {
        Map<String, Object> map = new LinkedHashMap<>();
        Demande demande = visa.getDemande();
        
        map.put("id", visa.getId());
        map.put("dateCreation", visa.getDateCreation());
        
        if (demande != null) {
            map.put("demandeId", demande.getId());
            
            // Ancien passeport (demande OG)
            if (demande.getPasseport() != null) {
                map.put("ancienPasseport", demande.getPasseport().getNumero());
                
                Demandeur demandeur = demande.getPasseport().getDemandeur();
                if (demandeur != null) {
                    map.put("nomComplet", (demandeur.getNom() != null ? demandeur.getNom() : "") + " " + 
                                         (demandeur.getPrenom() != null ? demandeur.getPrenom() : ""));
                    map.put("demandeurId", demandeur.getId());
                }
            }
        }

        // Nouveau passeport (Plus recent)
        List<Passeport> sortedPasseports = visa.getPasseports().stream()
            .sorted(Comparator.comparing(Passeport::getId).reversed())
            .toList();
            
        if (!sortedPasseports.isEmpty()) {
            map.put("nouveauPasseport", sortedPasseports.get(0).getNumero());
        } else {
            map.put("nouveauPasseport", map.get("ancienPasseport"));
        }

        return map;
    }
}