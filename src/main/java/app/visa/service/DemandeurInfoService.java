package app.visa.service;

import app.visa.entity.*;
import app.visa.repository.*;
import app.visa.dto.demande.DemandeDto;
import app.visa.dto.demande.HistoriqueDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.time.*;

@Service
@RequiredArgsConstructor
public class DemandeurInfoService {

    private final VisaRequestRepository visaRequestRepository;
    private final DemandeTransfertVisaRepository transfertRepository;
    private final DemandeDuplicataRepository duplicataRepository;
    private final VisaRequestService visaRequestService;
    private final TransfertVisaService transfertVisaService;
    private final DuplicataService duplicataService;
    private final PasseportRepository passeportRepository;
    private final DemandeurRepository demandeurRepository;
    private final CarteResidentRepository carteResidentRepository;
    private final DemandeService demandeService;

    /**
     * 
     * Mamoka ny liste demandes ho an'ny front office ito
     * 
     */

    @Transactional(readOnly = true)
    public Map<String, Object> getInfos(String numero, LocalDateTime dateDebut, LocalDateTime dateFin) {
        Demandeur demandeur = getDemandeurByNumero(numero);
        Map<String, Object> infos = buildDemandeurInfos(demandeur, numero, dateDebut, dateFin);
        
        // Get demandes specifiques
        if (numero != null) {
            String upperNum = numero.toUpperCase();
            if (upperNum.startsWith("DEMTRF")) {
                Optional<DemandeTransfertVisa> transfert = transfertRepository.findByNumero(numero);
                if (transfert.isPresent()) {
                    infos.put("demandeSelectionnee", buildTransfertDetails(transfert.get()));
                }
            } else if (upperNum.startsWith("DEMDUP")) {
                Optional<DemandeDuplicata> duplicata = duplicataRepository.findByNumero(numero);
                if (duplicata.isPresent()) {
                    infos.put("demandeSelectionnee", buildDuplicataDetails(duplicata.get()));
                }
            } else if (upperNum.startsWith("DEM")) {
                Optional<DemandeNouveauTitre> transformation = visaRequestRepository.findByNumero(numero);
                if (transformation.isPresent()) {
                    infos.put("demandeSelectionnee", buildTransformationDetails(transformation.get()));
                }
            }
        }
        
        return infos;
    }

    private Map<String, Object> buildTransformationDetails(DemandeNouveauTitre d) {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("type", "Transformation");
        details.put("numero", d.getNumero());
        details.put("dateCreation", d.getDateCreation());
        details.put("status", getStatus(d));
        
        if (d.getTypeDemande() != null) {
            details.put("typeDemande", d.getTypeDemande().getLibelle());
        }
        
        if (d.getPasseport() != null) {
            details.put("passeportNumero", d.getPasseport().getNumero());
            details.put("passeportDelivrance", d.getPasseport().getDateDelivrance());
            details.put("passeportExpiration", d.getPasseport().getDateExpiration());
        }
        
        if (d.getVisaTransformable() != null) {
            details.put("visaReference", d.getVisaTransformable().getReference());
            details.put("visaDateEntree", d.getVisaTransformable().getDateEntree());
            details.put("visaLieuEntree", d.getVisaTransformable().getLieuEntree());
            details.put("visaExpiration", d.getVisaTransformable().getDateExpiration());
        }
        return details;
    }

    private Map<String, Object> buildTransfertDetails(DemandeTransfertVisa t) {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("type", "Transfert");
        details.put("numero", t.getNumero());
        details.put("dateCreation", t.getDateCreation());
        details.put("status", getStatus(t));
        
        if (t.getNouveauPasseport() != null) {
            details.put("passeportNumero", t.getNouveauPasseport().getNumero());
            details.put("passeportDelivrance", t.getNouveauPasseport().getDateDelivrance());
            details.put("passeportExpiration", t.getNouveauPasseport().getDateExpiration());
        } else {
            DemandeNouveauTitre dnt = demandeService.getDemandeNouveauTitre(t);
            if (dnt != null && dnt.getPasseport() != null) {
                Passeport p = dnt.getPasseport();
                details.put("passeportNumero", p.getNumero());
                details.put("passeportDelivrance", p.getDateDelivrance());
                details.put("passeportExpiration", p.getDateExpiration());
            }
        }
        return details;
    }

