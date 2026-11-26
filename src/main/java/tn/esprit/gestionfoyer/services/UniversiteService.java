package tn.esprit.gestionfoyer.services;

import tn.esprit.gestionfoyer.entities.*;
import java.util.List;

public interface UniversiteService {
    List<Universite> retrieveAllUniversities();

    Universite addUniversite(Universite u);

    Universite updateUniversite(Universite u);

    Universite retrieveUniversite(long idUniversite);

    Universite affecterFoyerAUniversite(long idFoyer, String nomUniversite);

    Universite desaffecterFoyerAUniversite(long idUniversite);
}
