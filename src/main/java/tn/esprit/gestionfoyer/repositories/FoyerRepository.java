package tn.esprit.gestionfoyer.repositories;

import org.springframework.data.jpa.repository.*;
import jakarta.persistence.LockModeType;
import tn.esprit.gestionfoyer.entities.Foyer;

import java.util.*;

public interface FoyerRepository extends JpaRepository<Foyer, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select f from Foyer f where f.idFoyer = :id")
    Optional<Foyer> lockById(long id);
}
