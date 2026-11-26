package tn.esprit.gestionfoyer.repositories;

import org.springframework.data.jpa.repository.*;
import jakarta.persistence.LockModeType;
import tn.esprit.gestionfoyer.entities.Chambre;
import tn.esprit.gestionfoyer.entities.TypeChambre;

import java.util.*;

public interface ChambreRepository extends JpaRepository<Chambre, Long> {
    List<Chambre> findByBlocFoyerUniversiteNomUniversiteOrderByNumeroChambre(String nomUniversite);

    List<Chambre> findByBlocIdBlocAndTypeCOrderByNumeroChambre(long idBloc, TypeChambre typeC);

    @Query("select c from Chambre c where c.bloc.idBloc = :idBloc and c.typeC = :typeC order by c.numeroChambre")
    List<Chambre> chercherParBlocEtType(long idBloc, TypeChambre typeC);

    @Query("select c.idChambre from Chambre c where c.numeroChambre = :numeroChambre")
    Optional<Long> findIdByNumeroChambre(long numeroChambre);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Chambre c where c.idChambre = :id")
    Optional<Chambre> lockById(long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Chambre> findByBlocIdBlocOrderByIdChambre(long idBloc);

    @Query("""
            select c from Chambre c join c.bloc b join b.foyer f join f.universite u
            where (:nom is null or u.nomUniversite = :nom)
            and (:type is null or c.typeC = :type)
            and not exists (
                select r from Chambre candidate join candidate.reservations r
                where candidate = c and r.estValide = true and r.anneeUniversitaire >= :debut and r.anneeUniversitaire < :fin
            )
            order by u.nomUniversite, c.numeroChambre
            """)
    List<Chambre> findUnreserved(String nom, TypeChambre type, Date debut, Date fin);
}
