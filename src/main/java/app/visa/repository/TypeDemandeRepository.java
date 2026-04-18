package app.visa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import app.visa.entity.TypeDemande;

public interface TypeDemandeRepository extends JpaRepository<TypeDemande, Long> {
}
