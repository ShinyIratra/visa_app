package app.visa.service;

import java.util.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.web.multipart.MultipartFile;
import app.visa.entity.*;
import app.visa.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ScanService {

    private final VisaRequestRepository visaRequestRepository;
    private final DossierRepository dossierRepository;
    private final ReponseStatutVisaRepository reponseStatutVisaRepository;
    private final StatutRepository statutRepository;
    private final HistoriqueStatutRepository historiqueStatutRepository;
    private final DemandeService demandeService;

    private static final String UPLOAD_DIR = "uploads/scans";
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 mo
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(".pdf", ".jpg", ".jpeg", ".png");

    @Transactional(rollbackFor = Exception.class)
    public void sauvegarderFichiersScan(Integer demandeId, Map<String, MultipartFile> files) throws IOException {
        Demande demande = visaRequestRepository.findById(demandeId)
            .orElseThrow(() -> new IllegalArgumentException("Demande introuvable: " + demandeId));

        // 1. Controle
        validerFichiers(files);

        // 2. Metier
        Map<MultipartFile, Path> uploads = preparerUploads(demande, files);

        // 3. Persistance
        effectuerPersistancePhysique(uploads);
    }

    private void validerFichiers(Map<String, MultipartFile> files) {
        for (MultipartFile file : files.values()) {
            if (file != null && !file.isEmpty()) {
                if (file.getSize() > MAX_FILE_SIZE) {
                    throw new IllegalArgumentException("Le fichier " + file.getOriginalFilename() + " dépasse la taille maximale autorisée (5Mo)");
                }
                
                String originalFilename = file.getOriginalFilename();
                if (originalFilename == null || !estUneExtensionValide(originalFilename)) {
                    throw new IllegalArgumentException("Le fichier " + originalFilename + " a une extension non autorisée. Extensions permises : " + ALLOWED_EXTENSIONS);
                }
            }
        }
    }

    private boolean estUneExtensionValide(String filename) {
        String lowerName = filename.toLowerCase();
        return ALLOWED_EXTENSIONS.stream().anyMatch(lowerName::endsWith);
    }

    private Map<MultipartFile, Path> preparerUploads(Demande demande, Map<String, MultipartFile> files) throws IOException {
        String rootPath = System.getProperty("user.dir");
        Demandeur demandeur = demande.getPasseport().getDemandeur();
        String nomComplet = (demandeur.getNom() + "_" + demandeur.getPrenom()).replaceAll("[^a-zA-Z0-9_-]", "");
        
        // Structure: uploads/scans/ID_NOM_COMPLET/
        Path uploadPath = Paths.get(rootPath, UPLOAD_DIR, demandeur.getId() + "_" + nomComplet, "")
            .toAbsolutePath().normalize();
            
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        String timestamp = LocalDateTime.now().format(formatter);
        Map<MultipartFile, Path> uploads = new HashMap<>();

        for (Map.Entry<String, MultipartFile> entry : files.entrySet()) {
            MultipartFile file = entry.getValue();
            if (file != null && !file.isEmpty()) {
                String dossierIdStr = entry.getKey().replace("dossier_", "");
                String dossierLibelle = findDossierLibelle(dossierIdStr);

                String originalFilename = file.getOriginalFilename();
                String extension = originalFilename != null && originalFilename.contains(".") 
                    ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                    : "";

                String finalFileName = String.format("%d_%s_%s_%s%s", 
                    demandeur.getId(), nomComplet, dossierLibelle, timestamp, extension);
                
                uploads.put(file, uploadPath.resolve(finalFileName));
            }
        }
        return uploads;
    }

    private String findDossierLibelle(String dossierIdStr) {
        try {
            Integer dId = Integer.parseInt(dossierIdStr);
            return dossierRepository.findById(dId)
                .map(Dossier::getLibelle)
                .orElse("document")
                .replaceAll("[^a-zA-Z0-9_-]", "_");
        } catch (NumberFormatException e) {
            return "document";
        }
    }

    private void effectuerPersistancePhysique(Map<MultipartFile, Path> uploads) throws IOException {
        for (Map.Entry<MultipartFile, Path> entry : uploads.entrySet()) {
            entry.getKey().transferTo(entry.getValue().toFile());
        }
    }

    /**
     * CONTRÔLE 1: Vérifie que tous les dossiers (obligatoires ET facultatifs) 
     * de la demande sont cochés dans ReponseStatutVisa.
     */
    @Transactional(readOnly = true)
    public boolean controleAllDossiersCoches(Integer demandeId) {
        Demande demande = visaRequestRepository.findById(demandeId)
            .orElseThrow(() -> new IllegalArgumentException("Demande introuvable: " + demandeId));

        List<ReponseStatutVisa> reponses = reponseStatutVisaRepository.findByDemandeId(demandeId);
        
        for (ReponseStatutVisa r : reponses) {
            if (!Boolean.TRUE.equals(r.getValeur())) {
                return false;
            }
        }
        
        return !reponses.isEmpty();
    }

    public boolean controleChampsobligatoires(Map<String, Object> formData) {
        if (formData == null) {
            return false;
        }

        Map<String, Object> etatCivil = UtilService.getBloc(formData, "etat civil");
        if (!checkChampString(etatCivil, "nom") ||
            !checkChampString(etatCivil, "prenom") ||
            !checkChampString(etatCivil, "numTel") ||
            !checkChampString(etatCivil, "adresse") ||
            !checkChampString(etatCivil, "dateNaissance") ||
            !checkChampId(etatCivil, "nationalite") ||
            !checkChampId(etatCivil, "situationFamiliale")) {
            return false;
        }

        Map<String, Object> passeport = UtilService.getBloc(formData, "passeport");
        if (!checkChampString(passeport, "numero") ||
            !checkChampString(passeport, "dateDelivrance") ||
            !checkChampString(passeport, "dateExpiration")) {
            return false;
        }

        Map<String, Object> visaTransformable = UtilService.getBloc(formData, "visaTransformable");
        if (!checkChampString(visaTransformable, "reference") ||
            !checkChampString(visaTransformable, "dateEntree") ||
            !checkChampString(visaTransformable, "lieuEntree") ||
            !checkChampString(visaTransformable, "dateExpiration")) {
            return false;
        }

        Object dossiersFournisObj = formData.get("dossiersFournis");
        if (!(dossiersFournisObj instanceof List<?> dossiersListe) || dossiersListe.isEmpty()) {
            return false;
        }

        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public void marquerScanTermine(Integer demandeId) {
        Demande demande = visaRequestRepository.findById(demandeId)
            .orElseThrow(() -> new IllegalArgumentException("Demande introuvable: " + demandeId));

        // Créer un historique avec le nouveau statut
        Statut statut = statutRepository.findByLibelle(UtilService.STATUS_SCAN_TERMINE)
            .orElseThrow(() -> new IllegalArgumentException("Statut 'Scan terminé' introuvable"));

        HistoriqueStatut historique = new HistoriqueStatut();
        historique.setDemande(demande);
        historique.setStatut(statut);
        historique.setDateModification(java.time.LocalDateTime.now());

        historiqueStatutRepository.save(historique);
    }

    /**
     * Récupère les dossiers applicables pour une demande (pour l'affichage sur la page de scan).
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getDossiersAvecStatut(Integer demandeId) {
        Demande demande = visaRequestRepository.findById(demandeId)
            .orElseThrow(() -> new IllegalArgumentException("Demande introuvable: " + demandeId));

        List<Dossier> dossiers = dossierRepository.findDossiersPourTypeDemande(demande.getTypeDemande().getId());
        List<ReponseStatutVisa> reponses = reponseStatutVisaRepository.findByDemandeId(demandeId);

        Map<Integer, Boolean> reponseMap = new HashMap<>();
        for (ReponseStatutVisa r : reponses) {
            reponseMap.put(r.getDossier().getId(), r.getValeur());
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Dossier d : dossiers) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", d.getId());
            item.put("libelle", d.getLibelle());
            item.put("obligatoire", d.getObligatoire());
            item.put("coche", reponseMap.getOrDefault(d.getId(), false));
            result.add(item);
        }

        return result;
    }

    // ==================== HELPERS PRIVÉS ====================

    private boolean checkChampString(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val == null) {
            return false;
        }
        String str = val.toString().trim();
        return !str.isEmpty();
    }

    private boolean checkChampId(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val == null) {
            return false;
        }
        String str = val.toString().trim();
        if (str.isEmpty()) {
            return false;
        }
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
