package app.visa.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import app.visa.entity.HistoriqueStatut;
import app.visa.entity.HistoriqueStatutId;

public interface HistoriqueStatutRepository extends JpaRepository<HistoriqueStatut, HistoriqueStatutId> {

	@Query(
		value = "SELECT * FROM historiquestatut hs WHERE hs.id_demande = :demandeId ORDER BY hs.datemodification DESC LIMIT 1",
		nativeQuery = true
	)
	Optional<HistoriqueStatut> findLatestByDemandeId(@Param("demandeId") Long demandeId);
}