package tn.esprit.gestionfoyer.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.gestionfoyer.entities.*;
import tn.esprit.gestionfoyer.services.*;

import java.time.LocalDate;
import java.util.List;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class DevelopmentData implements ApplicationRunner {
    private final UniversiteService universites;
    private final FoyerService foyers;
    private final BlocService blocs;
    private final ChambreService chambres;
    private final EtudiantService etudiants;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!universites.retrieveAllUniversities().isEmpty() || !foyers.retrieveAllFoyers().isEmpty()
                || !blocs.retrieveBlocs().isEmpty() || !chambres.retrieveAllChambres().isEmpty()
                || !etudiants.retrieveAllEtudiants().isEmpty()) {
            return;
        }

        Universite universite = new Universite();
        universite.setNomUniversite("Universite Demo");
        universite.setAdresse("Tunis");
        universites.addUniversite(universite);

        Foyer foyer = new Foyer();
        foyer.setNomFoyer("Foyer Demo");
        foyer.setCapaciteFoyer(30);
        foyers.ajouterFoyerEtAffecterAUniversite(foyer, universite.getIdUniversite());

        Bloc bloc = new Bloc();
        bloc.setNomBloc("Bloc Demo");
        bloc.setCapaciteBloc(30);
        blocs.addBloc(bloc);
        blocs.affecterBlocAFoyer(bloc.getIdBloc(), foyer.getIdFoyer());

        long numero = 101;
        for (TypeChambre type : TypeChambre.values()) {
            Chambre chambre = new Chambre();
            chambre.setNumeroChambre(numero++);
            chambre.setTypeC(type);
            chambres.addChambre(chambre);
        }
        blocs.affecterChambresABloc(List.of(101L, 102L, 103L), bloc.getIdBloc());

        etudiants.addEtudiants(List.of(student(12345678L, "Amel"), student(12345679L, "Youssef")));
    }

    private Etudiant student(long cin, String prenom) {
        Etudiant etudiant = new Etudiant();
        etudiant.setCin(cin);
        etudiant.setNomEt("Demo");
        etudiant.setPrenomEt(prenom);
        etudiant.setEcole("ESPRIT");
        etudiant.setDateNaissance(AcademicYear.toDate(LocalDate.of(2004, 1, 15)));
        return etudiant;
    }
}
