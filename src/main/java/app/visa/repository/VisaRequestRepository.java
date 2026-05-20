package app.visa.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import app.visa.entity.Demande;

import java.util.Optional;
import java.util.List;

import app.visa.entity.DemandeNouveauTitre;

public interface VisaRequestRepository extends JpaRepository<DemandeNouveauTitre, Integer> {
    List<Demande> findByPasseportDemandeurId(Integer demandeurId);
}