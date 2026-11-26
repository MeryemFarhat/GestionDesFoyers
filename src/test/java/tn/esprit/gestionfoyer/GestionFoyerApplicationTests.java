package tn.esprit.gestionfoyer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import tn.esprit.gestionfoyer.entities.*;
import tn.esprit.gestionfoyer.exceptions.FoyerException;
import tn.esprit.gestionfoyer.services.*;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.datasource.url=jdbc:h2:mem:foyer_test;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false",
        "foyer.scheduler.initial-delay-ms=3600000"
})
class GestionFoyerApplicationTests {
    @Autowired UniversiteService universites;
    @Autowired FoyerService foyers;
    @Autowired BlocService blocs;
    @Autowired ChambreService chambres;
    @Autowired EtudiantService etudiants;
    @Autowired ReservationService reservations;
    @Autowired JdbcTemplate jdbc;
    @Value("${local.server.port}") int port;

    @Test
    void crudAndValidationUseTheRestApi() {
        RestClient client = RestClient.create("http://localhost:" + port);
        Foyer foyer = new Foyer();
        foyer.setNomFoyer("Foyer CRUD");
        foyer.setCapaciteFoyer(12);
        var created = client.post().uri("/api/foyers").body(foyer).retrieve().toEntity(Foyer.class);
        assertThat(created.getStatusCode().value()).isEqualTo(201);
        long id = created.getBody().getIdFoyer();
        foyer.setCapaciteFoyer(15);
        var updated = client.put().uri("/api/foyers/{id}", id).body(foyer).retrieve().body(Foyer.class);
        assertThat(updated.getCapaciteFoyer()).isEqualTo(15);
        assertThat(client.get().uri("/api/foyers/{id}", id).retrieve().body(Foyer.class).getNomFoyer())
                .isEqualTo("Foyer CRUD");
        assertThat(client.delete().uri("/api/foyers/{id}", id).retrieve().toBodilessEntity().getStatusCode().value())
                .isEqualTo(204);
        assertThatThrownBy(() -> client.get().uri("/api/foyers/{id}", id).retrieve().toBodilessEntity())
                .isInstanceOf(HttpClientErrorException.NotFound.class);
        assertThatThrownBy(() -> client.post().uri("/api/foyers").body(new Foyer()).retrieve().toBodilessEntity())
                .isInstanceOf(HttpClientErrorException.BadRequest.class);
        assertThatThrownBy(() -> client.post().uri("/api/etudiants").body(List.of(new Etudiant())).retrieve().toBodilessEntity())
                .isInstanceOf(HttpClientErrorException.BadRequest.class);
        assertThat(client.get().uri("/v3/api-docs").retrieve().body(String.class))
                .contains("/api/foyers", "/api/reservations", "/api/chambres/par-bloc-et-type/jpql");
    }

    @Test
    void associatesUniversityAndFoyerInBothDirections() {
        Universite universite = university("Universite Association");
        Foyer foyer = foyers.addFoyer(foyer("Foyer Association"));
        universites.affecterFoyerAUniversite(foyer.getIdFoyer(), universite.getNomUniversite());
        assertThat(universites.retrieveUniversite(universite.getIdUniversite()).getFoyer().getIdFoyer())
                .isEqualTo(foyer.getIdFoyer());
        universites.desaffecterFoyerAUniversite(universite.getIdUniversite());
        assertThat(universites.retrieveUniversite(universite.getIdUniversite()).getFoyer()).isNull();
        foyers.removeFoyer(foyer.getIdFoyer());
    }

