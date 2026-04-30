package app.visa.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "demandeduplicata")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DemandeDuplicata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_demande", nullable = false)
    private Demande demande;

    @Column(name = "datecreation", nullable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();

    @Column(name = "numero", unique = true)
    private String numero;

    @OneToMany(mappedBy = "duplicata", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HistoriqueStatutDemandeDuplicata> historiques;
}
