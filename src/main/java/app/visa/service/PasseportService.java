package app.visa.service;

import app.visa.entity.Demandeur;
import app.visa.entity.Passeport;
import app.visa.repository.DemandeurRepository;
import app.visa.repository.PasseportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PasseportService {

	private final PasseportRepository passeportRepository;
	private final DemandeurRepository demandeurRepository;

	@Transactional(rollbackOn = Exception.class)
	public Passeport createPasseport(Passeport passeport) {
		validerPasseport(passeport);

		Long demandeurId = passeport.getDemandeur().getId();
		Demandeur demandeur = demandeurRepository.findById(demandeurId)
			.orElseThrow(() -> new IllegalArgumentException("demandeur introuvable: " + demandeurId));

		passeport.setNumero(passeport.getNumero().trim());
		passeport.setDemandeur(demandeur);

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
