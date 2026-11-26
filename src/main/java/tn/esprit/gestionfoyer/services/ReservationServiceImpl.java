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
        lockChambre(res.getIdReservation());
        Reservation reservation = retrieveReservation(res.getIdReservation());
        if (res.getAnneeUniversitaire() != null && !reservation.getAnneeUniversitaire().equals(res.getAnneeUniversitaire())) {
            throw conflict("L'annee universitaire fait partie de l'identifiant");
        }
        if (res.isEstValide() && !reservation.isEstValide()) {
            throw conflict("Une reservation vide doit etre reactivee par une nouvelle inscription");
        }
        if (!res.isEstValide() && reservation.isEstValide()) {
            if (reservation.getEtudiants().size() > 1) {
                throw conflict("Annulez les places individuellement pour une chambre partagee");
            }
            reservation.getEtudiants().stream().findFirst().ifPresent(etudiant -> releasePlace(reservation, etudiant));
            reservation.setEstValide(false);
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
        if (repository.findActiveByEtudiant(etudiant.getIdEtudiant(), annee).isPresent()) {
            throw conflict("Cet etudiant a deja une reservation active cette annee");
        }

        for (Chambre chambre : chambreRepository.findByBlocIdBlocOrderByIdChambre(idBloc)) {
            Reservation reservation = chambre.getReservations().stream()
                    .filter(r -> r.getAnneeUniversitaire().equals(annee)).findFirst().orElse(null);
            if (reservation == null) {
                reservation = new Reservation();
                reservation.setIdReservation(chambre.getNumeroChambre() + "-" + bloc.getNomBloc()
                        + "-" + start.getYear() + "-" + (start.getYear() + 1));
                reservation.setAnneeUniversitaire(annee);
                reservation = repository.save(reservation);
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
        String id = repository.findActiveByEtudiant(etudiant.getIdEtudiant(), AcademicYear.toDate(AcademicYear.current()))
                .orElseThrow(() -> missing("Reservation active pour CIN", cinEtudiant));
        lockChambre(id);
        Reservation reservation = retrieveReservation(id);
        if (!reservation.getEtudiants().contains(etudiant)) {
            throw missing("Reservation active pour CIN", cinEtudiant);
        }
        releasePlace(reservation, etudiant);
        return reservation;
    }

    private void releasePlace(Reservation reservation, Etudiant etudiant) {
        reservation.getEtudiants().remove(etudiant);
        etudiant.getReservations().remove(reservation);
        reservation.setEstValide(!reservation.getEtudiants().isEmpty());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reservation> getReservationParAnneeUniversitaireEtNomUniversite(Date anneeUniversite, String nomUniversite) {
        universiteRepository.findByNomUniversite(nomUniversite)
                .orElseThrow(() -> missing("Universite", nomUniversite));
        Date start = AcademicYear.toDate(AcademicYear.start(AcademicYear.toLocalDate(anneeUniversite)));
        return repository.findByAnneeEtUniversite(start, nomUniversite);
    }

    private void lockChambre(String reservationId) {
        long idChambre = repository.findChambreId(reservationId)
                .orElseThrow(() -> missing("Reservation", reservationId));
        chambreRepository.lockById(idChambre).orElseThrow(() -> missing("Chambre", idChambre));
    }
}
