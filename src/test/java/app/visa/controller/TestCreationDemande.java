package app.visa.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import app.visa.controller.response.ApiResponse;
import app.visa.dto.VisaRequestDto;
import app.visa.entity.Categorie;
import app.visa.entity.Nationalite;
import app.visa.entity.SituationFamiliale;
import app.visa.repository.CategorieRepository;
import app.visa.repository.DemandeurRepository;
import app.visa.repository.NationaliteRepository;
import app.visa.repository.PasseportRepository;
import app.visa.repository.SituationFamilialeRepository;
import app.visa.repository.TypeDemandeRepository;
import app.visa.repository.VisaTransformableRepository;
import app.visa.service.DemandeService;
import app.visa.service.DemandeurService;
import app.visa.service.PasseportService;
import app.visa.service.VisaRequestService;
import app.visa.service.VisaTransformableService;

import org.springframework.http.ResponseEntity;

// mvn -q test -Dtest=TestCreationDemande
public class TestCreationDemande {
    private NationaliteRepository nationaliteRepo;
    private SituationFamilialeRepository situationRepo;
    private TypeDemandeRepository typeRepo;
    private CategorieRepository categorieRepo;
    private DemandeurRepository demandeurRepo;
    private PasseportRepository passeportRepo;
    private VisaTransformableRepository visaTransformableRepo;
    private app.visa.repository.DemandeRepository demandeRepo;

    private VisaRequestService visaRequestService;
    private DemandeurService demandeurService;
    private DemandeService demandeService;
    private PasseportService passeportService;
    private VisaTransformableService visaTransformableService;

    private VisaRequestController controller;

    private void setup() {
        nationaliteRepo = mock(NationaliteRepository.class);
        situationRepo = mock(SituationFamilialeRepository.class);
        typeRepo = mock(TypeDemandeRepository.class);
        categorieRepo = mock(CategorieRepository.class);
        demandeurRepo = mock(DemandeurRepository.class);
        passeportRepo = mock(PasseportRepository.class);
        visaTransformableRepo = mock(VisaTransformableRepository.class);
        demandeRepo = mock(app.visa.repository.DemandeRepository.class);

        visaRequestService = mock(VisaRequestService.class);
        demandeurService = new DemandeurService(demandeurRepo, nationaliteRepo, situationRepo);
        demandeService = new DemandeService(demandeRepo, typeRepo, categorieRepo);
        passeportService = new PasseportService(passeportRepo, demandeurRepo);
        visaTransformableService = new VisaTransformableService(visaTransformableRepo, passeportRepo, demandeurRepo);

        // Ho an le when()
        Nationalite nat = new Nationalite();
        nat.setId(1);
        nat.setLibelle("Americaine");

        SituationFamiliale sit = new SituationFamiliale();
        sit.setId(2);
        sit.setLibelle("Celibataire");

        Categorie cat = new Categorie();
        cat.setId(10);
        cat.setLibelle("Nouveau titre");

        when(nationaliteRepo.findById(1)).thenReturn(Optional.of(nat));
        when(situationRepo.findById(2)).thenReturn(Optional.of(sit));
        when(categorieRepo.findAll()).thenReturn(List.of(cat));

        controller = new VisaRequestController(
            visaRequestService,
            demandeurService,
            demandeService,
            passeportService,
            visaTransformableService,
            nationaliteRepo,
            situationRepo,
            typeRepo,
            categorieRepo
        );
    }

    @Test
    public void create_containsDemandeurDebugFields() {
        setup();
        VisaRequestDto dto = buildStandardDto();

        ResponseEntity<ApiResponse<Object>> resp = controller.create(dto);
        assertNotNull(resp);
        ApiResponse<Object> body = resp.getBody();
        assertNotNull(body);
        assertTrue(body.isSuccess());

        @SuppressWarnings("unchecked")
        Map<String, Object> debug = (Map<String, Object>) body.getData();
        assertTrue(debug.containsKey("demandeur"));
        @SuppressWarnings("unchecked")
        Map<String, Object> demandeurMap = (Map<String, Object>) debug.get("demandeur");
        assertEquals("1990-01-01", demandeurMap.get("dateNaissance"));
        assertEquals("Americaine", demandeurMap.get("nationalite"));
        assertEquals("Celibataire", demandeurMap.get("situationFamiliale"));
    }

    @Test
    public void create_containsPasseportDebugFields() {
        setup();
        VisaRequestDto dto = buildStandardDto();

        ResponseEntity<ApiResponse<Object>> resp = controller.create(dto);
        ApiResponse<Object> body = resp.getBody();
        @SuppressWarnings("unchecked")
        Map<String, Object> debug = (Map<String, Object>) body.getData();
        assertTrue(debug.containsKey("passeport"));
        @SuppressWarnings("unchecked")
        Map<String, Object> passeportMap = (Map<String, Object>) debug.get("passeport");
        assertEquals("ABC123", passeportMap.get("numero"));
        assertEquals("2020-01-01T08:00", passeportMap.get("dateDelivrance"));
    }

    @Test
    public void create_containsVisaTransformableDebugFields() {
        setup();
        VisaRequestDto dto = buildStandardDto();

        ResponseEntity<ApiResponse<Object>> resp = controller.create(dto);
        ApiResponse<Object> body = resp.getBody();
        @SuppressWarnings("unchecked")
        Map<String, Object> debug = (Map<String, Object>) body.getData();
        assertTrue(debug.containsKey("visaTransformable"));
        @SuppressWarnings("unchecked")
        Map<String, Object> vtMap = (Map<String, Object>) debug.get("visaTransformable");
        assertEquals("VISA-1", vtMap.get("reference"));
    }

    
    private VisaRequestDto buildStandardDto() {
        VisaRequestDto dto = new VisaRequestDto();

        Map<String, Object> etatCivil = new HashMap<>();
        etatCivil.put("nom", "RANDRIA");
        etatCivil.put("prenom", "Jean");
        etatCivil.put("nomJeuneFille", "");
        etatCivil.put("email", "jean@example.com");
        etatCivil.put("numTel", "0340000000");
        etatCivil.put("adresse", "Lot 1");
        etatCivil.put("dateNaissance", "1990-01-01");
        etatCivil.put("nationalite", 1L);
        etatCivil.put("situationFamiliale", 2L);

        Map<String, Object> passeport = new HashMap<>();
        passeport.put("numero", "ABC123");
        passeport.put("dateDelivrance", "2020-01-01T08:00");
        passeport.put("dateExpiration", "2030-01-01T08:00");

        Map<String, Object> vt = new HashMap<>();
        vt.put("reference", "VISA-1");
        vt.put("dateEntree", "2024-01-10T10:30");
        vt.put("lieuEntree", "IVATO");
        vt.put("dateExpiration", "2024-04-10T23:59");

        dto.setEtatCivil(etatCivil);
        dto.setPasseport(passeport);
        dto.setVisaTransformable(vt);
        return dto;
    }
}
