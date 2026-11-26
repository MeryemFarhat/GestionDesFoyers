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
public class EtudiantServiceImpl implements EtudiantService {
    private final EtudiantRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<Etudiant> retrieveAllEtudiants() {
        return repository.findAll();
    }

    @Override
    public List<Etudiant> addEtudiants(List<Etudiant> etudiants) {
        etudiants.forEach(etudiant -> etudiant.setIdEtudiant(0));
        return repository.saveAll(etudiants);
    }

    @Override
    public Etudiant updateEtudiant(Etudiant e) {
        Etudiant entity = repository.lockById(e.getIdEtudiant())
                .orElseThrow(() -> missing("Etudiant", e.getIdEtudiant()));
        entity.setNomEt(e.getNomEt());
        entity.setPrenomEt(e.getPrenomEt());
        entity.setCin(e.getCin());
        entity.setEcole(e.getEcole());
        entity.setDateNaissance(e.getDateNaissance());
        return entity;
    }

    @Override
    @Transactional(readOnly = true)
    public Etudiant retrieveEtudiant(long idEtudiant) {
        return repository.findById(idEtudiant).orElseThrow(() -> missing("Etudiant", idEtudiant));
    }

    @Override
    public void removeEtudiant(long idEtudiant) {
        Etudiant etudiant = repository.lockById(idEtudiant).orElseThrow(() -> missing("Etudiant", idEtudiant));
        if (!etudiant.getReservations().isEmpty()) {
            throw conflict("Cet etudiant est associe a des reservations");
        }
        repository.delete(etudiant);
    }
}
