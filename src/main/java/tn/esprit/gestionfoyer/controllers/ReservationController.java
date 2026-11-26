package tn.esprit.gestionfoyer.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

    @Operation(summary = "Ajouter une réservation",
            description = "Attribue une place pour l'année universitaire actuelle. Renvoie 409 si le bloc est complet ou si l'étudiant a déjà une réservation active.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Reservation ajouter(
            @Parameter(description = "Identifiant du bloc", example = "1") @RequestParam @Positive long idBloc,
            @Parameter(description = "CIN de l'étudiant", example = "12345678") @RequestParam @Positive long cinEtudiant) {
        return service.ajouterReservation(idBloc, cinEtudiant);
    }

    @Operation(summary = "Annuler la place d'un étudiant",
            description = "Libère la place de l'étudiant pour l'année en cours. Au dernier départ, invalide la réservation et détache la chambre. Les autres occupants conservent leur place.")
    @DeleteMapping("/etudiants/{cinEtudiant}")
    public Reservation annuler(
            @Parameter(description = "CIN de l'étudiant", example = "12345678") @PathVariable @Positive long cinEtudiant) {
        return service.annulerReservation(cinEtudiant);
    }

    @Operation(summary = "Rechercher les réservations d'une université par année",
            description = "Utilise une date de l'année universitaire du 1er septembre inclus au 1er septembre suivant exclu. Seules les réservations rattachées à une chambre de l'université sont retournées.")
    @GetMapping("/par-annee-et-universite")
    public List<Reservation> parAnneeEtUniversite(
            @Parameter(description = "Date comprise dans l'année universitaire", example = "2026-11-26")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate anneeUniversite,
            @Parameter(description = "Nom unique de l'université", example = "Universite Demo")
            @RequestParam @NotBlank String nomUniversite) {
        return service.getReservationParAnneeUniversitaireEtNomUniversite(
                AcademicYear.toDate(anneeUniversite), nomUniversite);
    }

    public record ReservationUpdate(
            @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd", timezone = "UTC")
            Date anneeUniversitaire, @NotNull Boolean estValide) {
    }
}
