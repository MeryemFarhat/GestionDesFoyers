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
public class FoyerServiceImpl implements FoyerService {
    private final FoyerRepository repository;
    private final UniversiteRepository universiteRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Foyer> retrieveAllFoyers() {
        return repository.findAll();
    }

    @Override
    public Foyer addFoyer(Foyer f) {
        f.setIdFoyer(0);
        return repository.save(f);
    }

    @Override
    public Foyer updateFoyer(Foyer f) {
        Foyer entity = repository.findById(f.getIdFoyer())
                .orElseThrow(() -> missing("Foyer", f.getIdFoyer()));
        entity.setNomFoyer(f.getNomFoyer());
        entity.setCapaciteFoyer(f.getCapaciteFoyer());
        return entity;
    }

    @Override
    @Transactional(readOnly = true)
    public Foyer retrieveFoyer(long idFoyer) {
        return repository.findById(idFoyer).orElseThrow(() -> missing("Foyer", idFoyer));
    }

    @Override
    public void removeFoyer(long idFoyer) {
        Foyer foyer = repository.lockById(idFoyer).orElseThrow(() -> missing("Foyer", idFoyer));
        if (foyer.getUniversite() != null || !foyer.getBlocs().isEmpty()) {
            throw conflict("Desaffectez le foyer et ses blocs avant de le supprimer");
        }
        repository.delete(foyer);
    }

    @Override
    public Foyer ajouterFoyerEtAffecterAUniversite(Foyer foyer, long idUniversite) {
        Universite universite = universiteRepository.lockById(idUniversite)
                .orElseThrow(() -> missing("Universite", idUniversite));
        if (universite.getFoyer() != null) {
            throw conflict("Cette universite a deja un foyer");
        }
        Foyer saved = addFoyer(foyer);
        saved.setUniversite(universite);
        universite.setFoyer(saved);
        return saved;
    }
}
