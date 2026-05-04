package app.visa.dto.demande;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DemandeDto {
    private String type;
    private String numero;
    private LocalDateTime dateCreation;
    private String statut;
    private List<HistoriqueDto> historique;
}
