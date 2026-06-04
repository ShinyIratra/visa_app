package app.visa.repository;

import app.visa.entity.FichierDossier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FichierDossierRepository extends JpaRepository<FichierDossier, Integer> {
}
