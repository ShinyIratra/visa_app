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
    private Long id;

    @Column(name = "Id_Demande", nullable = false)
    private Integer idDemande;

    @Column(name = "DateCreation", nullable = false)
    private LocalDateTime dateCreation;

    @Column(name = "Id_NouveauPasseport", nullable = false)
    private Integer idNouveauPasseport;
}