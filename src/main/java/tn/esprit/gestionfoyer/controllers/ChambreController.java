package tn.esprit.gestionfoyer.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import tn.esprit.gestionfoyer.entities.*;
import tn.esprit.gestionfoyer.services.ChambreService;

import java.util.List;

@RestController
@RequestMapping("/api/chambres")
@RequiredArgsConstructor
public class ChambreController {
    private final ChambreService service;

    @GetMapping
    public List<Chambre> list() {
        return service.retrieveAllChambres();
    }

    @GetMapping("/{id}")
    public Chambre get(@PathVariable @Positive long id) {
        return service.retrieveChambre(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Chambre create(@Valid @RequestBody Chambre body) {
        return service.addChambre(body);
    }

    @PutMapping("/{id}")
    public Chambre update(@PathVariable @Positive long id, @Valid @RequestBody Chambre body) {
        body.setIdChambre(id);
        return service.updateChambre(body);
    }

    @GetMapping("/par-universite")
    public List<Chambre> parUniversite(@RequestParam @NotBlank String nomUniversite) {
        return service.getChambresParNomUniversite(nomUniversite);
    }

    @GetMapping("/par-bloc-et-type")
    public List<Chambre> parBlocEtType(@RequestParam @Positive long idBloc, @RequestParam TypeChambre type) {
        return service.getChambresParBlocEtType(idBloc, type);
    }

    @GetMapping("/par-bloc-et-type/jpql")
    public List<Chambre> parBlocEtTypeJpql(@RequestParam @Positive long idBloc, @RequestParam TypeChambre type) {
        return service.getChambresParBlocEtTypeJpql(idBloc, type);
    }

    @Operation(summary = "Rechercher les chambres non réservées",
            description = "Retourne les chambres du type demandé sans réservation active pendant l'année universitaire actuelle. Le nom demandé est celui de l'université.")
    @GetMapping("/non-reservees")
    public List<Chambre> nonReservees(
            @Parameter(description = "Nom unique de l'université", example = "Universite Demo")
            @RequestParam @NotBlank String nomUniversite,
            @Parameter(description = "Type de chambre", example = "DOUBLE") @RequestParam TypeChambre type) {
        return service.getChambresNonReserveParNomUniversiteEtTypeChambre(nomUniversite, type);
    }
}
