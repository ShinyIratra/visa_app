package app.visa.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "HistoriqueStatutDemandeTransfert")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistoriqueStatutDemandeTransfert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "Id_Transfert", nullable = false)
    private DemandeTransfertVisa transfert;

    @ManyToOne(optional = false)
    @JoinColumn(name = "Id_Statut", nullable = false)
    private Statut statut;

    @Column(name = "DateModification", nullable = false)
    private LocalDateTime dateModification = LocalDateTime.now();
}
