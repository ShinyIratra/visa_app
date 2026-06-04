package app.visa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;
import java.util.ArrayList;

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
    @MapsId("idDemande")
    @JoinColumn(name = "id_demande", nullable = false)
    private Demande demande;

    @Column(name = "valeur", nullable = false)
    private Boolean valeur;

    @OneToMany(mappedBy = "reponseStatutVisa", fetch = FetchType.LAZY)
    private List<FichierDossier> fichiers = new ArrayList<>();
}
