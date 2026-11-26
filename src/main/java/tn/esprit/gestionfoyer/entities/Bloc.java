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
public class Bloc {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long idBloc;

    @NotBlank @Pattern(regexp = "[\\p{L}\\p{N} _-]+") @Column(nullable = false, unique = true)
    private String nomBloc;

    @Positive
    private long capaciteBloc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "foyer_id")
    @JsonIgnore
    private Foyer foyer;

    @OneToMany(mappedBy = "bloc", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private List<Chambre> chambres = new ArrayList<>();
}
