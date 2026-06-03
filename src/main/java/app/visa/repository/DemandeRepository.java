package app.visa.repository;

import java.util.Optional;

import app.visa.entity.Demande;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DemandeRepository extends JpaRepository<Demande, Integer> {
    Optional<Demande> findByNumero(String numero);
    Optional<Demande> findFirstByNumero(String numero);
}
