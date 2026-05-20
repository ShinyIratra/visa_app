package app.visa.repository;

import org.springframework.data.jpa.repository.*;

import app.visa.entity.Demande;

import java.util.Optional;
import java.util.List;

import app.visa.entity.DemandeNouveauTitre;

public interface VisaRequestRepository extends JpaRepository<DemandeNouveauTitre, Integer> {
    List<Demande> findByPasseportDemandeurId(Integer demandeurId);
    Optional<DemandeNouveauTitre> findByNumero(String numero);
    
    // Tokony JOIN Demande p ON p.id = d.id_demande io
    // Fa lazainy fa p.id = d.id noho le DemandeNouveauTitre miExtend demande
    // Donc mitovy @ findById ihany le izy hono (noverifieko, ie marina ny teniny) 
    @Query("""
        SELECT d
        FROM DemandeNouveauTitre d
        JOIN Demande p ON p.id = d.id
        WHERE p.id = :id
    """)
    Optional<DemandeNouveauTitre> findByDemandeId(Integer id);
}