    @Test
    void reservesToCapacityAndCancelsOnlyTheSelectedStudent() {
        Universite universite = university("Universite Reservations");
        Foyer foyer = foyers.ajouterFoyerEtAffecterAUniversite(foyer("Foyer Reservations"), universite.getIdUniversite());
        Bloc bloc = new Bloc();
        bloc.setNomBloc("Bloc Reservations");
        bloc.setCapaciteBloc(20);
        long idBloc = blocs.addBloc(bloc).getIdBloc();
        blocs.affecterBlocAFoyer(idBloc, foyer.getIdFoyer());
        long numero = 400;
        for (TypeChambre type : TypeChambre.values()) {
            Chambre chambre = new Chambre();
            chambre.setNumeroChambre(numero++);
            chambre.setTypeC(type);
            chambres.addChambre(chambre);
        }
        assertThatThrownBy(() -> blocs.affecterChambresABloc(List.of(400L, 999L), idBloc))
                .isInstanceOf(FoyerException.class);
        assertThat(chambres.getChambresParNomUniversite(universite.getNomUniversite())).isEmpty();
        blocs.affecterChambresABloc(List.of(400L, 401L, 402L), idBloc);
        assertThat(chambres.getChambresParNomUniversite(universite.getNomUniversite())).hasSize(3);
        assertThat(chambres.getChambresParBlocEtType(idBloc, TypeChambre.DOUBLE))
                .extracting(Chambre::getNumeroChambre).containsExactly(401L);
        assertThat(chambres.getChambresParBlocEtTypeJpql(idBloc, TypeChambre.DOUBLE))
                .extracting(Chambre::getNumeroChambre).containsExactly(401L);
        assertThat(chambres.getChambresNonReserveParNomUniversiteEtTypeChambre(universite.getNomUniversite(), TypeChambre.DOUBLE))
                .hasSize(1);

        etudiants.addEtudiants(LongStream.rangeClosed(200001, 200007).mapToObj(this::student).toList());
        var first = reservations.ajouterReservation(idBloc, 200001);
        assertThat(first.isEstValide()).isTrue();
        int year = AcademicYear.current().getYear();
        assertThat(first.getIdReservation()).isEqualTo("400-Bloc Reservations-" + year + "-" + (year + 1));
        assertThat(first.getEtudiants()).extracting(Etudiant::getCin).containsExactly(200001L);
        assertThatThrownBy(() -> reservations.ajouterReservation(idBloc, 200001))
                .isInstanceOf(FoyerException.class).hasMessageContaining("deja");
        var shared = reservations.ajouterReservation(idBloc, 200002);
        assertThat(shared.getIdReservation()).startsWith("401-Bloc Reservations-");
        assertThat(chambres.getChambresNonReserveParNomUniversiteEtTypeChambre(
                universite.getNomUniversite(), TypeChambre.DOUBLE)).isEmpty();
        assertThat(reservations.ajouterReservation(idBloc, 200003).getIdReservation()).isEqualTo(shared.getIdReservation());
        for (long cin = 200004; cin <= 200006; cin++) {
            assertThat(reservations.ajouterReservation(idBloc, cin).getIdReservation()).startsWith("402-Bloc Reservations-");
        }
        assertThatThrownBy(() -> reservations.ajouterReservation(idBloc, 200007))
                .isInstanceOf(FoyerException.class).hasMessageContaining("Aucune place");
        assertThat(chambres.getChambresNonReserveParNomUniversiteEtTypeChambre(universite.getNomUniversite(), TypeChambre.DOUBLE))
                .isEmpty();

        Reservation update = new Reservation();
        update.setIdReservation(shared.getIdReservation());
        assertThatThrownBy(() -> reservations.updateReservation(update))
                .isInstanceOf(FoyerException.class).hasMessageContaining("partagee");
        var afterCancellation = reservations.annulerReservation(200002);
        assertThat(afterCancellation.isEstValide()).isTrue();
        assertThat(afterCancellation.getEtudiants()).extracting(Etudiant::getCin).containsExactly(200003L);
        assertThat(jdbc.queryForObject("select count(*) from chambre_reservation where reservation_id = ?",
                Integer.class, shared.getIdReservation())).isEqualTo(1);
        assertThat(reservations.annulerReservation(200003).isEstValide()).isFalse();
        assertThat(jdbc.queryForObject("select count(*) from chambre_reservation where reservation_id = ?",
                Integer.class, shared.getIdReservation())).isZero();
        assertThat(jdbc.queryForObject("select count(*) from reservation_etudiant where reservation_id = ?",
                Integer.class, shared.getIdReservation())).isZero();
        assertThat(reservations.retrieveReservation(shared.getIdReservation()).isEstValide()).isFalse();
        assertThatThrownBy(() -> reservations.annulerReservation(200003))
                .isInstanceOf(FoyerException.class).hasMessageContaining("Reservation active");
        assertThat(reservations.getReservationParAnneeUniversitaireEtNomUniversite(
                AcademicYear.toDate(AcademicYear.current()), universite.getNomUniversite()))
                .extracting(Reservation::getIdReservation).doesNotContain(shared.getIdReservation());
        assertThat(chambres.getChambresNonReserveParNomUniversiteEtTypeChambre(universite.getNomUniversite(), TypeChambre.DOUBLE))
                .hasSize(1);
        assertThat(reservations.ajouterReservation(idBloc, 200007).getIdReservation()).isEqualTo(shared.getIdReservation());
        assertThat(reservations.updateReservation(update).isEstValide()).isFalse();
        assertThat(reservations.ajouterReservation(idBloc, 200007).isEstValide()).isTrue();
        assertThat(reservations.getReservationParAnneeUniversitaireEtNomUniversite(
                AcademicYear.toDate(AcademicYear.current().plusMonths(4)), universite.getNomUniversite())).hasSize(3);
        assertThat(jdbc.queryForList("select table_name from information_schema.tables where table_schema = 'PUBLIC'", String.class))
                .contains("UNIVERSITE", "FOYER", "BLOC", "CHAMBRE", "RESERVATION", "ETUDIANT",
                        "CHAMBRE_RESERVATION", "RESERVATION_ETUDIANT");
    }

