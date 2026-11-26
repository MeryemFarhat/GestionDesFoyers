package tn.esprit.gestionfoyer.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import tn.esprit.gestionfoyer.entities.*;
import tn.esprit.gestionfoyer.services.FoyerService;

import java.util.List;

@RestController
@RequestMapping("/api/foyers")
@RequiredArgsConstructor
public class FoyerController {
    private final FoyerService service;

    @GetMapping
    public List<Foyer> list() {
        return service.retrieveAllFoyers();
    }

    @GetMapping("/{id}")
    public Foyer get(@PathVariable @Positive long id) {
        return service.retrieveFoyer(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Foyer create(@Valid @RequestBody Foyer body) {
        return service.addFoyer(body);
    }

    @PutMapping("/{id}")
    public Foyer update(@PathVariable @Positive long id, @Valid @RequestBody Foyer body) {
        body.setIdFoyer(id);
        return service.updateFoyer(body);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Positive long id) {
        service.removeFoyer(id);
    }

    @PostMapping("/universite/{idUniversite}")
    @ResponseStatus(HttpStatus.CREATED)
    public Foyer ajouterEtAffecter(@PathVariable @Positive long idUniversite, @Valid @RequestBody Foyer foyer) {
        return service.ajouterFoyerEtAffecterAUniversite(foyer, idUniversite);
    }
}
