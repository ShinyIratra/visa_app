package app.visa.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "demandeur")
@Getter
@Setter
@NoArgsConstructor
public class Demandeur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "nom", nullable = false, length = 100)
    private String nom;

    @Column(name = "prenom", nullable = false, length = 100)
    private String prenom;

    @Column(name = "nomjeunefille", length = 100)
    private String nomJeuneFille;

    @Column(name = "email", length = 200)
    private String email;

    @Column(name = "numtel", nullable = false, length = 50)
    private String numTel;

    @Column(name = "datenaissance", nullable = false)
    private LocalDate dateNaissance;

    @Column(name = "adresse", nullable = false, length = 255)
    private String adresse;

    @Column(name = "id_nationalite", nullable = false)
    private Long idNationalite;

    @Column(name = "id_situationfamiliale", nullable = false)
    private Long idSituationFamiliale;
}
