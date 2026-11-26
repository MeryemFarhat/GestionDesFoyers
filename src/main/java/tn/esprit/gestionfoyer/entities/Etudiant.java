package tn.esprit.gestionfoyer.entities;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import tn.esprit.gestionfoyer.config.CalendarDateConverter;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Etudiant {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long idEtudiant;

    @NotBlank
    private String nomEt;

    @NotBlank
    private String prenomEt;

    @Positive @Column(nullable = false, unique = true)
    private long cin;

    @NotBlank
    private String ecole;

    @NotNull @Past @Convert(converter = CalendarDateConverter.class)
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "UTC")
    private Date dateNaissance;

    @ManyToMany(mappedBy = "etudiants")
    @JsonIgnore
    private List<Reservation> reservations = new ArrayList<>();
}
