package app.visa.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import app.visa.entity.Demande;
import java.util.Optional;
import java.util.List;

public interface VisaRequestRepository extends JpaRepository<Demande, Integer> {
    Optional<Demande> findByNumero(String numero);
    Optional<Demande> findFirstByNumero(String numero);
    List<Demande> findByPasseportDemandeurId(Integer demandeurId);
}