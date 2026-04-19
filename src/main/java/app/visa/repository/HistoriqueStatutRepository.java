package app.visa.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import app.visa.entity.HistoriqueStatut;
import app.visa.entity.HistoriqueStatutId;

public interface HistoriqueStatutRepository extends JpaRepository<HistoriqueStatut, HistoriqueStatutId> {
}