package app.visa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import app.visa.entity.SituationFamiliale;

public interface SituationFamilialeRepository extends JpaRepository<SituationFamiliale, Integer> {
}
