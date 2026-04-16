package app.visa.service;

import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.visa.entity.VisaRequest;
import app.visa.repository.VisaRequestRepository;

@Service
@RequiredArgsConstructor
public class VisaRequestService {

    private final VisaRequestRepository visaRequestRepository;

    public List<VisaRequest> findAll() {
        return visaRequestRepository.findAll();
    }

    public Optional<VisaRequest> findById(Long id) {
        return visaRequestRepository.findById(id);
    }

    @Transactional
    public VisaRequest save(VisaRequest visaRequest) {
        return visaRequestRepository.save(visaRequest);
    }

    @Transactional
    public void deleteById(Long id) {
        visaRequestRepository.deleteById(id);
    }
}
