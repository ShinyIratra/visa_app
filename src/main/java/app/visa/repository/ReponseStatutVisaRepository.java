package app.visa.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import app.visa.entity.ReponseStatutVisa;
import app.visa.entity.ReponseStatutVisaId;

public interface ReponseStatutVisaRepository extends JpaRepository<ReponseStatutVisa, ReponseStatutVisaId> {
}