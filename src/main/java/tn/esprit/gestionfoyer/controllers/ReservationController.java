package tn.esprit.gestionfoyer.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.util.Date;
import tn.esprit.gestionfoyer.entities.*;
import tn.esprit.gestionfoyer.services.ReservationService;
import tn.esprit.gestionfoyer.services.AcademicYear;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {
    private final ReservationService service;

    @GetMapping
    public List<Reservation> list() {
        return service.retrieveAllReservation();
    }

    @GetMapping("/{id}")
    public Reservation get(@PathVariable @NotBlank String id) {
        return service.retrieveReservation(id);
    }

    @PutMapping("/{id}")
    public Reservation update(@PathVariable @NotBlank String id, @Valid @RequestBody ReservationUpdate update) {
        Reservation body = new Reservation();
        body.setAnneeUniversitaire(update.anneeUniversitaire());
        body.setEstValide(update.estValide());
        body.setIdReservation(id);
        return service.updateReservation(body);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Reservation ajouter(@RequestParam @Positive long idBloc, @RequestParam @Positive long cinEtudiant) {
        return service.ajouterReservation(idBloc, cinEtudiant);
    }

    @DeleteMapping("/etudiants/{cinEtudiant}")
    public Reservation annuler(@PathVariable @Positive long cinEtudiant) {
        return service.annulerReservation(cinEtudiant);
    }

    @GetMapping("/par-annee-et-universite")
    public List<Reservation> parAnneeEtUniversite(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate anneeUniversite,
            @RequestParam @NotBlank String nomUniversite) {
        return service.getReservationParAnneeUniversitaireEtNomUniversite(
                AcademicYear.toDate(anneeUniversite), nomUniversite);
    }

    public record ReservationUpdate(
            @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd", timezone = "UTC")
            Date anneeUniversitaire, @NotNull Boolean estValide) {
    }
}
