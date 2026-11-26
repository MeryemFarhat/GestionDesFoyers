package tn.esprit.gestionfoyer.entities;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Chambre {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long idChambre;

    @Positive @Column(nullable = false, unique = true)
    private long numeroChambre;

    @NotNull @Enumerated(EnumType.STRING)
    private TypeChambre typeC;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bloc_id")
    @JsonIgnore
    private Bloc bloc;

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "chambre_reservation", joinColumns = @JoinColumn(name = "chambre_id"),
            inverseJoinColumns = @JoinColumn(name = "reservation_id", unique = true))
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private List<Reservation> reservations = new ArrayList<>();
}
