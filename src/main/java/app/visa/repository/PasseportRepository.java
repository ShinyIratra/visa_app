package app.visa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.List;

import app.visa.entity.Passeport;

public interface PasseportRepository extends JpaRepository<Passeport, Integer> {
    Optional<Passeport> findByNumero(String numero);
    List<Passeport> findByDemandeurId(Integer demandeurId);

    @Query(value = "SELECT p.* FROM passeport p JOIN visapasseport vp ON p.id = vp.id_passeport WHERE p.id_demandeur = :demandeurId ORDER BY vp.datecreation DESC LIMIT 1", nativeQuery = true)
    Optional<Passeport> findActuelByVisapasseport(@Param("demandeurId") Integer demandeurId);
}
