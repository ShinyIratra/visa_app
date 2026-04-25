package app.visa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import app.visa.entity.ReponseStatutVisa;
import app.visa.entity.ReponseStatutVisaId;

public interface ReponseStatutVisaRepository extends JpaRepository<ReponseStatutVisa, ReponseStatutVisaId> {
    List<ReponseStatutVisa> findByDemandeId(Integer demandeId);
}