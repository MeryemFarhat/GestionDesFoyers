package tn.esprit.gestionfoyer.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import tn.esprit.gestionfoyer.entities.*;
import tn.esprit.gestionfoyer.services.EtudiantService;

import java.util.List;

@RestController
@RequestMapping("/api/etudiants")
@RequiredArgsConstructor
public class EtudiantController {
    private final EtudiantService service;

    @GetMapping
    public List<Etudiant> list() {
        return service.retrieveAllEtudiants();
    }

    @GetMapping("/{id}")
    public Etudiant get(@PathVariable @Positive long id) {
        return service.retrieveEtudiant(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public List<Etudiant> create(@RequestBody @NotEmpty List<@NotNull @Valid Etudiant> body) {
        return service.addEtudiants(body);
    }

    @PutMapping("/{id}")
    public Etudiant update(@PathVariable @Positive long id, @Valid @RequestBody Etudiant body) {
        body.setIdEtudiant(id);
        return service.updateEtudiant(body);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Positive long id) {
        service.removeEtudiant(id);
    }
}
