package app.visa.repository;

import app.visa.entity.DemandeTransfertVisa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DemandeTransfertVisaRepository extends JpaRepository<DemandeTransfertVisa, Long> {
}
