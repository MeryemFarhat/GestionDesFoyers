package tn.esprit.gestionfoyer.entities;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import tn.esprit.gestionfoyer.config.CalendarDateConverter;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Reservation {
    @Id
    private String idReservation;

    @Convert(converter = CalendarDateConverter.class) @Column(nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "UTC")
    private Date anneeUniversitaire;

    private boolean estValide;

    @ManyToMany
    @JoinTable(name = "reservation_etudiant", joinColumns = @JoinColumn(name = "reservation_id"),
            inverseJoinColumns = @JoinColumn(name = "etudiant_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"reservation_id", "etudiant_id"}))
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Set<Etudiant> etudiants = new LinkedHashSet<>();
}