    @Test
    void concurrentRequestsCannotOverbookASingleRoom() throws Exception {
        Bloc bloc = new Bloc();
        bloc.setNomBloc("Bloc Concurrent");
        bloc.setCapaciteBloc(1);
        long idBloc = blocs.addBloc(bloc).getIdBloc();
        Chambre chambre = new Chambre();
        chambre.setNumeroChambre(500);
        chambre.setTypeC(TypeChambre.SIMPLE);
        chambres.addChambre(chambre);
        blocs.affecterChambresABloc(List.of(500L), idBloc);
        etudiants.addEtudiants(List.of(student(300001), student(300002)));

        var executor = Executors.newFixedThreadPool(2);
        try {
            List<Callable<Boolean>> requests = List.of(
                    () -> tryBooking(idBloc, 300001),
                    () -> tryBooking(idBloc, 300002));
            var responses = executor.invokeAll(requests, 15, TimeUnit.SECONDS);
            assertThat(List.of(responses.get(0).get(), responses.get(1).get()))
                    .containsExactlyInAnyOrder(true, false);
        } finally {
            executor.shutdownNow();
        }
    }

    private boolean tryBooking(long idBloc, long cin) {
        try {
            reservations.ajouterReservation(idBloc, cin);
            return true;
        } catch (FoyerException exception) {
            assertThat(exception).hasMessageContaining("Aucune place");
            return false;
        }
    }

    @Test
    void filtersByUniversityTypeAndAcademicYearBoundaries() {
        long chambre = roomInUniversity("Periode A", 601, TypeChambre.DOUBLE);
        long autre = roomInUniversity("Periode B", 602, TypeChambre.DOUBLE);
        roomInUniversity("Periode A", 603, TypeChambre.SIMPLE);
        LocalDate debut = AcademicYear.current();
        addDatedReservation(chambre, "periode-avant", debut.minusDays(1));
        addDatedReservation(autre, "periode-autre-universite", debut);
        addDatedReservation(chambre, "periode-apres", debut.plusYears(1));
        assertThat(chambres.getChambresNonReserveParNomUniversiteEtTypeChambre("Periode A", TypeChambre.DOUBLE))
                .extracting(Chambre::getNumeroChambre).containsExactly(601L);

        addDatedReservation(chambre, "periode-debut", debut);
        addDatedReservation(chambre, "periode-fin", debut.plusYears(1).minusDays(1));
        assertThat(reservations.getReservationParAnneeUniversitaireEtNomUniversite(
                AcademicYear.toDate(debut.plusMonths(5)), "Periode A"))
                .extracting(Reservation::getIdReservation).containsExactly("periode-debut", "periode-fin");
        assertThat(chambres.getChambresNonReserveParNomUniversiteEtTypeChambre("Periode A", TypeChambre.DOUBLE))
                .isEmpty();
    }

