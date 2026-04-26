package app.visa.service;

import app.visa.entity.CarteResident;
import app.visa.entity.Passeport;
import app.visa.repository.CarteResidentRepository;
import app.visa.repository.PasseportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CarteResidentService {

    private final CarteResidentRepository carteResidentRepository;
    private final PasseportRepository passeportRepository;

    public List<CarteResident> findAll() {
        return carteResidentRepository.findAll();
    }

    public CarteResident save(CarteResident carteResident) {
        return carteResidentRepository.save(carteResident);
    }

    public CarteResident findByLastNumeroPasseport(String lastNumeroPasseport) {
        Passeport passeport = passeportRepository.findByNumero(lastNumeroPasseport)
                .orElseThrow(() -> new IllegalArgumentException("Erreur Duplicata : Passeport avec numéro '" + lastNumeroPasseport + "' introuvable."));

        return carteResidentRepository.findFirstByPasseportIdOrderByDateCreationAsc(passeport.getId())
                .orElseThrow(() -> new IllegalArgumentException("Erreur Duplicata : Carte de résident liée au passeport avec numéro '" + lastNumeroPasseport + "' introuvable."));
    }
}