    private Map<String, Object> buildDuplicataDetails(DemandeDuplicata d) {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("type", "Duplicata");
        details.put("numero", d.getNumero());
        details.put("dateCreation", d.getDateCreation());
        details.put("status", getStatus(d));
        
        DemandeNouveauTitre dnt = demandeService.getDemandeNouveauTitre(d);
        if (dnt != null && dnt.getPasseport() != null) {
            Passeport p = dnt.getPasseport();
            details.put("passeportNumero", p.getNumero());
            details.put("passeportDelivrance", p.getDateDelivrance());
            details.put("passeportExpiration", p.getDateExpiration());
        }
        return details;
    }

    private Demandeur getDemandeurByNumero(String numero) {
        Demandeur demandeur;
        
        // DEM-..., DEMTRF-..., DEMDUP-...
        if (numero != null && numero.toUpperCase().startsWith("DEM")) {
            demandeur = findDemandeurByNumeroDemande(numero);
            if (demandeur == null) {
                throw new IllegalArgumentException("Aucun demandeur trouve pour le numero de demande : " + numero);
            }
        } else {
            Passeport passeport = passeportRepository.findFirstByNumero(numero).orElse(null);
            if (passeport == null || passeport.getDemandeur() == null) {
                throw new IllegalArgumentException("Aucun demandeur trouve pour le numero de passeport : " + numero);
            }
            demandeur = passeport.getDemandeur();
        }

        return demandeur;
    }

    private Map<String, Object> buildDemandeurInfos(Demandeur demandeur, String numeroPrioritaire, LocalDateTime dateDebut, LocalDateTime dateFin) {
        Map<String, Object> result = new LinkedHashMap<>();
        
        Map<String, Object> demandeurMap = new LinkedHashMap<>();
        demandeurMap.put("id", demandeur.getId());
        demandeurMap.put("nom", demandeur.getNom());
        demandeurMap.put("prenom", demandeur.getPrenom() != null ? demandeur.getPrenom() : "");
        demandeurMap.put("nomJeuneFille", demandeur.getNomJeuneFille());
        demandeurMap.put("dateNaissance", demandeur.getDateNaissance());
        demandeurMap.put("adresse", demandeur.getAdresse());
        demandeurMap.put("email", demandeur.getEmail());
        demandeurMap.put("numTel", demandeur.getNumTel());
        
        if (demandeur.getNationalite() != null) {
            demandeurMap.put("nationalite", demandeur.getNationalite().getLibelle());
        }
        if (demandeur.getSituationFamiliale() != null) {
            demandeurMap.put("situationFamiliale", demandeur.getSituationFamiliale().getLibelle());
        }
        
        result.put("demandeur", demandeurMap);

        // 2. Get all demandes (Transformation, Transfert, Duplicata)
        List<DemandeDto> toutesLesDemandes = new ArrayList<>();

        toutesLesDemandes.addAll(getTransformations(demandeur.getId()));
        toutesLesDemandes.addAll(getTransferts(demandeur.getId()));
        toutesLesDemandes.addAll(getDuplicatas(demandeur.getId()));

        // Filtre an'ny kamo, TODO: mikitika base
        if (dateDebut != null || dateFin != null) {
            toutesLesDemandes.removeIf(d -> {
                LocalDateTime dateCreation = d.getDateCreation();
                boolean isBeforeDebut = dateDebut != null && dateCreation.isBefore(dateDebut);
                boolean isAfterFin = dateFin != null && dateCreation.isAfter(dateFin);
                return isBeforeDebut || isAfterFin;
            });
        }

        // Tri decroissant, fa demande.numero == numeroPrioritaire no mandeha aloha
        toutesLesDemandes.sort((a, b) -> {
            String numA = a.getNumero();
            String numB = b.getNumero();

            if (numeroPrioritaire != null && numeroPrioritaire.equals(numA)) 
                return -1;
            if (numeroPrioritaire != null && numeroPrioritaire.equals(numB)) 
                return 1;

            return b.getDateCreation().compareTo(a.getDateCreation());
        });

        result.put("demandes", toutesLesDemandes);

        return result;
    }

