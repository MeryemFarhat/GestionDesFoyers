package tn.esprit.gestionfoyer.repositories;

import org.springframework.data.jpa.repository.*;
import jakarta.persistence.LockModeType;
import tn.esprit.gestionfoyer.entities.Universite;

import java.util.*;

public interface UniversiteRepository extends JpaRepository<Universite, Long> {
    Optional<Universite> findByNomUniversite(String nomUniversite);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from Universite u where u.idUniversite = :id")
    Optional<Universite> lockById(long id);
}
