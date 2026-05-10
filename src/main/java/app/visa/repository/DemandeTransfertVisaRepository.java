package app.visa.repository;

import app.visa.entity.DemandeTransfertVisa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import app.visa.entity.DemandeTransfertVisa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface DemandeTransfertVisaRepository extends JpaRepository<DemandeTransfertVisa, Integer> {
    Optional<DemandeTransfertVisa> findByNumero(String numero);
    Optional<DemandeTransfertVisa> findFirstByNumero(String numero);
    List<DemandeTransfertVisa> findByDemandePasseportDemandeurId(Integer demandeurId);
}
