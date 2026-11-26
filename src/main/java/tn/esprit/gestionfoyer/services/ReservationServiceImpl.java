package tn.esprit.gestionfoyer.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.gestionfoyer.entities.*;
import tn.esprit.gestionfoyer.repositories.*;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import static tn.esprit.gestionfoyer.exceptions.FoyerException.*;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationServiceImpl implements ReservationService {
    private final ReservationRepository repository;
    private final EtudiantRepository etudiantRepository;
    private final BlocRepository blocRepository;
    private final ChambreRepository chambreRepository;
    private final UniversiteRepository universiteRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Reservation> retrieveAllReservation() {
        return repository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Reservation retrieveReservation(String idReservation) {
        return repository.findById(idReservation).orElseThrow(() -> missing("Reservation", idReservation));
    }

    @Override
    public Reservation updateReservation(Reservation res) {
        var chambre = repository.findChambreId(res.getIdReservation()).map(this::lockChambre);
        Reservation reservation = retrieveReservation(res.getIdReservation());
        if (res.getAnneeUniversitaire() != null && !reservation.getAnneeUniversitaire().equals(res.getAnneeUniversitaire())) {
            throw conflict("L'annee universitaire fait partie de l'identifiant");
        }
        if (chambre.isEmpty() && reservation.isEstValide()) {
            throw conflict("La reservation vient d'etre reaffectee. Reessayez la modification");
        }
        if (res.isEstValide() && !reservation.isEstValide()) {
            throw conflict("Une reservation vide doit etre reactivee par une nouvelle inscription");
        }
        if (!res.isEstValide() && reservation.isEstValide()) {
            if (reservation.getEtudiants().size() > 1) {
                throw conflict("Annulez les places individuellement pour une chambre partagee");
            }
            Chambre room = chambre.orElseThrow(() -> missing("Chambre de la reservation", res.getIdReservation()));
            if (reservation.getEtudiants().isEmpty()) {
                detachEmptyReservation(room, reservation);
            } else {
                releasePlace(room, reservation, reservation.getEtudiants().iterator().next());
            }
        }
        return reservation;
    }

    @Override
    public Reservation ajouterReservation(long idBloc, long cinEtudiant) {
        Etudiant etudiant = etudiantRepository.findByCin(cinEtudiant)
                .orElseThrow(() -> missing("Etudiant CIN", cinEtudiant));
        Bloc bloc = blocRepository.lockById(idBloc).orElseThrow(() -> missing("Bloc", idBloc));
        LocalDate start = AcademicYear.current();
        Date annee = AcademicYear.toDate(start);
        Date fin = AcademicYear.toDate(start.plusYears(1));
        if (repository.findActiveByEtudiant(etudiant.getIdEtudiant(), annee, fin).isPresent()) {
            throw conflict("Cet etudiant a deja une reservation active cette annee");
        }

        for (Chambre chambre : chambreRepository.findByBlocIdBlocOrderByIdChambre(idBloc)) {
            Reservation reservation = chambre.getReservations().stream()
                    .filter(r -> !r.getAnneeUniversitaire().before(annee) && r.getAnneeUniversitaire().before(fin))
                    .findFirst().orElse(null);
            if (reservation == null) {
                String id = chambre.getNumeroChambre() + "-" + bloc.getNomBloc()
                        + "-" + start.getYear() + "-" + (start.getYear() + 1);
                reservation = repository.findById(id).orElseGet(() -> {
                    Reservation nouvelle = new Reservation();
                    nouvelle.setIdReservation(id);
                    nouvelle.setAnneeUniversitaire(annee);
                    return repository.save(nouvelle);
                });
                chambre.getReservations().add(reservation);
            }
            if (reservation.getEtudiants().size() < chambre.getTypeC().capacity()) {
                reservation.setEstValide(true);
                reservation.getEtudiants().add(etudiant);
                etudiant.getReservations().add(reservation);
                return reservation;
            }
        }
        throw conflict("Aucune place disponible dans ce bloc");
    }

    @Override
    public Reservation annulerReservation(long cinEtudiant) {
        Etudiant etudiant = etudiantRepository.findByCin(cinEtudiant)
                .orElseThrow(() -> missing("Etudiant CIN", cinEtudiant));
        LocalDate start = AcademicYear.current();
        String id = repository.findActiveByEtudiant(etudiant.getIdEtudiant(),
                        AcademicYear.toDate(start), AcademicYear.toDate(start.plusYears(1)))
                .orElseThrow(() -> missing("Reservation active pour CIN", cinEtudiant));
        Chambre chambre = repository.findChambreId(id).map(this::lockChambre)
                .orElseThrow(() -> missing("Reservation active pour CIN", cinEtudiant));
        Reservation reservation = retrieveReservation(id);
        if (!reservation.getEtudiants().contains(etudiant)) {
            throw missing("Reservation active pour CIN", cinEtudiant);
        }
        releasePlace(chambre, reservation, etudiant);
        return reservation;
    }

    private void releasePlace(Chambre chambre, Reservation reservation, Etudiant etudiant) {
        reservation.getEtudiants().remove(etudiant);
        etudiant.getReservations().remove(reservation);
        if (reservation.getEtudiants().isEmpty()) {
            detachEmptyReservation(chambre, reservation);
        }
    }

    private void detachEmptyReservation(Chambre chambre, Reservation reservation) {
        reservation.setEstValide(false);
        chambre.getReservations().remove(reservation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reservation> getReservationParAnneeUniversitaireEtNomUniversite(Date anneeUniversite, String nomUniversite) {
        universiteRepository.findByNomUniversite(nomUniversite)
                .orElseThrow(() -> missing("Universite", nomUniversite));
        LocalDate start = AcademicYear.start(AcademicYear.toLocalDate(anneeUniversite));
        return repository.findByAnneeEtUniversite(
                AcademicYear.toDate(start), AcademicYear.toDate(start.plusYears(1)), nomUniversite);
    }

    private Chambre lockChambre(long idChambre) {
        return chambreRepository.lockById(idChambre).orElseThrow(() -> missing("Chambre", idChambre));
    }
}
