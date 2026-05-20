package app.visa.entity;

import java.time.LocalDateTime;
import java.util.*;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name = "demande")
@Inheritance(strategy = InheritanceType.JOINED)
public class Demande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "numero", unique = true)
    private String numero;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    @Column(name = "datecreation", nullable = false)
    private LocalDateTime dateCreation;

    /* Nouveau titre - Transfert de visa - Duplicata */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_categorie", nullable = false)
    private Categorie categorie;

    @OneToMany(mappedBy = "demande", fetch = FetchType.LAZY)
    private List<HistoriqueStatut> historiques;

    public Demande() {}

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public Categorie getCategorie() {
        return categorie;
    }

    public void setCategorie(Categorie categorie) {
        this.categorie = categorie;
    }

    public List<HistoriqueStatut> getHistoriques() {
        return historiques;
    }

    public void setHistoriques(List<HistoriqueStatut> historiques) {
        this.historiques = historiques;
    }
}
