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
public class UniversiteServiceImpl implements UniversiteService {
    private final UniversiteRepository repository;
    private final FoyerRepository foyerRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Universite> retrieveAllUniversities() {
        return repository.findAll();
    }

    @Override
    public Universite addUniversite(Universite u) {
        u.setIdUniversite(0);
        return repository.save(u);
    }

    @Override
    public Universite updateUniversite(Universite u) {
        Universite entity = repository.findById(u.getIdUniversite())
                .orElseThrow(() -> missing("Universite", u.getIdUniversite()));
        entity.setNomUniversite(u.getNomUniversite());
        entity.setAdresse(u.getAdresse());
        return entity;
    }

    @Override
    @Transactional(readOnly = true)
    public Universite retrieveUniversite(long idUniversite) {
        return repository.findById(idUniversite).orElseThrow(() -> missing("Universite", idUniversite));
    }

    @Override
    public Universite affecterFoyerAUniversite(long idFoyer, String nomUniversite) {
        Universite found = repository.findByNomUniversite(nomUniversite)
                .orElseThrow(() -> missing("Universite", nomUniversite));
        Universite universite = repository.lockById(found.getIdUniversite())
                .orElseThrow(() -> missing("Universite", nomUniversite));
        Foyer foyer = foyerRepository.lockById(idFoyer)
                .orElseThrow(() -> missing("Foyer", idFoyer));

        if (foyer.getUniversite() != null && foyer.getUniversite().getIdUniversite() != universite.getIdUniversite()) {
            throw conflict("Ce foyer est deja affecte a une autre universite");
        }
        if (universite.getFoyer() != null && universite.getFoyer().getIdFoyer() != idFoyer) {
            throw conflict("Cette universite a deja un foyer");
        }
        foyer.setUniversite(universite);
        universite.setFoyer(foyer);
        return universite;
    }

    @Override
    public Universite desaffecterFoyerAUniversite(long idUniversite) {
        Universite universite = repository.lockById(idUniversite)
                .orElseThrow(() -> missing("Universite", idUniversite));
        if (universite.getFoyer() != null) {
            universite.getFoyer().setUniversite(null);
            universite.setFoyer(null);
        }
        return universite;
    }
}
