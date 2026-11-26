package tn.esprit.gestionfoyer.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.gestionfoyer.entities.*;
import tn.esprit.gestionfoyer.repositories.*;

import java.util.List;
import java.util.Date;

import static tn.esprit.gestionfoyer.exceptions.FoyerException.*;

@Service
@RequiredArgsConstructor
@Transactional
public class ChambreServiceImpl implements ChambreService {
    private final ChambreRepository repository;
    private final UniversiteRepository universiteRepository;
    private final BlocRepository blocRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Chambre> retrieveAllChambres() {
        return repository.findAll();
    }

    @Override
    public Chambre addChambre(Chambre c) {
        c.setIdChambre(0);
        return repository.save(c);
    }

    @Override
    public Chambre updateChambre(Chambre c) {
        Chambre entity = repository.lockById(c.getIdChambre())
                .orElseThrow(() -> missing("Chambre", c.getIdChambre()));
        if (entity.getNumeroChambre() != c.getNumeroChambre() && !entity.getReservations().isEmpty()) {
            throw conflict("Une chambre avec des reservations ne peut pas etre renumerotee");
        }
        Date currentYear = AcademicYear.toDate(AcademicYear.current());
        if (entity.getReservations().stream().anyMatch(r -> r.isEstValide()
                && r.getAnneeUniversitaire().equals(currentYear)
                && r.getEtudiants().size() > c.getTypeC().capacity())) {
            throw conflict("La capacite est inferieure au nombre de reservations actives");
        }
        entity.setNumeroChambre(c.getNumeroChambre());
        entity.setTypeC(c.getTypeC());
        return entity;
    }

    @Override
    @Transactional(readOnly = true)
    public Chambre retrieveChambre(long idChambre) {
        return repository.findById(idChambre).orElseThrow(() -> missing("Chambre", idChambre));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Chambre> getChambresParNomUniversite(String nomUniversite) {
        requireUniversity(nomUniversite);
        return repository.findByBlocFoyerUniversiteNomUniversiteOrderByNumeroChambre(nomUniversite);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Chambre> getChambresParBlocEtType(long idBloc, TypeChambre typeC) {
        requireBloc(idBloc);
        return repository.findByBlocIdBlocAndTypeCOrderByNumeroChambre(idBloc, typeC);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Chambre> getChambresParBlocEtTypeJpql(long idBloc, TypeChambre typeC) {
        requireBloc(idBloc);
        return repository.chercherParBlocEtType(idBloc, typeC);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Chambre> getChambresNonReserveParNomUniversiteEtTypeChambre(String nomUniversite, TypeChambre type) {
        requireUniversity(nomUniversite);
        return repository.findUnreserved(nomUniversite, type, AcademicYear.toDate(AcademicYear.current()));
    }

    private void requireBloc(long id) {
        if (!blocRepository.existsById(id)) {
            throw missing("Bloc", id);
        }
    }

    private void requireUniversity(String nom) {
        universiteRepository.findByNomUniversite(nom).orElseThrow(() -> missing("Universite", nom));
    }
}
