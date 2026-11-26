package tn.esprit.gestionfoyer.repositories;

import org.springframework.data.jpa.repository.*;
import jakarta.persistence.LockModeType;
import tn.esprit.gestionfoyer.entities.Bloc;

import java.util.*;

public interface BlocRepository extends JpaRepository<Bloc, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Bloc b where b.idBloc = :id")
    Optional<Bloc> lockById(long id);
}
