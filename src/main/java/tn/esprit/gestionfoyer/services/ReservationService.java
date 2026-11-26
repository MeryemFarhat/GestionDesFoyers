package tn.esprit.gestionfoyer.services;

import tn.esprit.gestionfoyer.entities.*;
import java.util.*;

public interface ReservationService {
    List<Reservation> retrieveAllReservation();

    Reservation updateReservation(Reservation res);

    Reservation retrieveReservation(String idReservation);

    Reservation ajouterReservation(long idBloc, long cinEtudiant);

    Reservation annulerReservation(long cinEtudiant);

    List<Reservation> getReservationParAnneeUniversitaireEtNomUniversite(Date anneeUniversite, String nomUniversite);
}
