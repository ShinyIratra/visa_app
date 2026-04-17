package app.visa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import app.visa.entity.Categorie;

public interface CategorieRepository extends JpaRepository<Categorie, Long> {
}
