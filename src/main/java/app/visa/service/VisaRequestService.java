package app.visa.service;

import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.visa.entity.Demande;
import app.visa.repository.VisaRequestRepository;

@Service
@RequiredArgsConstructor
public class VisaRequestService {

    private final VisaRequestRepository visaRequestRepository;

    public List<Demande> findAll() {
        return visaRequestRepository.findAll();
    }

    public Optional<Demande> findById(Long id) {
        return visaRequestRepository.findById(id);
    }

    @Transactional
    public Demande save(Demande demande) {
        return visaRequestRepository.save(demande);
    }

    @Transactional
    public void deleteById(Long id) {
        visaRequestRepository.deleteById(id);
    }
}