    private List<DemandeDto> getTransformations(Integer demandeurId) {
        List<Demande> transformations = visaRequestRepository.findByPasseportDemandeurId(demandeurId);
        List<DemandeDto> result = new ArrayList<>();
        for (Demande d : transformations) {
            result.add(new DemandeDto(
                "TRANSFORMATION",
                d.getNumero(),
                d.getDateCreation(),
                getStatus(d),
                formatHistoriqueTransformation(d)
            ));
        }
        return result;
    }

    private List<DemandeDto> getTransferts(Integer demandeurId) {
        List<DemandeTransfertVisa> transferts = transfertRepository.findByDemandeurId(demandeurId);
        List<DemandeDto> result = new ArrayList<>();
        for (DemandeTransfertVisa t : transferts) {
            result.add(new DemandeDto(
                "TRANSFERT",
                t.getNumero(),
                t.getDateCreation(),
                getStatus(t),
                formatHistoriqueTransfert(t)
            ));
        }
        return result;
    }

    private List<DemandeDto> getDuplicatas(Integer demandeurId) {
        List<DemandeDuplicata> duplicatas = duplicataRepository.findByDemandeurId(demandeurId);
        List<DemandeDto> result = new ArrayList<>();
        for (DemandeDuplicata d : duplicatas) {
            result.add(new DemandeDto(
                "DUPLICATA",
                d.getNumero(),
                d.getDateCreation(),
                getStatus(d),
                formatHistoriqueDuplicata(d)
            ));
        }
        return result;
    }

    private List<HistoriqueDto> formatHistoriqueTransformation(Demande d) {
        if (d.getHistoriques() == null) return Collections.emptyList();
        return d.getHistoriques().stream()
            .sorted(Comparator.comparing(HistoriqueStatut::getDateModification).reversed())
            .map(h -> new HistoriqueDto(h.getStatut().getLibelle(), h.getDateModification()))
            .toList();
    }

    private List<HistoriqueDto> formatHistoriqueTransfert(DemandeTransfertVisa t) {
        if (t.getHistoriques() == null) return Collections.emptyList();
        return t.getHistoriques().stream()
            .sorted(Comparator.comparing(HistoriqueStatut::getDateModification).reversed())
            .map(h -> new HistoriqueDto(h.getStatut().getLibelle(), h.getDateModification()))
            .toList();
    }

    private List<HistoriqueDto> formatHistoriqueDuplicata(DemandeDuplicata d) {
        if (d.getHistoriques() == null) 
            return Collections.emptyList();

        return d.getHistoriques().stream()
            .sorted(Comparator.comparing(HistoriqueStatut::getDateModification).reversed())
            .map(h -> new HistoriqueDto(h.getStatut().getLibelle(), h.getDateModification()))
            .toList();
    }

    private Demandeur findDemandeurByNumeroDemande(String numero) {
        Optional<DemandeNouveauTitre> d = visaRequestRepository.findByNumero(numero);
        if (d.isPresent()) return d.get().getPasseport().getDemandeur();

        Optional<DemandeTransfertVisa> t = transfertRepository.findByNumero(numero);
        if (t.isPresent()) {
            Passeport p = demandeService.getPasseport(t.get());
            return p != null ? p.getDemandeur() : null;
        }

        Optional<DemandeDuplicata> dup = duplicataRepository.findByNumero(numero);
        if (dup.isPresent()) {
            Passeport p = demandeService.getPasseport(dup.get());
            return p != null ? p.getDemandeur() : null;
        }

        return null;
    }

