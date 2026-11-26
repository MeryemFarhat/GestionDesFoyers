package tn.esprit.gestionfoyer.services;

import tn.esprit.gestionfoyer.entities.*;
import java.util.List;

public interface EtudiantService {
    List<Etudiant> retrieveAllEtudiants();

    List<Etudiant> addEtudiants(List<Etudiant> etudiants);

    Etudiant updateEtudiant(Etudiant e);

    Etudiant retrieveEtudiant(long idEtudiant);

    void removeEtudiant(long idEtudiant);
}
