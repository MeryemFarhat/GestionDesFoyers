package tn.esprit.gestionfoyer.services;

import tn.esprit.gestionfoyer.entities.*;
import java.util.List;

public interface FoyerService {
    List<Foyer> retrieveAllFoyers();

    Foyer addFoyer(Foyer f);

    Foyer updateFoyer(Foyer f);

    Foyer retrieveFoyer(long idFoyer);

    void removeFoyer(long idFoyer);

    Foyer ajouterFoyerEtAffecterAUniversite(Foyer foyer, long idUniversite);
}
