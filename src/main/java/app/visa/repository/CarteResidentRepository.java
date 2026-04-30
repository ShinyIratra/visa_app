package app.visa.repository;

import app.visa.entity.CarteResident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CarteResidentRepository extends JpaRepository<CarteResident, Integer> {
    Optional<CarteResident> findFirstByPasseportIdOrderByDateCreationAsc(Integer passeportId);
    java.util.List<CarteResident> findByPasseportId(Integer passeportId);
}
