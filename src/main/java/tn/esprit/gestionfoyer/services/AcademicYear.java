package tn.esprit.gestionfoyer.services;

import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;

public final class AcademicYear {
    private static final ZoneId TUNIS = ZoneId.of("Africa/Tunis");

    private AcademicYear() {
    }

    public static LocalDate current() {
        return start(LocalDate.now(TUNIS));
    }

    public static LocalDate start(LocalDate date) {
        int year = date.getYear();
        if (date.getMonth().getValue() < Month.SEPTEMBER.getValue()) {
            year--;
        }
        return LocalDate.of(year, Month.SEPTEMBER, 1);
    }

    public static Date toDate(LocalDate date) {
        return Date.from(date.atStartOfDay(ZoneOffset.UTC).toInstant());
    }

    public static LocalDate toLocalDate(Date date) {
        return LocalDate.ofInstant(java.time.Instant.ofEpochMilli(date.getTime()), ZoneOffset.UTC);
    }
}
