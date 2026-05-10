package app.visa.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import org.springframework.web.multipart.MultipartFile;
import app.visa.entity.*;
import app.visa.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PhotoService {

    private final VisaRequestRepository visaRequestRepository;
    private final DossierRepository dossierRepository;
    private final ReponseStatutVisaRepository reponseStatutVisaRepository;
    private final StatutRepository statutRepository;
    private final HistoriqueStatutRepository historiqueStatutRepository;

    private static final String PHOTO_DIR = "uploads/photos";

    public Map<String, Object> getPhotoEtSignatureUrls(Integer demandeId) {
        Demande demande = visaRequestRepository.findById(demandeId)
            .orElseThrow(() -> new IllegalArgumentException("Demande introuvable: " + demandeId));

        Demandeur demandeur = demande.getPasseport().getDemandeur();
        String nomComplet = (demandeur.getNom() + "_" + demandeur.getPrenom()).replaceAll("[^a-zA-Z0-9_-]", "");
        String folderName = demandeur.getId() + "_" + nomComplet;

        String rootPath = System.getProperty("user.dir");
        Path uploadPath = Paths.get(rootPath, PHOTO_DIR, folderName).toAbsolutePath().normalize();

        Map<String, Object> result = new HashMap<>();
        if (Files.exists(uploadPath)) {
            try {
                // Find latest photo and signature
                Optional<Path> photo = Files.list(uploadPath)
                    .filter(p -> p.getFileName().toString().contains("_photo_"))
                    .max(Comparator.comparing(p -> p.toFile().lastModified()));
                Optional<Path> signature = Files.list(uploadPath)
                    .filter(p -> p.getFileName().toString().contains("_signature_"))
                    .max(Comparator.comparing(p -> p.toFile().lastModified()));

                if (photo.isPresent()) {
                    result.put("photoUrl", "/uploads/photos/" + folderName + "/" + photo.get().getFileName().toString());
                }
                if (signature.isPresent()) {
                    result.put("signatureUrl", "/uploads/photos/" + folderName + "/" + signature.get().getFileName().toString());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public void sauvegarderPhotoEtSignature(Integer demandeId, MultipartFile photo, MultipartFile signature) throws IOException {
        Demande demande = visaRequestRepository.findById(demandeId)
            .orElseThrow(() -> new IllegalArgumentException("Demande introuvable: " + demandeId));

        Demandeur demandeur = demande.getPasseport().getDemandeur();
        String nomComplet = (demandeur.getNom() + "_" + demandeur.getPrenom()).replaceAll("[^a-zA-Z0-9_-]", "");
        
        // Structure: uploads/photos/ID_NOM_COMPLET/
        String rootPath = System.getProperty("user.dir");
        Path uploadPath = Paths.get(rootPath, PHOTO_DIR, demandeur.getId() + "_" + nomComplet).toAbsolutePath().normalize();
            
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        String timestamp = LocalDateTime.now().format(formatter);

        // Sauvegarder la photo
        if (photo != null && !photo.isEmpty()) {
            String finalPhotoName = String.format("%d_%s_photo_%s.jpg", demandeur.getId(), nomComplet, timestamp);
            photo.transferTo(uploadPath.resolve(finalPhotoName).toFile());
        }

        // Sauvegarder la signature
        if (signature != null && !signature.isEmpty()) {
            String finalSignatureName = String.format("%d_%s_signature_%s.png", demandeur.getId(), nomComplet, timestamp);
            signature.transferTo(uploadPath.resolve(finalSignatureName).toFile());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void marquerPhotoTerminee(Integer demandeId) {
        Demande demande = visaRequestRepository.findById(demandeId)
            .orElseThrow(() -> new IllegalArgumentException("Demande introuvable: " + demandeId));

        // Créer un historique avec le nouveau statut (utilisation possible de UtilService.STATUS_PHOTO_SCANNEE)
        Statut statut = statutRepository.findByLibelle(UtilService.STATUS_PHOTO_SCANNEE)
            .orElseThrow(() -> new IllegalArgumentException("Statut 'Photo scannée' introuvable dans la base"));

        HistoriqueStatut historique = new HistoriqueStatut();
        historique.setDemande(demande);
        historique.setStatut(statut);
        historique.setDateModification(java.time.LocalDateTime.now());

        historiqueStatutRepository.save(historique);
    }
}
