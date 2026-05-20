package app.visa.repository;

import app.visa.entity.DemandeDuplicata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import app.visa.entity.DemandeDuplicata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.List;

@Repository
public interface DemandeDuplicataRepository extends JpaRepository<DemandeDuplicata, Integer> {
    Optional<DemandeDuplicata> findByNumero(String numero);
    Optional<DemandeDuplicata> findFirstByNumero(String numero);

    @Query("SELECT d FROM DemandeDuplicata d WHERE " +
           "EXISTS (SELECT nt FROM DemandeNouveauTitre nt WHERE nt.id = d.demandeOrigine.id AND nt.passeport.demandeur.id = :demandeurId) OR " +
           "EXISTS (SELECT tr FROM DemandeTransfertVisa tr WHERE tr.id = d.demandeOrigine.id AND tr.nouveauPasseport.demandeur.id = :demandeurId)")
    List<DemandeDuplicata> findByDemandeurId(@Param("demandeurId") Integer demandeurId);
}
