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
public class Foyer {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long idFoyer;

    @NotBlank
    private String nomFoyer;

    @Positive
    private long capaciteFoyer;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "universite_id", unique = true)
    @JsonIgnore
    private Universite universite;

    @OneToMany(mappedBy = "foyer", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private List<Bloc> blocs = new ArrayList<>();
}
