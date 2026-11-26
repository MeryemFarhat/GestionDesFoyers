package tn.esprit.gestionfoyer.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.gestionfoyer.entities.*;
import tn.esprit.gestionfoyer.repositories.*;

import java.util.List;

import static tn.esprit.gestionfoyer.exceptions.FoyerException.*;

@Service
@RequiredArgsConstructor
@Transactional
public class BlocServiceImpl implements BlocService {
    private final BlocRepository repository;
    private final ChambreRepository chambreRepository;
    private final FoyerRepository foyerRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Bloc> retrieveBlocs() {
        return repository.findAll();
    }

    @Override
    public Bloc addBloc(Bloc bloc) {
        bloc.setIdBloc(0);
        return repository.save(bloc);
    }

    @Override
    public Bloc updateBloc(Bloc bloc) {
        Bloc entity = repository.lockById(bloc.getIdBloc())
                .orElseThrow(() -> missing("Bloc", bloc.getIdBloc()));
        if (!entity.getNomBloc().equals(bloc.getNomBloc())
                && entity.getChambres().stream().anyMatch(c -> !c.getReservations().isEmpty())) {
            throw conflict("Un bloc avec des reservations ne peut pas etre renomme");
        }
        entity.setNomBloc(bloc.getNomBloc());
        entity.setCapaciteBloc(bloc.getCapaciteBloc());
        return entity;
    }

    @Override
    @Transactional(readOnly = true)
    public Bloc retrieveBloc(long idBloc) {
        return repository.findById(idBloc).orElseThrow(() -> missing("Bloc", idBloc));
    }

    @Override
    public void removeBloc(long idBloc) {
        Bloc bloc = repository.lockById(idBloc).orElseThrow(() -> missing("Bloc", idBloc));
        List<Chambre> chambres = chambreRepository.findByBlocIdBlocOrderByIdChambre(idBloc);
        if (chambres.stream().anyMatch(c -> !c.getReservations().isEmpty())) {
            throw conflict("Un bloc avec des reservations ne peut pas etre supprime");
        }
        chambres.forEach(c -> c.setBloc(null));
        repository.delete(bloc);
    }

    @Override
    public Bloc affecterChambresABloc(List<Long> numChambre, long idBloc) {
        Bloc bloc = repository.lockById(idBloc).orElseThrow(() -> missing("Bloc", idBloc));
        List<Long> ids = numChambre.stream().distinct().map(numero -> chambreRepository.findIdByNumeroChambre(numero)
                .orElseThrow(() -> missing("Chambre numero", numero))).sorted().toList();
        for (long id : ids) {
            Chambre chambre = chambreRepository.lockById(id).orElseThrow(() -> missing("Chambre", id));
            Bloc previous = chambre.getBloc();
            if (previous != null && previous.getIdBloc() == idBloc) {
                continue;
            }
            if (!chambre.getReservations().isEmpty()) {
                throw conflict("Une chambre avec des reservations ne peut pas changer de bloc");
            }
            if (previous != null) {
                previous.getChambres().remove(chambre);
            }
            chambre.setBloc(bloc);
            bloc.getChambres().add(chambre);
        }
        return bloc;
    }

    @Override
    public Bloc affecterBlocAFoyer(long idBloc, long idFoyer) {
        Bloc bloc = repository.lockById(idBloc).orElseThrow(() -> missing("Bloc", idBloc));
        Foyer foyer = foyerRepository.lockById(idFoyer).orElseThrow(() -> missing("Foyer", idFoyer));
        bloc.setFoyer(foyer);
        return bloc;
    }

    @Override
    public Bloc desaffecterBlocDuFoyer(long idBloc) {
        Bloc bloc = repository.lockById(idBloc).orElseThrow(() -> missing("Bloc", idBloc));
        bloc.setFoyer(null);
        return bloc;
    }
}
