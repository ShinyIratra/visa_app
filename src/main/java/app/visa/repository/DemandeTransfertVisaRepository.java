package app.visa.repository;

import app.visa.entity.DemandeTransfertVisa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import app.visa.entity.DemandeTransfertVisa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.List;

@Repository
public interface DemandeTransfertVisaRepository extends JpaRepository<DemandeTransfertVisa, Integer> {
    Optional<DemandeTransfertVisa> findByNumero(String numero);
    Optional<DemandeTransfertVisa> findFirstByNumero(String numero);

    @Query("SELECT t FROM DemandeTransfertVisa t WHERE t.nouveauPasseport.demandeur.id = :demandeurId")
    List<DemandeTransfertVisa> findByDemandeurId(@Param("demandeurId") Integer demandeurId);
}
