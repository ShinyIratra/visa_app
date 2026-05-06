package app.visa.repository;

import app.visa.entity.DemandeDuplicata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import app.visa.entity.DemandeDuplicata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface DemandeDuplicataRepository extends JpaRepository<DemandeDuplicata, Integer> {
    Optional<DemandeDuplicata> findByNumero(String numero);
    Optional<DemandeDuplicata> findFirstByNumero(String numero);
    List<DemandeDuplicata> findByDemandePasseportDemandeurId(Integer demandeurId);
}
