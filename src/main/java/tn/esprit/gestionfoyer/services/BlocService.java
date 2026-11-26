package tn.esprit.gestionfoyer.services;

import tn.esprit.gestionfoyer.entities.*;
import java.util.List;

public interface BlocService {
    List<Bloc> retrieveBlocs();

    Bloc addBloc(Bloc bloc);

    Bloc updateBloc(Bloc bloc);

    Bloc retrieveBloc(long idBloc);

    void removeBloc(long idBloc);

    Bloc affecterChambresABloc(List<Long> numChambre, long idBloc);

    Bloc affecterBlocAFoyer(long idBloc, long idFoyer);

    Bloc desaffecterBlocDuFoyer(long idBloc);
}
