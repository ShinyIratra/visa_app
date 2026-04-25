package app.visa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import app.visa.entity.Nationalite;

public interface NationaliteRepository extends JpaRepository<Nationalite, Integer> {
}
