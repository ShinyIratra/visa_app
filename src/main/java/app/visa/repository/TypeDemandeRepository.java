package app.visa.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import app.visa.entity.TypeDemande;

public interface TypeDemandeRepository extends JpaRepository<TypeDemande, Long> {

    @Query("SELECT t FROM TypeDemande t WHERE t.libelle = :libelle")

	Optional<TypeDemande> findByLibelle(@Param("libelle") String libelle);
}
