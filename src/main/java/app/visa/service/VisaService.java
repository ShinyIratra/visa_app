package app.visa.service;

import app.visa.entity.Visa;
import app.visa.repository.VisaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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
}