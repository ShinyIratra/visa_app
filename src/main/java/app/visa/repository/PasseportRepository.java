package app.visa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

import app.visa.entity.Passeport;

public interface PasseportRepository extends JpaRepository<Passeport, Integer> {
    Optional<Passeport> findByNumero(String numero);
}
