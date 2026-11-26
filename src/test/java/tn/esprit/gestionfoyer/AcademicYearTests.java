package tn.esprit.gestionfoyer;

import org.junit.jupiter.api.Test;
import tn.esprit.gestionfoyer.services.AcademicYear;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class AcademicYearTests {
    @Test
    void academicYearChangesOnSeptemberFirst() {
        assertThat(AcademicYear.start(LocalDate.of(2026, 8, 31))).isEqualTo(LocalDate.of(2025, 9, 1));
        assertThat(AcademicYear.start(LocalDate.of(2026, 9, 1))).isEqualTo(LocalDate.of(2026, 9, 1));
    }

    @Test
    void dateMappingPreservesCalendarDay() {
        LocalDate start = LocalDate.of(2026, 9, 1);
        assertThat(AcademicYear.toLocalDate(AcademicYear.toDate(start))).isEqualTo(start);
    }
}
