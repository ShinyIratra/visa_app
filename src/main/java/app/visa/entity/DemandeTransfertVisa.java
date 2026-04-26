package app.visa.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "demandetransfertvisa")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DemandeTransfertVisa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_demande", nullable = false)
    private Demande demande;

    @Column(name = "datecreation", nullable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_nouveaupasseport", nullable = false)
    private Passeport nouveauPasseport;

    @OneToMany(mappedBy = "transfert", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HistoriqueStatutDemandeTransfert> historiques;
}