    @Test
    void advancedEndpointsValidateParametersAndDocumentExamples() {
        RestClient client = RestClient.create("http://localhost:" + port);
        assertThatThrownBy(() -> client.post().uri("/api/reservations?idBloc=0&cinEtudiant=12345678")
                .retrieve().toBodilessEntity()).isInstanceOf(HttpClientErrorException.BadRequest.class);
        assertThatThrownBy(() -> client.delete().uri("/api/reservations/etudiants/99999999")
                .retrieve().toBodilessEntity()).isInstanceOf(HttpClientErrorException.NotFound.class);
        assertThatThrownBy(() -> client.get().uri("/api/reservations/par-annee-et-universite?anneeUniversite=2026&nomUniversite=Inconnue")
                .retrieve().toBodilessEntity()).isInstanceOf(HttpClientErrorException.BadRequest.class);
        assertThatThrownBy(() -> client.get().uri("/api/chambres/non-reservees?nomUniversite=Inconnue&type=DOUBLE")
                .retrieve().toBodilessEntity()).isInstanceOf(HttpClientErrorException.NotFound.class);
        assertThat(client.get().uri("/v3/api-docs").retrieve().body(String.class))
                .contains("12345678", "2026-11-26", "Universite Demo",
                        "/api/reservations/etudiants/{cinEtudiant}", "/api/chambres/non-reservees");
    }

    private long roomInUniversity(String nom, long numero, TypeChambre type) {
        var universite = universites.retrieveAllUniversities().stream()
                .filter(candidate -> nom.equals(candidate.getNomUniversite())).findFirst()
                .orElseGet(() -> university(nom));
        var foyer = universite.getFoyer();
        if (foyer == null) {
            foyer = foyers.ajouterFoyerEtAffecterAUniversite(foyer(nom), universite.getIdUniversite());
        }
        Bloc bloc = new Bloc();
        bloc.setNomBloc(nom + " " + numero);
        bloc.setCapaciteBloc(10);
        long idBloc = blocs.addBloc(bloc).getIdBloc();
        blocs.affecterBlocAFoyer(idBloc, foyer.getIdFoyer());
        Chambre chambre = new Chambre();
        chambre.setNumeroChambre(numero);
        chambre.setTypeC(type);
        long idChambre = chambres.addChambre(chambre).getIdChambre();
        blocs.affecterChambresABloc(List.of(numero), idBloc);
        return idChambre;
    }

    private void addDatedReservation(long idChambre, String id, LocalDate date) {
        jdbc.update("insert into reservation (id_reservation, annee_universitaire, est_valide) values (?, ?, true)", id, date);
        jdbc.update("insert into chambre_reservation (chambre_id, reservation_id) values (?, ?)", idChambre, id);
    }

    private Universite university(String nom) {
        Universite universite = new Universite();
        universite.setNomUniversite(nom);
        universite.setAdresse("Tunis");
        return universites.addUniversite(universite);
    }

    private Foyer foyer(String nom) {
        Foyer foyer = new Foyer();
        foyer.setNomFoyer(nom);
        foyer.setCapaciteFoyer(20);
        return foyer;
    }

    private Etudiant student(long cin) {
        Etudiant etudiant = new Etudiant();
        etudiant.setCin(cin);
        etudiant.setNomEt("Test");
        etudiant.setPrenomEt("Etudiant");
        etudiant.setEcole("ESPRIT");
        etudiant.setDateNaissance(AcademicYear.toDate(LocalDate.of(2003, 4, 10)));
        return etudiant;
    }
}
