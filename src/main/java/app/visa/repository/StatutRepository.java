package app.visa.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import app.visa.entity.Statut;

public interface StatutRepository extends JpaRepository<Statut, Integer> {

    Optional<Statut> findByOrdre(Integer ordre);

    @Query("SELECT s FROM Statut s WHERE s.libelle = :libelle")
    Optional<Statut> findByLibelle(@Param("libelle") String libelle);
}