package app.visa.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ReponseStatutVisaId implements Serializable {

    @Column(name = "id_dossier")
    private Long idDossier;

    @Column(name = "id_requetevisa")
    private Long idRequeteVisa;
}
