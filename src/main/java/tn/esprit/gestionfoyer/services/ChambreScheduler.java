package tn.esprit.gestionfoyer.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.gestionfoyer.repositories.ChambreRepository;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChambreScheduler {
    private final ChambreRepository repository;

    @Scheduled(fixedDelayString = "${foyer.scheduler.interval-ms}",
            initialDelayString = "${foyer.scheduler.initial-delay-ms}")
    @Transactional(readOnly = true)
    public void afficherChambresNonReservees() {
        var current = AcademicYear.current();
        var chambres = repository.findUnreserved(null, null,
                AcademicYear.toDate(current), AcademicYear.toDate(current.plusYears(1)));
        log.info("{} chambre(s) non reservee(s) pour l'annee {}-{}", chambres.size(), current.getYear(), current.getYear() + 1);
        for (var chambre : chambres) {
            var bloc = chambre.getBloc();
            log.info("Universite {}, bloc {}, chambre {} ({})",
                    bloc.getFoyer().getUniversite().getNomUniversite(), bloc.getNomBloc(),
                    chambre.getNumeroChambre(), chambre.getTypeC());
        }
    }
}
