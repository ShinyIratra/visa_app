package app.visa.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import app.visa.entity.Dossier;

@Repository
public interface DossierRepository extends JpaRepository<Dossier, Long> {

	@Query("SELECT d FROM Dossier d WHERE d.typeDemande.id = :typeDemandeId OR d.typeDemande.libelle = 'Commun' ORDER BY d.id")

	List<Dossier> findDossiersPourTypeDemande(@Param("typeDemandeId") Long typeDemandeId);

	List<Dossier> findByTypeDemande_IdIn(Collection<Long> typeDemandeIds);
}
