package app.visa.repository;

import app.visa.entity.CarteResident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CarteResidentRepository extends JpaRepository<CarteResident, Integer> {
}