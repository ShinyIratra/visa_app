package app.visa.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import app.visa.entity.Categorie;

public interface CategorieRepository extends JpaRepository<Categorie, Integer> {

	@Query("SELECT c FROM Categorie c WHERE c.libelle = :libelle")

	Optional<Categorie> findByLibelle(@Param("libelle") String libelle);
}
