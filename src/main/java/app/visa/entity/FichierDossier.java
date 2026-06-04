package app.visa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "fichierdossier")
@Getter
@Setter
@NoArgsConstructor
public class FichierDossier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
        @JoinColumn(name = "id_dossier", referencedColumnName = "id_dossier", nullable = false),
        @JoinColumn(name = "id_demande", referencedColumnName = "id_demande", nullable = false)
    })
    private ReponseStatutVisa reponseStatutVisa;

    @Column(name = "cheminfichier", nullable = false)
    private String cheminFichier;

    @Column(name = "datemodification", nullable = false, insertable = false, updatable = false)
    private LocalDateTime dateModification;
}
