package tn.esprit.gestionfoyer.repositories;

import org.springframework.data.jpa.repository.*;
import jakarta.persistence.LockModeType;
import tn.esprit.gestionfoyer.entities.Etudiant;

import java.util.*;

public interface EtudiantRepository extends JpaRepository<Etudiant, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Etudiant> findByCin(long cin);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Etudiant e where e.idEtudiant = :id")
    Optional<Etudiant> lockById(long id);
}
