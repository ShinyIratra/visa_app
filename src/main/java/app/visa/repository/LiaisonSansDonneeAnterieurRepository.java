package app.visa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import app.visa.entity.LiaisonSansDonneeAnterieur;
import java.util.Optional;

@Repository
public interface LiaisonSansDonneeAnterieurRepository extends JpaRepository<LiaisonSansDonneeAnterieur, Integer> {
    Optional<LiaisonSansDonneeAnterieur> findTopByOrderByIdentifiantDesc();
}
