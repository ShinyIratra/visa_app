package app.visa.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name = "historiquestatut")
@Getter
@Setter
@NoArgsConstructor
public class HistoriqueStatut {

    @EmbeddedId
    private HistoriqueStatutId id = new HistoriqueStatutId();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("idDemande")
    @JoinColumn(name = "id_demande", nullable = false)
    private Demande demande;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("idStatut")
    @JoinColumn(name = "id_statut", nullable = false)
    private Statut statut;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    @Column(name = "datemodification", nullable = false)
    private LocalDateTime dateModification;
}
