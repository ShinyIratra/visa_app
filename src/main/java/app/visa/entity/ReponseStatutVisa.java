package app.visa.entity;

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

@Entity
@Table(name = "reponsestatutvisa")
@Getter
@Setter
@NoArgsConstructor
public class ReponseStatutVisa {

    @EmbeddedId
    private ReponseStatutVisaId id = new ReponseStatutVisaId();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("idDossier")
    @JoinColumn(name = "id_dossier", nullable = false)
    private Dossier dossier;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("idRequeteVisa")
    @JoinColumn(name = "id_requetevisa", nullable = false)
    private RequeteVisa requeteVisa;

    @Column(name = "valeur", nullable = false)
    private Boolean valeur;
}
