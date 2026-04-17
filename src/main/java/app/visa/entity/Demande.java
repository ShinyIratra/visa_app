package app.visa.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name = "demande")
@Getter
@Setter
@NoArgsConstructor
public class Demande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    @Column(name = "datecreation", nullable = false)
    private LocalDateTime dateCreation;

    @Column(name = "id_typedemande", nullable = false)
    private Long idTypeDemande;

    @Column(name = "id_passeport", nullable = false)
    private Long idPasseport;

    @Column(name = "id_categorie", nullable = false)
    private Long idCategorie;
}
