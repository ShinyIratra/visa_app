package app.visa.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import app.visa.entity.Demandeur;

public interface DemandeurRepository extends JpaRepository<Demandeur, Long> {
}
