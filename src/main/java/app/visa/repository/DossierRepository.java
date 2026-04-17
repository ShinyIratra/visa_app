package app.visa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import app.visa.entity.Dossier;

@Repository
public interface DossierRepository extends JpaRepository<Dossier, Long> {
}
