package app.visa.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

import lombok.EqualsAndHashCode;

@Entity
@Table(name = "demandetransfertvisa")
@PrimaryKeyJoinColumn(name = "id_demande")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class DemandeTransfertVisa extends Demande {

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_demandeorigine", nullable = false)
    private Demande demandeOrigine;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_nouveaupasseport", nullable = false)
    private Passeport nouveauPasseport;
}
