package app.visa.entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name = "visa")
@Getter
@Setter
@NoArgsConstructor
public class Visa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    @Column(name = "datecreation", nullable = false)
    private LocalDateTime dateCreation;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "visapasseport",
        joinColumns = @JoinColumn(name = "id_visa"),
        inverseJoinColumns = @JoinColumn(name = "id_passeport")
    )
    private Set<Passeport> passeports = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_demande", nullable = false)
    private Demande demande;

    @Column(name = "numero", unique = true)
    private String numero;

    @Column(name = "datedebut")
    private LocalDateTime dateDebut;

    @Column(name = "dateexpiration")
    private LocalDateTime dateExpiration;
}
