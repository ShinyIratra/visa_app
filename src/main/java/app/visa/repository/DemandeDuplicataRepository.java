package app.visa.repository;

import app.visa.entity.DemandeDuplicata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DemandeDuplicataRepository extends JpaRepository<DemandeDuplicata, Integer> {
}