    private String getStatus(Object entity) {
        if (entity instanceof Demande d) {
            Statut s = demandeService.getDernierStatus(d);
            return s != null ? s.getLibelle() : "Aucun";
        }
        return "Inconnu";
    }

    /**
     * 
     * Ho an le liste demandeurs ao @ backoffice ny ato
     * 
     */

    
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAllDemandeurs() {
        return demandeurRepository.findAll().stream()
                .map(this::buildDemandeurMap)
                .toList();
    }

    private Map<String, Object> buildDemandeurMap(Demandeur demandeur) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", demandeur.getId());
        map.put("nom", demandeur.getNom());
        map.put("prenom", demandeur.getPrenom() != null ? demandeur.getPrenom() : "");
        
        Passeport currentPasseport = getPasseportActuel(demandeur.getId());
        Passeport originalPasseport = getPasseportOriginal(demandeur.getId());

        map.put("passeportActuel", formatPasseport(currentPasseport));
        map.put("passeportOriginal", formatPasseport(originalPasseport));
        
        if (currentPasseport != null) {
            map.put("visas", buildVisasMap(currentPasseport));
            map.put("cartesResident", buildCartesResidentMap(currentPasseport));
        } else {
            map.put("visas", Collections.emptyList());
            map.put("cartesResident", Collections.emptyList());
        }
        return map;
    }

    private Passeport getPasseportActuel(Integer demandeurId) {
        Passeport actuelFromVisa = passeportRepository.findActuelByVisapasseport(demandeurId).orElse(null);
        if (actuelFromVisa != null) {
            return actuelFromVisa;
        }

        // Fallback demande en cours
        List<Passeport> passeports = passeportRepository.findByDemandeurId(demandeurId);
        if (passeports == null || passeports.isEmpty()) return null;

        return passeports.stream()
                .max(Comparator.comparing(Passeport::getDateExpiration))
                .orElse(null);
    }

    private Passeport getPasseportOriginal(Integer demandeurId) {
        List<Passeport> passeports = passeportRepository.findByDemandeurId(demandeurId);
        if (passeports == null || passeports.isEmpty()) return null;
        
        return passeports.stream()
                .min(Comparator.comparing(Passeport::getDateDelivrance))
                .orElse(null);
    }

    private Map<String, Object> formatPasseport(Passeport passeport) {
        if (passeport == null) return null;
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("numero", passeport.getNumero());
        map.put("dateDelivrance", passeport.getDateDelivrance());
        map.put("dateExpiration", passeport.getDateExpiration());
        return map;
    }

    private List<Map<String, Object>> buildVisasMap(Passeport passeport) {
        return passeport.getVisas().stream().map(v -> {
            Map<String, Object> vMap = new LinkedHashMap<>();
            vMap.put("numero", v.getNumero() != null ? v.getNumero() : "");
            vMap.put("dateDebut", v.getDateDebut());
            vMap.put("dateExpiration", v.getDateExpiration());
            return vMap;
        }).toList();
    }

    private List<Map<String, Object>> buildCartesResidentMap(Passeport passeport) {
        return carteResidentRepository.findByPasseportId(passeport.getId()).stream().map(c -> {
            Map<String, Object> cMap = new LinkedHashMap<>();
            cMap.put("numero", c.getNumero() != null ? c.getNumero() : "");
            if (c.getDateDebut() != null) cMap.put("dateDebut", c.getDateDebut());
            if (c.getDateExpiration() != null) cMap.put("dateExpiration", c.getDateExpiration());
            return cMap;
        }).toList();
    }
}
