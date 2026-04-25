
package app.visa.service;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import app.visa.repository.CategorieRepository;
import app.visa.repository.PasseportRepository;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CategorieService {

	private final CategorieRepository categorieRepository;

    public Integer getCategorieByLibelle(String libelle) {
        return categorieRepository.findByLibelle(libelle)
            .orElseThrow(() -> new IllegalArgumentException("Catégorie '" + libelle + "' introuvable."))
            .getId();
    }
}
