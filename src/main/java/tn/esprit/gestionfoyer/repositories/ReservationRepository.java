package tn.esprit.gestionfoyer.repositories;

import org.springframework.data.jpa.repository.*;
import tn.esprit.gestionfoyer.entities.Reservation;

import java.util.*;

public interface ReservationRepository extends JpaRepository<Reservation, String> {
    @Query("""
            select r from Chambre c join c.reservations r
            where c.bloc.foyer.universite.nomUniversite = :nom and r.anneeUniversitaire >= :debut and r.anneeUniversitaire < :fin
            order by r.idReservation
            """)
    List<Reservation> findByAnneeEtUniversite(Date debut, Date fin, String nom);

    @Query("""
            select r.idReservation from Reservation r join r.etudiants e
            where e.idEtudiant = :id and r.anneeUniversitaire >= :debut and r.anneeUniversitaire < :fin and r.estValide = true
            """)
    Optional<String> findActiveByEtudiant(long id, Date debut, Date fin);

    @Query("select c.idChambre from Chambre c join c.reservations r where r.idReservation = :id")
    Optional<Long> findChambreId(String id);
}
