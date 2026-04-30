package app.visa.repository;

import app.visa.entity.HistoriqueStatutDemandeDuplicata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HistoriqueStatutDemandeDuplicataRepository extends JpaRepository<HistoriqueStatutDemandeDuplicata, Integer> {
}
