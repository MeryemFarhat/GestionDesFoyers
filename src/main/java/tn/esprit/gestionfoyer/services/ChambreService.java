package tn.esprit.gestionfoyer.services;

import tn.esprit.gestionfoyer.entities.*;
import java.util.List;

public interface ChambreService {
    List<Chambre> retrieveAllChambres();

    Chambre addChambre(Chambre c);

    Chambre updateChambre(Chambre c);

    Chambre retrieveChambre(long idChambre);

    List<Chambre> getChambresParNomUniversite(String nomUniversite);

    List<Chambre> getChambresParBlocEtType(long idBloc, TypeChambre typeC);

    List<Chambre> getChambresParBlocEtTypeJpql(long idBloc, TypeChambre typeC);

    List<Chambre> getChambresNonReserveParNomUniversiteEtTypeChambre(String nomUniversite, TypeChambre type);
}
