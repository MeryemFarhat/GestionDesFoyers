package tn.esprit.gestionfoyer.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import tn.esprit.gestionfoyer.entities.*;
import tn.esprit.gestionfoyer.services.UniversiteService;

import java.util.List;

@RestController
@RequestMapping("/api/universites")
@RequiredArgsConstructor
public class UniversiteController {
    private final UniversiteService service;

    @GetMapping
    public List<Universite> list() {
        return service.retrieveAllUniversities();
    }

    @GetMapping("/{id}")
    public Universite get(@PathVariable @Positive long id) {
        return service.retrieveUniversite(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Universite create(@Valid @RequestBody Universite body) {
        return service.addUniversite(body);
    }

    @PutMapping("/{id}")
    public Universite update(@PathVariable @Positive long id, @Valid @RequestBody Universite body) {
        body.setIdUniversite(id);
        return service.updateUniversite(body);
    }

    @PutMapping("/affectation")
    public Universite affecter(@RequestParam @Positive long idFoyer, @RequestParam @NotBlank String nomUniversite) {
        return service.affecterFoyerAUniversite(idFoyer, nomUniversite);
    }

    @DeleteMapping("/{id}/foyer")
    public Universite desaffecter(@PathVariable @Positive long id) {
        return service.desaffecterFoyerAUniversite(id);
    }
}
