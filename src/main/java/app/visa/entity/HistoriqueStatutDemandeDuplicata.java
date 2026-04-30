package app.visa.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "historiquestatutdemandeduplicata")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistoriqueStatutDemandeDuplicata {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_duplicata", nullable = false)
    private DemandeDuplicata duplicata;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_statut", nullable = false)
    private Statut statut;

    @Column(name = "datemodification", nullable = false)
    private LocalDateTime dateModification = LocalDateTime.now();
}
