
package app.visa.service;

import app.visa.entity.Demandeur;
import app.visa.entity.Passeport;
import app.visa.repository.DemandeurRepository;
import app.visa.repository.PasseportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PasseportService {

	private final PasseportRepository passeportRepository;
	private final DemandeurRepository demandeurRepository;

    public Passeport buildPasseport(Map<String, Object> passMap, Demandeur demandeur) {
        Passeport passeport = new Passeport();
        passeport.setNumero((String) passMap.get("numero"));
        passeport.setDateDelivrance(LocalDateTime.parse((String) passMap.get("dateDelivrance")));
        passeport.setDateExpiration(LocalDateTime.parse((String) passMap.get("dateExpiration")));
        passeport.setDemandeur(demandeur);
        return passeport;
    }

	@Transactional(rollbackFor = Exception.class)
	public Passeport createPasseport(Passeport passeport) {
		validerPasseport(passeport);

		Integer demandeurId = passeport.getDemandeur().getId();
		Demandeur demandeur = demandeurRepository.findById(demandeurId)
			.orElseThrow(() -> new IllegalArgumentException("demandeur introuvable: " + demandeurId));

		passeport.setNumero(passeport.getNumero().trim());
		passeport.setDemandeur(demandeur);

		return passeportRepository.save(passeport);
	}

	@Transactional(rollbackFor = Exception.class)
	public Passeport updatePasseport(Integer id, Passeport details) {
		Passeport passeport = passeportRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("passeport introuvable: " + id));

		passeport.setNumero(details.getNumero() != null ? details.getNumero().trim() : null);
		passeport.setDateDelivrance(details.getDateDelivrance());
		passeport.setDateExpiration(details.getDateExpiration());

		validerPasseport(passeport);
		return passeportRepository.save(passeport);
	}

	private void validerPasseport(Passeport passeport) {
		if (passeport == null) {
			throw new IllegalArgumentException("passeport obligatoire.");
		}
		if (estVide(passeport.getNumero())) {
			throw new IllegalArgumentException("numero de passeport obligatoire.");
		}
		if (passeport.getDateDelivrance() == null) {
			throw new IllegalArgumentException("date de delivrance obligatoire.");
		}
		if (passeport.getDateExpiration() == null) {
			throw new IllegalArgumentException("date d'expiration obligatoire.");
		}
		if (passeport.getDateExpiration().isBefore(passeport.getDateDelivrance())) {
			throw new IllegalArgumentException("la date d'expiration doit etre apres la date de delivrance.");
		}
		if (passeport.getDemandeur() == null || passeport.getDemandeur().getId() == null) {
			throw new IllegalArgumentException("demandeur obligatoire.");
		}
	}

	private boolean estVide(String valeur) {
		return valeur == null || valeur.isBlank();
	}
}
