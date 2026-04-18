package app.visa.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "passeport")
@Getter
@Setter
@NoArgsConstructor
public class Passeport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "numero", nullable = false, length = 50)
    private String numero;

    @Column(name = "datedelivrance", nullable = false)
    private LocalDateTime dateDelivrance;

    @Column(name = "dateexpiration", nullable = false)
    private LocalDateTime dateExpiration;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_demandeur", nullable = false)
    private Demandeur demandeur;
}
