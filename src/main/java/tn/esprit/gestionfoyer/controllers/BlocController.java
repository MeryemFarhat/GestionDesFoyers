package tn.esprit.gestionfoyer.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import tn.esprit.gestionfoyer.entities.*;
import tn.esprit.gestionfoyer.services.BlocService;

import java.util.List;

@RestController
@RequestMapping("/api/blocs")
@RequiredArgsConstructor
public class BlocController {
    private final BlocService service;

    @GetMapping
    public List<Bloc> list() {
        return service.retrieveBlocs();
    }

    @GetMapping("/{id}")
    public Bloc get(@PathVariable @Positive long id) {
        return service.retrieveBloc(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Bloc create(@Valid @RequestBody Bloc body) {
        return service.addBloc(body);
    }

    @PutMapping("/{id}")
    public Bloc update(@PathVariable @Positive long id, @Valid @RequestBody Bloc body) {
        body.setIdBloc(id);
        return service.updateBloc(body);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Positive long id) {
        service.removeBloc(id);
    }

    @PutMapping("/{id}/chambres")
    public Bloc affecterChambres(@PathVariable @Positive long id,
                                 @RequestBody @NotEmpty List<@NotNull @Positive Long> numeros) {
        return service.affecterChambresABloc(numeros, id);
    }

    @PutMapping("/{id}/foyer/{idFoyer}")
    public Bloc affecterFoyer(@PathVariable @Positive long id, @PathVariable @Positive long idFoyer) {
        return service.affecterBlocAFoyer(id, idFoyer);
    }

    @DeleteMapping("/{id}/foyer")
    public Bloc desaffecterFoyer(@PathVariable @Positive long id) {
        return service.desaffecterBlocDuFoyer(id);
    }
}
