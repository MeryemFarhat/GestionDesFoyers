package tn.esprit.gestionfoyer.entities;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Universite {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long idUniversite;

    @NotBlank @Column(nullable = false, unique = true)
    private String nomUniversite;

    @NotBlank
    private String adresse;

    @OneToOne(mappedBy = "universite", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Foyer foyer;
}
