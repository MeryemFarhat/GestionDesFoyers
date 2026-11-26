package tn.esprit.gestionfoyer.config;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.LocalDate;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Date;

@Converter
public class CalendarDateConverter implements AttributeConverter<Date, LocalDate> {
    @Override
    public LocalDate convertToDatabaseColumn(Date date) {
        return date == null ? null : LocalDate.ofInstant(Instant.ofEpochMilli(date.getTime()), ZoneOffset.UTC);
    }

    @Override
    public Date convertToEntityAttribute(LocalDate date) {
        return date == null ? null : Date.from(date.atStartOfDay(ZoneOffset.UTC).toInstant());
    }
}
