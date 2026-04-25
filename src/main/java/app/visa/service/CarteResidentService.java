package app.visa.service;

import app.visa.entity.CarteResident;
import app.visa.repository.CarteResidentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CarteResidentService {

    private final CarteResidentRepository carteResidentRepository;

    public List<CarteResident> findAll() {
        return carteResidentRepository.findAll();
    }

    public CarteResident save(CarteResident carteResident) {
        return carteResidentRepository.save(carteResident);
    }
}