# GestionFoyer

Spring Boot 4.2.0 application for university housing. It uses Java 17, the Gradle wrapper, Spring Data JPA, Hibernate, and a persistent H2 database.

## Run

Import `build.gradle` as a Gradle project in IntelliJ IDEA and select a JDK 17 for Gradle. Set the run configuration's working directory to the project root so the relative database path points to the workspace. From PowerShell:

```powershell
.\gradlew.bat build
.\gradlew.bat test
.\gradlew.bat bootRun
```

To seed a university, residence, block, three room types and two students on an empty database:

```powershell
.\gradlew.bat bootRun --args="--spring.profiles.active=dev"
```

GestionFoyer listens locally on port 8081 because the Oracle listener uses port 8080 on this workstation. When running an executable JAR on Windows, copy it to a directory outside `build/` before launching it so `gradlew clean` can remove the build outputs.

- API base URL is `http://localhost:8081/api`.
- Swagger UI is `http://localhost:8081/swagger-ui/index.html`.
- OpenAPI JSON is `http://localhost:8081/v3/api-docs`.
- H2 console is `http://localhost:8081/h2-console`. Use driver `org.h2.Driver`, JDBC URL `jdbc:h2:file:./data/foyerdb`, user `sa`, and an empty password. Run `SHOW TABLES;` to inspect the schema.
- Test report is generated at `build/reports/tests/test/index.html`. Integration tests use in-memory H2.

## API

| Resource | Collection operations | Item operations |
| --- | --- | --- |
| `/api/universites` | GET, POST | GET, PUT |
| `/api/foyers` | GET, POST | GET, PUT, DELETE |
| `/api/blocs` | GET, POST | GET, PUT, DELETE |
| `/api/chambres` | GET, POST | GET, PUT |
| `/api/etudiants` | GET, POST with an array of students | GET, PUT, DELETE |
| `/api/reservations` | GET, POST with block ID and student CIN | GET, PUT |

| Method | Path | Input |
| --- | --- | --- |
| PUT | `/api/universites/affectation` | `idFoyer`, `nomUniversite` |
| DELETE | `/api/universites/{id}/foyer` | University ID |
| POST | `/api/foyers/universite/{idUniversite}` | Foyer JSON |
| PUT | `/api/blocs/{id}/foyer/{idFoyer}` | Block and foyer IDs |
| DELETE | `/api/blocs/{id}/foyer` | Block ID |
| PUT | `/api/blocs/{id}/chambres` | JSON array of room numbers |
| GET | `/api/chambres/par-universite` | `nomUniversite` |
| GET | `/api/chambres/par-bloc-et-type` | `idBloc`, `type` using a derived JPA query |
| GET | `/api/chambres/par-bloc-et-type/jpql` | `idBloc`, `type` using JPQL |
| GET | `/api/chambres/non-reservees` | `nomUniversite`, `type` |
| POST | `/api/reservations` | `idBloc`, `cinEtudiant` |
| DELETE | `/api/reservations/etudiants/{cinEtudiant}` | Student CIN |
| GET | `/api/reservations/par-annee-et-universite` | `anneeUniversite` (YYYY-MM-DD), `nomUniversite` |

Values listed as inputs outside the path or JSON body are query parameters. Swagger exposes the request schemas. Create requests return 201, successful entity deletes return 204, and validation, missing entities, and conflicts return 400, 404, and 409 respectively.

```powershell
$api = 'http://localhost:8081/api'
Invoke-RestMethod -Uri "$api/universites"
Invoke-RestMethod -Method Post -Uri "$api/foyers" -ContentType 'application/json' -Body '{"nomFoyer":"Foyer Central","capaciteFoyer":80}'

# Use the block ID returned by GET /api/blocs for your database.
Invoke-RestMethod -Method Post -Uri "$api/reservations?idBloc=1&cinEtudiant=12345678"
Invoke-RestMethod -Uri "$api/chambres/non-reservees?nomUniversite=Universite%20Demo&type=DOUBLE"
Invoke-RestMethod -Method Delete -Uri "$api/reservations/etudiants/12345678"
```

## Business rules

- The academic year starts September 1 in Africa/Tunis. New records store the start of the academic year. The lookup accepts any date within that year and filters stored dates from September 1 inclusive to the following September 1 exclusive.
- A room has one reservation record per academic year, for example `102-Bloc Demo-2026-2027`. Room numbers, block names, university names and student CINs are unique to keep identification and assignment unambiguous. Allocation picks the lowest room ID with available capacity. Foyer and block capacities are descriptive, while room type enforces occupancy.
- Cancelling by CIN releases the student's current-year place. The reservation remains valid while another student occupies it. When the last student leaves, the reservation is invalidated and detached from the room. Its row is retained and reused by ID on the next booking for that room and year. An unreserved room has no active reservation during the current academic year, so a partially occupied room is excluded. The university lookup follows the room association and therefore excludes detached records. The cancellation operation preserves active shared reservations until the last occupant leaves.
- Reservation PUT takes `{"estValide":false}` to release the sole occupant of a reservation. An optional `anneeUniversitaire` must match the stored date. Shared reservations require individual cancellation. Booking reactivates an empty reservation. Room number, block name, and room-to-block assignment are protected while room reservations are linked.
- To delete a foyer, first detach its university and blocks. Blocks and students with linked reservations are protected from deletion.
- The scheduler logs unreserved rooms across universities. Configure `foyer.scheduler.interval-ms` and `foyer.scheduler.initial-delay-ms` in properties or use `FOYER_SCHEDULER_INTERVAL_MS` and `FOYER_SCHEDULER_INITIAL_DELAY_MS`. The timing aspect logs reservation creation duration.

### Tester les quatre services dans Swagger

Dans le profil `dev`, utiliser `Universite Demo`, les CIN `12345678` et `12345679`, et relever l'identifiant de `Bloc Demo` via `GET /api/blocs` (souvent `1`). Effectuer la création avant l'annulation.

| Methode | Chemin et exemple |
| --- | --- |
| POST | `/api/reservations?idBloc=1&cinEtudiant=12345678` |
| DELETE | `/api/reservations/etudiants/12345678`, parametre de chemin `cinEtudiant` |
| GET | `/api/reservations/par-annee-et-universite?anneeUniversite=2026-11-26&nomUniversite=Universite%20Demo` |
| GET | `/api/chambres/non-reservees?nomUniversite=Universite%20Demo&type=DOUBLE` |

Pour la recherche par annee, remplacer la date par une date de l'annee universitaire souhaitee.
La consigne de recherche des chambres mentionne le foyer. La signature impose `nomUniversite`, donc le filtre porte sur le nom unique de l'universite.