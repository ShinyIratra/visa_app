package app.visa.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "DemandeTransfertVisa")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DemandeTransfertVisa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "Id_Demande", nullable = false)
    private Demande demande;

    @Column(name = "DateCreation", nullable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();

    @ManyToOne(optional = false)
    @JoinColumn(name = "Id_NouveauPasseport", nullable = false)
    private Passeport nouveauPasseport;

    // TODO: Asina id visa ve ? 
}